package gq.panop.hibernate.dao;

import gq.panop.util.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

public class NavajoLogDao {
    

    public List<String> getTransactionIds2(String clientId){
        String queryString = "Select njl.transactionId FROM NavajoLog njl where njl.clientId = :clientId";
        

    }

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
    
    public List<objmy> getTransactionIdsANDTimestamps(String clientId){
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        
        List<objmy> wrapper = new ArrayList<objmy>();
        
        try{
            tx = session.beginTransaction();
            String queryString = "Select njl.transactionId, njl.timestamp FROM NavajoLog njl where njl.clientId = :clientId";
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
        return wrapper;
    }
    
    class objmy{
        private String transactionId=null;
        private String timestamp=null;
        
        
        public objmy(String transactionId, String timestamp){
            this.transactionId = transactionId;
            this.timestamp = timestamp;
        }
        /**
         * @return the transactionId
         */
        public String getTransactionId() {
            return transactionId;
        }

        /**
         * @param transactionId the transactionId to set
         */
        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        /**
         * @return the timestamp
         */
        public String getTimestamp() {
            return timestamp;
        }

        /**
         * @param timestamp the timestamp to set
         */
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
        
        
    }
    
}
