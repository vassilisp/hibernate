package gq.panop.hibernate;

import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.dao.AuditLogDao;
import gq.panop.hibernate.dao.NavajoLogDao;
import gq.panop.hibernate.model.AccessLog;
import gq.panop.hibernate.mytypes.TransactionId_Timestamp;
import gq.panop.util.PerformanceUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IntervalFreq {

    /*for each user get his transitions ordered by timestamp
     * Then calculate and store the intervals between request
     * This is done in an attempt to figure out two major frequencies.
     * The browser generated and the user generated
     */
    public static void generate(){
        
        
        PerformanceUtil performance = new PerformanceUtil("ms");
        AuditLogDao adlDao = new AuditLogDao();
        AccessLogDao aclDao = new AccessLogDao();
        NavajoLogDao njlDao = new NavajoLogDao();
        
        performance.Tick();
        List<String> userIds = adlDao.getAllUsers();
        performance.Tock();

        
        //count every interval << interval, how many times >>
        HashMap<Integer,Integer> intervals = new HashMap<Integer,Integer>();
        List<Object[]> timestamps = null;


        for (String userId : userIds){
        
            System.out.println(userId);
            performance.Tick();
            
            List<String> clientIds = adlDao.getClientIds(userId);
            
            for(String clientId:clientIds){
                List<AccessLog> accessLogs = aclDao.getAccessLogs_fromNavajoLog(clientId);
                
                timestamps = aclDao.getOrderedClientIdTimestamps(clientId);
                
                List<TransactionId_Timestamp> TransTime = njlDao.getTransactionIdsANDTimestamps(clientId);
                for(TransactionId_Timestamp tT:TransTime){
                    System.out.println(tT.toString());
                    
                    AccessLog acl = aclDao.getAccessLog(tT.getTransactionId());
                    System.out.println(acl.getReferer() + ", " + acl.getRequestedResource() + ", " + acl.getClientIP());
                }
                
                
                System.out.println(accessLogs.size() + " .//. " + timestamps.size());
                BigInteger val1=null;
                BigInteger val2=null;
                Integer counter1 = 0;
                Integer counter2 = 0;
                for (AccessLog acl:accessLogs){
                    counter1++;
                    val1 = acl.getRequestDate();
                    Boolean found = false;
                    for(Object[] item:timestamps){
                        counter2++;
                        BigInteger firstValue = (BigInteger) item[0];
                        val2 = firstValue;
                        
                        
                        if (val1.equals(val2)){
                            found = true;
                            if(counter1>=counter2){
                                System.out.println("At the same place");
                            }
                            break;
                        }

                    }
                    
                    counter2=0;
                    System.out.println(found);

                }counter1=0;
                
            }
        }
            
            /*
            performance.Tock("retrieving AccessLog timestamps for a specific userId by first finding the clientIds from the AuditLog and then the"
                    + " transactionIds performed by those clientIds from NavajoLog");
            System.out.println(timestamps.size());
            
            Integer interval = -1;
            Long previousTimestamp = null; //not set yet
            Long timestamp;
            
            Integer count = 0;
            for (BigInteger BItimestamp:timestamps){
                timestamp = BItimestamp.longValueExact();
                if (previousTimestamp != null){
                    interval = (int) (timestamp - previousTimestamp);    
                }
                
                count = intervals.get(interval);
                if (count==null) {
                    intervals.put(interval, 0);
                }else{
                    intervals.put(interval, intervals.get(interval)+1);
                }
                
                previousTimestamp = timestamp;
            }
           

        }
        */
        
        System.out.println(intervals.size());
        
        for (Integer key: intervals.keySet()){
            System.out.println(intervals.get(key));
            
        }
        
        
        
        
    }
}
