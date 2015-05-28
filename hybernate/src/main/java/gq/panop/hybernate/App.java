package gq.panop.hybernate;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import gq.panop.hybernate.dao.AccessLogDao;
import gq.panop.hybernate.dao.AuditLogDao;
import gq.panop.hybernate.model.AccessLog;
import gq.panop.hybernate.model.AuditLog;
import gq.panop.util.PerformanceUtil;



/**
 * Hello world!
 *
 */
public class App 
{

    public static void main( String[] args ){
        long starttime;
        long endtime;

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
    	performance.Tock();
    	for (String user : userIds){
    		System.out.println(user);
    	}
    	
    	
    	//Create a keyboard scanner and verify userId existance
    	Boolean foundFlag = false;
    	while(!foundFlag){
    		Scanner keyboard = new Scanner(System.in);
    		System.out.print("perform database request for userId: ");
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
    	
    	for (AuditLog auditLogs : dao.getAuditLogs(userId)){
    		System.out.println(auditLogs.toString());
    	}
    	
    	performance.Tick();
    	for (AuditLog auditLogs : dao.getAuditLogs(userId)){
    		System.out.println(auditLogs.getTransactionId());
    	}
    	performance.Tock("slow AuditLog retrieval by userId took");

    	
    	performance.Tick();
    	List<String> transactionIds = dao.getTransactionIds2(userId);
    	for (String transactionId : transactionIds){
    		System.out.println(transactionId);
    	}
    	performance.Tock("fast AuditLog retrieval by userId took");

  
    	
    	
    	AccessLogDao accessLogDao = new AccessLogDao();
    	
    	//Retrieve a transactionId by providing a transactionId
    	performance.Tick();
    	String transactionId = "c0a80dcd-0dfd-aaccc-146b854ed38-00007c51";
    	AccessLog accessLog = accessLogDao.getAccessLog(transactionId);
    	System.out.println(accessLog.toString());
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
    	
    	//Retrieving all accessLogs by providing a userId (all done by MySQL
    	performance.Tick();
    	List<AccessLog> accessLogsbyUser = accessLogDao.getAccessLogs(userId);
    	for (AccessLog accLog: accessLogsbyUser){

    		System.out.println(accLog.toString());
    	}
    	performance.Tock("Retrieving all accessLogs by providing a userId (all done by MySQL)");

    	System.out.println("half half length result size : " + accessLogs.size() + "  , full length result size :  " + accessLogs.size());
    	
    	
    	Comparator accessLogCompare = new Comparator(){
			@Override
			public int compare(Object arg0, Object arg1) {
				AccessLog aL1 = (AccessLog) arg0;
				AccessLog aL2 = (AccessLog) arg1;

				Long v1 = aL1.getRequestDate().longValue();
				Long v2 = aL2.getRequestDate().longValue();
				return (v1 > v2)? 1 : -1 ;
			}	
    	};
    	
    	Collections.sort(accessLogsbyUser, accessLogCompare);
    	Collections.sort(accessLogs, accessLogCompare);

    	/*

    	for(int i=0; i< accessLogs.size(); i++)	{
    		System.out.println(accessLogs.get(i));
    		System.out.println(accessLogsbyUser.get(i));
    		System.out.println(accessLogsbyUser.get(i).equals(accessLogs.get(i)));
    	}
    	
		*/
    	
    }
    

    private static void separate(){
    	System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    	System.out.println("");
    }
    
}
