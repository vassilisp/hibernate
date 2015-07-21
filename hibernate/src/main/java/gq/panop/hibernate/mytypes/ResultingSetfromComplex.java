package gq.panop.hibernate.mytypes;

import gq.panop.hibernate.model.AccessLog;

public class ResultingSetfromComplex {

    private String transactionId;
    private Long timestamp;
    private String userId;
    private String referer;
    private String requestedResource;
    private String clientId;
    


    @Override
    public String toString() {
        return "ResultingSetfromComplex [transactionId=" + transactionId
                + ", timestamp=" + timestamp + ", userId=" + userId
                + ", referer=" + referer + ", requestedResource="
                + requestedResource + ", clientId=" + clientId + "]";
    }

    public ResultingSetfromComplex(String transactionId, Long timestamp,
            String userId, String referer, String requestedResource,
            String clientId) {
        super();
        this.transactionId = transactionId;
        this.timestamp = timestamp;
        this.userId = userId;
        this.referer = referer;
        this.requestedResource = requestedResource;
        this.clientId = clientId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRequestedResource() {
        return requestedResource;
    }

    public void setRequestedResource(String requestedResource) {
        this.requestedResource = requestedResource;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    

    
    
}
