package co.lotc.core.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import co.lotc.core.Tythan;
import co.lotc.core.TythanProvider;
import co.lotc.core.agnostic.Command;
import co.lotc.core.bukkit.command.ArcheCommandExecutor;
import co.lotc.core.bukkit.command.BrigadierProvider;
import co.lotc.core.bukkit.command.BukkitKommandant;
import co.lotc.core.bukkit.command.ItemArg;
import co.lotc.core.bukkit.command.SenderTypes;
import co.lotc.core.bukkit.menu.MenuListener;
import co.lotc.core.bukkit.util.ChatBuilder;
import co.lotc.core.bukkit.util.Run;
import co.lotc.core.bukkit.util.WeakBlock;
import co.lotc.core.bukkit.wrapper.BukkitCommand;
import co.lotc.core.bukkit.wrapper.BukkitConfig;
import co.lotc.core.command.ArcheCommand;
import co.lotc.core.command.brigadier.CommandNodeManager;
import co.lotc.core.command.brigadier.Kommandant;
import lombok.Getter;
import lombok.var;

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
	
	@Override
	public void registerRootCommand(Command wrapper, ArcheCommand handler) {
		var pluginCommand = ((BukkitCommand) wrapper).getHandle();
		ArcheCommandExecutor executor = new ArcheCommandExecutor(handler);
		pluginCommand.setExecutor(executor);
	}
	
	@Override
	public Kommandant newKommandant(ArcheCommand built) {
		return new BukkitKommandant(built);
	}
	
	private void listen(Listener l) {
		Bukkit.getPluginManager().registerEvents(l, this);
	}

}
