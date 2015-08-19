package SessionHandlers;

import gq.panop.hibernate.URLNormalizer;
import gq.panop.hibernate.UniqueIDAssigner;
import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.hibernate.mytypes.Transition;
import gq.panop.util.MiscUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionHandlerKeepAll implements SessionHandler{

    private List<Transition> transitions = new ArrayList<Transition>();
    
    private UniqueIDAssigner uID = new UniqueIDAssigner();

    private String clientId;
    private String userId;
    //=========================================================================
    private Boolean discardParameters = false;
    private Integer afterTokenizer = 0;
    private Boolean discardImages = false;
    //-------------------------------------------------------------------------
    private Boolean discardCSSICO = false;

    private Pattern pattern;

    @Override
    public void newUser(String userId, String clientId) {
        transitions.clear();
        this.clientId = clientId;
        this.userId = userId;
        
    }


    @Override
    public void nextSession(AugmentedACL session) {

        // Just map AugmentedACL to transition and add it to the transition list
        
        
        String referer = session.getAccessLog().getReferer();
        String target = session.getAccessLog().getRequestedResource();
        //Define what parts to keep from the target (all, skip parameters, ..)
        
        referer = MiscUtil.URLRefererCleaner(referer);
        target = MiscUtil.URLTargetCleaner(target);
        
        Long timestamp = session.getTimestamp();                
               
        Boolean isImage = (((target.toLowerCase().endsWith(".png")||referer.toLowerCase().endsWith(".png") ||referer.toLowerCase().endsWith(".jpg") ||
                target.toLowerCase().endsWith(".jpg") ||referer.toLowerCase().endsWith(".gif") || target.toLowerCase().endsWith(".gif")) && discardImages));
        Boolean isCSS = (target.contains("css")) || (referer.contains("css"));
        isCSS = isCSS && discardCSSICO;
        Boolean isICO = (target.contains(".ico") || referer.contains(".ico")); 
        isICO = isICO && discardCSSICO;
        Boolean isSpecialRequest = false;
        if (isImage || isCSS || isICO) {
            isSpecialRequest = true;
        }else{
            isSpecialRequest = false;
        }
        

 
        if (discardParameters){
            referer = MiscUtil.discardParamaters(referer);
            target = MiscUtil.discardParamaters(target);
        }



        if (!isSpecialRequest){
            
            
            Transition trans = new Transition(referer, target, timestamp);
            
            /*
            if (afterTokenizer>0){
                referer = tokenizing(referer);
                target = tokenizing(target);
            }*/
            

            
            
            String uUser = uID.userVectorizer(userId);
            trans.setUserId(uUser);
            trans.setSessionId(clientId);

            trans.setSubSessionId("subSession:0");

            trans.setTransactionId(session.getAccessLog().getTransactionId());

            //----------------------------------------------------------------
            trans.setRefererID(uID.pageVectorizer(referer));
            trans.setTargetID(uID.pageVectorizer(target));
            //----------------------------------------------------------------
            
            String refererID4 = MiscUtil.custom_Parser(referer, 4);
            String targetID4 = MiscUtil.custom_Parser(target, 4);
            
            String refererID3 = MiscUtil.custom_Parser(referer, 3);
            String targetID3 = MiscUtil.custom_Parser(target, 3);
            
            String refererID2 = MiscUtil.custom_Parser(referer, 2);
            String targetID2 = MiscUtil.custom_Parser(target, 2);
            
            String refererID1 = MiscUtil.custom_Parser(referer, 1);
            String targetID1 = MiscUtil.custom_Parser(target, 1);
            
            trans.setRefererID1(uID.pageVectorizer(refererID1));
            trans.setRefererID2(uID.pageVectorizer(refererID2));
            trans.setRefererID3(uID.pageVectorizer(refererID3));
            trans.setRefererID4(uID.pageVectorizer(refererID4));
            
            trans.setTargetID1(uID.pageVectorizer(targetID1));
            trans.setTargetID2(uID.pageVectorizer(targetID2));
            trans.setTargetID3(uID.pageVectorizer(targetID3));
            trans.setTargetID4(uID.pageVectorizer(targetID4));
            
            //------------------------------------------------------------------

     
            transitions.add(trans);
        }
    }

    
    public String getParameterString(){
        String paramString = "";
        if (discardParameters){
            paramString += "DParam-";
        }else{
            paramString += "KParam-";
        }
        
        if (discardImages){
            paramString += "DImg-";
        }else{
            paramString += "KImg-";
        }
        
        paramString += "Tok" + afterTokenizer.toString() + "-";
        
        if (discardCSSICO){
            paramString += "DCss-";
        }else{
            paramString += "KCss-";
        }
        
        return paramString;
    }
    
    public Boolean getDiscardCSSICO() {
        return discardCSSICO;
    }

    public String getName(){
        return "SessionHandlerKeepAll";
    }
    public void setDiscardCSSICO(Boolean discardCSSICO) {
        this.discardCSSICO = discardCSSICO;
    }


    @Override
    public List<Transition> getSessions() {
        return transitions;
    }

    @Override
    public UniqueIDAssigner getUniqueIDAssigner() {
        return uID;
    }


    public Boolean getDiscardParameters() {
        return discardParameters;
    }


    public void setDiscardParameters(Boolean discardParameters) {
        this.discardParameters = discardParameters;
    }


    public Integer getAfterTokenizer() {
        return afterTokenizer;
    }


    public void setAfterTokenizer(Integer tokenizer) {
        this.afterTokenizer = tokenizer;
        
        String regex = "\\/+(\\w)+\\/";
        for (int i=1; i<tokenizer; i++){
            regex += "+(\\w)+\\/";
        }

        pattern = Pattern.compile(regex);
        
    }
    
    public String tokenizing(String URL){
        Matcher matcher = pattern.matcher(URL);
        
        matcher.find();
        String result = "";
        try{
            result = matcher.group();
        }catch(Throwable e){
            result = URL;
        }
        return result;
    }


    public Boolean getDiscardImages() {
        return discardImages;
    }


    public void setDiscardImages(Boolean discardImages) {
        this.discardImages = discardImages;
    }
    
    

}
