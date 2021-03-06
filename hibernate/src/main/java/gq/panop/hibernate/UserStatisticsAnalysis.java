package gq.panop.hibernate;

import gq.panop.hibernate.mytypes.UserStatistics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class UserStatisticsAnalysis {

    private HashMap<String, UserStatistics> userStatistics = null; 
    
    private Integer meanTransitions;
    private Long variance;
    private Double std;

    private Integer meanNofActiveDays = 0 ;
    private Integer STDofActiveDays;
    
    public Integer getSTDofActiveDays() {
        return STDofActiveDays;
    }

    public void setSTDofActiveDays(Integer sTDofActiveDays) {
        STDofActiveDays = sTDofActiveDays;
    }

    public void analyze(String userStatisticsSelector){
        
        String path = "serializedObjects/";
        String filename = "";
        
        if(userStatisticsSelector.equalsIgnoreCase("default")){
            filename = path + "default.ser";
        }else if (userStatisticsSelector.equalsIgnoreCase("select")){
        
            File folder = new File(path);
            File[] listOfFiles = folder.listFiles();
            System.out.println(listOfFiles.length);

            Integer counter = 0;
            for (File currentFile:listOfFiles){
                System.out.println( counter++ + " ) " + currentFile.getName());
            }

            System.out.println("Choose userStatistics file to open for deserialization and analysis: ");
            Scanner keyboard = new Scanner(System.in);
            String input = "";
            Boolean found = false;
        
            Integer index = 0;
            while(found==false){
                input = keyboard.next();
            
                try{
                    index = Integer.valueOf(input);
            
                    if (index == -1){
                        break;
                    }
                    if (index>=0 && index< counter){
                        found = true;
                        filename = listOfFiles[index].getPath();
                    }
                
                }catch(Throwable e){
                    e.printStackTrace();
                    found = false;
                }
            }
        }else {
            filename = path + userStatisticsSelector;
        }
            
            
        try {
            FileInputStream fileIn = new FileInputStream(filename);

            ObjectInputStream objectIn = new ObjectInputStream(fileIn);

            userStatistics = (HashMap<String,UserStatistics>) objectIn.readObject();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();           
        }
        
        
        HashMap<String,Integer> keptValues = new HashMap<String,Integer>();
        Integer mean = 0;
        Integer tmp = 0;
        Double meanNOfActiveDays = 0D;
        for (Entry<String, UserStatistics> userStat:userStatistics.entrySet()){
            tmp = userStat.getValue().getTotalRealTransitions();
            mean += tmp;
            keptValues.put(userStat.getKey(), tmp);
            
            meanNOfActiveDays += userStat.getValue().getNumberOfActiveDays();
        }
        
        meanNOfActiveDays = meanNOfActiveDays/userStatistics.size();
        this.meanNofActiveDays = meanNOfActiveDays.intValue();
        
        Double varOfDays = 0D;
        for (Entry<String, UserStatistics> userStat:userStatistics.entrySet()){
            Double tmp1 = userStat.getValue().getNumberOfActiveDays() - meanNOfActiveDays;
            Double power = Math.pow(tmp1, 2);
            varOfDays += power;
        }
        varOfDays = varOfDays / userStatistics.size();
        Double stdOfDays = Math.sqrt(varOfDays);
        this.STDofActiveDays = stdOfDays.intValue();
        System.out.println("meanOfDays: " + meanNOfActiveDays);
        System.out.println("variance of Days: " + varOfDays);
        System.out.println("STD of Days: " + stdOfDays);
        
        
        mean = mean/userStatistics.size();
        this.meanTransitions = mean;
        System.out.println("Mean: " + mean);
        
        Long variance = 0L;
        for (Entry<String, Integer> set: keptValues.entrySet()){
            Double tmp1 = set.getValue().doubleValue()-mean.doubleValue();
            Double power = Math.pow(tmp1,2); 
            variance += power.longValue();            
        }
        
        variance = variance/keptValues.size();
        this.variance = variance;
        System.out.println("Variance: " + variance);
        
        Double std = Math.sqrt(variance);
        this.std = std;
        System.out.println("Standard deviation: " + std);
        
        

        
    }
    
    public List<String> returnNUsersAroundValue(Integer N, Integer value){
        
        //pick N users around average, or pick N random users
        
        
        Integer distance = 0;
        List<UserDistance> userDistances = new ArrayList<UserDistance>();
        for (Entry<String, UserStatistics> set:userStatistics.entrySet()){
            distance = Math.abs(set.getValue().getTotalRealTransitions() - value);
            UserDistance userDist = new UserDistance();
            userDist.setUser(set.getKey());
            userDist.setDistance(distance);
            
            userDistances.add(userDist);
        }
        
        Collections.sort(userDistances, UserDistance.compareDistance());
        
        Integer Ni = N;
        System.out.println("Dist from VALUE, transitions, activeDays,avgPerDay, username");
        List<String> result = new ArrayList<String>();
        for(int c=0; c<Ni; c++){
            
            System.out.print(userDistances.get(0).getDistance()  + "  ,            ");
            
            
            Integer total = userStatistics.get(userDistances.get(0).getUser()).getTotalRealTransitions();
            System.out.print(total + " ,     ");
            
            Integer active = userStatistics.get(userDistances.get(0).getUser()).getNumberOfActiveDays();
            System.out.print(active + "  ,      ");
            
            Integer avgPerDay = total/active;
            System.out.print(avgPerDay + "  ,       ");
            
            System.out.println(userDistances.get(0).getUser());
            
            
            result.add(userDistances.remove(0).getUser());
        }
        
        System.out.println(N +" users with number of transitions around given VALUE: " + value);
        for (String tmpStr:result){
            System.out.println(tmpStr);
        }
        
        return result;
        
    }
    
    public List<String> returnNUsersWithAvgPerDayAroundValue(Integer N, Integer value){
        
        
        //pick N users around average, or pick N random users
        
        List<UserDistance> userAVGPerDayDist = new ArrayList<UserDistance>();
        for (Entry<String, UserStatistics> tmp : userStatistics.entrySet()){
            
            Integer totalKept = tmp.getValue().getTotalRealTransitions();
            Integer activeDays = tmp.getValue().getNumberOfActiveDays();
            Integer avgPD = totalKept/activeDays;
            
            System.out.println(totalKept + " , " +activeDays + " , " + avgPD);
            
            Integer distance = Math.abs(avgPD-value);
            
            if (Math.abs(activeDays-meanNofActiveDays)<=STDofActiveDays || activeDays>meanNofActiveDays){
                UserDistance e = new UserDistance();
                e.setUser(tmp.getKey());
                e.setDistance(distance);
                userAVGPerDayDist.add(e);
            }
        }
        
        
        
        Collections.sort(userAVGPerDayDist, UserDistance.compareDistance());
        
        Integer Ni = N;
        System.out.println("Dist from VALUE, transitions, activeDays,avgPerDay, username");
        List<String> result = new ArrayList<String>();
        for(int c=0; c<Ni; c++){
            
            System.out.print(userAVGPerDayDist.get(0).getDistance()  + "  ,            ");
            
            
            Integer total = userStatistics.get(userAVGPerDayDist.get(0).getUser()).getTotalRealTransitions();
            System.out.print(total + " ,     ");
            
            Integer active = userStatistics.get(userAVGPerDayDist.get(0).getUser()).getNumberOfActiveDays();
            System.out.print(active + "  ,      ");
            
            Integer avgPerDay = total/active;
            System.out.print(avgPerDay + "  ,       ");
            
            System.out.println(userAVGPerDayDist.get(0).getUser());
            
            
            result.add(userAVGPerDayDist.remove(0).getUser());
        }
        
        System.out.println(N +" users with AVERAGE TRANSITIONS PER DAY around the given VALUE: " + value);
        for (String tmpStr:result){
            System.out.println(tmpStr);
        }
        
        return result;
        
    }
    
    public Integer getMeanNofActiveDays() {
        return meanNofActiveDays;
    }

    public Integer getMeanTransitions() {
        return meanTransitions;
    }

    public Long getVariance() {
        return variance;
    }

    public Double getStd() {
        return std;
    }

    public List<String> getNRandomUsersAround(Integer N, Integer value, Integer range){
        List<String> result = new ArrayList<String>();
        
        
        
        return result;
    }

}

class UserDistance{
    private String user;
    private Integer distance;
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public Integer getDistance() {
        return distance;
    }
    public void setDistance(Integer distance) {
        this.distance = distance;
    }
    
    public static Comparator<UserDistance> compareDistance(){
        return new Comparator<UserDistance>(){

            @Override
            public int compare(UserDistance arg0, UserDistance arg1) {
                if (arg0.distance>arg1.distance){
                    return 1;
                }else if(arg0.distance<arg1.distance){
                    return -1;
                }else{
                    return 0;
                }
            }
        };
    }
}
