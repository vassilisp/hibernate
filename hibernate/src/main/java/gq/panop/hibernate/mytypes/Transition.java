package gq.panop.hibernate.mytypes;

public class Transition {
    private String referer;
    private String target;
    private Long timestamp;
    private String sessionId;
    private String userId;
    private String transactionId;
    
    private String subSessionId;

    private String transitionID;
    private String targetID;

    public Transition(String referer, String target, Long timestamp) {
        super();
        this.referer = referer;
        this.target = target;
        this.timestamp = timestamp;
    }
    public Transition(String referer, String target, Long timestamp, String sessionId) {
        super();
        this.referer = referer;
        this.target = target;
        this.timestamp = timestamp;
        this.sessionId = sessionId;
    }
    public Transition(String referer, String target, Long timestamp,
            String sessionId, String userId, String transactionId) {
        super();
        this.referer = referer;
        this.target = target;
        this.timestamp = timestamp;
        this.sessionId = sessionId;
        this.userId = userId;
        this.transactionId = transactionId;
    }

    public String getSubSessionId() {
        return subSessionId;
    }
    public void setSubSessionId(String subSessionId) {
        this.subSessionId = subSessionId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public String getReferer() {
        return referer;
    }
    public void setReferer(String referer) {
        this.referer = referer;
    }
    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }
    public Long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    
    
    public String getTransitionID() {
        return transitionID;
    }
    public void setTransitionID(String transitionID) {
        this.transitionID = transitionID;
    }
    public String getTargetID() {
        return targetID;
    }
    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }
    @Override
    public String toString() {
        return "Transition [referer=" + referer + ", target=" + target
                + ", timestamp=" + timestamp + ", sessionId=" + sessionId
                + ", userId=" + userId + ", transactionId=" + transactionId
                + ", subSessionId=" + subSessionId + ", transitionID="
                + transitionID + ", targetID=" + targetID + "]";
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((referer == null) ? 0 : referer.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Transition other = (Transition) obj;
        if (referer == null) {
            if (other.referer != null)
                return false;
        } else if (!referer.equals(other.referer))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        return true;
    }

    
    


}
