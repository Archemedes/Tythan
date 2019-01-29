package co.lotc.core.bukkit.util;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import co.lotc.core.CoreLog;
import co.lotc.core.agnostic.AbstractChatStream;
import co.lotc.core.bukkit.TythanBukkit;
import co.lotc.core.bukkit.wrapper.BukkitSender;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.var;
import lombok.experimental.FieldDefaults;
import net.md_5.bungee.api.chat.BaseComponent;

public class ChatStream extends AbstractChatStream<ChatStream>{
	private final UUID uuid;
	
	public ChatStream(Player p) {
		super(new BukkitSender(p));
		this.uuid = p.getUniqueId();
	}

	public <T extends Event> ChatStream listen(String contextTag, BaseComponent message, Class<T> c, Function<T, Object> listener) {
		prompt(contextTag, message, new HeyListen<>(uuid, c, listener));
		return this;
	}
	
	public ChatStream prompt(String contextTag, BaseComponent message) {
		return validatePrompt(contextTag, message, Predicates.alwaysTrue());
	}
	
	public ChatStream validatePrompt(String contextTag, BaseComponent message, Predicate<String> filter) {
		return listen(contextTag, message, AsyncPlayerChatEvent.class, x->{
			if(x.getPlayer().getUniqueId().equals(uuid)) {
				if(filter.apply(x.getMessage())) return x.getMessage();
			}
			
			return null;
		});
	}
	
	
	@Override
	protected ChatStream getThis() {
		return this;
	}
	
	//Don't know if this will work
	@FieldDefaults(level=AccessLevel.PRIVATE)
	@RequiredArgsConstructor
	private static class HeyListen<T extends Event> implements Consumer<Prompt>,Listener,EventExecutor {
		final UUID uuid;
		final Class<T> clazz;
		final Function<T, Object> function;
		
		Prompt prompt;
		BukkitTask giveUp;
		
		@Override
		public void accept(Prompt p) {
			prompt = p;
			task();
			var tythan = TythanBukkit.get();
			var m = Bukkit.getPluginManager();
			
			m.registerEvent(AsyncPlayerChatEvent.class, this, EventPriority.LOW, this, tythan, true);
			if(clazz != AsyncPlayerChatEvent.class) m.registerEvent(clazz, this, EventPriority.LOW, this, tythan, true);
		}

		@Override
		public void execute(Listener listener, Event event) throws EventException {
			CoreLog.debug("Catching Event as " + this);
			
			if(event instanceof AsyncPlayerChatEvent) {
				var as = (AsyncPlayerChatEvent) event;
				as.setCancelled(true);
				if("stop".equals(as.getMessage().toLowerCase()) && as.getPlayer().getUniqueId().equals(uuid) ) {
					Run.as(TythanBukkit.get()).sync(()->abandon());
					return;
				}
			}
			
			if(clazz.isInstance(event)) {
				task();
				T theEvent = clazz.cast(event);
				Object result = function.apply(theEvent);
				if(result != null) {
					abandon();
					prompt.fulfil(result);
				}
			}
		}
		
		private void task() {
			if(giveUp != null) giveUp.cancel();
			giveUp = Bukkit.getScheduler().runTaskLater(TythanBukkit.get(), ()->{
				giveUp = null;
				abandon();
				CoreLog.debug("Giving up on prompt " + this);
			}, 20*20);
		}
		
		private void abandon() {
			HandlerList.unregisterAll(this);
			if(giveUp != null) giveUp.cancel();
		}
		
		@Override
		public String toString() {
			return "HeyListen{" + uuid.toString() + "->" + prompt.getText().toLegacyText() + "}";
		}
	}
}
