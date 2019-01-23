package co.lotc.core.bukkit.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.agnostic.AbstractChatBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class ChatBuilder extends AbstractChatBuilder<ChatBuilder> {

	public ChatBuilder() {
		super("");
	}
	
	public ChatBuilder(String initial) {
		super(initial);
	}
	
	public ChatBuilder color(ChatColor color) {
		handle.color(color.asBungee());
		return getThis();
	}
	
	public ChatBuilder hoverItem(ItemStack is) {
		return event(HoverEvent.Action.SHOW_ITEM, ItemUtil.getItemJson(is));
	}
	
	@Override
	protected ChatBuilder getThis() {
		return this;
	}
	
	public ChatBuilder send(Player p) {
		p.spigot().sendMessage(handle.create());
		return getThis();
	}
	
	public ChatBuilder send(CommandSender s) {
		if(s instanceof Player) return send((Player) s);
		s.sendMessage(toLegacyText());
		
		return getThis();
	}

}
