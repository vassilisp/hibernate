package gq.panop.hibernate;

import gq.panop.hibernate.dao.AccessLogDao;
import gq.panop.hibernate.dao.AuditLogDao;
import gq.panop.hibernate.model.AccessLog;
import gq.panop.hibernate.model.AuditLog;
import gq.panop.util.HibernateUtil;
import gq.panop.util.PerformanceUtil;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Playground {

    public Playground(){}
    
    public static void Start(){
        
        String userId = "mcico";
        
        PerformanceUtil performance = new PerformanceUtil("ms");
        AuditLogDao dao = new AuditLogDao();
        /*

        for (AuditLog auditLog :  dao.getAllLogs()){
            System.out.println(auditLog.toString());
        }
        */

        
        performance.Tick();
        List<String> userIds = dao.getAllUsers();
        for (String user : userIds){
            System.out.println(user);
        }
        performance.Tock();
        separate();
        
        
        //Create a keyboard scanner and verify userId existance
        Boolean foundFlag = false;
        Scanner keyboard = new Scanner(System.in);
        while(!foundFlag){
            System.out.print("perform database request for userId: ");
            

            //for testing purposes
            // TODO remove this in production
            //userId = "tom";
            // TODO and use this
            userId = keyboard.next();
            
            
            for (String user : userIds){
                if (user.equals(userId)){
                    foundFlag=true;
                    break;
                }
            }
            if (foundFlag==true){
                System.out.println("Performing Search for userId: " + userId);
                break;
            }else{System.out.println("UserId not Found in list of available users");}
        }
        //keyboard.close();
        
        performance.Tick();
        for (AuditLog auditLogs : dao.getAuditLogs(userId)){
            System.out.println(auditLogs.toString());
        }
        performance.Tock("get the AuditLogs with a specific userId");
        separate();
        
        performance.Tick();
        for (AuditLog auditLogs : dao.getAuditLogs(userId)){
            System.out.println(auditLogs.getTransactionId());
        }
        performance.Tock("slow AuditLog retrieval by userId took");
        separate();

        
        performance.Tick();
        List<String> transactionIds = dao.getTransactionIds(userId);
        for (String transactionId : transactionIds){
            System.out.println(transactionId);
        }
        performance.Tock("fast AuditLog retrieval by userId took");
        separate();

  
        
        
        AccessLogDao accessLogDao = new AccessLogDao();
        
        //Retrieve a transactionId by providing a transactionId
        performance.Tick();
        String transactionId = "c0a80dcd-0dfd-aaccc-146b854ed38-00007c51";
        AccessLog accessLog = accessLogDao.getAccessLog(transactionId);
        if (accessLog != null){
            System.out.println(accessLog.toString());
        }
        performance.Tock("Retrieve a transactionId by providing a transactionId");
        separate();
        
        //Retrieving all accessLogs by providing a list of transactionIds
        performance.Tick();
        List<AccessLog> accessLogs = accessLogDao.getAccessLogs(transactionIds);
        if(accessLogs.isEmpty()) System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@-- result is empty");
        for (AccessLog accLog : accessLogs){
            System.out.println(accLog.toString());
        }
        performance.Tock("Retrieving all accessLogs by providing a list of transactionIds");
        separate();
        
        //Retrieving all accessLogs by providing a userId (all done by MySQL)
        performance.Tick();
        List<AccessLog> accessLogsbyUser = accessLogDao.getAccessLogs_fromAuditLog(userId);
        for (AccessLog accLog: accessLogsbyUser){

            System.out.println(accLog.toString());
            //Date realDate = new Date(accLog.getRequestDate().longValue());
            //System.out.println("The date is : " + realDate);
        }
        performance.Tock("Retrieving all accessLogs by providing a userId (all done by MySQL)");

        System.out.println("half half length result size : " + accessLogs.size() + "  , full length result size :  " + accessLogs.size());
        
        
        //create comparator required to sort the result lists based on their timestamps
        Comparator<AccessLog> accessLogCompare = new Comparator<AccessLog>(){
            @Override
            public int compare(AccessLog aL1, AccessLog aL2){
                return (aL1.getRequestDate().longValue() > aL2.getRequestDate().longValue())? 1 : -1 ;
            }   
        };
        
        performance.Tick();
        Collections.sort(accessLogsbyUser, accessLogCompare);
        Collections.sort(accessLogs, accessLogCompare);
        performance.Tock("sorting of result lists");

        /*
        for(int i=0; i< accessLogs.size(); i++) {
            System.out.println(accessLogs.get(i));
            System.out.println(accessLogsbyUser.get(i));
            System.out.println(accessLogsbyUser.get(i).equals(accessLogs.get(i)));
        }
        */
        
        /*
        Date realDate = new Date();     
        for (int i=0;i<10;i++){
            long time = System.currentTimeMillis();
            System.out.println(time);

            realDate.setTime(time);
            System.out.println("The date is : " + realDate + "  |  " + realDate.getHours()+ "  |  " +realDate.getMinutes() + "  |  " + realDate.getSeconds());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        */

        
        performance.Tick();
        List<String> clientIds = dao.getClientIds(userId);
        performance.Tock("Retrive List of clientIds from the AuditLog providing a userId");
        int i=0;
        for (String cids : clientIds){
            System.out.println(i + ")\t" + cids);
            i++;
        }
        System.out.println("Results: " + clientIds.size());
        separate();
        
        
        /* Performing a search by providing the index of the clientId given above
        String clientId = "";
        //Create a keyboard scanner and verify clientId existance
        //Scanner keyboard = new Scanner(System.in);
        Integer index=-1;
        while(true){
          System.out.print("perform search request for clientId index: ");
          try{
              String in = keyboard.next();
              index = Integer.parseInt(in);
          }catch(Throwable e){
              System.out.println(e);
          }
          if ((index>=0) && (index<i)){
              foundFlag=true;
              try{
                  clientId = clientIds.get(index);
              }catch(Throwable e){
                  System.out.println(e);
              }
              System.out.println("Performing Search for clientId: " + index + " )\t" + clientId);
              break;
            }else{
                System.out.println("Index not available in range of available indexes");
            }
        }
        
        
        performance.Tick();
        List<AccessLog> acl = accessLogDao.getAccessLogs_fromNavajoLog(clientId);
        performance.Tock("retrieving AccessLogs for a specific clientId by first finding the transactionIds performed by this"
                + " clientId from NavajoLog");
        for (AccessLog acl_iter: acl){
            System.out.println(acl_iter.getRequestDate().toString() + " - " + toDate(acl_iter.getRequestDate().longValue()) + " /// " 
                    + acl_iter.toString());
        }
        separate();
        */
        
        
        List<AccessLog> aa1 = accessLogDao.getAccessLogs_fromNavajoLog_fromAuditLog(userId);
        List<BigInteger> aa2 = accessLogDao.getOrderedUserTimestamps(userId);
        System.out.println("query 1 size: " +  aa1.size() + " ... query 2 size: " + aa2.size());
        
        
        
        
        performance.Tick();
        List<AccessLog> acl2 = accessLogDao.getAccessLogs_fromNavajoLog_fromAuditLog(userId);

        performance.Tock("retrieving AccessLogs for a specific userId by first finding the clientIds from the AuditLog and then the"
                + " transactionIds performed by those clientIds from NavajoLog");System.out.println(acl2.size());
                
        //ask about displaying the results, in group (x) or all at once (0)
        Integer groupSize = -1;
        System.out.println("Show results? (y/n)");
        String in = keyboard.next();
        if (in.equals("y")){
            System.out.println("define group size (0 displays all results at once)");
            in = keyboard.next();
            groupSize = Integer.parseInt(in);
        }

        if (groupSize==0) groupSize=acl2.size();
        Integer endValue = acl2.size()/groupSize;
        AccessLog currentAcL;
        for(int i1=0; i1<endValue; i1++){
            System.out.println("Showing AccessLog for userId: " + userId + " , records " + (i1*groupSize+1) 
                    + " to " + ((i1*groupSize)+groupSize) + "out of " + acl2.size() + " / type 'e' to exit");
            for(int j=0; j<groupSize; j++){
                try{
                    currentAcL = acl2.get((i1*groupSize)+j);
                    System.out.println(currentAcL.getRequestDate().toString() + " - " + toDate(currentAcL.getRequestDate().longValue()) 
                            + " /// " + currentAcL.toString());
                    
                    //performs a separate query to fetch the acurate timestamp from the NavajoLog
                    System.out.println(currentAcL.getNavajoLog().getTimestamp());
                }catch( Throwable e){
                    System.out.println(e);
                    break;   
                }
            }
            in = keyboard.nextLine();
            if (in.equalsIgnoreCase("e")) break;
        }
        
        for (AccessLog acl_iter: acl2){
            //System.out.println(acl_iter.getRequestDate().toString() + " - " + toDate(acl_iter.getRequestDate().longValue()) + " /// " + acl_iter.toString());
        }
        
        
        //TODO uncoment that .. keyboard.close();
       
        
        
    }
    
    private static String toDate(long timestamp){
        Date realDate = new Date();
        realDate.setTime(timestamp);
        return realDate.toString();
    }

    private static void separate(){
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("");
    }
}
