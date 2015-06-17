package gq.panop.util;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscUtil {
    
    public static String custom_Parser(String URI, Integer numberOfTokens){
        
        //For reference only
        /* 
        String[] cont = test.split("/");
        for (String str : cont){
            System.out.println(str);
        }
        */
        
        String regex = "\\/+(\\w)+\\/";
        for (int i=1; i<numberOfTokens; i++){
            regex += "+(\\w)+\\/";
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(URI);
        
        matcher.find();
        return matcher.group();
    }
    
    public static Date toDate(long timestamp){

        Date realDate = new Date();
        realDate.setTime(timestamp);
        return realDate;
    }
}
