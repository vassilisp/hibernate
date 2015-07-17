package gq.panop.hibernate;

import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.hibernate.mytypes.Transition;
import gq.panop.util.MiscUtil;

import java.util.ArrayList;
import java.util.List;

public class SessionHandlerUniversal implements SessionHandler {

    private Integer internalCounter = 0;
    
    private Integer subSessionThreshold = 1000 * 60 * 25; //ms * sec * min
    private Integer revisitedThreshold = 1000 * 2;//
    private Integer autoRequestThreshold = 1500; //ms
    private String userId;
    private String clientId;

    private Long lastUniversalRequest = 0L;
    private String lastSubSessionId = "";
    
    private Boolean SearchHiddenConnections = true;
    private Boolean discardParameters = false;
    private Boolean discardImages = true;
    public Boolean acceptUnlinkedNodes = true;
    private Boolean generateGraphs = false;
    private Boolean debugMode = false;
    
    private Integer tokenizer = 0;
    
    
    private List<Transition> transitions = new ArrayList<Transition>();
    
    private List<Node> loadedNodes = new ArrayList<Node>();
    

    private Integer subSessionCounter=0;
    
    private jungGraphCreatorStringVertices jgc = null;
    //private jungGraphCreatorStringVertices jgc = null;
    
    public SessionHandlerUniversal(Integer subSessionThreshold, Integer autoRequestThreshold){
        this.subSessionThreshold = subSessionThreshold;
        this.autoRequestThreshold = autoRequestThreshold;
    }
    
    public SessionHandlerUniversal(Integer subSessionThreshold,
            Integer autoRequestThreshold, Boolean generateGraphs) {
        super();
        this.subSessionThreshold = subSessionThreshold;
        this.autoRequestThreshold = autoRequestThreshold;
        this.generateGraphs = generateGraphs;
    }

    public void newUser(String userId, String clientId){
        this.userId = userId;
        this.clientId = clientId;
        this.subSessionCounter = 0;
        this.transitions.clear();
        this.loadedNodes.clear();
        this.lastSubSessionId = "subSession:" + subSessionCounter;
        this.lastUniversalRequest = 0L;
        
        if (generateGraphs){
            //this.jgc = new jungGraphCreatorStringVertices(true, false);
            this.jgc = new jungGraphCreatorStringVertices(true, false);
        }
    }
    

    public void nextSession(AugmentedACL session){
        String referer = session.getAccessLog().getReferer();
        
        String target = session.getAccessLog().getRequestedResource();
        //Define what parts to keep from the target (all, skip parameters, ..)
        
        referer = MiscUtil.URLRefererCleaner(referer);
        target = MiscUtil.URLTargetCleaner(target);
        
        Long timestamp = session.getTimestamp();                
               
        Boolean isImage = (((target.toLowerCase().endsWith(".png")||referer.toLowerCase().endsWith(".png") ||referer.toLowerCase().endsWith(".jpg") ||
                referer.toLowerCase().endsWith(".gif")) && discardImages));
        Boolean isCSS = (target.contains("css")) || (referer.contains("css"));
        Boolean isICO = (target.contains(".ico") || referer.contains(".ico")); 
        Boolean specialReq = false;
        if (isImage || isCSS || isICO) {
            specialReq = true;
            customDeb("AUTOREQUEST DETECTED BY TYPE");
        }

        if (tokenizer>0){
            referer = MiscUtil.custom_Parser(referer, tokenizer);
            target = MiscUtil.custom_Parser(referer, tokenizer);
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
        
        customDeb("===========================================================");
        customDeb(referer + "  /// " + target + "    == " + timestamp);

        Integer refererIndex = loadedNodes.indexOf(currentRefererNode);
        Integer targetIndex = loadedNodes.indexOf(currentTargetNode);

        Node loadedReferer = null;
        Node loadedTarget = null;

        if (refererIndex>=0) loadedReferer = loadedNodes.get(refererIndex);
        if (targetIndex>=0) loadedTarget = loadedNodes.get(targetIndex);
        
        Integer interval = null;
        Integer subSessionInterval = null;
        //---------------------------------------------------------------------
        //CASE 1: old target old referer
        if (refererIndex>=0 && targetIndex>=0){

            
            interval = ((Long) (timestamp - loadedReferer.getLastRequest())).intValue();
            subSessionInterval = ((Long) (timestamp - lastUniversalRequest)).intValue();
            
            customDeb("BOTH - " + interval);
            
            updateReferer(loadedReferer, timestamp);
            updateTarget(loadedTarget, timestamp);

            
            
            if (interval>autoRequestThreshold){
                if (subSessionInterval<subSessionThreshold || subSessionThreshold<=0){
                    String currentSubSession = lastSubSessionId;
                    
                    loadedTarget.setSubSessionId(currentSubSession);
                    loadedReferer.setSubSessionId(currentSubSession);
                    
                    SearchToActivateConnections(loadedReferer, specialReq);
                    keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, currentSubSession);
                    lastUniversalRequest = timestamp;
                }else{
                    String subSessionId = "subSession:" + subSessionCounter++;
                    lastSubSessionId = subSessionId;
                    
                    loadedReferer.setSubSessionId(subSessionId);
                    loadedTarget.setSubSessionId(subSessionId);
                    keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, subSessionId);
                    lastUniversalRequest = timestamp;
                    //after this point it is also safe to purge all nodes older from the referer
                }
            }
            customDeb(loadedReferer.getSubSessionId());

        }
        //CASE 2: referer exists, target is new
        else if(refererIndex>=0){
            interval = ((Long) (timestamp - loadedReferer.getLastRequest())).intValue();
            subSessionInterval = ((Long) (timestamp - lastUniversalRequest)).intValue();
            customDeb("REFERER - " + interval);
            
            updateReferer(loadedReferer, timestamp);
            createNode(currentTargetNode, timestamp);

            if (subSessionInterval<subSessionThreshold || subSessionThreshold<=0){
                String currentSubSession = lastSubSessionId;
                currentTargetNode.setSubSessionId(currentSubSession);

                if (interval>autoRequestThreshold){
                    SearchToActivateConnections(loadedReferer, specialReq);
                    keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, currentSubSession);
                    lastUniversalRequest = timestamp;
                }else{
                    InConnection inConnection = new InConnection(loadedReferer, true, timestamp, transactionId);
                    currentTargetNode.getInConnections().add(inConnection);
                    customDeb("ADDED inConnection to TARGET");
                }
            }else{
                String subSessionId = "subSession:" + subSessionCounter++;
                loadedReferer.setSubSessionId(subSessionId);
                currentTargetNode.setSubSessionId(subSessionId);
                lastSubSessionId = subSessionId;
                
                if (interval>autoRequestThreshold){
                    keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, subSessionId);
                    lastUniversalRequest = timestamp;
                }else{
                    InConnection inConnection = new InConnection(loadedReferer, true, timestamp, transactionId);
                    currentTargetNode.getInConnections().add(inConnection);
                    customDeb("ADDED inConnection to TARGET");
                }
            }
            customDeb(loadedReferer.getSubSessionId());
            
                
        }
        //CASE 3: target exists, referer is new
        else if (targetIndex>=0){
            
            customDeb("ONLY TARGET");
            //referer doesn't exist
            createNode(currentRefererNode, timestamp);

            //target exist so update it
            updateTarget(loadedTarget, timestamp);


            if (!specialReq){
                if (loadedTarget.getLastVisit()> loadedTarget.getLastRequest()){
                    subSessionInterval = ((Long)(timestamp - loadedTarget.getLastVisit())).intValue();
                }else{
                    subSessionInterval = ((Long)(timestamp - loadedTarget.getLastRequest())).intValue();
                }
                
                if (subSessionInterval< subSessionThreshold || subSessionThreshold<=0){
                    String currentSubSession = lastSubSessionId;
                    
                    currentRefererNode.setSubSessionId(currentSubSession);
                    //Since referer is new it wont have any inConnections - you can search if you want but it wont have any
                    keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, currentSubSession);
                    lastUniversalRequest = timestamp;
                }else{
                    String subSessionId = "subSession:" + subSessionCounter++;
                    lastSubSessionId = subSessionId;
                    currentRefererNode.setSubSessionId(subSessionId);
                    //Since referer is new it wont have any inConnections - you can search if you want but it wont have any
                    keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, subSessionId);
                    lastUniversalRequest = timestamp;
                }
            }
            


        }
        //CASE 4: target and referer are new Nodes
        else if (targetIndex<0 && refererIndex<0){
            
            customDeb("TOTALY NEW NODES");
            //No target No referer exist
            createNode(currentRefererNode, timestamp);
            createNode(currentTargetNode, timestamp);
            
            String subSessionId = null;
            if (!specialReq){
                
                subSessionInterval = ((Long)(timestamp - lastUniversalRequest)).intValue();
                customDeb("SubSessionInterval: " + subSessionInterval);
                if (subSessionInterval<subSessionThreshold || subSessionThreshold<=0){
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
                subSessionId = lastSubSessionId;
                currentRefererNode.setSubSessionId(subSessionId);
                currentTargetNode.setSubSessionId(subSessionId);
            }
            //Since referer is new it wont have any inConnections - you can search if you want but it wont have any;
            keepWithDetails(specialReq, referer, target, timestamp, transactionId, userId, subSessionId);
            lastUniversalRequest = timestamp;
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
                    customDeb("ADDED inConnection to TARGET");
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
        customDeb(" -- CREATE Node [lastVisit=lastRequest=timestamp=CURRENTTIMESTAMP");
    }
    
    private void updateReferer(Node e, Long timestamp){
        e.setLastRequest(timestamp);
        customDeb(" -- UPDATE referers [lastRequest]");
    }
    
    private void updateTarget(Node e, Long timestamp){
        Integer revisitInterval = ((Long)(timestamp - e.getLastVisit())).intValue();
        customDeb("Revisiting interval: " + revisitInterval);
        
        if (revisitedThreshold<0 || (revisitedThreshold>0 && revisitInterval>revisitedThreshold)){
            e.setLastVisit(timestamp);
            e.setLastRequest(timestamp);
            customDeb(" : UPDATE target [lastVisit AND lastRequest]");
        }
    }
    
    public List<Transition> getSessions(){
        return transitions;
    }
    
    private void SearchToActivateConnections(Node e, Boolean specialReq){
        if (!specialReq && SearchHiddenConnections){
            Node currentNode = e;
            InConnection lastConnection = null;

            List<InConnection> copyOfConnections = new ArrayList<InConnection>();
            copyOfConnections.addAll(currentNode.getInConnections());

            customDeb("Searching for inactive last connections (possible connections: " +copyOfConnections.size());

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
                        customDeb(" -- Found inactive Connection");
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
        customDeb("*****   " + internalCounter++);
        transitions.add(transition);
        if (generateGraphs){
            jgc.AddTransition(transition);
        }
    }
    
    private void keepWithDetails(Boolean specialReq, String referer, String target, Long timestamp, String transactionId, String userId, String subSessionId){
        if (!specialReq){
            customDeb("   --- Saving Transition");
            Transition transition = new Transition(referer, target, timestamp);
            transition.setTransactionId(transactionId);
            transition.setUserId(userId);
            transition.setSubSessionId(subSessionId);
            transition.setSessionId(clientId);
            
            keep(transition);
        }
    }
    
    private void customDeb(String text){
        if (debugMode){
            System.out.println(text);
        }
    }
    
    
    private void graphAdd(Node e){
        loadedNodes.add(e);
    }

    
    
    public Boolean getDebugMode() {
        return debugMode;
    }

    public void setDebugMode(Boolean debugMode) {
        this.debugMode = debugMode;
    }

    public Boolean getGenerateGraphs() {
        return generateGraphs;
    }

    public void setGenerateGraphs(Boolean generateGraphs) {
        this.generateGraphs = generateGraphs;
    }


    public Boolean getSearchHiddenConnections() {
        return SearchHiddenConnections;
    }

    public Integer getTokenizer() {
        return tokenizer;
    }

    public void setTokenizer(Integer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public void setSearchHiddenConnections(Boolean searchHiddenConnections) {
        SearchHiddenConnections = searchHiddenConnections;
    }

    public Boolean getDiscardParameters() {
        return discardParameters;
    }

    public void setDiscardParameters(Boolean discardParameters) {
        this.discardParameters = discardParameters;
    }

    public Boolean getDiscardImages() {
        return discardImages;
    }

    public void setDiscardImages(Boolean discardImages) {
        this.discardImages = discardImages;
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
