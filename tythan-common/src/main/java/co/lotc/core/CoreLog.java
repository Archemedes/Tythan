package co.lotc.core;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CoreLog {
	private static Tythan core;
	
	private CoreLog() {}
	
	public static void log(Level level, String msg) {
		get().log(level, msg);
	}
	
	public static void log(Level level, String msg, Throwable thrown) {
		get().log(level, msg, thrown);
	}
	
	public static void severe(String msg) {
		get().severe(msg);
	}
	
	public static void severe(String msg, Throwable throwable) {
		get().log(Level.SEVERE, msg, throwable);
	}
	
	
	public static void warning(String msg) {
		get().warning(msg);
	}
	
	public static void info(String msg) {
		get().info(msg);
	}
	
	public static void debug(String msg) {
		if(core.isDebugging())
			get().info("[DEBUG] " + msg);
	}
	
	public static boolean isDebugging() {
		return core.isDebugging();
	}
	
	static void set(Tythan newCore) {
		core = newCore;
	}
	
	private static Logger get() {
		return core.getLogger();
	}
}
