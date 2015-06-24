package gq.panop.hibernate.dao;

import gq.panop.hibernate.mytypes.TransactionId_Timestamp;
import gq.panop.util.HibernateUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

public class NavajoLogDao {
    


    public List<String> getTransactionIds(String clientId){
        StatelessSession session = HibernateUtil.getSessionFactory().openStatelessSession();
        Transaction tx = null;
        
        List<String> transactionIds = new ArrayList<String>();
        
        try{
            tx = session.beginTransaction();
            String queryString = "Select njl.transactionId FROM NavajoLog njl where njl.clientId = :clientId";
            Query query = session.createQuery(queryString);
            query.setString("clientId", clientId);
            
            transactionIds = query.list();

        }catch (RuntimeException e){
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }finally {
            //session.flush();
            session.close();
        }
        return transactionIds;
    }
    
    public List<TransactionId_Timestamp> getTransactionIdsANDTimestamps(String clientId){
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        
        List<Object[]> wrapper = new ArrayList<Object[]>();
        
        try{
            tx = session.beginTransaction();
            String queryString = "Select njl.transactionId, njl.timestamp FROM NavajoLog njl where njl.clientId = :clientId GROUP BY njl.transactionId ORDER BY njl.timestamp";
            Query query = session.createQuery(queryString);
            query.setString("clientId", clientId);
            
            wrapper = query.list();
        }catch (RuntimeException e){
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        }finally {
            session.flush();
            session.close();
        }
        
        List<TransactionId_Timestamp> tT = new ArrayList<TransactionId_Timestamp>();
        
        wrapper.forEach(n -> tT.add(new TransactionId_Timestamp(n[0].toString(), (Long)((BigInteger)n[1]).longValue())));
        /*
        for (Object[] tmp:wrapper){
            TransactionId_Timestamp tTobject = new TransactionId_Timestamp(tmp[0].toString(), (Long)((BigInteger)tmp[1]).longValue()); 
            tT.add(tTobject);
        }
        */
        return tT;
    }
    
    
}
