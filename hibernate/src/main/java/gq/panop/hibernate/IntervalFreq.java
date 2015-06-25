package gq.panop.hibernate;

import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.dao.AuditLogDao;
import gq.panop.hibernate.dao.NavajoLogDao;
import gq.panop.hibernate.model.AccessLog;
import gq.panop.hibernate.model.NavajoLog;
import gq.panop.hibernate.mytypes.ResultingSetfromComplex;
import gq.panop.hibernate.mytypes.TransactionId_Timestamp;
import gq.panop.util.PerformanceUtil;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class IntervalFreq {

    /*for each user get his transitions ordered by timestamp
     * Then calculate and store the intervals between request
     * This is done in an attempt to figure out two major frequencies.
     * The browser generated and the user generated
     */
    public static void generate(){
        
        
        PerformanceUtil performance = new PerformanceUtil("ms");
        PerformanceUtil userPerformance = new PerformanceUtil("ms");
        AuditLogDao adlDao = new AuditLogDao();
        AccessLogDao aclDao = new AccessLogDao();
        NavajoLogDao njlDao = new NavajoLogDao();
        
        performance.Tick();
        List<String> userIds = adlDao.getAllUsers();
        performance.Tock();

        
        //count every interval << interval, how many times >>
        HashMap<Long,Integer> intervals = new HashMap<Long,Integer>();

        String filePath = "data" + ((int)(System.currentTimeMillis()/10000)) + ".txt";
        //CSVWriter writer = null;
        PrintWriter writer = null;
        try{
            //writer = new CSVWriter(new FileWriter(Path));
            writer = new PrintWriter(filePath, "UTF-8");
        }catch(Throwable e){
            System.err.println("Error creating output file" + System.lineSeparator() + e );
        }
        performance.Tick();
        for (String userId : userIds){
        
            System.out.println(userId);
            userPerformance.Tick();
            
            List<String> clientIds = adlDao.getClientIds(userId);
            
            String tmp_buf = "";
            for(String clientId:clientIds){
                
                //Get AccessLog transactionId entries and NavajoLog timestamps for those entries
                /*
                 *Effectively it is the same as the one below but the one below performs only a simple select
                 *Query while this one joins ACL and NJL
                 *Also entries with missing details from the ACL are not selected with this one
                 */
                //List<TransactionId_Timestamp> timestamps = aclDao.getOrderedACLClientIdNJLTimestamps(clientId);
                
                //Get NavajoLog transactionId and timestamps
                //List<TransactionId_Timestamp> transTime = njlDao.getTransactionIdsANDTimestamps(clientId);
                
                /* Tests for the above
                if (timestamps.size()>0 && transTime.size()>0 && timestamps.size() != transTime.size()){
                    System.err.println("WARNING - Different size of tables returned: IntervalFreq.java");
                }
                
                for(TransactionId_Timestamp tT:transTime){
                    System.out.println(tT.toString());
                    
                    AccessLog acl = aclDao.getAccessLog(tT.getTransactionId());
                    System.out.println(acl.getReferer() + ", " + acl.getRequestedResource() + ", " + acl.getClientIP());
                }
                */
                
                /*More tests - delete later 
                BigInteger val1=null;
                Long val2=null;
                Integer counter1 = 0;
                Integer counter2 = 0;
                for (AccessLog acl:accessLogs){
                    counter1++;
                    val1 = acl.getRequestDate();
                    Boolean found = false;
                    for(TransactionId_Timestamp item:timestamps){
                        counter2++;
                        Long firstValue = item.getTimestamp();
                        val2 = firstValue;
                        
                        
                        if (val2.equals(val1.longValueExact())){
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
                */
                
                //performance.Tick();
                List<AccessLog> accessLogs = aclDao.getAccessLogs_fromNavajoLog(clientId);
                
                Long interval = -1L;
                Long previousTimestamp = null; //not set yet
                Long timestamp = 0L;

                Integer count = 0;
                for (AccessLog acl : accessLogs){
                    
                    try{
                        timestamp = acl.getNavajoLog().getTimestamp().longValue();
                    }catch(Throwable e){
                        System.err.println(e);
                    }
                    if (previousTimestamp != null){
                        interval = timestamp - previousTimestamp;

                        count = intervals.get(interval);
                        if (count==null) {
                            intervals.put(interval, 1);
                        }else{
                            intervals.put(interval, intervals.get(interval)+1);
                        }
                        
                        tmp_buf += interval.toString() + System.lineSeparator();
                    }
                    
                    previousTimestamp = timestamp;
                }


            }
            userPerformance.Tock("Finished user:" + userId);
            writer.print(tmp_buf);
        }
        performance.Tock("Finished all users all files");
        System.out.println(intervals.size());

        for (Long key: intervals.keySet()){
            //System.out.println(key + ": " + intervals.get(key));
        }
        
        
        writer.close();
        System.out.println("Writer closed");
    }
}
