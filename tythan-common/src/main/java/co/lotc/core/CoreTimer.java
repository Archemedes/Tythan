package co.lotc.core;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import com.google.common.collect.Maps;

public class CoreTimer {
	private static final Map<String, Long> timings = Maps.newConcurrentMap();
	
	public static void startTiming(String why){
		if(!Tythan.get().isDebugging()) return;
		
		long time = System.nanoTime();
		Validate.notNull(why);
		timings.put(why, time);
	}
	
	public static void stopTiming(String why){
		if(!Tythan.get().isDebugging()) return;
		
		long time = System.nanoTime();
		
		Validate.notNull(why);
		
		if(timings.containsKey(why)){
			long took = (time - timings.get(why))/1000;
			CoreLog.debug("operation '" + why + "' took " + took + "μs");
		}
	}
	
	public static void stopAllTiming(){
		if(!Tythan.get().isDebugging()) return;
		
		long time = System.nanoTime();
		for(Entry<String, Long> t : timings.entrySet())
			CoreLog.debug("timed action '" + t.getKey() + "' taking " + (time - t.getValue())/1000 + "μs");
		
		timings.clear();
	}
}
