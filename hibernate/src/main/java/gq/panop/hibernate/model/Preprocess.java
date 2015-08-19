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
	
	public Preprocess(){}

	
    public String getProcessID() {
        return processID;
    }


    public void setProcessID(String processID) {
        this.processID = processID;
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

    public String getRefererID() {
        return refererID;
    }

    public void setRefererID(String refererID) {
        this.refererID = refererID;
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
                + clientId + ", subSession=" + subSession + ", refererID="
                + refererID + ", targetID=" + targetID + "]";
    }

}
