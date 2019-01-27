package co.lotc.core.bukkit.util;

import org.bukkit.entity.Player;

import co.lotc.core.agnostic.AbstractChatStream;
import co.lotc.core.bukkit.wrapper.BukkitSender;
import net.md_5.bungee.api.chat.BaseComponent;

public class ChatStream extends AbstractChatStream{

	public ChatStream(Player p) {
		super(new BukkitSender(p));
	}
	
	public ChatStream prompt(String contextTag, BaseComponent text) {
		this.prompt(contextTag, promptText, fulfillment)
	}

}
