package gq.panop.hibernate.mytypes;

public class Transition {
    private String referer;
    private String target;
    private Long timestamp;
    private String sessionId;
    private String userId;
    private String transactionId;
    
    private String subSessionId;

    private String refererID;
    private String targetID;

    
    private String refererID1;
    private String targetID1;
    private String refererID2;
    private String targetID2;
    private String refererID3;
    private String targetID3;
    private String refererID4;
    private String targetID4;
    
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
    
    public String getRefererID1() {
        return refererID1;
    }
    public void setRefererID1(String refererID1) {
        this.refererID1 = refererID1;
    }
    public String getTargetID1() {
        return targetID1;
    }
    public void setTargetID1(String targetID1) {
        this.targetID1 = targetID1;
    }
    public String getRefererID2() {
        return refererID2;
    }
    public void setRefererID2(String refererID2) {
        this.refererID2 = refererID2;
    }
    public String getTargetID2() {
        return targetID2;
    }
    public void setTargetID2(String targetID2) {
        this.targetID2 = targetID2;
    }
    public String getRefererID3() {
        return refererID3;
    }
    public void setRefererID3(String refererID3) {
        this.refererID3 = refererID3;
    }
    public String getTargetID3() {
        return targetID3;
    }
    public void setTargetID3(String targetID3) {
        this.targetID3 = targetID3;
    }
    public String getRefererID4() {
        return refererID4;
    }
    public void setRefererID4(String refererID4) {
        this.refererID4 = refererID4;
    }
    public String getTargetID4() {
        return targetID4;
    }
    public void setTargetID4(String targetID4) {
        this.targetID4 = targetID4;
    }
    public String getRefererID() {
        return refererID;
    }
    public void setRefererID(String transitionID) {
        this.refererID = transitionID;
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
                + ", subSessionId=" + subSessionId + ", refererID="
                + refererID + ", targetID=" + targetID + "]";
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
