package gq.panop.util;

import java.util.List;
import java.util.Scanner;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {
	private static SessionFactory sessionFactory = null;
	private static ServiceRegistry serviceRegistry;
	
    public static SessionFactory createSessionFactory(){
    	
    	try{
    		//Create the SessionFactory from hibernate.cfg.xml
    		Configuration configuration = new Configuration();
        	configuration.configure();
        	
        	serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        	sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        	return sessionFactory;	
    	} catch (Throwable ex){
    		//Log the exception because it might be swallowed
    		System.err.println("Initial SessionFactory creation failed." + ex);
    		throw new ExceptionInInitializerError(ex);
    	}    	
    }
    
    public static SessionFactory getSessionFactory(){
    	if (sessionFactory == null) createSessionFactory();
    	return sessionFactory;
    }
    
	public static <T> List<T> performSimpleStatelessQuery(StatelessSession session, Query query){

		Transaction tx = null;
		
		List<T> result = null;
		
		try{
			tx = session.beginTransaction();
			result = query.list();
			tx.commit();
		}catch (Throwable ex){
			if (tx!=null) tx.rollback();
			ex.printStackTrace();
		}finally {
			//session.flush();
			session.close();
		}
		
		return result;
		
	}
	
	   public static <T> List<T> performSimpleQuery(Session session, Query query){

	        Transaction tx = null;
	        
	        List<T> result = null;
	        
	        try{
	            tx = session.beginTransaction();
	            result = query.list();
	            tx.commit();
	        }catch (Throwable ex){
	            if (tx!=null) tx.rollback();
	            ex.printStackTrace();
	        }finally {
	            session.flush();
	            session.close();
	        }
	        
	        return result;
	        
	    }
	public static Object performUniqueStatelessQuery(StatelessSession session, Query query){
		Transaction tx = null;
		
		Object result = null;
		
		try{
			tx = session.beginTransaction();
			result = query.uniqueResult();
		}catch (Throwable ex){
			if (tx!=null) tx.rollback();
			ex.printStackTrace();
		}finally {
			//session.flush();
			session.close();
		}
		
		return result;
		
	}

	public static Object performUniqueStringSearch(String queryString){
	    StatelessSession session = HibernateUtil.getSessionFactory().openStatelessSession();
	    Transaction tx = null;

	    Object result = null;

	    Query query = session.createQuery(queryString);

	    try{
	        tx = session.beginTransaction();
	        result = query.uniqueResult();
	        tx.commit();
	    }catch (Throwable ex){
	        if (tx!=null) tx.rollback();
	        ex.printStackTrace();
	    }finally {
	        //session.flush();
	        session.close();
	    }

	    return result;

	}
	
	
	public static int hqlTruncate(String myTable){
	    String hql = String.format("delete from %s",myTable);
	    Session session = HibernateUtil.getSessionFactory().openSession();
	    Transaction tx = null;
	    Integer result = -1;
	    try{
	        tx = session.beginTransaction();
	        Query query = session.createQuery(hql);
	        result = query.executeUpdate();

	        System.out.println("Delete " + result + " entries from " + myTable + " (Y/N)?");
	        Scanner keyboard = new Scanner(System.in);
	        String response = keyboard.next();
	        if (response.toLowerCase().equals("y")){
	            tx.commit();
	        }else{
	            tx.rollback();
	            result = 0;
	        }
	    }catch(Throwable e){
	        if (tx!=null) tx.rollback();
	        e.printStackTrace();
	    }finally{
	        session.flush();
	        session.close();
	    }
	    return result;
	}
	
	   public static int hqlDelete(String myTable){
	        String hql = String.format("DROP TABLE %s;",myTable);
	        Session session = HibernateUtil.getSessionFactory().openSession();
	        Query query = session.createSQLQuery(hql);
	        Integer result=-1;
	        try{
	            result = query.executeUpdate();
	        }catch(Throwable e){
	            e.printStackTrace();
	        }
	        session.flush();
	        session.close();
	        return result;
	    }
	
	public static int hqlCreatePreProcessTable(String myTable){
	      
	    String hql = String.format("CREATE TABLE IF NOT EXISTS %s ",myTable);
	    hql += "( "
	            + "transactionId VARCHAR(255),"
	            + " userId VARCHAR(255),"
	            + " referer TEXT,"
	            + " requestedResource TEXT,"
	            + " timestamp BIGINT(20),"
	            + " clientId VARCHAR(255),"
	            + " subSession VARCHAR(50),"
	            + " clientIP VARCHAR(255),"
	            + " userAgent TEXT,"
	            + " statusCode int(11),"
	            + " INDEX trans_index (transactionId)"
	            + ");";
	            
	    Session session = HibernateUtil.getSessionFactory().openSession();
	    Query query = session.createSQLQuery(hql);
	    Integer result = -1;
	    result = query.executeUpdate();
	    session.flush();
	    session.close();
	    return result;
	}
}
