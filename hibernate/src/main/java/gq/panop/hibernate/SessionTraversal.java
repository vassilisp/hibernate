package gq.panop.hibernate;

import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.dao.AuditLogDao;
import gq.panop.hibernate.dao.NavajoLogDao;
import gq.panop.hibernate.dao.PreprocessDao;
import gq.panop.hibernate.model.AccessLog;
import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.hibernate.mytypes.Transition;
import gq.panop.hibernate.mytypes.UserStatistics;
import gq.panop.util.MiscUtil;
import gq.panop.util.PerformanceUtil;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;


public class SessionTraversal {

    private List<String> requestedUserIds = null;
    
    private Writer writer = null;
    
    //=========================================================================
    private Boolean generateStatistics = true;
    private Boolean writeStatisticToFile = true;
    private Boolean detailedReport = true;
    private Integer selectedUserIdsOption = -1; //default
    //=========================================================================
    
    public void setupRequestedUserIds(List<String> requestedUserIds){
        this.requestedUserIds = requestedUserIds;
    }
    
    public void start(){
        PerformanceUtil performance = new PerformanceUtil("ms");
        PerformanceUtil overallPerf = new PerformanceUtil("ms");
        
        AuditLogDao adlDao = new AuditLogDao();
        AccessLogDao aclDao = new AccessLogDao();
        
        PreprocessDao prepDao = new PreprocessDao();
        
        /*
        try{
            performance.Tick();
            Thread.sleep(200);;
            performance.Lap();
            Thread.sleep(1000);
            performance.Tock("different name");
        }catch(Throwable e){System.out.println(e);};
        */
        
        overallPerf.Tick();
        //get all users;
        performance.Tick();
        List<String> userIds = adlDao.getAllUsers();
        performance.Tock();
         
        Map<String, UserStatistics> userStatistics = new HashMap<String, UserStatistics>();
                
        //SessionHandlerTimeThreshold shTT = new SessionHandlerTimeThreshold(0, 1250);
        //shTT.setParallelDraw(true);
        
        //SessionHandlerInterGroupTimeThreshold shigTT = new SessionHandlerInterGroupTimeThreshold(1000*60*20, 1000);
        //shigTT.setParallelDraw(true);

        //SessionHandler settings ---------------------------------------------
        SessionHandlerUniversal sHU = new SessionHandlerUniversal(1000*60*20, 2000);
        sHU.setGenerateGraphs(false);
        sHU.setDiscardImages(true);
        sHU.setDiscardParameters(true);
        sHU.setSearchHiddenConnections(false);
        sHU.setDebugMode(false);
        sHU.setTokenizer(0);
        //---------------------------------------------------------------------

        //Pick Session handlder
        SessionHandler SH = sHU;

        //List to hold kept (processed transitions)
        List<Transition> realTransitions = new ArrayList<Transition>();
        List<String> selectedUserIds = selectionOfUserIds(userIds);
        UserStatistics uS = new UserStatistics();

        Integer TotalUsers = userIds.size();
        Integer userCounter = 1;
        
        Integer totalClientIds = 0;
        
        Long etaEstimation = 0L;
        Long etaAccumulator = 0L;
        //get all unique clientIds for each user
        selectedUserIds.remove("null");
        for(String userId:selectedUserIds){
            System.out.println("#+#+#+#+#+#+##+#+#+#+#+## ((" + userCounter++ + "/" + TotalUsers + ")) || Session traversal starting for user:" + userId);
            
            performance.Tick("Acquire");
            List<String> clientIds = adlDao.getClientIds(userId);
            
            totalClientIds += clientIds.size();
            
            //performance.Lap();
            //get all transactionIds for each clientId

            /*a clientId usually is similar to a daily session
             *a user can have multiple daily sessions active
             */

            Integer clientIdCounter = 0;
            Integer progress = 0;
            Integer prevProgress = 0;
            for (int i=0;i<50; i++){
                System.out.print("x");
            }
            System.out.println("|");
            
            
            for(String clientId:clientIds){
                List<AugmentedACL> augmentedACLs = aclDao.getFullEfficientOrderedSessionTransactions(clientId);
                //augmentedACLs list contains all transactions of the given clientId session
                //performance.Lap();
                
                
                progress = (int) (++clientIdCounter * (50.0/clientIds.size()));
                //System.out.print("(" + progress + ")-");
                for (int i=0;i<progress-prevProgress; i++){
                    System.out.print("=");
                }
                prevProgress = progress;
                
                
                if (augmentedACLs.size()>0){

                    //-----------------------------------------------------
                    
                    
                    SH.newUser(userId, clientId);
                    //Session processing
                    for(AugmentedACL augmentedACL:augmentedACLs){
                        //shTT.nextSession(augmentedACL);
                        SH.nextSession(augmentedACL);
                    }
                    //realTransitions.addAll(SH.getSessions());
                    //System.out.println("Number of kept transitions: " + SH.getSessions().size() + " // Total: " + augmentedACLs.size());
                    if (generateStatistics) {
                        //System.out.println(SH.getSessions().size());
                        
                        uS.addDayStatistic(SH.getSessions().get(0).getTimestamp(), SH.getSessions().size());
                        uS.addTotalRealTransitions(SH.getSessions().size());
                        uS.addTotalLogTransitions(augmentedACLs.size()); 
                    }
                    
                    //---------------------------------------------------------
                    //Save results
                    prepDao.saveTransactions(SH.getSessions());
                    
                    //---------------------------------------------------------

                }  
            }
  
            if (generateStatistics){
                userStatistics.put(userId, uS);
                uS = new UserStatistics();
            }
            
            System.out.println("");
            etaAccumulator = ((etaAccumulator*(userCounter-2)) + performance.Tock("Process"))/(userCounter-1);
            etaEstimation = etaAccumulator * (TotalUsers-(userCounter-1));
            etaEstimation = etaEstimation/(1000*60);
            System.out.println("**************************************************** - - E T A : " + etaEstimation + "minutes");

        }
        Long overallTime = overallPerf.Tock("---Overall Performance");
        
        if (generateStatistics == true){
            Integer totalTransitions = 0;
            Integer totalLogTransitions = 0;
            for (UserStatistics tmp_uS:userStatistics.values()){
                totalTransitions += tmp_uS.getTotalRealTransitions();
                totalLogTransitions += tmp_uS.getTotalLogTransitions();
            }

            Integer totalSelectedUsers = userStatistics.keySet().size();
            
            Long keptPerc = 100 * totalTransitions.longValue()/totalLogTransitions.longValue();
            stater("###############################################");
            stater("STATISTICS");
            stater("-----------------------------------------------");
            stater("Total transitions from Logs: " + totalLogTransitions );
            stater("Total transitions kept after SessionHandling: " + totalTransitions);
            stater("Kept transitions % : " + keptPerc);
            stater("-----------------------------------------------");
            stater("Total Selected users: " + totalSelectedUsers);
            stater("Total clientIds traversed: " + totalClientIds);   
            stater("-----------------------------------------------");
            stater("Total execution time: " + overallTime/(1000*60) + " mins");
            stater("###############################################");
            
            if (detailedReport){
                String text = "";
                Map<String, UserStatistics> sorted = sortByVal(userStatistics, TotalLog);
                text = String.format("     %-10s, %-7s, %-7s, %-7s, %-7s","USER", "LOG", "KEPT", "FIRST", "LAST");
                stater(text);
                Integer index = 0;
                for (Map.Entry<String, UserStatistics> set :sorted.entrySet()){
                    text = String.format("%-3d) %-10s, %-7s, %-7s, %-7s, %-7s" ,index++, set.getKey(), set.getValue().getTotalLogTransitions().toString(),
                            set.getValue().getTotalRealTransitions(), set.getValue().getFirstDay(), set.getValue().getLastDay());
                    stater(text);
                }

                //get top N users
                Integer N = 10;
                Integer i = 0;
                String topKey = "";
                String dailyMsg = "";
                stater("Top 10 users");
                for (String key:sorted.keySet()){
                    if ((i++ < N)){
                        stater(key);
                        
                        //daily info
                        dailyMsg = "";
                        

                        
                        Map<String, Integer> sortedDays = sortByVal(sorted.get(key).getDailyStatistic(), dailyStatComparator);
                        for (Entry<String, Integer> entrySet:sortedDays.entrySet()){
                            dailyMsg += entrySet.getKey() + ")" + entrySet.getValue() + "\t";
                        }
                        stater(dailyMsg);
                    }else{
                        break;
                    }
                        
                }
                
                Map<String, Integer> totalDailyStats = new HashMap<String, Integer>();
                
                for (UserStatistics tmpUs: userStatistics.values()){
                    Map<String, Integer> sortedTotalDailyStats = sortByVal(tmpUs.getDailyStatistic(), dailyStatComparator);
                    for(Entry<String, Integer> daily : sortedTotalDailyStats.entrySet()){
                        Integer previous = 0;
                        previous = totalDailyStats.get(daily.getKey());
                        if (previous == null) previous = 0;
                        totalDailyStats.put(daily.getKey(), previous + daily.getValue());
                    }
                }
                
                stater("=======================================================");
                Map<String,Integer> sortedDailyStats = sortByVal(totalDailyStats, dailyStatComparator);
                for (Entry<String, Integer> tmp:sortedDailyStats.entrySet()){
                    String textBuf = tmp.getKey() + ")" + tmp.getValue();
                    stater(textBuf);
                }
                
            }
            if(writeStatisticToFile){
                try{
                    writer.close();
                }catch(Throwable e){
                    System.err.println(e);
                }
            }
            
            for (Transition trans:realTransitions){
                //System.out.println(trans);
            }
            
            try {
                Calendar a = Calendar.getInstance();
                String filename = "serializedObjects/userStats";
                filename = filename +a.get(Calendar.DAY_OF_MONTH) + (a.get(Calendar.MONTH)+1) 
                        + a.get(Calendar.MINUTE) + ".ser" ;
                
                FileOutputStream fileOut = new FileOutputStream(filename);
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
                
                objectOut.writeObject(userStatistics);
                
                objectOut.flush();
                objectOut.close();
                
                fileOut.close();
                System.out.println("UserStatistics serialized to file");
                
                
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        
    }
    
    private void stater(String text){
        text = text + System.lineSeparator();
        if (generateStatistics){
            System.out.print(text);
        }
        if(writeStatisticToFile){        
            try {

                writer.append(text);
            } catch (Throwable e) {

                e.printStackTrace();
                try{
                    Calendar a = Calendar.getInstance();
                    String filename = "statReports/statReport_";
                    filename = filename +a.get(Calendar.DAY_OF_MONTH) + "_" 
                            + ((Integer)(a.get(Calendar.MONTH)+1)).toString()
                            +  "_" + a.get(Calendar.HOUR_OF_DAY) +  "_" + a.get(Calendar.MINUTE);
                    
                    writer = new BufferedWriter( new OutputStreamWriter(
                            new FileOutputStream(filename), "utf-8"));
                    writer.append(text);
                } catch (UnsupportedEncodingException | FileNotFoundException ee) {
                    System.err.println("ERROR OPENING FILE FOR WRITING");
                    ee.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }

    }
    
    
    static Comparator TotalLog = new Comparator(){

        @Override
        public int compare(Object o1, Object o2) {
            return ((Map.Entry<String, UserStatistics>) o2).getValue().getTotalLogTransitions().compareTo(
                        ((Map.Entry<String, UserStatistics>) o1).getValue().getTotalLogTransitions());
        }
        
    };
    

    static Comparator dailyStatComparator = new Comparator(){

        @Override
        public int compare(Object o1, Object o2) {
            String[] date1 =  ((Map.Entry<String, Integer>) o1).getKey().split("\\.");
            String[] date2 = ((Map.Entry<String, Integer>) o2).getKey().split("\\.");
            if (date1[1].equals(date2[1])){
                if (Integer.valueOf(date1[0])>Integer.valueOf(date2[0])){
                    return 1;
                }else{
                    return -1;
                }
            }else{
                if (Integer.valueOf(date1[1])>Integer.valueOf(date2[1])){
                    return 1;
                }
                else{
                    return -1;
                }
            }

        }
    };
        
    private static Map sortByVal(Map unsortedMap, Comparator comp){
        List list = new LinkedList(unsortedMap.entrySet());
        
        Collections.sort(list, comp);
        
        Map sortedMap = new LinkedHashMap();
        
        for (Iterator it = list.iterator(); it.hasNext();){
            Map.Entry entry = (Map.Entry) it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
            
        }
        return sortedMap;
    }

    
    private static List sortByValtoList(Map unsortedMap){
        List list = new LinkedList(unsortedMap.entrySet());
        
        Collections.sort(list, new Comparator(){

            @Override
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<String, UserStatistics>) o2).getValue().getTotalRealTransitions().compareTo(
                            ((Map.Entry<String, UserStatistics>) o1).getValue().getTotalRealTransitions());
            }
            
        });
        
        return list;
    }
    
    private List<String> selectionOfUserIds(List<String> userIds){
        //decide which users should be kept for the final set
        
        List<String> selectedUserIds = new ArrayList<String>();
        if (selectedUserIdsOption==-1){
            //set default value
            Integer selectedUserIdsOption = 3;
        }
        switch(selectedUserIdsOption){
        
        case 1:
            //OPTION1
            //random N users, where N is given:
            Collections.shuffle(userIds);
            ;
            //how many persons to pick
            Integer N = 50;
            for (int i=0; i<N; i++){
                selectedUserIds.add(userIds.remove(0));
            }
            break;
        
        case 2:
            //OPTION2
            //Select by hand
            selectedUserIds.add("Sabine");
            break;
            
        default:
            //DEFAULT OPTION
            //Return all
            selectedUserIds.addAll(userIds);
            break;
            
        }
        return selectedUserIds;
    }
    
    private void visialize(List<Transition> Transitions){
        jungGraphCreatorStringVertices jgc = new jungGraphCreatorStringVertices(false, false);
        for (Transition trans : Transitions){
            jgc.AddTransition(trans);
        }
    }
    
}


