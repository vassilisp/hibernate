package gq.panop.hibernate.dao;

import gq.panop.hibernate.model.AuditLog;
import gq.panop.util.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

public class AuditLogDao {

	public List<AuditLog> getAllLogs(){
		
		Session session = HibernateUtil.getSessionFactory().openSession();
    	Transaction tx = null;
    	
    	List<AuditLog> result = new ArrayList<AuditLog>();
    	try{
    		tx = session.beginTransaction();
    		//do something
    		
    		String queryString = "from AuditLog";
    		result = session.createQuery(queryString).list();
    		
    		for (AuditLog auditLog : (List<AuditLog>) result) {
    			System.out.println("AuditLog (" + auditLog.getTimestamp() + " ) : "
    					+ auditLog.getUserId());
    		}
    		tx.commit();
    	}catch (Exception e){
    		if (tx!=null) tx.rollback();
    		e.printStackTrace();
    	}finally {
    		session.flush();
    		session.close();
    	}
    	return (List<AuditLog>) result;
	}
	
	//get all transactionId for a given userId
	public List<AuditLog> getAuditLogs(String userId){
		Session session = HibernateUtil.getSessionFactory().openSession();
    	Transaction tx = null;
    	
    	List<AuditLog> transactionIds = new ArrayList<AuditLog>();
    	
    	try{
    		tx = session.beginTransaction();
    		String queryString = "From AuditLog where userId = :userId";
    		Query query = session.createQuery(queryString);
    		query.setString("userId", userId);
    		
    		transactionIds = query.list();
    	}catch (RuntimeException e){
    		if (tx!=null) tx.rollback();
    		e.printStackTrace();
    	}finally {
    		session.flush();
    		session.close();
    	}
    	return transactionIds;
	}
	
	public List<String> getTransactionIds(String userId){
		Session session = HibernateUtil.getSessionFactory().openSession();
    	Transaction tx = null;
    	
    	List<String> transactionIds = new ArrayList<String>();
    	
    	try{
    		tx = session.beginTransaction();
    		String queryString = "Select al.transactionId From AuditLog as al where al.userId = :userId";
    		Query query = session.createQuery(queryString);
    		query.setString("userId", userId);
    		
    		transactionIds = query.list();
    	}catch (RuntimeException e){
    		if (tx!=null) tx.rollback();
    		e.printStackTrace();
    	}finally {
    		session.flush();
    		session.close();
    	}
    	return transactionIds;
	}
	
	public List<String> getAllUsers(){
		
	    StatelessSession session = HibernateUtil.getSessionFactory().openStatelessSession();
		String queryString = "SELECT DISTINCT al.userId FROM AuditLog AS al";
		Query query =  session.createQuery(queryString);
		return HibernateUtil.performSimpleStatelessQuery(session, query);
	}
	
	public List<String> getClientIds(String userId){
	    
	    StatelessSession session = HibernateUtil.getSessionFactory().openStatelessSession();
	    String queryString = "SELECT DISTINCT al.clientId FROM AuditLog al WHERE al.userId=:userId AND NOT al.clientId='null'";
	    Query query = session.createQuery(queryString);
	    query.setString("userId", userId);
	    return HibernateUtil.performSimpleStatelessQuery(session, query);
	}
}
