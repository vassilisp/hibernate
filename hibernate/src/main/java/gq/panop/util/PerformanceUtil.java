package gq.panop.util;

import java.util.HashMap;

public class PerformanceUtil {
	private Long time  = -1L;
	private Boolean set = false;
	
	private long result;
	private String details = "";
	private String info = "";
	private static String res;
	
	private static HashMap<Long,Long> entity;
	
	public PerformanceUtil(String resolution){
		res = resolution;
	}
	public void Tick(){
		time = getTime();
		set = true;
	}
	
	public void Tick(String info){
        time = getTime();
        set = true;
        this.info = info;
    }
	
	public long Tock(String info){
		return returnPerformance(info, true);
	}
	
	public long Tock(){
	    return returnPerformance("", true);
	}
	
	public long Lap(){
	    return returnPerformance(" ~[[Lap]]", false);
	}
	
	public long Lap(String info){
	    return returnPerformance(info +" ~[[Lap]]", false);
	}
	
	private long returnPerformance(String info, Boolean close){
	    if (set){
            result = getTime() - time;
            
            if ((info.length()>0)||(this.info.length()>0)){
                details = "(-- " + this.info + " : " + info + " --)";
            }else{
                details = "";
            }
            System.out.println(details + " //Performance result: " + result + res);
            
            if ((set)&&(close)){
                set = false;
                this.info = "";
            }
        }else{
            System.out.println("Tick not pressed");
            result = -1L;
        }
        return result;
	}
	
	public static void TickWithId(long tickId){
		entity.put(tickId, getTime());
	}
	
	public  static void TockWithId(long tickId){
		String message = "empty";
		try{
			Long time = entity.get(tickId);
			Long exTime = getTime() - time;
			message = "//Performance result ( " + tickId + " ) : " + exTime + res;
		}catch (Throwable ex){
			message = "Error while evaluating performance - Set Tick first";
		}
			
			System.out.println(message);
	}
	
	private static long getTime(){
		if (res.equals("ns")){
			return System.nanoTime();			
		}else{
			return System.currentTimeMillis();
		}
	}
	
    public void separate(){
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("");
    }

}

