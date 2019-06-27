package co.lotc.core.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import co.lotc.core.Tythan;
import co.lotc.core.TythanProvider;
import co.lotc.core.bukkit.command.BrigadierProvider;
import co.lotc.core.bukkit.command.ItemArg;
import co.lotc.core.bukkit.command.SenderTypes;
import co.lotc.core.bukkit.item.RestrictionListener;
import co.lotc.core.bukkit.menu.MenuListener;
import co.lotc.core.bukkit.util.ChatBuilder;
import co.lotc.core.bukkit.util.Run;
import co.lotc.core.bukkit.util.WeakBlock;
import co.lotc.core.bukkit.wrapper.BukkitConfig;
import co.lotc.core.command.brigadier.CommandNodeManager;
import lombok.Getter;

public class TythanBukkit extends JavaPlugin implements Tythan {
	public static TythanBukkit get(){
		return (TythanBukkit) Tythan.get();
	}
	
	@Getter private boolean debugging;
	
	@Override
	public void onLoad() {
		TythanProvider.init(this);
		ConfigurationSerialization.registerClass(WeakBlock.class, "WeakBlock");
	}
	
	@Override
	public void onEnable(){
		saveDefaultConfig();

		debugging = getConfig().getBoolean("debug");
		
		registerCommandParameterTypes();
		listen(new MenuListener());
		listen(new RestrictionListener());
		
		Run.as(this).delayed(2, ()->{ //Brigadier singleton deep inside NMS: get and inject
			CommandNodeManager.getInstance().inject(BrigadierProvider.get().getBrigadier().getRoot());
		});
	}
	
	private void registerCommandParameterTypes() {
		SenderTypes.registerCommandSenderType();
		SenderTypes.registerPlayerType();
		SenderTypes.registerOfflinePlayerType();

		ItemArg.buildItemStackParameter();
		ItemArg.buildMaterialParameter();
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

	private void listen(Listener l) {
		Bukkit.getPluginManager().registerEvents(l, this);
	}

}
