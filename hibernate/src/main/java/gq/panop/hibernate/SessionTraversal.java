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
            System.out.println("Session traversal starting for user:" + userId);
            List<String> clientIds = adlDao.getClientIds(userId);
            
            //get all transactionIds for each clientId
            for(String clientId:clientIds){
                System.out.println("traversing: " + clientId);
                List<String> transactionIds = njlDao.getTransactionIds(clientId);
                
                performance.Tick();
                if(transactionIds.size()>0){
                    List<AccessLog> acl = aclDao.getAccessLogs(transactionIds);
                    for(AccessLog item:acl){
                        //System.out.println(item.toString());
                    }
                }
                performance.Tock("TEST2");
            }


        }
        

        
        
        
        
        
        
    }
}