package gq.panop.util;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
    
	public static <T> List<T> performSimpleQuery(Query query){
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = null;
		
		List<T> result = null;
		
		try{
			tx = session.beginTransaction();
			result = query.list();
		}catch (Throwable ex){
			if (tx!=null) tx.rollback();
			ex.printStackTrace();
		}finally {
			session.flush();
			session.close();
		}
		
		return result;
		
	}
	public static Object performUniqueQuery(Query query){
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = null;
		
		Object result = null;
		
		try{
			tx = session.beginTransaction();
			result = query.uniqueResult();
		}catch (Throwable ex){
			if (tx!=null) tx.rollback();
			ex.printStackTrace();
		}finally {
			session.flush();
			session.close();
		}
		
		return result;
		
	}
}