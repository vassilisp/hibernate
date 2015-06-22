package gq.panop.hibernate;

import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.dao.AuditLogDao;
import gq.panop.hibernate.model.AccessLog;
import gq.panop.util.PerformanceUtil;

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
        AuditLogDao dao = new AuditLogDao();
        
        AccessLogDao accessLogDao = new AccessLogDao();
        performance.Tick();
        List<String> userIds = dao.getAllUsers();
        performance.Tock();
        
        //ArrayList<Integer> intervals = new ArrayList<Integer>();
        
        //count every interval << interval, how many times >>
        HashMap<Integer,Integer> intervals = new HashMap<Integer,Integer>();
        
        for (String user : userIds){
        
            System.out.println(user);
            performance.Tick();
            List<Integer> timestamps = accessLogDao.getOrderedUserTimestamps(user);
            performance.Tock("retrieving AccessLog timestamps for a specific userId by first finding the clientIds from the AuditLog and then the"
                    + " transactionIds performed by those clientIds from NavajoLog");
            System.out.println(timestamps.size());
            
            Integer interval = -1;
            Integer previousTimestamp = -1; //not set yet
            
            Integer count = 0;
            for (Integer timestamp:timestamps){
                if (previousTimestamp != -1){
                    interval = timestamp - previousTimestamp;    
                }
                
                count = intervals.get(interval);
                if (count==0) {
                    intervals.put(interval, 0);
                }else{
                    intervals.put(interval, intervals.get(interval)+1);
                }
                
                previousTimestamp = timestamp;
            }
           

        }
        
        System.out.println(intervals.size());
        
        for (Integer key: intervals.keySet()){
            System.out.println(intervals.get(key));
            
        }
        
        
        
        
    }
}
