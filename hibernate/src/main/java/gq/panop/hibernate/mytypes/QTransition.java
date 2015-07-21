package gq.panop.hibernate.mytypes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class QTransition {
    private String referer;
    private String target;
    private Integer time;
    
    
    
    
    public QTransition(String referer, String target) {
        super();
        this.referer = referer;
        this.target = target;
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
    public Integer getTime() {
        return time;
    }
    public void setTime(Integer time) {
        this.time = time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((referer == null) ? 0 : referer.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
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
        QTransition other = (QTransition) obj;
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
        return true;
    }
    
  
    

    


}
