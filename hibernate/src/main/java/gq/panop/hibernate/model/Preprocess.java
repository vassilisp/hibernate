package gq.panop.hibernate.model;

import java.math.BigInteger;

public class Preprocess{

    private String processID;
    
	private String clientIP;
	private String requestedResource;
	private String UserId;
	private String referer;
	private String userAgent;
	private String transactionId;
	private Long timestamp;
	private Integer statusCode;
	private String clientId;
	private String subSession;

	private String transitionID;
	private String targetID;
	
	public Preprocess(){}

	
    public String getProcessID() {
        return processID;
    }


    public void setProcessID(String processID) {
        this.processID = processID;
    }


    public String getClientIP() {
        return clientIP;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
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

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
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

    public String getProcessId() {
        return processID;
    }

    public void setProcessId(String processId) {
        this.processID = processId;
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
        return "Preprocess [processID=" + processID + ", clientIP=" + clientIP
                + ", requestedResource=" + requestedResource + ", UserId="
                + UserId + ", referer=" + referer + ", userAgent=" + userAgent
                + ", transactionId=" + transactionId + ", timestamp="
                + timestamp + ", statusCode=" + statusCode + ", clientId="
                + clientId + ", subSession=" + subSession + ", transitionID="
                + transitionID + ", targetID=" + targetID + "]";
    }

}
