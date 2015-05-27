package gq.panop.util;

import org.hibernate.SessionFactory;
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
}
