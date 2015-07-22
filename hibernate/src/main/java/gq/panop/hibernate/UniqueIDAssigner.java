package gq.panop.hibernate;

import java.util.HashMap;
import java.util.Map;

import gq.panop.hibernate.mytypes.Transition;

public class UniqueIDAssigner {
    
    private Map<String, String> refererMap = new HashMap<String, String>();
    private Integer uniqueRefererCounter = 0;
    private Map<String, String> targetMap = new HashMap<String, String>();
    private Integer uniqueTargetCounter = 0;
    
    private Map<String, String> pageMap = new HashMap<String, String>();
    private Integer uniquePageCounter = 0;

    public String refererVectorizer(Transition transition){
        String referer = transition.getReferer();
        
        String searchRefererResult = refererMap.get(referer);
        
        String result;
        if (searchRefererResult == null){
            //create new transitionID and put in dictionary
            result = "R" + uniqueRefererCounter++;
            refererMap.put(referer, result);
        }else{
            //retrieve and assign
            result = searchRefererResult;
        }
        
        return result;
        
    }
    
    public String targetVectorizer(Transition transition){
        String target = transition.getTarget();
        
        String searchTargetResult = targetMap.get(target);
        
        String result;
        if (searchTargetResult == null){
            result = "T" + uniqueTargetCounter++;
            targetMap.put(target, result);
        }else{
            result = searchTargetResult;
        }
        
        return result;
    }
    
    public String pageVectorizer(String page){
        
        String searchPageResult = pageMap.get(page);
        
        String result;
        if (searchPageResult == null){
            //create new transitionID and put in dictionary
            result = "P" + uniquePageCounter++;
            pageMap.put(page, result);
        }else{
            //retrieve and assign
            result = searchPageResult;
        }
        
        return result;
    }
    
    public Map<String, String> getRefererMap(){
        return refererMap;
    }
    
    public Map<String, String> getTargetMap(){
        return this.targetMap;
    }
    
    public Map<String, String> getPageMap(){
        return this.pageMap;
    }
    

}
