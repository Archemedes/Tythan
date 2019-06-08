package co.lotc.core.bungee.servers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Event;

@AllArgsConstructor
public class ServerStatusChangeEvent extends Event {
	@Getter
	ServerInfo server;
	ServerStatus serverStatus;
}

