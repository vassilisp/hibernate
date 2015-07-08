package gq.panop.hibernate;

import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.dao.AuditLogDao;
import gq.panop.hibernate.dao.NavajoLogDao;
import gq.panop.hibernate.model.AccessLog;
import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.hibernate.mytypes.Transition;
import gq.panop.util.MiscUtil;
import gq.panop.util.PerformanceUtil;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.TreeMap;
import java.util.Iterator;


public class SessionTraversal {

    private List<String> requestedUserIds = null;
    
    private Writer writer = null;
    
    //=========================================================================
    private Boolean generateStatistics = true;
    private Boolean writeStatisticToFile = true;
    private Boolean detailedReport = true;
    //=========================================================================
    
    public void setupRequestedUserIds(List<String> requestedUserIds){
        this.requestedUserIds = requestedUserIds;
    }
    
    public void start(){
        PerformanceUtil performance = new PerformanceUtil("ms");
        PerformanceUtil overallPerf = new PerformanceUtil("ms");
        
        AuditLogDao adlDao = new AuditLogDao();
        AccessLogDao aclDao = new AccessLogDao();
        NavajoLogDao njlDao = new NavajoLogDao();
        
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

        SessionHandlerUniversal sHU = new SessionHandlerUniversal(1000*60*20, 2000);
        sHU.setGenerateGraphs(false);
        sHU.setDiscardImages(true);
        sHU.setDiscardParameters(true);
        sHU.setSearchHiddenConnections(false);
        sHU.setDebugMode(false);

        //Pick Session handlder
        SessionHandler SH = sHU;

        //List to hold kept (processed transitions)
        List<Transition> realTransitions = new ArrayList<Transition>();
        List<String> selectedUserIds = selectionOfUserIds(userIds);
        UserStatistics uS = new UserStatistics();

        //get all unique clientIds for each user
        for(String userId:selectedUserIds){
            System.out.println("#+#+#+#+#+#+##+#+#+#+#+## Session traversal starting for user:" + userId);
            
            performance.Tick("Acquire");
            List<String> clientIds = adlDao.getClientIds(userId);

            performance.Lap();
            //get all transactionIds for each clientId

            /*a clientId usually is similar to a daily session
             *a user can have multiple daily sessions active
             */

            
            for(String clientId:clientIds){
                List<AugmentedACL> augmentedACLs = aclDao.getFullEfficientOrderedSessionTransactions(clientId);
                //augmentedACLs list contains all transactions of the given clientId session

                if (augmentedACLs.size()>0){

                    //-----------------------------------------------------
                    
                    
                    SH.newUser(userId, clientId);
                    //Session processing
                    for(AugmentedACL augmentedACL:augmentedACLs){
                        //shTT.nextSession(augmentedACL);
                        SH.nextSession(augmentedACL);
                    }
                    realTransitions.addAll(SH.getSessions());
                    if (generateStatistics) {
                        //System.out.println(SH.getSessions().size());
                        
                        uS.addDayStatistic(SH.getSessions().get(0).getTimestamp(), SH.getSessions().size());
                        uS.addTotalRealTransitions(SH.getSessions().size());
                        uS.addTotalLogTransitions(augmentedACLs.size()); 
                    }
                    

                    //-----------------------------------------------------

                }  
            }
  
            if (generateStatistics){
                userStatistics.put(userId, uS);
                uS = new UserStatistics();
            }
            
            performance.Tock("Process");
            
  

            // Older methods used for benchmarking
            /*
            performance.Tick();
            for(String clientId:clientIds){

                //transactionIds contain all transactions for a give session
                List<String> transactionIds = njlDao.getTransactionIds(clientId);

                if(transactionIds.size()>0){
                    List<AccessLog> acl = aclDao.getAccessLogs(transactionIds);

                    for(AccessLog item:acl){
                        item.getNavajoLog().getTimestamp();
                        //System.out.println(item.toString());
                    }   
                }
            }
            performance.Tock("TEST1 - SLOW");
             */

            /*
            performance.Tick();
            for(String clientId:clientIds){
                //System.out.println("traversing: " + clientId);

                List<AccessLog> accessLogs = aclDao.getAccessLogs_fromNavajoLog(clientId);

                for(AccessLog item:accessLogs){
                    item.getNavajoLog().getTimestamp();
                    //System.out.println(item.toString());
                }
            }
            performance.Tock("TEST2 - FAST");
             */

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
            stater("Total Selected users: " + totalSelectedUsers);
            stater("-----------------------------------------------");
            stater("Total execution time: " + overallTime);
            stater("###############################################");
            
            
            if (detailedReport){
                String text = "";
                Map<String, UserStatistics> sorted = sortByVal(userStatistics);
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
                for (String key:sorted.keySet()){
                    if ((i++ < N)){
                        System.out.println(key);
                    }else{
                        break;
                    }
                        
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
    private static Map sortByVal(Map unsortedMap){
        List list = new LinkedList(unsortedMap.entrySet());
        
        Collections.sort(list, new Comparator(){

            @Override
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<String, UserStatistics>) o2).getValue().getTotalRealTransitions().compareTo(
                            ((Map.Entry<String, UserStatistics>) o1).getValue().getTotalRealTransitions());
            }
            
        });
        
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
        Integer option = 3;
        switch(option){
        
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
    
    private class UserStatistics{
        
        private Integer totalLogTransitions = 0;
        private Integer totalRealTransitions = 0;
        private Long firtTimestamp;
        private Long lastTimestamp;
        private String firstDay;
        private String lastDay;
        
        private Boolean first = true;
        
        private Map<String, Integer> dailyStatistic = new HashMap<String, Integer>();

        public Integer getTotalLogTransitions() {
            return totalLogTransitions;
        }

        public void setTotalLogTransitions(Integer totalLogTransitions) {
            this.totalLogTransitions = totalLogTransitions;
        }

        public Integer getTotalRealTransitions() {
            return totalRealTransitions;
        }

        public String getFirstDay() {
            return firstDay;
        }

        public String getLastDay() {
            return lastDay;
        }

        public void setTotalRealTransitions(Integer totalRealTransitions) {
            this.totalRealTransitions = totalRealTransitions;
        }

        public Long getFirtTimestamp() {
            return firtTimestamp;
        }

        public Long getLastTimestamp() {
            return lastTimestamp;
        }

        public Map<String, Integer> getDailyStatistic() {
            return dailyStatistic;
        }

        
        public void setDayStatistic(String day, Integer numOfTrans){
            this.dailyStatistic.put(day, numOfTrans);
        }
        
        public void addDayStatistic(Long timestamp, Integer numfOfTrans){
            Calendar a = Calendar.getInstance();
            a.setTimeInMillis(timestamp);
            String day = a.get(Calendar.DAY_OF_MONTH) + "." + a.get(Calendar.MONTH);
            
            //keeping first and last timestamps and days
            if (first){
                firtTimestamp = timestamp;
                firstDay = day;
                first = false;
            }
            lastTimestamp = timestamp;
            lastDay = day;
            
            //Keeping statistics
            if (!this.dailyStatistic.containsKey(day)){
                this.dailyStatistic.put(day, numfOfTrans);
            }else{
                this.dailyStatistic.put(day, this.dailyStatistic.get(day)+numfOfTrans);
            }
        }
        
        public void addTotalRealTransitions(Integer numOfTrans){
            this.totalRealTransitions += numOfTrans;
        }
        
        public void addTotalLogTransitions(Integer numOfTrans){
            this.totalLogTransitions += numOfTrans;
        }
        
    }
}


