package gq.panop.hibernate.model;

import java.math.BigInteger;

public class AccessLog{

	private String clientIP;
	private String requestedResource;
	private String serverName;
	private String referer;
	private String userAgent;
	private String transactionId;
	private BigInteger requestDate;
	private Integer statusCode;
	private Integer responseSize;
	private Integer contentLength;
	private Integer processTimeInSec;
	private String remoteLogname;
	private String remoteUser;
	
	private AuditLog auditLog;
	
	public AccessLog(){}

	/**
	 * @return the clientIP
	 */
	public String getClientIP() {
		return clientIP;
	}

	/**
	 * @param clientIP the clientIP to set
	 */
	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

	/**
	 * @return the requestedResource
	 */
	public String getRequestedResource() {
		return requestedResource;
	}

	/**
	 * @param requestedResource the requestedResource to set
	 */
	public void setRequestedResource(String requestedResource) {
		this.requestedResource = requestedResource;
	}

	/**
	 * @return the serverName
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * @param serverName the serverName to set
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * @return the referer
	 */
	public String getReferer() {
		return referer;
	}

	/**
	 * @param referer the referer to set
	 */
	public void setReferer(String referer) {
		this.referer = referer;
	}

	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
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
	 * @return the requestDate
	 */
	public BigInteger getRequestDate() {
		return requestDate;
	}

	/**
	 * @param requestDate the requestDate to set
	 */
	public void setRequestDate(BigInteger requestDate) {
		this.requestDate = requestDate;
	}

	/**
	 * @return the statusCode
	 */
	public Integer getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode the statusCode to set
	 */
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * @return the responseSize
	 */
	public Integer getResponseSize() {
		return responseSize;
	}

	/**
	 * @param responseSize the responseSize to set
	 */
	public void setResponseSize(Integer responseSize) {
		this.responseSize = responseSize;
	}

	/**
	 * @return the contentLength
	 */
	public Integer getContentLength() {
		return contentLength;
	}

	/**
	 * @param contentLength the contentLength to set
	 */
	public void setContentLength(Integer contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * @return the processTimeInSec
	 */
	public Integer getProcessTimeInSec() {
		return processTimeInSec;
	}

	/**
	 * @param processTimeInSec the processTimeInSec to set
	 */
	public void setProcessTimeInSec(Integer processTimeInSec) {
		this.processTimeInSec = processTimeInSec;
	}

	/**
	 * @return the remoteLogname
	 */
	public String getRemoteLogname() {
		return remoteLogname;
	}

	/**
	 * @param remoteLogname the remoteLogname to set
	 */
	public void setRemoteLogname(String remoteLogname) {
		this.remoteLogname = remoteLogname;
	}

	/**
	 * @return the remoteUser
	 */
	public String getRemoteUser() {
		return remoteUser;
	}

	/**
	 * @param remoteUser the remoteUser to set
	 */
	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}
	
	
	
	/**
	 * @return the auditLog
	 */
	public AuditLog getAuditLog() {
		return auditLog;
	}

	/**
	 * @param auditLog the auditLog to set
	 */
	public void setAuditLog(AuditLog auditLog) {
		this.auditLog = auditLog;
	}

	@Override
	public String toString(){
		return "[ clientIP: " + clientIP + ", requestedResource:" + requestedResource + ", serverName: " + serverName 
				+ ",  referer: " + referer + ", userAgent:" + userAgent + ", transactionId:" + transactionId 
				+ ", requestDate:" + requestDate + ", requestDate:" + requestDate + ", statusCode:" + statusCode
				+ ", responseSize:" + responseSize+ ", contentLength:" + contentLength+ ", processTimeInSec:" + processTimeInSec+ ", remoteLogname:" + remoteLogname
				+ ", remoteUser:" + remoteUser+ " ]";
	}
	
	public String toBetterString(){
		return "";
	}	
}
