package gq.panop.hybernate;

import java.util.ArrayList;
import java.util.List;

import gq.panop.hybernate.dao.AuditLogDao;
import gq.panop.hybernate.model.AuditLog;



/**
 * Hello world!
 *
 */
public class App 
{
	

	
    public static void main( String[] args ){
        long starttime;
        long endtime;

    	AuditLogDao dao = new AuditLogDao();
        /*

    	for (AuditLog auditLog :  dao.getAllLogs()){
    		System.out.println(auditLog.toString());
    	}
    	*/
    	
    	for (AuditLog auditLogs : dao.getAuditLogs("ancsa")){
    		System.out.println(auditLogs.toString());
    	}
    	
    	starttime = System.nanoTime();
    	for (AuditLog auditLogs : dao.getAuditLogs("ancsa")){
    		System.out.println(auditLogs.getTransactionId());
    	}
    	endtime = System.nanoTime();
    	System.out.println("slow took: " + Long.toString(endtime-starttime));
    	
    	starttime = System.nanoTime();
    	List<String> transactionIds = dao.getTransactionIds("ancsa");
    	for (String transactionId : transactionIds){
    		System.out.println(transactionId);
    	}
       	endtime = System.nanoTime();
    	System.out.println("fast took: " + Long.toString(endtime-starttime));
    }
    

    
    
}
