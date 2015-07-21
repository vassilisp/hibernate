package gq.panop.hibernate.model;

public class MLentry {

    private String processID;
    private String userId;
    private String clientId;
    private String subSession;
    private String transitions;
    private String targets;
    
    private Integer total;

    public MLentry(){}

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSubSession() {
        return subSession;
    }

    public void setSubSession(String subSession) {
        this.subSession = subSession;
    }

    public String getTransitions() {
        return transitions;
    }

    public void setTransitions(String transitions) {
        this.transitions = transitions;
    }

    public String getTargets() {
        return targets;
    }

    public void setTargets(String targets) {
        this.targets = targets;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "MLentry [processID=" + processID + ", userId=" + userId
                + ", clientId=" + clientId + ", subSession=" + subSession
                + ", transitions=" + transitions + ", targets=" + targets
                + ", total=" + total + "]";
    }
   
    
}
