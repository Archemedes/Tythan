package co.lotc.core.bukkit.network;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import co.lotc.core.CoreLog;
import co.lotc.core.network.NetworkCommunication;
import lombok.val;

public class BukkitNetworkCommunication implements NetworkCommunication, PluginMessageListener {
	private final Set<UUID> parkingLot = new HashSet<>();

	@Override
	public void acquireLock(boolean longTerm) {
		// TODO Auto-generated method stub

	}

	@Override
	public void relinquishLock(boolean longTerm) {
		// TODO Auto-generated method stub

	}

	@Override
	public void awaitSignal(String reason, Runnable callback) {
		awaitSignal(SERVER_STRING_PROXY, reason, callback);
	}
	
	@Override
	public void awaitSignal(String server, String reason, Runnable callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processSignal(String origin, String reason) {
		val event = new AsyncNetworkSignalEvent(origin, reason);
		Bukkit.getPluginManager().callEvent(event);
		
		val intents = event.intents;
		synchronized(intents) {
			while(!intents.isEmpty()) {
				try {
					intents.wait(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				val iter = intents.iterator();
				while(iter.hasNext()) {
					Plugin plugin = iter.next();
					if(!plugin.isEnabled()) iter.remove();
					else CoreLog.severe("Plugin timeout while registering Network intent on " + reason + ": " + plugin.getName());
				}
			}
		}
		
		//TODO return to sender?
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		// TODO Auto-generated method stub
		
	}


}
