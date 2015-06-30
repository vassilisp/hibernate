package gq.panop.hibernate.mytypes;

import gq.panop.hibernate.model.AccessLog;
import gq.panop.hibernate.model.NavajoLog;

public class AugmentedACL {

    private AccessLog accessLog;
    private NavajoLog navajoLog;
    private Long timestamp;
    private String clientId;
    private String InferedSession;
    
    public AugmentedACL() {
        super();
    }

    public AugmentedACL(AccessLog accessLog, NavajoLog navajoLog,
            Long timestamp, String clientId) {
        super();
        this.accessLog = accessLog;
        this.navajoLog = navajoLog;
        this.timestamp = timestamp;
        this.clientId = clientId;
    }

    public AugmentedACL(AccessLog accessLog, Long timestamp, String clientId) {
        super();
        this.accessLog = accessLog;
        this.timestamp = timestamp;
        this.clientId = clientId;
    }

    public String getInferedSession() {
        return InferedSession;
    }

    public void setInferedSession(String inferedSession) {
        InferedSession = inferedSession;
    }

    public AccessLog getAccessLog() {
        return accessLog;
    }

    public void setAccessLog(AccessLog accessLog) {
        this.accessLog = accessLog;
    }

    public NavajoLog getNavajoLog() {
        return navajoLog;
    }

    public void setNavajoLog(NavajoLog navajoLog) {
        this.navajoLog = navajoLog;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    
}
