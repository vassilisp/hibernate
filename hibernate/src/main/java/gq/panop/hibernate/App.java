package gq.panop.hibernate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gq.panop.util.HibernateUtil;
import gq.panop.util.MiscUtil;


/**
 * Hello world!
 *
 */
public class App 
{

    public static void main( String[] args ){

        //TransitionGraph tg = new TransitionGraph();
        //tg.tester();
        
        SessionTraversal st = new SessionTraversal();
        st.start();
        
        //Playground.Start();        
        
        //IntervalFreq.generate();
        

        
        //Graphs.Start();
        
        //AnimatingAddNodeDemo.play();
       
        //JungGraph jg = new JungGraph();
        //jg.Start();
        
        //InteractiveGraphView1 igv = new InteractiveGraphView1();
        //igv.start();
        
        HibernateUtil.getSessionFactory().close();
        
        
        String test = "GET /fecru/static/mzfr4p/2static/style/jquery/theme/concat.commonScriptAndStyleIncludes.4a8d31baefacdc3a65a699257c20ccaf.cache.css HTTP/1.1";
        
        System.out.println(MiscUtil.custom_Parser(test,2));
        System.out.println(MiscUtil.custom_Parser(test, 3));

    }
    
    
    
}
