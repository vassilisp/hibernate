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

    

    public void start(){
        HashMap<String, UserStatistics> userStatistics = null;

        String path = "serializedObjects/";

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
        String filename = "";
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

            try {
                FileInputStream fileIn = new FileInputStream(filename);

                ObjectInputStream objectIn = new ObjectInputStream(fileIn);

                userStatistics = (HashMap<String,UserStatistics>) objectIn.readObject();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                found = false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                found = false;
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                found = false;
            }
        }
        
        HashMap<String,Integer> keptValues = new HashMap<String,Integer>();
        Integer mean = 0;
        Integer tmp = 0;
        for (Entry<String, UserStatistics> userStat:userStatistics.entrySet()){
            tmp = userStat.getValue().getTotalLogTransitions();
            mean += tmp;
            keptValues.put(userStat.getKey(), tmp);
        }
        mean = mean/userStatistics.size();
        System.out.println("Mean: " + mean);
        
        Long variance = 0L;
        
        for (Entry<String, Integer> set: keptValues.entrySet()){
            Double tmp1 = set.getValue().doubleValue()-mean.doubleValue();
            Double power = Math.pow(tmp1,2); 
            variance += power.longValue();            
        }
        
        variance = variance/keptValues.size();
        System.out.println("Variance: " + variance);
        System.out.println("Standard deviation: " + Math.sqrt(variance));
        
        
        
        //pick N users around average, or pick N random users
        
        
        Integer distance = 0;
        List<UserDistance> userDistances = new ArrayList<UserDistance>();
        for (Entry<String, UserStatistics> set:userStatistics.entrySet()){
            distance = Math.abs(set.getValue().getTotalRealTransitions() - mean);
            UserDistance userDist = new UserDistance();
            userDist.setUser(set.getKey());
            userDist.setDistance(distance);
            
            userDistances.add(userDist);
        }
        
        Collections.sort(userDistances, UserDistance.compareDistance());
        
        Integer N = 10;
        List<String> result = new ArrayList<String>();
        for(int c=0; c<N; c++){
            
            System.out.print(userDistances.get(0).getDistance()  + "  , ");
            
            System.out.print(userStatistics.get(userDistances.get(0).getUser()).getTotalRealTransitions() + " , ");
            
            System.out.println(userDistances.get(0));
            
            result.add(userDistances.remove(0).getUser());
        }
        
        System.out.println(N +" users around mean");
        for (String tmpStr:result){
            System.out.println(tmpStr);
        }
        
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
