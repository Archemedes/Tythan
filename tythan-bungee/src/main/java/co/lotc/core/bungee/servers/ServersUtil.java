package co.lotc.core.bungee.servers;

import co.lotc.core.bungee.TythanBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ServersUtil {

	private static Set<ServerInfo> onlineServers;
	private static ScheduledTask task;

	public static void init() {
		onlineServers = ConcurrentHashMap.newKeySet();
		task = ProxyServer.getInstance().getScheduler().schedule(TythanBungee.get(), ServersUtil::ping, 5, TimeUnit.SECONDS);
	}

	public static void disable() {
		task.cancel();
		onlineServers = null;
	}

	public static boolean isOnline(ServerInfo server) {
		return onlineServers.contains(server);
	}

	public static boolean isOnline(String server) {
		return isOnline(ProxyServer.getInstance().getServerInfo(server));
	}

	public static Set<ServerInfo> getOnlineServers() {
		return onlineServers;
	}

	private static void ping() {
		for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
			server.ping((result, error) -> {
				boolean previouslyOnline = onlineServers.contains(server);
				if (error == null && !previouslyOnline) {
					onlineServers.add(server);
					ProxyServer.getInstance().getPluginManager().callEvent(new ServerStatusChangeEvent(server, ServerStatus.ONLINE));
				} else if (error == null && previouslyOnline) {
					onlineServers.remove(server);
					ProxyServer.getInstance().getPluginManager().callEvent(new ServerStatusChangeEvent(server, ServerStatus.OFFLINE));
				}
			});
		}
	}
}
