package gq.panop.hibernate;

import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.dao.AuditLogDao;

import gq.panop.hibernate.mytypes.AugmentedACL;

import gq.panop.util.PerformanceUtil;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        
        performance.Tick();
        List<String> userIds = adlDao.getAllUsers();
        performance.Tock();

        
        //count every interval << interval, how many times >>
        HashMap<Long,Integer> intervals = new HashMap<Long,Integer>();

        String filePath = "";
        String fileName = "data" + ((int)(System.currentTimeMillis()/10000)) + ".txt";
        //CSVWriter writer = null;
        PrintWriter writer = null;
        try{
            //writer = new CSVWriter(new FileWriter(Path));
            writer = new PrintWriter(filePath + fileName, "UTF-8");
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
                
                List<AugmentedACL> aACL = aclDao.getFullEfficientOrderedSessionTransactions(clientId);
                
                Long interval = -1L;
                Long previousTimestamp = null; //not set yet
                Long timestamp = 0L;

                Integer count = 0;
                for (AugmentedACL aclItem : aACL){
                    
                    try{
                        timestamp = aclItem.getTimestamp();
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
