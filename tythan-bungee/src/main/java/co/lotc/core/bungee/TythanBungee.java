package co.lotc.core.bungee;

import co.lotc.core.Tythan;
import co.lotc.core.TythanCommon;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

public class TythanBungee extends Plugin implements Tythan {
	private final TythanCommon common = new TythanCommon(this);
	@Getter private boolean debugging;
	
	@Override
	public void onLoad() {
		common.onLoad();
	}
	
	@Override
	public void onEnable(){

	}

	@Override
	public void onDisable(){

	}
	
}

