package co.lotc.core.bukkit.convo;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.google.common.base.Functions;
import com.google.common.base.Predicates;

import co.lotc.core.agnostic.AbstractChatStream;
import co.lotc.core.bukkit.wrapper.BukkitSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatStream extends AbstractChatStream<ChatStream>{
	private final UUID uuid;
	
	public ChatStream(Player p) {
		super(new BukkitSender(p));
		this.uuid = p.getUniqueId();
	}

	public <T extends Event> ChatStream listen(String contextTag, BaseComponent message, Class<T> c, Function<T, Object> listener) {
		prompt(contextTag, message, new PromptListener<>(uuid, c, listener));
		return this;
	}
	
	public ChatStream prompt(String contextTag, String message) {
		return prompt(contextTag, new TextComponent(message));
	}
	
	public ChatStream prompt(String contextTag, BaseComponent message) {
		return prompt(contextTag, message, Predicates.alwaysTrue());
	}
	
	public ChatStream prompt(String contextTag, String message, Predicate<String> filter) {
		return prompt(contextTag, new TextComponent(message), filter);
	}
	
	public ChatStream prompt(String contextTag, BaseComponent message, Predicate<String> filter) {
		return prompt(contextTag, message, filter, Functions.identity());
	}
	
	public ChatStream prompt(String contextTag, String message, Predicate<String> filter, Function<String, ?> mapper) {
		return prompt(contextTag, new TextComponent(message), filter, mapper);
	}
	
	public ChatStream prompt(String contextTag, BaseComponent message, Predicate<String> filter, Function<String, ?> mapper) {
		return listen(contextTag, message, AsyncPlayerChatEvent.class, x->{
			if(x.getPlayer().getUniqueId().equals(uuid)) {
				if(filter.test(x.getMessage())) return mapper.apply(x.getMessage());
			}
			
			return null;
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
