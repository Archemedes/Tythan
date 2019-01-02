package co.lotc.core.bungee;

import co.lotc.core.TythanCommon;
import net.md_5.bungee.api.plugin.Plugin;

public class TythanBungee extends Plugin implements TythanCommon {
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

