package co.lotc.core.bungee;

import co.lotc.core.Tythan;
import co.lotc.core.TythanProvider;
import co.lotc.core.agnostic.Sender;
import co.lotc.core.bungee.command.BrigadierInjector;
import co.lotc.core.bungee.servers.ServersUtil;
import co.lotc.core.bungee.util.ChatBuilder;
import co.lotc.core.bungee.wrapper.BungeeConfig;
import co.lotc.core.bungee.wrapper.BungeeSender;
import co.lotc.core.command.ParameterType;
import de.exceptionflug.protocolize.api.protocol.ProtocolAPI;
import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TythanBungee extends Plugin implements Tythan {
	@Getter private boolean debugging;
	public static final Function<Sender, CommandSender> UNWRAP_SENDER = s->((BungeeSender) s).getHandle();
	public static final Function<Sender, ProxiedPlayer> UNWRAP_PLAYER = UNWRAP_SENDER.andThen(s->(s instanceof ProxiedPlayer)? ((ProxiedPlayer) s):null);
	public static final Supplier<List<String>> PLAYER_COMPLETER = ()-> ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList());
	
	public static TythanBungee get(){
		return (TythanBungee) Tythan.get();
	}
	
	@Override
	public void onLoad() {
		TythanProvider.init(this);
	}
	
	@Override
	public void onEnable(){
		saveDefaultConfig();
		registerCommandParameterTypes();
		ProtocolAPI.getEventManager().registerListener(new BrigadierInjector());
		ServersUtil.init();
	}

	@Override
	public void onDisable(){
		ServersUtil.disable();
	}
	
	private void registerCommandParameterTypes() {
		new ParameterType<>(CommandSender.class).senderMapper(UNWRAP_SENDER).register();
		new ParameterType<>(ProxiedPlayer.class)
				.senderMapper(UNWRAP_PLAYER)
				.mapperWithSender((send,s)->{
					if("@p".equals(s)) s = send.getName();
					if(s.length() == 36) {
						try {return ProxyServer.getInstance().getPlayer(UUID.fromString(s));}
						catch(IllegalArgumentException e) {return null;}
					} else {
						return ProxyServer.getInstance().getPlayer(s);
					}
				})
				.completer(PLAYER_COMPLETER).register();
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
}

