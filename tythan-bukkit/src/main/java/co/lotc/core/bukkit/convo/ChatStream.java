package co.lotc.core.bukkit.convo;

import java.util.function.Function;
import java.util.function.Predicate;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.google.common.base.Predicates;

import co.lotc.core.agnostic.AbstractChatStream;
import co.lotc.core.bukkit.wrapper.BukkitSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatStream extends AbstractChatStream<ChatStream>{
	
	public ChatStream(Player p) {
		super(new BukkitSender(p), p.getUniqueId());
	}

	public <T extends PlayerEvent> ChatStream listen(String contextTag, BaseComponent message, Class<T> c, Function<T, Object> listener) {
		return listen(contextTag, message, c, listener, e->e.getPlayer());
	}
	
	public <T extends Event> ChatStream listen(String contextTag, BaseComponent message, Class<T> c, Function<T, Object> listener, Function<T, Player> howToGetPlayer) {
		prompt(contextTag, message, new PromptListener<>(uuid, c, listener, howToGetPlayer));
		return this;
	}
	
	@Override
	public ChatStream prompt(String contextTag, BaseComponent message, Predicate<String> filter, Function<String, ?> mapper) {
		return listen(contextTag, message, AsyncPlayerChatEvent.class, x->{
			if(filter.test(x.getMessage())) return mapper.apply(x.getMessage());
			else return null;
		});
	}
	
	public ChatStream clickBlockPrompt(String contextTag, String message) {
		return clickBlockPrompt(contextTag, new TextComponent(message));
	}
	
	public ChatStream clickBlockPrompt(String contextTag, String message, Predicate<Block> filter) {
		return clickBlockPrompt(contextTag, new TextComponent(message), filter);
	}
	
	public ChatStream clickBlockPrompt(String contextTag, BaseComponent message) {
		return clickBlockPrompt(contextTag, message, Predicates.alwaysTrue());
	}
	
	public ChatStream clickBlockPrompt(String contextTag, BaseComponent message, Predicate<Block> filter) {
		return listen(contextTag, message, PlayerInteractEvent.class, e->{
				if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
					Block b = e.getClickedBlock();
					e.setCancelled(true);
					if(filter.test(b)) return b;
				}
				return null;
		});
	}
	
	public ChatStream clickEntityPrompt(String contextTag, String message) {
		return clickEntityPrompt(contextTag, new TextComponent(message));
	}
	
	public ChatStream clickEntityPrompt(String contextTag, String message, Predicate<Entity> filter) {
		return clickEntityPrompt(contextTag, new TextComponent(message), filter);
	}
	
	public ChatStream clickEntityPrompt(String contextTag, BaseComponent message) {
		return clickEntityPrompt(contextTag, message, Predicates.alwaysTrue());
	}
	
	public ChatStream clickEntityPrompt(String contextTag, BaseComponent message, Predicate<Entity> filter) {
		return listen(contextTag, message, PlayerInteractEntityEvent.class, e->{
				Entity entity = e.getRightClicked();
				e.setCancelled(true);
				if(filter.test(entity)) return entity;
				return null;
		});
	}
	
	@Override
	protected ChatStream getThis() {
		return this;
	}
}
