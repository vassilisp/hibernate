package gq.panop.hibernate.mytypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserStatistics implements Serializable {

    private Integer totalLogTransitions = 0;
    private Integer totalRealTransitions = 0;
    private Long firtTimestamp;
    private Long lastTimestamp;
    private String firstDay;
    private String lastDay;
    
    private Boolean first = true;
    
    private Map<String, Integer> dailyStatistic = new HashMap<String, Integer>();
    
    private Map<String, Integer> clientIdStatistics = new HashMap<String, Integer>();
    
    public Integer getTotalLogTransitions() {
        return totalLogTransitions;
    }

    public void setTotalLogTransitions(Integer totalLogTransitions) {
        this.totalLogTransitions = totalLogTransitions;
    }

    public Integer getTotalRealTransitions() {
        return totalRealTransitions;
    }

    public String getFirstDay() {
        return firstDay;
    }

    public String getLastDay() {
        return lastDay;
    }

    public void setTotalRealTransitions(Integer totalRealTransitions) {
        this.totalRealTransitions = totalRealTransitions;
    }

    public Long getFirtTimestamp() {
        return firtTimestamp;
    }

    public Long getLastTimestamp() {
        return lastTimestamp;
    }

    public Map<String, Integer> getDailyStatistic() {
        return dailyStatistic;
    }

    
    public void setClientIdStatistic(String clientId, Integer numOfTrans){
        this.clientIdStatistics.put(clientId, numOfTrans);
    }
    
    
    
    public void addDayStatistic(Long timestamp, Integer numfOfTrans){
        Calendar a = Calendar.getInstance();
        a.setTimeInMillis(timestamp);
        String day = a.get(Calendar.DAY_OF_MONTH) + "." + (a.get(Calendar.MONTH)+1);
        
        //keeping first and last timestamps and days
        if (first){
            firtTimestamp = timestamp;
            firstDay = day;
            first = false;
        }
        lastTimestamp = timestamp;
        lastDay = day;
        
        //Keeping statistics
        if (!this.dailyStatistic.containsKey(day)){
            this.dailyStatistic.put(day, numfOfTrans);
        }else{
            this.dailyStatistic.put(day, this.dailyStatistic.get(day)+numfOfTrans);
        }
    }
    
    public void addTotalRealTransitions(Integer numOfTrans){
        this.totalRealTransitions += numOfTrans;
    }
    
    public void addTotalLogTransitions(Integer numOfTrans){
        this.totalLogTransitions += numOfTrans;
    }
    
    public Integer getNumberOfActiveDays(){
        Integer counter = 0;
        for (String tmp:dailyStatistic.keySet()){
            counter++;
        }
        return counter;
    }
    
    
    public Map<String,Double> getMeanTransitionsPerClientId(){
        Long mean = 0L;
        for (Integer trans: clientIdStatistics.values()){
            mean += trans;
        }
        mean = mean/clientIdStatistics.size();
        
        Double variance = 0D;
        for (Integer trans: clientIdStatistics.values()){
             variance += Math.pow(trans - mean,2);
        }
        
        variance = variance/clientIdStatistics.size();
        Double std = Math.sqrt(variance);
        
        Map<String, Double> result = new HashMap<String, Double>();
        result.put("mean", mean.doubleValue());
        result.put("variance", variance);
        result.put("std", std);
        
        return result;
        
        
    }
}

