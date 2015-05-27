package gq.panop.hybernate.model;

import java.math.BigInteger;
import java.util.Date;

public class AuditLog {
	//private int id;
	private BigInteger timestamp;
	private String userId;
	private String transactionId;
	private String clientId;
	
	public AuditLog(){}

	public BigInteger getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(BigInteger timestamp) {
		this.timestamp = timestamp;
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
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	@Override
	public String toString(){
		return "[ timestamp: " + timestamp + ", userId:" + userId + ", transactionId: " + transactionId 
				+ ",  clientId: " + clientId + " ]";
	}
	
	
	
}
