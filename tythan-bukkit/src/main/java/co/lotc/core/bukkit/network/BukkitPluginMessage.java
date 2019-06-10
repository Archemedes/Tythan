package co.lotc.core.bukkit.network;

import java.io.DataOutputStream;

import org.bukkit.plugin.Plugin;

import co.lotc.core.agnostic.PluginOwned;
import co.lotc.core.network.PluginMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BukkitPluginMessage implements PluginOwned<Plugin>, PluginMessage {
	@Getter private final String subChannel;
	
	public BukkitPluginMessage() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getRecipient() {
		
	}

	@Override
	public void send() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DataOutputStream getPayload() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Plugin getPlugin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getChannel() {
		return "BungeeCord";
	}

}
