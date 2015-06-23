package gq.panop.hibernate;

import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.dao.AuditLogDao;
import gq.panop.hibernate.dao.NavajoLogDao;
import gq.panop.hibernate.model.AccessLog;
import gq.panop.util.PerformanceUtil;

import java.util.List;

public class SessionTraversal {

    
    public void start(){
        PerformanceUtil performance = new PerformanceUtil("ms");
        
        AuditLogDao adlDao = new AuditLogDao();
        AccessLogDao aclDao = new AccessLogDao();
        NavajoLogDao njlDao = new NavajoLogDao();
        
        //get all users;
        performance.Tick();
        List<String> userIds = adlDao.getAllUsers();
        performance.Tock();
        
        //get all unique clientIds for each user
        for(String userId:userIds){
            System.out.println("#+#+#+#+#+#+##+#+#+#+#+## Session traversal starting for user:" + userId);
            List<String> clientIds = adlDao.getClientIds(userId);
            
            //get all transactionIds for each clientId
            
            /*a clientId usually is similar to a daily session
             *a user can have multiple daily sessions active
             */
            for(String clientId:clientIds){
                System.out.println("traversing: " + clientId);
                
                
                performance.Tick();
                List<AccessLog> acl2 = aclDao.getAccessLogs_fromNavajoLog(clientId);
                performance.Tock("TEST2");
                
                
                //transactionIds contain all transactions for a give session
                List<String> transactionIds = njlDao.getTransactionIds(clientId);
                
                performance.Tick();
                if(transactionIds.size()>0){
                    List<AccessLog> acl = aclDao.getAccessLogs(transactionIds);
                    performance.Tock("test1");
                    
                    System.out.println(acl2.contains(acl));
                    System.out.println(acl2.size() +" ---" + acl.size());
                    
                    for(AccessLog item:acl){
                        //System.out.println(item.toString());
                    }
                }

                

                
                
                
            }


        }
        

        
        
        
        
        
        
    }
}
