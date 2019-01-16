package co.lotc.core.bukkit;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import co.lotc.core.Tythan;
import co.lotc.core.TythanCommon;
import co.lotc.core.bukkit.util.ChatBuilder;
import co.lotc.core.bukkit.util.WeakBlock;
import lombok.Getter;

public class TythanBukkit extends JavaPlugin implements Tythan {
	private final TythanCommon common = new TythanCommon(this);
	@Getter private boolean debugging;
	
	@Override
	public void onLoad() {
		common.onLoad();
		ConfigurationSerialization.registerClass(WeakBlock.class, "WeakBlock");
	}
	
	@Override
	public void onEnable(){
		saveDefaultConfig();
		Configuration config = getConfig();
	}
	
	
	@Override
	public void onDisable(){

	}

	@Override
	public ChatBuilder chatBuilder() {
		return new ChatBuilder();
	}
}
