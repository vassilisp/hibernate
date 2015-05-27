package gq.panop.util;

import java.util.HashMap;

public class PerformanceUtil {
	private Long time;
	private Boolean set = false;
	
	private static HashMap<Long,Long> entity;
	
	public void Tick(){
		time = System.nanoTime();
		set = true;
	}
	
	public void Tock(String info){
		if (set){
			time = System.nanoTime() - time;
			System.out.println("(-- " + info + " --) //Performance result: " + time + " ns");
		}else{
			System.out.println("Tick not pressed");
		}
	}
	
	public void Tock(){
		if (set){
			time = System.nanoTime() - time;
			System.out.println("//Performance result: " + time + " ns");
		}else{
			System.out.println("Tick not pressed");
		}
	}
	
	public static void TickWithId(long tickId){
		entity.put(tickId, System.nanoTime());
	}
	
	public  static void TockWithId(long tickId){
		String message = "empty";
		try{
			Long time = entity.get(tickId);
			Long exTime = System.nanoTime()-time;
			message = "//Performance result ( " + tickId + " ) : " + exTime + " ns";
		}catch (Throwable ex){
			message = "Error while evaluating performance - Set Tick first";
		}
			
			System.out.println(message);
	}
	
}
