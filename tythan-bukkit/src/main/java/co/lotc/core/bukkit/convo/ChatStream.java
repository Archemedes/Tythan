package co.lotc.core.bukkit.convo;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.primitives.Ints;

import co.lotc.core.agnostic.AbstractChatStream;
import co.lotc.core.bukkit.util.ChatBuilder;
import co.lotc.core.bukkit.wrapper.BukkitSender;
import lombok.var;
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
	
	public ChatStream choice(String contextTag, BaseComponent message, String... options) {
		var cb = new ChatBuilder().append(message).newline();
		for(String option : options) cb.appendButton(option, option);
		message = cb.build();
		
		Function<String, String> maps = s-> (Stream.of(options).filter(o->s.equalsIgnoreCase(s)).findAny().orElse(null));
		return prompt(contextTag, message, maps);
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
	
	public ChatStream prompt(String contextTag, BaseComponent message, Function<String, ?> mapper) {
		return prompt(contextTag, message, Predicates.alwaysTrue(), Functions.identity());
	}
	
	public ChatStream prompt(String contextTag, String message, Predicate<String> filter, Function<String, ?> mapper) {
		return prompt(contextTag, new TextComponent(message), filter, mapper);
	}
	
	public ChatStream prompt(String contextTag, BaseComponent message, Predicate<String> filter, Function<String, ?> mapper) {
		return listen(contextTag, message, AsyncPlayerChatEvent.class, x->{
			if(filter.test(x.getMessage())) return mapper.apply(x.getMessage());
			else return null;
		});
	}
	
	public ChatStream intPrompt(String contextTag, String message) {
		return intPrompt(contextTag, new TextComponent(message));
	}
	
	public ChatStream intPrompt(String contextTag, BaseComponent message) {
		return prompt(contextTag, message, NumberUtils::isDigits, Ints::tryParse);
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
