package gq.panop.util;

import java.util.HashMap;

public class PerformanceUtil {
	private Long time;
	private Boolean set = false;
	
	private static String res;
	
	private static HashMap<Long,Long> entity;
	
	public PerformanceUtil(String resolution){
		res = resolution;
	}
	public void Tick(){
		time = getTime();
		set = true;
	}
	
	public void Tock(String info){
		if (set){
			time = getTime() - time;
			System.out.println("(-- " + info + " --) //Performance result: " + time + res);
		}else{
			System.out.println("Tick not pressed");
		}
	}
	
	public void Tock(){
		if (set){
			time = getTime() - time;
			System.out.println("//Performance result: " + time + res);
			set = false;
		}else{
			System.out.println("Tick not pressed");
		}
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

