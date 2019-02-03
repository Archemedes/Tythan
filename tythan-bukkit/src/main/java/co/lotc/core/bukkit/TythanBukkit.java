package co.lotc.core.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import co.lotc.core.Tythan;
import co.lotc.core.TythanCommon;
import co.lotc.core.bukkit.listener.ChatStreamListener;
import co.lotc.core.bukkit.menu.MenuListener;
import co.lotc.core.bukkit.util.ChatBuilder;
import co.lotc.core.bukkit.util.WeakBlock;
import co.lotc.core.bukkit.wrapper.BukkitConfig;
import lombok.Getter;

public class TythanBukkit extends JavaPlugin implements Tythan {
	private final TythanCommon common = new TythanCommon(this);
	@Getter private boolean debugging = true;
	
	@Override
	public void onLoad() {
		common.onLoad();
		ConfigurationSerialization.registerClass(WeakBlock.class, "WeakBlock");
	}
	
	@Override
	public void onEnable(){
		saveDefaultConfig();
		
		listen(new ChatStreamListener(this));
		listen(new MenuListener());
	}
	
	@Override
	public BukkitConfig config() {
		return new BukkitConfig(getConfig());
	}
	
	@Override
	public void onDisable(){

	}

	@Override
	public ChatBuilder chatBuilder() {
		return new ChatBuilder();
	}
	
	public static TythanBukkit get(){
		return (TythanBukkit) Tythan.get();
	}
	
	private void listen(Listener l) {
		Bukkit.getPluginManager().registerEvents(l, this);
	}
}
