package gq.panop.hibernate;

import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.dao.AuditLogDao;
import gq.panop.hibernate.dao.NavajoLogDao;
import gq.panop.hibernate.model.AccessLog;
import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.hibernate.mytypes.Transition;
import gq.panop.util.PerformanceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SessionTraversal {

    private List<String> requestedUserIds = null;
    
    private Boolean generateStatistics = true;
    
    public void setupRequestedUserIds(List<String> requestedUserIds){
        this.requestedUserIds = requestedUserIds;
    }
    
    public void start(){
        PerformanceUtil performance = new PerformanceUtil("ms");
        PerformanceUtil overallPerf = new PerformanceUtil("ms");
        
        AuditLogDao adlDao = new AuditLogDao();
        AccessLogDao aclDao = new AccessLogDao();
        NavajoLogDao njlDao = new NavajoLogDao();
        
        try{
        performance.Tick();
        Thread.sleep(200);;
        performance.Lap();
        Thread.sleep(1000);
        performance.Tock("different name");
        }catch(Throwable e){System.out.println(e);};
        
        overallPerf.Tick();
        //get all users;
        performance.Tick();
        List<String> userIds = adlDao.getAllUsers();
        performance.Tock();
        
        
        Map<String, Integer> userRealTransCounter = null;
        Map<String, Integer> userLogTransCounter = null;
        Integer TotalUsers = null;
        if (generateStatistics == true){
            userRealTransCounter = new HashMap<String, Integer>();
            userLogTransCounter = new HashMap<String, Integer>();
            TotalUsers = 0;
        }
                
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
        Integer sessionKeptTransitions = null;
        Integer sessionLogTransitions = null;
        List<String> selectedUserIds = selectionOfUserIds(userIds);

        //get all unique clientIds for each user
        for(String userId:selectedUserIds){
            System.out.println("#+#+#+#+#+#+##+#+#+#+#+## Session traversal starting for user:" + userId);
            List<String> clientIds = adlDao.getClientIds(userId);

            if (generateStatistics == true){
                userLogTransCounter.put(userId, 0);
                userRealTransCounter.put(userId, 0);
                sessionKeptTransitions = 0;
                sessionLogTransitions = 0;
            }
            //get all transactionIds for each clientId

            /*a clientId usually is similar to a daily session
             *a user can have multiple daily sessions active
             */

            performance.Tick();
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
                        sessionKeptTransitions += SH.getSessions().size();
                        sessionLogTransitions += augmentedACLs.size();
                        //System.out.println(SH.getSessions().size());
                    }
                    

                    //-----------------------------------------------------


                }

            }
            if (generateStatistics == true){
                userRealTransCounter.put(userId, sessionKeptTransitions);
                userLogTransCounter.put(userId, sessionLogTransitions);
            }
            performance.Tock("Super FAST");



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
        overallPerf.Tock("---Overall Performance");
        
        if (generateStatistics == true){
            Integer totalTransitions = 0;
            for (Integer val:userRealTransCounter.values()){
                totalTransitions += val;
            }
            
            Integer totalSelectedUsers = 0;
            totalSelectedUsers = userRealTransCounter.keySet().size();
            
            Integer totalLogTransitions = 0;
            for (Integer val:userLogTransCounter.values()){
                totalLogTransitions += val;
            }
            
            Long keptPerc = 100 * totalTransitions.longValue()/totalLogTransitions.longValue();
            System.out.println("###############################################");
            System.out.println("STATISTICS");
            System.out.println("-----------------------------------------------");
            System.out.println("Total transitions from Logs: " + totalLogTransitions );
            System.out.println("Total transitions kept after SessionHandling: " + totalTransitions);
            System.out.println("Kept transitions % : " + keptPerc);
            System.out.println("Total Selected users: " + totalSelectedUsers);
            System.out.println("###############################################");
            
            System.out.printf("%-10s, %-7s, %s"  + System.lineSeparator(),"USER", "LOG", "KEPT");
            for (Map.Entry<String, Integer>set:userLogTransCounter.entrySet()){
                System.out.printf("%-10s, %-7s ,%s " + System.lineSeparator() , set.getKey(), set.getValue().toString(), userRealTransCounter.get(set.getKey()).toString());
            }
        }
        
        for (Transition trans:realTransitions){
            //System.out.println(trans);
        }
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
    }
    
}
