package gq.panop.hybernate.dao;

import gq.panop.hybernate.model.AccessLog;
import gq.panop.util.HibernateUtil;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class AccessLogDao {

	public List<AccessLog> getAccessLog(String transactionId){
		List<AccessLog> accessLog = null;
		
		String queryString = "From AccessLog where transactionId = :transactionId";
		
    	Query query = HibernateUtil.getSessionFactory().openSession().createQuery(queryString);
    	query.setString("transactionId", transactionId);
    	
    	accessLog = HibernateUtil.performSimpleQuery(query);
		return accessLog ;
	}
	
	public List<AccessLog> getAccessLogs(List<String> transactionIds){

		
		String queryString = "FROM AccessLog WHERE transactionId IN (:transactionIds)";
		
		Query query = HibernateUtil.getSessionFactory().openSession().createQuery(queryString);
		query.setParameterList("transactionIds", transactionIds);
		
		List<AccessLog> accessLogs = HibernateUtil.performSimpleQuery(query);
		return accessLogs;
	}
	
	public List<AccessLog> getAccessLogs(String userId_fromAuditLog){
		
		String queryString = "FROM AccessLog WHERE transactionId IN ( FROM AuditLog WHERE userId = :userId )";
		Query query = HibernateUtil.getSessionFactory().openSession().createQuery(queryString);
		query.setString("userId", userId_fromAuditLog);
		
		List<AccessLog> accessLogs = HibernateUtil.performSimpleQuery(query);
		return accessLogs;
		
		
		
	}
	
	
	
	
}
