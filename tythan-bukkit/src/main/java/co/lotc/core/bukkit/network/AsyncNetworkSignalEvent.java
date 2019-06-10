package co.lotc.core.bukkit.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import lombok.Getter;

@Getter
public class AsyncNetworkSignalEvent extends Event {
  private final String server;
  private final String reason;
  
  final List<Plugin> intents = Collections.synchronizedList(new ArrayList<>());
  
	public AsyncNetworkSignalEvent(String server, String reason) {
		super(true);
		this.server = server;
		this.reason = reason;
	}
	
	public void completeIntent(Plugin plugin) {
		synchronized(intents) {
			intents.remove(plugin);
			intents.notifyAll();
		}
	}
	
	public void registerIntent(Plugin plugin){
		intents.add(plugin);
	}

	private static final HandlerList handlers = new HandlerList();
	public static HandlerList getHandlerList() {return handlers;}
	@Override public HandlerList getHandlers() {return handlers;}
}
