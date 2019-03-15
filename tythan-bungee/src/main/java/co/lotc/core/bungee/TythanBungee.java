package co.lotc.core.bungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import co.lotc.core.DependencyLoader;
import co.lotc.core.Tythan;
import co.lotc.core.TythanCommon;
import co.lotc.core.agnostic.Command;
import co.lotc.core.bungee.command.BungeeCommandExecutor;
import co.lotc.core.bungee.command.BungeeCommandData;
import co.lotc.core.bungee.util.ChatBuilder;
import co.lotc.core.bungee.wrapper.BungeeConfig;
import co.lotc.core.bungee.wrapper.BungeeSender;
import co.lotc.core.command.ArcheCommand;
import co.lotc.core.command.ParameterType;
import lombok.Getter;
import lombok.var;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class TythanBungee extends Plugin implements Tythan {
	private final TythanCommon common = new TythanCommon(this);
	@Getter private boolean debugging;
	
	@Override
	public void onLoad() {
		common.onLoad();
	}
	
	@Override
	public void onEnable(){
		saveDefaultConfig();
		registerCommandParameterTypes();
	}

	@Override
	public void onDisable(){

	}
	
	private void registerCommandParameterTypes() {
		new ParameterType<>(CommandSender.class).senderMapper(s->((BungeeSender) s).getHandle()).register();
	}
	
	@Override
	public BungeeConfig config() {
		return new BungeeConfig(getConfig());
	}
	
	Configuration getConfig() {
		try {
			return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
			return new Configuration();
		}
	}
	
	protected void saveDefaultConfig() {
		if(!getDataFolder().exists()) getDataFolder().mkdir();

		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			try (InputStream in = getResourceAsStream("config.yml")) {
				Files.copy(in, file.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public ChatBuilder chatBuilder() {
		return new ChatBuilder();
	}
	
	@Override
	public void registerRootCommand(Command wrapper, ArcheCommand command) {
		var sc = (BungeeCommandData) wrapper;
		var exec = new BungeeCommandExecutor(command, sc);
		getProxy().getPluginManager().registerCommand(sc.getPlugin(), exec);
	}
	
	static { //Thanks Tofuus for the gross hacks <3
		DependencyLoader.loadJars(new File("plugins/Tythan/jars"));
	}
}

