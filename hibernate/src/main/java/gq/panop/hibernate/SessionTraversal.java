package gq.panop.hibernate;

import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.dao.AuditLogDao;
import gq.panop.hibernate.dao.NavajoLogDao;
import gq.panop.hibernate.model.AccessLog;
import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.util.PerformanceUtil;

import java.util.List;

public class SessionTraversal {

    
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
        
        
        SessionHandlerTimeThreshold shTT = new SessionHandlerTimeThreshold(0, 2000);
        shTT.setParallelDraw(true);
        
        //get all unique clientIds for each user
        for(String userId:userIds){
            System.out.println("#+#+#+#+#+#+##+#+#+#+#+## Session traversal starting for user:" + userId);
            List<String> clientIds = adlDao.getClientIds(userId);
            
            //get all transactionIds for each clientId
            
            /*a clientId usually is similar to a daily session
             *a user can have multiple daily sessions active
             */
            
            performance.Tick();
            for(String clientId:clientIds){
                List<AugmentedACL> augmentedACLs = aclDao.getFullEfficientOrderedSessionTransactions(clientId);
                //augmentedACLs list contains all transactions of the given clientId session
            
                shTT.newUser(userId, clientId);
                //Session processing
                for(AugmentedACL augmentedACL:augmentedACLs){
                    shTT.nextSession(augmentedACL);
                }
                
                shTT.getSessions();
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

    }
    
    private void SessionProcessor(AugmentedACL augACL){
        
    }
}
