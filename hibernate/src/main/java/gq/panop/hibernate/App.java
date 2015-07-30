package gq.panop.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import SessionHandlers.SessionHandler;
import SessionHandlers.SessionHandlerKeepAll;
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

        
        //Delete table
        //System.out.println(HibernateUtil.hqlDelete("Preprocess"));
        
        
        Integer result = 0;
        //Truncate all entries in a table
        //result = HibernateUtil.hqlTruncate("Preprocess");
        //System.out.println(result);
        

        if (result==-1){
            System.out.println(HibernateUtil.hqlCreatePreProcessTable("Preprocess"));
        }
        
        
        UserStatisticsAnalysis usa = new UserStatisticsAnalysis();
        //usa.analyze("default");
        //usa.returnNUsersAroundValue(20, usa.getMeanTransitions());
        
        //usa.returnNUsersWithAvgPerDayAroundValue(20, usa.getMeanTransitions()/usa.getMeanNofActiveDays());
        
        
        
        UserStatisticsAnalysisBEFORE usaTotalLog = new UserStatisticsAnalysisBEFORE();
        usaTotalLog.analyze("default");
        usaTotalLog.printAllAnalysis();
        //usaTotalLog.returnNUsersAroundValue(20, usaTotalLog.getMeanTransitions());
        //usaTotalLog.returnNUsersWithAvgPerDayAroundValue(20, usaTotalLog.getMeanTransitions()/usaTotalLog.getMeanNofActiveDays());
        
        //usaTotalLog.returnNRandomUsersAroundXtimesSTD(20, 1.0);
        
        List<String>requestedUserIds = new ArrayList<String>();
        requestedUserIds = usaTotalLog.returnNRandomUsersAroundXtimesSTD(50, 0.9);
        requestedUserIds = usaTotalLog.returnNRandomUsersAroundXtimeSTD_clientId(50, 0.9);
        
        SessionTraversal st = new SessionTraversal(null);

        st.setupRequestedUserIds(requestedUserIds);
        
        SessionHandlerKeepAll SHk = new SessionHandlerKeepAll();
        SHk.setDiscardImages(true);
        SHk.setDiscardParameters(true);
        SHk.setDiscardCSSICO(true);
        SHk.setAfterTokenizer(3);
        
        
        st.setupSessionHandler(SHk);
        st.setDryRun(true);
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
        
        
        //String test = "GET /fecru/static/mzfr4p/2static/style/jquery/theme/concat.commonScriptAndStyleIncludes.4a8d31baefacdc3a65a699257c20ccaf.cache.css HTTP/1.1";
        
        //System.out.println(MiscUtil.custom_Parser(test,2));
        //System.out.println(MiscUtil.custom_Parser(test, 3));

    }
    
    
    
}
