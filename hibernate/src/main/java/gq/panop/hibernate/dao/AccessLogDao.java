package gq.panop.hibernate.dao;

import gq.panop.hibernate.model.AccessLog;
import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.hibernate.mytypes.ResultingSetfromComplex;
import gq.panop.hibernate.mytypes.TransactionId_Timestamp;
import gq.panop.util.HibernateUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

public class AccessLogDao {

	public AccessLog getAccessLog(String transactionId){
	    
	    StatelessSession session = HibernateUtil.getSessionFactory().openStatelessSession();
	    
		String queryString = "From AccessLog where transactionId = :transactionId";
    	Query query = session.createQuery(queryString);
    	query.setString("transactionId", transactionId);
    	

		return (AccessLog) HibernateUtil.performUniqueStatelessQuery(session, query);
	}
	
	   public AccessLog getAccessLog2(String transactionId){
	        
           StatelessSession session = HibernateUtil.getSessionFactory().openStatelessSession();
           
	       String queryString = "From AccessLog where transactionId = :transactionId";
	        
	        Query query = session.createQuery(queryString);
	        query.setString("transactionId", transactionId);

	        Transaction tx = null;
	        
	        AccessLog result = null;
	        
	        try{
	            tx = session.beginTransaction();
	            result = (AccessLog) query.uniqueResult();
	        }catch (Throwable ex){
	            if (tx!=null) tx.rollback();
	            ex.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return result;
	    }
	
	public List<AccessLog> getAccessLogs(List<String> transactionIds){

	    Session session = HibernateUtil.getSessionFactory().openSession();
		String queryString = "FROM AccessLog WHERE transactionId IN (:transactionIds)";
		
		Query query = session.createQuery(queryString);
		query.setParameterList("transactionIds", transactionIds);
		
		List<AccessLog> accessLogs = HibernateUtil.performSimpleQuery(session, query);
		return accessLogs;
	}
	
	public List<AccessLog> getAccessLogs_fromAuditLog(String userId){
		
	    
	    StatelessSession session = HibernateUtil.getSessionFactory().openStatelessSession();
		
	    //String queryString = "FROM AccessLog WHERE transactionId IN ( SELECT transactionId FROM AuditLog WHERE userId = :userId )";
		//old optimized//  String queryString = "Select acl FROM AccessLog acl JOIN acl.auditLog WHERE acl.transactionId=acl.auditLog.transactionId ANDacl.auditLog.userId =:userId";
		//String queryString = "SELECT acl FROM AccessLog acl JOIN acl.auditLog WHERE acl.auditLog.userId =:userId";
	    
	    String queryString = "SELECT acl FROM AccessLog acl WHERE acl.auditLog.userId =:userId";

		Query query = session.createQuery(queryString);
		query.setString("userId", userId);
		
		List<AccessLog> accessLogs = HibernateUtil.performSimpleStatelessQuery(session, query);
		return accessLogs;
	}
	
	public List<AccessLog> getAccessLogs_fromNavajoLog(String clientId){
		 
	    //String queryString = "SELECT acl FROM AccessLog acl JOIN acl.navajoLog WHERE acl.navajoLog.clientId = :clientId";
	    //TODO return also the acl.navajoLog.timestamp because now it is fetched with an extra query in the backgroun 
	    String queryString = "Select acl FROM AccessLog acl WHERE acl.navajoLog.clientId = :clientId ORDER BY acl.navajoLog.timestamp";
	    Session session = HibernateUtil.getSessionFactory().openSession();
	    Query query = session.createQuery(queryString);
		query.setString("clientId", clientId);
		
		List<AccessLog> accessLogs = HibernateUtil.performSimpleQuery(session, query);
	        
		return accessLogs;
	}
	

	public List<AccessLog> getAccessLogs_fromNavajoLog_fromAuditLog(String userId){
	    
	    StatelessSession session = HibernateUtil.getSessionFactory().openStatelessSession();
	    
	    //NOT WORKING queryString VV
	    //String queryString = "SELECT acl FROM AccessLog acl JOIN acl.navajoLog njl JOIN njl.auditLog adl WHERE acl.auditLog.userId = :userId";

	    String queryString = "SELECT acl FROM AccessLog acl WHERE acl.navajoLog.auditLog.userId = :userId "
	            + "AND NOT acl.navajoLog.auditLog.clientId='null' group by acl.transactionId ORDER BY acl.navajoLog.timestamp";
	    //String queryString = "SELECT distinct acl, njl FROM AccessLog acl, NavajoLog njl WHERE acl.transactionId=njl.transactionId AND njl.auditLog.userId = :userId AND NOT njl.auditLog.clientId='null'";
	    Query query = HibernateUtil.getSessionFactory().openSession().createQuery(queryString);
	    query.setString("userId", userId);
	    
	    List<AccessLog> accessLogs = HibernateUtil.performSimpleStatelessQuery(session, query);
	    return accessLogs;
	}
	
	/*
	public Object<AccessLog,NavajoLog> getAccessLogs_AND_NavajoLogs_fromNavajoLog_fromAuditLog(String userId){
	    //NOT WORKING queryString VV
	    //String queryString = "SELECT acl FROM AccessLog acl JOIN acl.navajoLog njl JOIN njl.auditLog adl WHERE acl.auditLog.userId = :userId";

	    String queryString = "SELECT distinct acl FROM AccessLog acl WHERE acl.navajoLog.auditLog.userId = :userId AND NOT acl.navajoLog.auditLog.clientId='null'";
	    //String queryString = "SELECT distinct acl, njl. FROM AccessLog acl, NavajoLog njl WHERE njl.auditLog.userId = :userId AND NOT njl.auditLog.clientId='null'";
	    Query query = HibernateUtil.getSessionFactory().openSession().createQuery(queryString);
	    query.setString("userId", userId);

	    List<AccessLog> accessLogs = HibernateUtil.performSimpleQuery(query);
	    return accessLogs;
	}
	
	   class myType{
	       private timestamp;
	       
	   }
	  
	  */
	
	
	   public List<BigInteger> getOrderedUserTimestamps(String userId){
	       //Dont like this one-- Avoid it
	       
	       StatelessSession session = HibernateUtil.getSessionFactory().openStatelessSession();
	       
	        String queryString = "SELECT distinct acl.requestDate, acl.transactionId FROM AccessLog acl "
	                + "WHERE acl.navajoLog.auditLog.userId = :userId AND NOT acl.navajoLog.auditLog.clientId='null' group by acl.transactionId order by acl.requestDate";
	        //String queryString = "SELECT distinct acl, njl FROM AccessLog acl, NavajoLog njl WHERE acl.transactionId=njl.transactionId AND njl.auditLog.userId = :userId AND NOT njl.auditLog.clientId='null'";
	        Query query = session.createQuery(queryString);
	        query.setString("userId", userId);
	        
	        List<BigInteger> accessLogs = HibernateUtil.performSimpleStatelessQuery(session, query);
	        return accessLogs;
	    }

       public List<TransactionId_Timestamp> getOrderedACLClientIdNJLTimestamps(String clientId){

           StatelessSession session = HibernateUtil.getSessionFactory().openStatelessSession();
           
            String queryString = "SELECT acl.transactionId, acl.navajoLog.timestamp FROM AccessLog acl "
                    + "WHERE acl.navajoLog.clientId= :clientId GROUP BY acl.transactionId ORDER BY acl.requestDate";
            Query query = session.createQuery(queryString);
            query.setString("clientId", clientId);
            
            List<Object[]> tmp = HibernateUtil.performSimpleStatelessQuery(session, query);
            
            List<TransactionId_Timestamp> tT = new ArrayList<TransactionId_Timestamp>();
            tmp.forEach(n -> tT.add(new TransactionId_Timestamp(n[0].toString(), (Long)((BigInteger)n[1]).longValue())));
            return tT;
        }
       
       public List<AugmentedACL> getFullEfficientOrderedSessionTransactions(String clientId){
           //TODO be carefull -- NOT WORKING DUE TO MISSING/NULL VALUES in the result set
           StatelessSession session = HibernateUtil.getSessionFactory().openStatelessSession();
           
            //String queryString = "SELECT acl.transactionId, acl.navajoLog.timestamp, acl.referer, acl.requestedResource, acl.navajoLog.clientId "
            //        + "FROM AccessLog acl WHERE acl.navajoLog.clientId= :clientId GROUP BY acl.transactionId ORDER BY acl.requestDate";
           
           String queryString = "SELECT acl, acl.navajoLog.timestamp, acl.navajoLog.clientId "
                           + "FROM AccessLog acl WHERE acl.navajoLog.clientId= :clientId GROUP BY acl.transactionId ORDER BY acl.requestDate";
            Query query = session.createQuery(queryString);
            query.setReadOnly(true);
            query.setString("clientId", clientId);
            
            List<Object[]> tmp = HibernateUtil.performSimpleStatelessQuery(session, query);
            
            
            //List<ResultingSetfromComplex> rSet = new ArrayList<ResultingSetfromComplex>();    
            //tmp.forEach(n -> rSet.add(new ResultingSetfromComplex(n[0].toString(), (Long)((BigInteger)n[1]).longValue(), "NOTGIVEN", n[2].toString(),n[3].toString(),n[4].toString() )));
            
            List<AugmentedACL> aAcl = new ArrayList<AugmentedACL>();
            for(Object[] obj:tmp){
                AugmentedACL aclItem = new AugmentedACL();
                try{
                aclItem.setAccessLog((AccessLog) obj[0]);
                aclItem.setTimestamp(((BigInteger) obj[1]).longValueExact());
                aclItem.setClientId((String) obj[2]);
                }catch(Throwable e){
                    System.err.println(e);
                }
                
                aAcl.add(aclItem);
                
            }
            
            return aAcl;
        }
       
}


