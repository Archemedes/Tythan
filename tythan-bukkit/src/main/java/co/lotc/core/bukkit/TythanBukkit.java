package co.lotc.core.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import co.lotc.core.DependencyLoader;
import co.lotc.core.Tythan;
import co.lotc.core.TythanProvider;

public class TythanBukkit extends JavaPlugin implements Tythan {
	private boolean debugMode;
	
	@Override
	public void onLoad() {
		TythanProvider.init(this);
		DependencyLoader.loadJars(getDataFolder());
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
