package gq.panop.hibernate.dao;

import gq.panop.hibernate.model.AccessLog;
import gq.panop.hibernate.model.Preprocess;
import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.hibernate.mytypes.ResultingSetfromComplex;
import gq.panop.hibernate.mytypes.TransactionId_Timestamp;
import gq.panop.hibernate.mytypes.Transition;
import gq.panop.util.HibernateUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

public class PreprocessDao {

    public List<Preprocess> getPreprocess(String processID){


        StatelessSession session = HibernateUtil.getSessionFactory().openStatelessSession();

        String queryString = "FROM Preprocess prepro WHERE prepro.processID =:processID";

        Query query = session.createQuery(queryString);
        query.setString("processID", processID);

        List<Preprocess> preprocess = HibernateUtil.performSimpleStatelessQuery(session, query);
        return preprocess;
    }

	public Boolean saveTransactions(List<Transition> transitions, String processId){
	    
	    List<Preprocess> preprocessList = new ArrayList<Preprocess>();
	    
	    preprocessList = trans2preprocessListMapping(transitions, processId);
	    
	    if (preprocessList.size() != transitions.size()){
            System.err.println("DIFFERENT LIST SIZES - CRITICAL");
        }
	    
	    Session session = HibernateUtil.getSessionFactory().openSession();
	    
	    Transaction tx = session.beginTransaction();
	    
	    Boolean executed = false;
	    int counter = 0;
	    while(executed == false){
	        try{
	            for (Preprocess preprocess:preprocessList){
	                session.save(preprocess);
	            }

	            session.flush();
	            tx.commit();
	            executed = true;
	        }catch(Throwable e){
	            if (tx != null){
	                tx.rollback();
	            }
	            e.printStackTrace();
	            System.err.println("Trying again " + String.valueOf(counter));
	            
	            try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {e1.printStackTrace();}
	            
	        }
	        //After 20 tries quit and exit program 
	        if (counter++ == 20){
	            System.err.println("ERROR WHILE ATTEMPTING TO WRITE TO THE DB AFTER 20 TIMES");
	            System.exit(99);
	        }
	    }
	    session.close();
	    return true;
	}
	
	public List<Preprocess> trans2preprocessListMapping(List<Transition> transitions, String processId){
	    List<Preprocess> preprocessList = new ArrayList<Preprocess>();
	    
	    for(Transition trans:transitions){
	        Preprocess preprocess = new Preprocess();
	        
	        preprocess.setProcessId(processId);
	        
	        preprocess.setUserId(trans.getUserId());
	        preprocess.setClientId(trans.getSessionId());
	        preprocess.setReferer(trans.getReferer());
	        preprocess.setRequestedResource(trans.getTarget());
	        preprocess.setTimestamp(trans.getTimestamp());
	        preprocess.setTransactionId(trans.getTransactionId());
	        preprocess.setSubSession(trans.getSubSessionId());
	        preprocess.setClientIP("EMPTY");
	        preprocess.setStatusCode(0);
	        preprocess.setUserAgent("EMPTY");
	        
	        preprocess.setRefererID(trans.getRefererID());
	        preprocess.setTargetID(trans.getTargetID());
	        
	        preprocessList.add(preprocess);
	    }
	    if (preprocessList.size() != transitions.size()){
	        System.err.println("DIFFERENT LIST SIZES - CRITICAL");
	    }
	    
	    return preprocessList;
	}
       
}


