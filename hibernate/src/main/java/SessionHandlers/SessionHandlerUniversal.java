package SessionHandlers;

import gq.panop.hibernate.URLNormalizer;
import gq.panop.hibernate.UniqueIDAssigner;
import gq.panop.hibernate.jungGraphCreatorStringVertices;
import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.hibernate.mytypes.Transition;
import gq.panop.util.MiscUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionHandlerUniversal implements SessionHandler {

    private Integer internalCounter = 0;
    
    private String userId;
    private String clientId;

    private Long lastUniversalRequest = 0L;
    private String lastSubSessionId = "";
    
//==========================PARAMS=============================================
    private Integer subSessionThreshold = 1000 * 60 * 25; //ms * sec * min
    private Integer revisitedThreshold = 1000 * 2;//
    private Integer autoRequestThreshold = 1500; //ms

    private Boolean SearchHiddenConnections = false;
  
    private Boolean generateGraphs = false;
    private Boolean debugMode = false;
    
    private Boolean discardParameters = true;
    private Boolean discardImages = true;
    private Integer beforeTokenizer = 0;
    private Boolean discardCSSICO = true;
    
    private Integer afterTokenizer = 0;
//=============================================================================    
    
    private List<Transition> transitions = new ArrayList<Transition>();
    
    private UniqueIDAssigner uID = new UniqueIDAssigner();
    
    private List<Node> loadedNodes = new ArrayList<Node>();
    

    private Integer subSessionCounter=0;
    
    private URLNormalizer urlN = new URLNormalizer(discardParameters, beforeTokenizer, discardImages);
    
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
    

    public void setURLNormalizer(Boolean discardParameters, Integer tokenizer, Boolean discardImages){
        urlN = new URLNormalizer(discardParameters, tokenizer, discardImages);
    }
    
    public void nextSession(AugmentedACL session){
        //---------------URL normalizer----------------------------------------
        String referer = session.getAccessLog().getReferer();
        
        String target = session.getAccessLog().getRequestedResource();
        //Define what parts to keep from the target (all, skip parameters, ..)
        
        referer = MiscUtil.URLRefererCleaner(referer);
        target = MiscUtil.URLTargetCleaner(target);
                       
               
        Boolean isImage = (((target.toLowerCase().endsWith(".png")||referer.toLowerCase().endsWith(".png") ||referer.toLowerCase().endsWith(".jpg") ||
                referer.toLowerCase().endsWith(".gif")) && discardImages));
        Boolean isCSS = (target.contains("css")) || (referer.contains("css"));
        Boolean isICO = (target.contains(".ico") || referer.contains(".ico")); 
        Boolean specialReq = false;
        if (isImage || isCSS || isICO) {
            specialReq = true;
            customDeb("AUTOREQUEST DETECTED BY TYPE");
        }

        if (beforeTokenizer>0){
            referer = MiscUtil.custom_Parser(referer, beforeTokenizer);
            target = MiscUtil.custom_Parser(referer, beforeTokenizer);
        }

        if (discardParameters){
            referer = MiscUtil.discardParamaters(referer);
            target = MiscUtil.discardParamaters(target);
        }
        //---------------------------------------------------------------------
        
        
        Long timestamp = session.getTimestamp();
        String transactionId = session.getAccessLog().getTransactionId();
        
        
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
        /*
        if(afterTokenizer>0){
            String referer = transition.getReferer();
            referer = MiscUtil.custom_Parser(referer, afterTokenizer);
            
            String target = transition.getTarget();
            target = MiscUtil.custom_Parser(target, afterTokenizer);
            
        }*/
        
        String referer = transition.getReferer();
        String target = transition.getTarget();
        
        transition.setRefererID(uID.pageVectorizer(transition.getReferer()));
        transition.setTargetID(uID.pageVectorizer(transition.getTarget()));
        transition.setUserId(uID.userVectorizer(transition.getUserId()));
        
        
        
        String refererID4 = MiscUtil.custom_Parser(referer, 4);
        String targetID4 = MiscUtil.custom_Parser(target, 4);
        
        String refererID3 = MiscUtil.custom_Parser(referer, 3);
        String targetID3 = MiscUtil.custom_Parser(target, 3);
        
        String refererID2 = MiscUtil.custom_Parser(referer, 2);
        String targetID2 = MiscUtil.custom_Parser(target, 2);
        
        String refererID1 = MiscUtil.custom_Parser(referer, 1);
        String targetID1 = MiscUtil.custom_Parser(target, 1);
        
        transition.setRefererID1(uID.pageVectorizer(refererID1));
        transition.setRefererID2(uID.pageVectorizer(refererID2));
        transition.setRefererID3(uID.pageVectorizer(refererID3));
        transition.setRefererID4(uID.pageVectorizer(refererID4));
        
        transition.setTargetID1(uID.pageVectorizer(targetID1));
        transition.setTargetID2(uID.pageVectorizer(targetID2));
        transition.setTargetID3(uID.pageVectorizer(targetID3));
        transition.setTargetID4(uID.pageVectorizer(targetID4));
        
        //------------------------------------------------------------------
        
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
    
    public String getParameterString(){
     
        String paramString = "";
        if (discardParameters){
            paramString += "DParam-";
        }else{
            paramString += "KParam-";
        }
        
        if (discardImages){
            paramString += "DImg-";
        }else{
            paramString += "KImg-";
        }
        
        paramString += "Tok" + beforeTokenizer.toString() + "-";
        
        if (discardCSSICO){
            paramString += "DCss-";
        }else{
            paramString += "KCss-";
        }

        if (SearchHiddenConnections){
            paramString += "FindHidden-";
        }else{
            paramString += "DropHidden-";
        }
        
        paramString += System.lineSeparator();
        paramString += "//" + "autoT:" + autoRequestThreshold +" | revT:" + revisitedThreshold + " |subSesT: " + subSessionThreshold;
        
        return paramString;
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

    public Integer getBeforeTokenizer() {
        return beforeTokenizer;
    }

    public void setBeforeTokenizer(Integer tokenizer) {
        this.beforeTokenizer = tokenizer;
    }

    
    public Integer getAfterTokenizer() {
        return afterTokenizer;
    }

    public void setAfterTokenizer(Integer afterTokenizer) {
        this.afterTokenizer = afterTokenizer;
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
    
    public String getName(){
        return "SessionHandlerUniversal";
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
    
    public UniqueIDAssigner getUniqueIDAssigner(){
        return this.uID;
    }
    
}
