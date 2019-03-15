package co.lotc.core.bukkit;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Function;

import co.lotc.core.DependencyLoader;
import co.lotc.core.Tythan;
import co.lotc.core.TythanCommon;
import co.lotc.core.agnostic.Sender;
import co.lotc.core.bukkit.listener.ChatStreamListener;
import co.lotc.core.bukkit.menu.MenuListener;
import co.lotc.core.bukkit.util.ChatBuilder;
import co.lotc.core.bukkit.util.WeakBlock;
import co.lotc.core.bukkit.wrapper.BukkitConfig;
import co.lotc.core.bukkit.wrapper.BukkitSender;
import co.lotc.core.command.ArcheCommand;
import co.lotc.core.command.types.ArgTypeTemplate;
import lombok.Getter;

public class TythanBukkit extends JavaPlugin implements Tythan {
	public static TythanBukkit get(){
		return (TythanBukkit) Tythan.get();
	}
	
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

		debugging = getConfig().getBoolean("debug");
		
		registerCommandParameterTypes();
		
		listen(new ChatStreamListener(this));
		listen(new MenuListener());
	}
	
	private void registerCommandParameterTypes() {
		Function<Sender, CommandSender> function = s->((BukkitSender) s).getHandle();
		
		new ArgTypeTemplate<>(CommandSender.class).senderMapper(function).register();
		new ArgTypeTemplate<>(Player.class).senderMapper( function.andThen(cs->(cs instanceof Player)? ((Player) cs): null)).register();
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
	
	@Override
	public void registerRootCommand(ArcheCommand builder) {
		
	}
	
	private void listen(Listener l) {
		Bukkit.getPluginManager().registerEvents(l, this);
	}
	
	static { //Thanks Tofuus for the gross hacks <3
		DependencyLoader.loadJars(new File("plugins/Tythan/jars"));
	}
}
