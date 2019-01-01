package co.lotc.core.bungee;

import co.lotc.core.Tythan;
import co.lotc.core.TythanProvider;
import net.md_5.bungee.api.plugin.Plugin;

public class TythanBungee extends Plugin implements Tythan {
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

