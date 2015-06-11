package gq.panop.hibernate;

import gq.panop.util.HibernateUtil;


/**
 * Hello world!
 *
 */
public class App 
{

    public static void main( String[] args ){

        //Playground.Start();
        
        Graphs.Start();
        
        HibernateUtil.getSessionFactory().close();

    }
    
    
    
}
