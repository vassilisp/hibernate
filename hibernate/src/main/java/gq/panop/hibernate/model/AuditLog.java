package gq.panop.hibernate.model;

import java.math.BigInteger;
import java.util.List;

public class AuditLog {
	//private int id;
	private BigInteger timestamp;
	private String userId;
	private String transactionId;
	private String clientId;
	
	private AccessLog accessLog;
	private List<NavajoLog> navajoLog;

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
	
	/**
     * @return the navajoLog
     */
    public List<NavajoLog> getNavajoLog() {
        return navajoLog;
    }

    /**
     * @param navajoLog the navajoLog to set
     */
    public void setNavajoLog(List<NavajoLog> navajoLog) {
        this.navajoLog = navajoLog;
    }

    @Override
	public String toString(){
		return "[ timestamp: " + timestamp + ", userId:" + userId + ", transactionId: " + transactionId 
				+ ",  clientId: " + clientId + " ]";
	}
	
	
	
}
