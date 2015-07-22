package SessionHandlers;

import gq.panop.hibernate.JungGraphCreator;
import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.hibernate.mytypes.Transition;
import gq.panop.util.MiscUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class SessionHandlerTimeThreshold implements SessionHandler{


    private String userId;
    private String clientId;
    
    //------------Settings---------------------------
    private Boolean parallelDraw = false;
    private JungGraphCreator jungGraph = null;
    public Boolean acceptUnlinkedNodes = true;
    private Boolean discardParameters = true;
    private Boolean keepTimeStatistics = false;

    
    private Integer subSessionThreshold = 1000 * 60 * 25 * 0; //ms * sec * min * (0/1 - disable/enable)
    private Integer revisitedThreshold = 1000 * 15 * 1;//
    private Integer autoRequestThreshold = 1000 * 2; //ms
    //-----------------------------------------------
    
    private HashMap<Long, Integer> intervalStatistics = new HashMap<Long, Integer>();
    private List<Long> intervalList = new ArrayList<Long>();
    
    private List<Transition> transitions = new ArrayList<Transition>();
    private List<Transition> transitionBuffer = new ArrayList<Transition>();
    
    private List<Node> loadedNodes = new ArrayList<Node>();
    
    public SessionHandlerTimeThreshold(Integer subSessionThreshold, Integer autoRequestThreshold){
        this.subSessionThreshold = subSessionThreshold;
        this.autoRequestThreshold = autoRequestThreshold;
    }
    
    public void newUser(String userId, String clientId){
        this.userId = userId;
        this.clientId = clientId;
        this.transitions.clear();
        this.transitionBuffer.clear();
        this.jungGraph = new JungGraphCreator(true, false);
    }
    
    public void setParallelDraw(Boolean parallelDraw){
        this.parallelDraw = parallelDraw;
        this.jungGraph = new JungGraphCreator(true, false);
    }

    
    public void nextSession(AugmentedACL session){
        String referer = session.getAccessLog().getReferer();
        
        String target = session.getAccessLog().getRequestedResource();
        //Define what parts to keep from the target (all, skip parameters, ..)
        
        referer = MiscUtil.URLRefererCleaner(referer);
        target = MiscUtil.URLTargetCleaner(target);
        
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
        
        if (!(target.contains("css")) && !(referer.contains("css")) && !(target.contains(".ico"))){

            System.out.println(referer + "  /// " + target);
            Long timestamp = session.getTimestamp();

            Transition currentTransition = new Transition(referer, target, timestamp);
            currentTransition.setTransactionId(session.getAccessLog().getTransactionId());
            currentTransition.setUserId(userId);



            Long interval = null;

            ///----ACTUAL CODE FROM HERE


            Node pastNode = null;
            Boolean linkFound = false;
            for (int i=0 ; i<loadedNodes.size(); i++){
                pastNode = loadedNodes.get(i);
                interval = currentTransition.getTimestamp() - pastNode.getTimestamp();

                if (pastNode.getName().equals(currentTransition.getReferer())){
                    System.out.println("target-referer /" + pastNode.getName());
                    System.out.println(interval);
                    System.out.println("----------------------------");
                    addIntervalToStatistics(interval);

                    linkFound = true;
                    if (interval>autoRequestThreshold){
                        transitionBuffer.add(currentTransition);

                        updateLoadedNodes(currentTargetNode);
                        updateLoadedNodes(currentRefererNode);
                        transitions.add(currentTransition);
                        parallelDrawing(currentTransition);
                        break;
                    }else{
                        updateLoadedNodes(currentRefererNode);
                        updateLoadedNodes(currentTargetNode);
                        break;
                    }
                }else if (currentTransition.getTarget().equals(pastNode.getName())){
                    //linkFound = true;
                    if (interval>autoRequestThreshold){
                        if (interval<revisitedThreshold && revisitedThreshold>0){
                            //not a new visit
                            System.out.println("Revisit under threshold detected -- do nothings");
                        }else{
                            //new visit (default behavior)
                            //transitionBuffer.get(i).setTimestamp(currentTransition.getTimestamp());

                            updateLoadedNodes(currentTargetNode);
                            updateLoadedNodes(currentRefererNode);
                            //transitions.add(currentTransition);
                            //parallelDrawing(currentTransition);
                            //break;
                        }
                    }
                }
            }


            if (linkFound==false){
                if (acceptUnlinkedNodes==true){
                    System.out.println("new unlinked node");
                    //perhaps check unlinked nodes for the interval between the last transaction
                    updateLoadedNodes(currentTargetNode);
                    updateLoadedNodes(new Node(referer, timestamp));
                    transitionBuffer.add(currentTransition);
                    transitions.add(currentTransition);
                    parallelDrawing(currentTransition);
                }
            }
        }else{
            System.out.println("CSS FOUND");
            updateLoadedNodes(currentTargetNode);
            updateLoadedNodes(currentRefererNode);
        }
        
    }
    
    private void addIntervalToStatistics(Long interval){
        if (keepTimeStatistics){
            if (intervalStatistics.containsKey(interval)){
                intervalStatistics.put(interval, intervalStatistics.get(interval) + 1);
            }else{
                intervalStatistics.put(interval, 1);
            }
            intervalList.add(interval);
        }
    }
    
    public void writeStatisticsToFile(String path){
        if (keepTimeStatistics){
            String timeId = ((Long)(System.currentTimeMillis()/10000)).toString();
            timeId = timeId.substring(5, timeId.length());
            String fileName = "intervals" + timeId + ".txt";
            PrintWriter writer = null;
            try{
                //writer = new CSVWriter(new FileWriter(Path));
                writer = new PrintWriter(path + fileName, "UTF-8");
            }catch(Throwable e){
                System.err.println("Error creating output file" + System.lineSeparator() + e );
            }
            intervalList.forEach((writer::println));
        }
    }
    private void updateLoadedNodes(Node e){
        Boolean found = false;
        for (Node tmpNode:loadedNodes){
            if (tmpNode.getName().equals(e.getName())){
                tmpNode.setTimestamp(e.getTimestamp());
                found = true;
                break;
            }
        }
        if (!found){
            loadedNodes.add(e);
        }
        
    }
    
    public List<Transition> getSessions(){
        jungGraph.Refresh();
        return transitions;
    }
    
    public void DrawSession(){
        JungGraphCreator jgc = new JungGraphCreator(false, false);
        for (Transition transition: transitions){
            jgc.AddTransition(transition);
        }
    }
    
    public void parallelDrawing(Transition transition){
        if (parallelDraw){
            jungGraph.AddTransition(transition);
        }
    }
    

    
   public class Node{
        private String name;
        private Long timestamp;
        private String sessioId;
        
        
        public String getSessioId() {
            return sessioId;
        }
        public void setSessioId(String sessioId) {
            this.sessioId = sessioId;
        }
        public Node(String name, Long timestamp, String sessioId) {
            super();
            this.name = name;
            this.timestamp = timestamp;
            this.sessioId = sessioId;
        }
        public Node(String name, Long timestamp) {
            super();
            this.name = name;
            this.timestamp = timestamp;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Long getTimestamp() {
            return timestamp;
        }
        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
        
        
    }
    
    class SimpleTransition{
        private String referer;
        private String target;
        private Long timestamp;
        private String sessionId;

        public SimpleTransition(String referer, Long timestamp) {
            super();
            this.referer = referer;
            this.timestamp = timestamp;
        }
        
        
        public String getReferer() {
            return referer;
        }

        public void setReferer(String name) {
            this.referer = name;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
        
        public Long getTimestamp() {
            return timestamp;
        }
        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
        
        public boolean similar(SimpleTransition o){
            try{
                if (((SimpleTransition)o).target==target || ((SimpleTransition)o).referer==target){
                    return true;
                }else{
                    return false;
                }
            }catch(Throwable e){
                return false;
            }
        }
    }
}
