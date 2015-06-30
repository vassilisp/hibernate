package gq.panop.hibernate.mytypes;

public class Transition {
    private String referer;
    private String target;
    private Long timestamp;
    private String sessionId;
    private String userId;
    private String transactionId;


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



}
