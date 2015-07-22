import gq.panop.hibernate.mytypes.ProfileTransition;
import gq.panop.hibernate.mytypes.QTransition;
import gq.panop.hibernate.mytypes.Transition;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class SionDetector {

    private String userId;
    
    private Boolean detection;
    
    private Long windowStartTime;
    private Integer timeCounter = 0;
    
    private Queue<QTransition> trQ = new LinkedList<QTransition>();
    private List<ProfileTransition> tr_shadow = new LinkedList<ProfileTransition>();
    private List<ProfileTransition> tr_shadow_copy = new LinkedList<ProfileTransition>();
    
    
    //=========================================================================
    private Integer blockTimeout = 1000 * 60 * 2; //mil * sec * min 
    //=========================================================================
    
    public SionDetector(String userId){
        this.userId = userId;
        trQ.clear();
        tr_shadow.clear();
        tr_shadow_copy.clear();
    }
    
    public void nextSession(Transition transition){
        //training:
        
        //block until timeout
        if (windowStartTime == 0){
            windowStartTime = transition.getTimestamp();
        }
        
        if (transition.getTimestamp() - windowStartTime < blockTimeout){
            QTransition qTrans = new QTransition(transition.getReferer(), transition.getTarget());
            qTrans.setTime(timeCounter++);
            trQ.add(qTrans);
            
            ProfileTransition pTransition = new ProfileTransition(transition.getReferer(), transition.getTarget());
            if (!tr_shadow.contains(pTransition)){
                pTransition.addProfileTime(timeCounter++);
                tr_shadow.add(pTransition);
            }else{
                tr_shadow.get(tr_shadow.indexOf(pTransition)).addProfileTime(timeCounter++);
            }
            
        }else{
            if (detection){
                tr_shadow_copy.addAll(tr_shadow);
                Float norm = 0F;
                for (QTransition qTrans:trQ){
                    norm += weightTransition(qTrans);
                    
                    //tr_shadow_copy.get(tr_shadow_copy.indexOf(pTrans))
                }
                
                
                
            }
            
        }
        
        
        
    }
    
    private float weightTransition(QTransition qTrans){
        Integer time = qTrans.getTime();
        
        return 0F;
    }
 

}
