package gq.panop.hibernate.dao;

import gq.panop.hibernate.model.AccessLog;
import gq.panop.util.HibernateUtil;

import java.util.List;

import org.hibernate.Query;

public class AccessLogDao {

	public AccessLog getAccessLog(String transactionId){
		AccessLog accessLog = null;
		
		String queryString = "From AccessLog where transactionId = :transactionId";
		
    	Query query = HibernateUtil.getSessionFactory().openSession().createQuery(queryString);
    	query.setString("transactionId", transactionId);
    	
    	accessLog = (AccessLog) HibernateUtil.performUniqueQuery(query);
		return accessLog ;
	}
	
	public List<AccessLog> getAccessLogs(List<String> transactionIds){

		String queryString = "FROM AccessLog WHERE transactionId IN (:transactionIds)";
		
		Query query = HibernateUtil.getSessionFactory().openSession().createQuery(queryString);
		query.setParameterList("transactionIds", transactionIds);
		
		List<AccessLog> accessLogs = HibernateUtil.performSimpleQuery(query);
		return accessLogs;
	}
	
	public List<AccessLog> getAccessLogs_fromAuditLog(String userId){
		
		//String queryString = "FROM AccessLog WHERE transactionId IN ( SELECT transactionId FROM AuditLog WHERE userId = :userId )";
		//old optimized//  String queryString = "Select acl FROM AccessLog acl JOIN acl.auditLog WHERE acl.transactionId=acl.auditLog.transactionId ANDacl.auditLog.userId =:userId";
		//String queryString = "SELECT acl FROM AccessLog acl JOIN acl.auditLog WHERE acl.auditLog.userId =:userId";
       String queryString = "SELECT acl FROM AccessLog acl WHERE acl.auditLog.userId =:userId";

		Query query = HibernateUtil.getSessionFactory().openSession().createQuery(queryString);
		query.setString("userId", userId);
		
		List<AccessLog> accessLogs = HibernateUtil.performSimpleQuery(query);
		return accessLogs;
	}
	
	public List<AccessLog> getAccessLogs_fromNavajoLog(String clientId){
		 
	    //String queryString = "SELECT acl FROM AccessLog acl JOIN acl.navajoLog WHERE acl.navajoLog.clientId = :clientId";
	    String queryString = "FROM AccessLog acl WHERE acl.navajoLog.clientId = :clientId";
		Query query = HibernateUtil.getSessionFactory().openSession().createQuery(queryString);
		query.setString("clientId", clientId);
		
		List<AccessLog> accessLogs = HibernateUtil.performSimpleQuery(query);
	        
		return accessLogs;
	}
	

	public List<AccessLog> getAccessLogs_fromNavajoLog_fromAuditLog(String userId){
	    //NOT WORKING queryString VV
	    //String queryString = "SELECT acl FROM AccessLog acl JOIN acl.navajoLog njl JOIN njl.auditLog adl WHERE acl.auditLog.userId = :userId";
	    String queryString = "SELECT acl FROM AccessLog acl WHERE acl.navajoLog.auditLog.userId = :userId";
	    Query query = HibernateUtil.getSessionFactory().openSession().createQuery(queryString);
	    query.setString("userId", userId);
	    
	    List<AccessLog> accessLogs = HibernateUtil.performSimpleQuery(query);
	    return accessLogs;
	}
	
	
	
}
