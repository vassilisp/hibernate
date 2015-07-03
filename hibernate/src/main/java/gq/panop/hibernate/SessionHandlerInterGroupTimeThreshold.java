package gq.panop.hibernate;

import gq.panop.hibernate.SessionHandlerTimeThreshold.Node;
import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.hibernate.mytypes.Transition;
import gq.panop.util.MiscUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SessionHandlerInterGroupTimeThreshold {

    private Integer internalCounter = 0;
    
    private Integer subSessionThreshold = 1000 * 60 * 25; //ms * sec * min
    private Integer revisitedThreshold = 1000 * 2;//
    private Integer autoRequestThreshold = 1500; //ms
    private String userId;
    private String clientId;
    private AugmentedACL previousSession;
    
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

        //CASE 1: old target old referer
        if (refererIndex>=0 && targetIndex>=0){

            interval = ((Long) (timestamp - loadedReferer.getLastRequest())).intValue();
            System.out.print("BOTH - " + interval);
            
            updateReferer(loadedReferer, timestamp);
            updateTarget(loadedTarget, timestamp);

            if (interval>autoRequestThreshold){
                SearchToActivateConnections(loadedReferer, specialReq);
                keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId);
            }

        }
        //CASE 2: referer exists, target is new
        else if(refererIndex>=0){
            interval = ((Long) (timestamp - loadedReferer.getLastRequest())).intValue();
            System.out.println("REFERER - " + interval);
            
            updateReferer(loadedReferer, timestamp);
            createNode(currentTargetNode, timestamp);

            if (interval>autoRequestThreshold){
                SearchToActivateConnections(loadedReferer, specialReq);
                keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId);

            }else{
                InConnection inConnection = new InConnection(loadedReferer, true, timestamp, transactionId);
                currentTargetNode.getInConnections().add(inConnection);
                System.out.println("ADDED inConnection to TARGET");
            }
            
        }
        //CASE 3: target exists, referer is new
        else if (targetIndex>=0){
            
            System.out.println("ONLY TARGET");
            //referer doesn't exist
            createNode(currentRefererNode, timestamp);

            //target exist so update it
            updateTarget(loadedTarget, timestamp);
            
            
            //Since referer is new it wont have any inConnections - you can search if you want but it wont have any
            System.out.println("--Saving Transition");
            keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId);;


        }
        //CASE 4: target and referer are new Nodes
        else if (targetIndex<0 && refererIndex<0){

            System.out.println("TOTALY NEW NODES");
            //No target No referer exist
            createNode(currentRefererNode, timestamp);
            createNode(currentTargetNode, timestamp);

            //Since referer is new it wont have any inConnections - you can search if you want but it wont have any;
            keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId);
        }


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
        graphAdd(e);
        System.out.println(" -- CREATE Node [lastVisit=lastRequest=timestamp=CURRENTTIMESTAMP");
    }
    
    private void updateReferer(Node e, Long timestamp){
        e.setLastRequest(timestamp);
        System.out.println(" -- UPDATE referers [lastRequest]");
    }
    
    private void updateTarget(Node e, Long timestamp){
        Integer revisitInterval = ((Long)(timestamp - e.getLastVisit())).intValue();
        System.out.print("Revisiting interval: " + revisitInterval);
        
        if (revisitedThreshold<0 || (revisitedThreshold>0 && revisitInterval>revisitedThreshold)){
            e.setLastVisit(timestamp);
            System.out.println(" : UPDATE target [lastVisit]");
        }
    }
    
    public List<Transition> getSessions(){
        return transitions;
    }
    
    private void SearchToActivateConnections(Node e, Boolean specialReq){
        if (!specialReq){
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

                            loadedNodes.get(loadedNodes.indexOf(otherEnd));

                            Transition transition = new Transition(otherEnd.getName(), e.getName(), lastConnection.getTimestamp());
                            transition.setTransactionId(lastConnection.getTransactionId());
                            transition.setUserId(userId);
                            keep(transition);
                            currentNode = lastConnection.getOtherEnd();
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
    
    private void keepWithDetails(Boolean specialReq, String referer, String target, Long timestamp, String transactionId, String userId){
        if (!specialReq){
            System.out.println("   --- Saving Transition");
            Transition transition = new Transition(referer, target, timestamp);
            transition.setTransactionId(transactionId);
            transition.setUserId(userId);
            
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

        public Node(String name, Long timestamp) {
            super();
            this.name = name;
            this.timestamp = timestamp;
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
