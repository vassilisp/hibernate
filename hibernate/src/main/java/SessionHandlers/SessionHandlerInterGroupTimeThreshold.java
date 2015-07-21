package SessionHandlers;

import gq.panop.hibernate.JungGraphCreator;
import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.hibernate.mytypes.Transition;
import gq.panop.util.MiscUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import SessionHandlers.SessionHandlerTimeThreshold.Node;

public class SessionHandlerInterGroupTimeThreshold implements SessionHandler{

    private Integer internalCounter = 0;
    
    private Integer subSessionThreshold = 1000 * 60 * 25; //ms * sec * min
    private Integer revisitedThreshold = 1000 * 2;//
    private Integer autoRequestThreshold = 1500; //ms
    private String userId;
    private String clientId;

    private Long lastUniversalRequest = 0L;
    private String lastSubSessionId = "";
    
    private Boolean searchHiddenConnections = false;
    private Boolean discardParameters = true;
    private Boolean discardImages = false;
    public Boolean acceptUnlinkedNodes = true;
    private List<Transition> transitions = new ArrayList<Transition>();
    
    private List<Node> loadedNodes = new ArrayList<Node>();
    

    private Integer subSessionCounter=0;
    
    private JungGraphCreator jgc = null;
    
    public SessionHandlerInterGroupTimeThreshold(Integer subSessionThreshold, Integer autoRequestThreshold){
        this.subSessionThreshold = subSessionThreshold;
        this.autoRequestThreshold = autoRequestThreshold;
    }
    
    public void newUser(String userId, String clientId){
        this.userId = userId;
        this.clientId = clientId;
        this.subSessionCounter = 0;
        this.transitions.clear();
        this.loadedNodes.clear();
        this.lastSubSessionId = "";
        this.lastUniversalRequest = 0L;
        
        this.jgc = new JungGraphCreator(true, false);
    }
    

    public void nextSession(AugmentedACL session){
        String referer = session.getAccessLog().getReferer();
        
        String target = session.getAccessLog().getRequestedResource();
        //Define what parts to keep from the target (all, skip parameters, ..)
        
        referer = MiscUtil.URLRefererCleaner(referer);
        target = MiscUtil.URLTargetCleaner(target);
        
        Long timestamp = session.getTimestamp();                
        
     
        Boolean isImage = (((target.endsWith(".png")||referer.endsWith(".png")) && discardImages));
        Boolean isCSS = (target.contains("css")) || (referer.contains("css"));
        Boolean isICO = (target.contains(".ico") || referer.contains(".ico")); 
        Boolean specialReq = false;
        if (isImage || isCSS || isICO) {
            specialReq = true;
            System.err.println("AUTOREQUEST DETECTED BY TYPE");
        }

        String transactionId = session.getAccessLog().getTransactionId();
        /*
        Transition currentTransition = new Transition(referer, target, timestamp);
        currentTransition.setTransactionId(transactionId);
        currentTransition.setUserId(userId);
         */

        if (discardParameters){
            if(referer.indexOf("?")>0){
                referer = referer.substring(0, referer.indexOf("?"));
            }
            if(target.indexOf("?")>0){
                target = target.substring(0, target.indexOf("?"));
            }
        }
        Node currentTargetNode = new Node(target, session.getTimestamp());
        Node currentRefererNode = new Node(referer, session.getTimestamp());
        
        System.out.println("===========================================================");
        System.out.println(referer + "  /// " + target + "    == " + timestamp);

        Integer refererIndex = loadedNodes.indexOf(currentRefererNode);
        Integer targetIndex = loadedNodes.indexOf(currentTargetNode);

        Node loadedReferer = null;
        Node loadedTarget = null;

        if (refererIndex>=0) loadedReferer = loadedNodes.get(refererIndex);
        if (targetIndex>=0) loadedTarget = loadedNodes.get(targetIndex);
        
        Integer interval = null;

        //---------------------------------------------------------------------
        //CASE 1: old target old referer
        if (refererIndex>=0 && targetIndex>=0){

            interval = ((Long) (timestamp - loadedReferer.getLastRequest())).intValue();
            System.out.print("BOTH - " + interval);
            
            updateReferer(loadedReferer, timestamp);
            updateTarget(loadedTarget, timestamp);

            lastUniversalRequest = timestamp;
            
            if (interval>autoRequestThreshold){
                if (interval<subSessionThreshold || subSessionThreshold<=0){
                    String currentSubSession = loadedReferer.getSubSessionId();
                    lastSubSessionId = currentSubSession;
                    
                    loadedTarget.setSubSessionId(currentSubSession);
                    
                    SearchToActivateConnections(loadedReferer, specialReq);
                    keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, currentSubSession);
                }else{
                    String subSessionId = "subSession:" + subSessionCounter++;
                    lastSubSessionId = subSessionId;
                    
                    loadedReferer.setSubSessionId(subSessionId);
                    loadedTarget.setSubSessionId(subSessionId);
                    keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, subSessionId); 
                    //after this point it is also safe to purge all nodes older from the referer
                }
            }

        }
        //CASE 2: referer exists, target is new
        else if(refererIndex>=0){
            interval = ((Long) (timestamp - loadedReferer.getLastRequest())).intValue();
            System.out.println("REFERER - " + interval);
            
            updateReferer(loadedReferer, timestamp);
            createNode(currentTargetNode, timestamp);
            
            lastUniversalRequest = timestamp;

            if (interval<subSessionThreshold || subSessionThreshold<=0){
                String currentSubSession = loadedReferer.getSubSessionId();
                currentTargetNode.setSubSessionId(currentSubSession);
                lastSubSessionId = currentSubSession;
                if (interval>autoRequestThreshold){
                    SearchToActivateConnections(loadedReferer, specialReq);
                    keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, currentSubSession);
                }else{
                    InConnection inConnection = new InConnection(loadedReferer, true, timestamp, transactionId);
                    currentTargetNode.getInConnections().add(inConnection);
                    System.out.println("ADDED inConnection to TARGET");
                }
            }else{
                String subSessionId = "subSession:" + subSessionCounter++;
                loadedReferer.setSubSessionId(subSessionId);
                currentTargetNode.setSubSessionId(subSessionId);
                lastSubSessionId = subSessionId;
                
                if (interval>autoRequestThreshold){
                    keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, subSessionId);
                }else{
                    InConnection inConnection = new InConnection(loadedReferer, true, timestamp, transactionId);
                    currentTargetNode.getInConnections().add(inConnection);
                    System.out.println("ADDED inConnection to TARGET");
                }
            }
            System.out.println(loadedReferer.getSubSessionId());
            
                
        }
        //CASE 3: target exists, referer is new
        else if (targetIndex>=0){
            
            System.out.println("ONLY TARGET");
            //referer doesn't exist
            createNode(currentRefererNode, timestamp);

            //target exist so update it
            updateTarget(loadedTarget, timestamp);
            
            lastUniversalRequest = timestamp;


            if (!specialReq){
                Integer reverseInterval = 0;
                if (loadedTarget.getLastVisit()> loadedTarget.getLastRequest()){
                    reverseInterval = ((Long)(timestamp - loadedTarget.getLastVisit())).intValue();
                }else{
                    reverseInterval = ((Long)(timestamp - loadedTarget.getLastRequest())).intValue();
                }
                if (reverseInterval< subSessionThreshold || subSessionThreshold<=0){
                    String currentSubSession = loadedReferer.getSubSessionId();
                    lastSubSessionId = currentSubSession;
                    currentRefererNode.setSubSessionId(currentSubSession);
                    //Since referer is new it wont have any inConnections - you can search if you want but it wont have any
                    keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, currentSubSession);
                }else{
                    String subSessionId = "subSession:" + subSessionCounter++;
                    lastSubSessionId = subSessionId;
                    currentRefererNode.setSubSessionId(subSessionId);
                    //Since referer is new it wont have any inConnections - you can search if you want but it wont have any
                    keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, subSessionId);
                }
            }
            


        }
        //CASE 4: target and referer are new Nodes
        else if (targetIndex<0 && refererIndex<0){
            
            System.out.println("TOTALY NEW NODES");
            //No target No referer exist
            createNode(currentRefererNode, timestamp);
            createNode(currentTargetNode, timestamp);
            
            String subSessionId = null;
            if (!specialReq){
                
                Integer lastInterval = ((Long)(timestamp - lastUniversalRequest)).intValue();
                if (lastInterval<subSessionThreshold || subSessionThreshold<=0){
                    subSessionId = lastSubSessionId;
                    currentRefererNode.setSubSessionId(subSessionId);
                    currentTargetNode.setSubSessionId(subSessionId);
                }else{
                    subSessionId = "subSession:" + subSessionCounter++;
                    lastSubSessionId = subSessionId;
                    currentRefererNode.setSubSessionId(subSessionId);
                    currentTargetNode.setSubSessionId(subSessionId);
                }
                
            }else{
                subSessionId = "subSession:XXX";
                currentRefererNode.setSubSessionId(subSessionId);
                currentTargetNode.setSubSessionId(subSessionId);
            }
            
            //Since referer is new it wont have any inConnections - you can search if you want but it wont have any;
            keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, subSessionId);
        }
        //---------------------------------------------------------------------


        /*
        if (targetIndex>0){
            if (revisitInterval>revisitedThreshold){
                updateTarget(loadedTarget, timestamp);
            }
        }else{
            createNode(currentTargetNode, timestamp);
        }
        
        if (refererIndex>=0){
            updateReferer(loadedReferer, timestamp);
             
            if (interval>autoRequestThreshold){
                if (targetIndex<0){
                    //add InConnection
                    InConnection inConnection = new InConnection(loadedReferer, true, timestamp, transactionId);
                    currentTargetNode.getInConnections().add(inConnection);
                    System.out.println("ADDED inConnection to TARGET");
                }
              //keep with Special check
            }
        }else{
            createNode(currentRefererNode, timestamp);
            //keep with Special check
        }
        */
        
        
    }
    
    
    private void createNode(Node e, Long timestamp){
        e.setLastRequest(timestamp);
        e.setLastVisit(timestamp);
        e.setTimestamp(timestamp);
        //e.setSubSessionId(subSessionId);
        graphAdd(e);
        System.out.println(" -- CREATE Node [lastVisit=lastRequest=timestamp=CURRENTTIMESTAMP");
    }
    
    private void updateReferer(Node e, Long timestamp){
        e.setLastRequest(timestamp);
        System.out.println(" -- UPDATE referers [lastRequest]");
    }
    
    private void updateTarget(Node e, Long timestamp){
        Integer revisitInterval = ((Long)(timestamp - e.getLastVisit())).intValue();
        System.out.println("Revisiting interval: " + revisitInterval);
        
        if (revisitedThreshold<0 || (revisitedThreshold>0 && revisitInterval>revisitedThreshold)){
            e.setLastVisit(timestamp);
            e.setLastRequest(timestamp);
            System.out.println(" : UPDATE target [lastVisit AND lastRequest]");
        }
    }
    
    public List<Transition> getSessions(){
        return transitions;
    }
    
    private void SearchToActivateConnections(Node e, Boolean specialReq){
        if (!specialReq && searchHiddenConnections){
            Node currentNode = e;
            List<Node> visitedNodes = new ArrayList<Node>();
            InConnection lastConnection = null;

            List<InConnection> copyOfConnections = new ArrayList<InConnection>();
            copyOfConnections.addAll(currentNode.getInConnections());

            System.out.println("Searching for inactive last connections (possible connections: " +copyOfConnections.size());

            Boolean exit = false;
            while (exit==false){
                if (currentNode.getInConnections().size()>0){
                    for (InConnection inConnection:copyOfConnections){
                        if (!(lastConnection == null)){
                            if (inConnection.getTimestamp()>lastConnection.getTimestamp()){
                                lastConnection = inConnection;
                            }
                        }else{
                            lastConnection = inConnection;
                        }

                        currentNode.getInConnections().remove(inConnection);
                    }
                    if (lastConnection.getTemporary() == true){
                        Integer interval =((Long)(e.getLastVisit()-lastConnection.getTimestamp())).intValue(); 
                        System.out.println(" -- Found inactive Connection");
                        if (interval<subSessionThreshold){
                            lastConnection.setTemporary(false);
                            Node otherEnd = lastConnection.getOtherEnd();

                            Transition transition = new Transition(otherEnd.getName(), e.getName(), lastConnection.getTimestamp());
                            transition.setTransactionId(lastConnection.getTransactionId());
                            String subSessionId = otherEnd.getSubSessionId();
                            transition.setSubSessionId(subSessionId);
                            transition.setUserId(userId);
                            keep(transition);
                            currentNode = otherEnd;
                        }
                    }else{
                        break;
                    }
                }else{
                    break;
                }

            }
        }
    }
    
    
    private void keep(Transition transition){
        System.out.println("*****   " + internalCounter++);
        transitions.add(transition);
        jgc.AddTransition(transition);
    }
    
    private void keepWithDetails(Boolean specialReq, String referer, String target, Long timestamp, String transactionId, String userId, String subSessionId){
        if (!specialReq){
            System.out.println("   --- Saving Transition");
            Transition transition = new Transition(referer, target, timestamp);
            transition.setTransactionId(transactionId);
            transition.setUserId(userId);
            transition.setSubSessionId(subSessionId);
            
            keep(transition);
        }
    }
    
    private void graphAdd(Node e){
        loadedNodes.add(e);
    }

    
    private class InConnection{
        private Node otherEnd;
        private Boolean temporary = false;
        private Long timestamp;
        private String transactionId;
        
        public Long getTimestamp() {
            return timestamp;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public InConnection(Node otherEnd, Boolean temporary, Long timestamp, String transactionId) {
            super();
            this.otherEnd = otherEnd;
            this.temporary = temporary;
            this.timestamp = timestamp;
            this.transactionId = transactionId;
        }

        public InConnection(Node otherEnd, Boolean temporary) {
            super();
            this.otherEnd = otherEnd;
            this.temporary = temporary;
        }

        public Node getOtherEnd() {
            return otherEnd;
        }

        public void setOtherEnd(Node otherEnd) {
            this.otherEnd = otherEnd;
        }

        public Boolean getTemporary() {
            return temporary;
        }

        public void setTemporary(Boolean temporary) {
            this.temporary = temporary;
        }

        public InConnection(Node otherEnd) {
            super();
            this.otherEnd = otherEnd;
        }    
        
    }
    
    private class Node{
        private String name;
        private List<InConnection> InConnections = new ArrayList<InConnection>();
        private Long timestamp;
        private Long lastRequest;
        private Long lastVisit;
        
        private String subSessionId;

        public Node(String name, Long timestamp) {
            super();
            this.name = name;
            this.timestamp = timestamp;
        }


        public String getSubSessionId() {
            return subSessionId;
        }


        public void setSubSessionId(String subSessionId) {
            this.subSessionId = subSessionId;
        }


        public Long getLastVisit() {
            return lastVisit;
        }


        public void setLastVisit(Long lastVisit) {
            this.lastVisit = lastVisit;
        }


        public String getName() {
            return name;
        }


        public void setName(String name) {
            this.name = name;
        }


        public List<InConnection> getInConnections() {
            return InConnections;
        }


        public void setInConnections(List<InConnection> inConnections) {
            InConnections = inConnections;
        }


        public Long getTimestamp() {
            return timestamp;
        }


        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }


        public Long getLastRequest() {
            return lastRequest;
        }


        public void setLastRequest(Long lastRequest) {
            this.lastRequest = lastRequest;
        }


        public boolean equals(Object o){
            if (!(o==null) && (o instanceof Node)){
                if (((Node)o).getName().equals(this.name)){
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }
    }
    
}
