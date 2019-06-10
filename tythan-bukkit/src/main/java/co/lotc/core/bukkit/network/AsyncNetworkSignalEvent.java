package co.lotc.core.bukkit.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import lombok.Getter;

public class AsyncNetworkSignalEvent extends Event {
  @Getter private final String server;
  @Getter private final String reason;
  
  private final Map<Plugin, AtomicInteger> intents = new ConcurrentHashMap<>();
  private final AtomicBoolean fired = new AtomicBoolean();
  private final AtomicInteger latch = new AtomicInteger();

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
