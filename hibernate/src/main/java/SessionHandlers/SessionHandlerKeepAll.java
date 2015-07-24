package SessionHandlers;

import gq.panop.hibernate.URLNormalizer;
import gq.panop.hibernate.UniqueIDAssigner;
import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.hibernate.mytypes.Transition;
import gq.panop.util.MiscUtil;

import java.util.ArrayList;
import java.util.List;

public class SessionHandlerKeepAll implements SessionHandler{

    private List<Transition> transitions = new ArrayList<Transition>();
    
    private UniqueIDAssigner uID = new UniqueIDAssigner();

    private String clientId;
    private String userId;
    //=========================================================================
    private Boolean discardParameters = false;
    private Integer tokenizer = 0;
    private Boolean discardImages = false;
    //-------------------------------------------------------------------------
    private Boolean discardCSSICO = false;
    

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
                referer.toLowerCase().endsWith(".gif")) && discardImages));
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
        

        if (tokenizer>0){
            referer = MiscUtil.custom_Parser(referer, tokenizer);
            target = MiscUtil.custom_Parser(referer, tokenizer);
        }
        



        if (discardParameters){
            referer = MiscUtil.discardParamaters(referer);
            target = MiscUtil.discardParamaters(target);
        }



        if (!isSpecialRequest){
            Transition trans = new Transition(referer, target, timestamp);

            trans.setUserId(userId);
            trans.setSessionId(clientId);

            trans.setSubSessionId("subSession:0");

            trans.setTransactionId(session.getAccessLog().getTransactionId());

            trans.setRefererID(uID.pageVectorizer(referer));
            trans.setTargetID(uID.pageVectorizer(target)); 

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
        
        paramString += "Tok" + tokenizer.toString() + "-";
        
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


    public Integer getTokenizer() {
        return tokenizer;
    }


    public void setTokenizer(Integer tokenizer) {
        this.tokenizer = tokenizer;
    }


    public Boolean getDiscardImages() {
        return discardImages;
    }


    public void setDiscardImages(Boolean discardImages) {
        this.discardImages = discardImages;
    }
    
    

}
