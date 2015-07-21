package gq.panop.hibernate.dao;


import gq.panop.hibernate.model.MLentry;

import gq.panop.util.HibernateUtil;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class MLtableDao {

	public Boolean saveML(List<MLentry> mlentries){
	    
	    Session session = HibernateUtil.getSessionFactory().openSession();
	    
	    Transaction tx = session.beginTransaction();
	    
	    Boolean executed = false;
	    int counter = 0;
	    while(executed == false){
	        try{
	            for (MLentry mlentry:mlentries){
	                session.save(mlentry);
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
	            System.err.println("ERROR WHILE ATTEMPTING TO WRITE TO THE DB (MLtable) AFTER 20 TIMES");
	            System.exit(99);
	        }
	    }
	    session.close();
	    return true;
	}       
}


