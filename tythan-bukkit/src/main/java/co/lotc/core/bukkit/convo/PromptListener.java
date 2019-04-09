package co.lotc.core.bukkit.convo;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.scheduler.BukkitTask;

import co.lotc.core.CoreLog;
import co.lotc.core.agnostic.AbstractChatStream.Prompt;
import co.lotc.core.bukkit.TythanBukkit;
import co.lotc.core.bukkit.util.Run;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.var;
import lombok.experimental.FieldDefaults;

//Don't know if this will work
@FieldDefaults(level=AccessLevel.PRIVATE)
@RequiredArgsConstructor
class PromptListener<T extends Event> implements Consumer<Prompt>,Listener,EventExecutor {
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
				CoreLog.debug("Prompt was fulfilled: " + this);
			} else {
				prompt.sendPrompt();
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