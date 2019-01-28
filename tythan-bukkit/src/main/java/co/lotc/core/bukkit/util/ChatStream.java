package co.lotc.core.bukkit.util;

import org.bukkit.entity.Player;

import co.lotc.core.agnostic.AbstractChatStream;
import co.lotc.core.bukkit.wrapper.BukkitSender;

public class ChatStream extends AbstractChatStream{

	public ChatStream(Player p) {
		super(new BukkitSender(p));
	}

}
