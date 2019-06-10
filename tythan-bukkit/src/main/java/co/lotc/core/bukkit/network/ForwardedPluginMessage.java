package co.lotc.core.bukkit.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;

import co.lotc.core.agnostic.PluginOwned;
import co.lotc.core.network.PluginMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class ForwardedPluginMessage implements PluginMessage, PluginOwned<Plugin> {
	@Getter private final Plugin plugin;
	@Getter private final String recipient;
	
	private final ByteArrayOutputStream payloadBytes = new ByteArrayOutputStream();
	@Getter private final DataOutputStream payload = new DataOutputStream(payloadBytes);
	

	@Override
	public String getChannel() {
		return "BungeeCord";
	}

	@Override
	public String getSubchannel() {
		return "Forward";
	}

	@Override
	public void send() {
		Player p = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
		if(p == null) return; //TODO warnings?
		
		val out = ByteStreams.newDataOutput();
		out.writeUTF(getSubchannel());
		out.writeUTF(getRecipient());
		
		val array = payloadBytes.toByteArray();
    out.writeShort(array.length);
    out.write(array);
    p.sendPluginMessage(getPlugin(), getChannel(), out.toByteArray());
	}

}
