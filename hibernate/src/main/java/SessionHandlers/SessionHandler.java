package SessionHandlers;

import java.util.List;

import gq.panop.hibernate.UniqueIDAssigner;
import gq.panop.hibernate.mytypes.AugmentedACL;
import gq.panop.hibernate.mytypes.Transition;

public interface SessionHandler {

    public void newUser(String userId, String clientId);
    
    public void nextSession(AugmentedACL session);
    
    public List<Transition> getSessions();
    
    public UniqueIDAssigner getUniqueIDAssigner();
}
