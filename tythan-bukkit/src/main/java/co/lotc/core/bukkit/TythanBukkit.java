package co.lotc.core.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import co.lotc.core.TythanCommon;

public class TythanBukkit extends JavaPlugin implements TythanCommon {
	private boolean debugMode;
	
	@Override
	public void load() {

	}
	
	@Override
	public void onEnable(){
		
	}

	@Override
	public void onDisable(){

	}
	
	@Override
	public boolean isDebugging() {
		return debugMode;
	}
}
