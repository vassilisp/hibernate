package gq.panop.hibernate.model;

import java.math.BigInteger;

public class NavajoLog {
    private BigInteger timestamp;
    private String clientIp;
    private String transactionId;
    private String clientId;
    private String eventType;
    private String decryptedFrontendRequest;
    private String frontendRequest;
    
    private AccessLog accessLog;
    
    public NavajoLog(){}

    /**
     * @return the timestamp
     */
    public BigInteger getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the clientIp
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * @param clientIp the clientIp to set
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    /**
     * @return the transactionId
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * @param transactionId the transactionId to set
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @return the eventType
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * @param eventType the eventType to set
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * @return the decryptedFrontendRequest
     */
    public String getDecryptedFrontendRequest() {
        return decryptedFrontendRequest;
    }

    /**
     * @param decryptedFrontendRequest the decryptedFrontendRequest to set
     */
    public void setDecryptedFrontendRequest(String decryptedFrontendRequest) {
        this.decryptedFrontendRequest = decryptedFrontendRequest;
    }

    /**
     * @return the frontendRequest
     */
    public String getFrontendRequest() {
        return frontendRequest;
    }

    /**
     * @param frontendRequest the frontendRequest to set
     */
    public void setFrontendRequest(String frontendRequest) {
        this.frontendRequest = frontendRequest;
    }

    /**
     * @return the accessLog
     */
    public AccessLog getAccessLog() {
        return accessLog;
    }

    /**
     * @param accessLog the accessLog to set
     */
    public void setAccessLog(AccessLog accessLog) {
        this.accessLog = accessLog;
    }
    
    

}
