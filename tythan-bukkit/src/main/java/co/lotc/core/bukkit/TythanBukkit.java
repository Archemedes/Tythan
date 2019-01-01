package co.lotc.core.bukkit;

import co.lotc.core.Tythan;
import co.lotc.core.TythanProvider;

import org.bukkit.plugin.java.JavaPlugin;

public class TythanBukkit extends JavaPlugin implements Tythan {
	private boolean debugMode;
	
	@Override
	public void onLoad() {
		TythanProvider.init(this);
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
