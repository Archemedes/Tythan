package co.lotc.core.bungee.convo;

import static net.md_5.bungee.api.ChatColor.GRAY;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;


import co.lotc.core.CoreLog;
import co.lotc.core.agnostic.AbstractChatStream.Prompt;
import co.lotc.core.bungee.TythanBungee;
import co.lotc.core.bungee.util.ChatBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.var;
import lombok.experimental.FieldDefaults;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@FieldDefaults(level=AccessLevel.PRIVATE)
@RequiredArgsConstructor
abstract class PromptListener<T extends Event> implements Consumer<Prompt>,Listener {
	final UUID uuid;
	final Class<T> clazz;
	final Function<T, Object> function;
	final Function<T, ProxiedPlayer> howToGetPlayer;
	
	Prompt prompt;
	ScheduledTask giveUp;
	
	boolean active = true;
	
	@Override
	public void accept(Prompt p) {
		prompt = p;
		task();
		var tythan = TythanBungee.get();
		var m = proxy().getPluginManager();
		
		m.registerListener(tythan, this);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void chat(ChatEvent e) {
		if(e.getSender() instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) e.getSender();
			if(player.getUniqueId().equals(uuid)) {
				e.setCancelled(true);
				if("stop".equals(e.getMessage().toLowerCase())) {
					new ChatBuilder("Stop command received. Exiting").color(GRAY).send(player);
					abandon();
					return;
				}
			}
		}
	}
	
	protected void execute(T event) {
		CoreLog.debug("Catching Event as " + this);
		
		if(!active) {
			CoreLog.debug("But the event Listener has fulfilled its purpse (abandoned, active=false)");
			return;
		}
		
		T theEvent = clazz.cast(event);
		ProxiedPlayer who = howToGetPlayer.apply(theEvent);
		if(who != null && who.getUniqueId().equals(uuid)) {
			task();
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
		giveUp = proxy().getScheduler().schedule(TythanBungee.get(), ()->{
			giveUp = null;
			abandon();
			ProxiedPlayer p = proxy().getPlayer(uuid);
			if(p != null) new ChatBuilder("We didn't receive your input in time. Exiting.").color(GRAY).send(p);
			CoreLog.debug("Giving up on prompt " + this);
		}, 40, TimeUnit.SECONDS);
	}
	
	private void abandon() {
		proxy().getPluginManager().unregisterListener(this);
		active = false;
		if(giveUp != null) giveUp.cancel();
	}
	
	private ProxyServer proxy() {
		return TythanBungee.get().getProxy();
	}
	
	@Override
	public String toString() {
		return "HeyListen{" + uuid.toString() + "->" + prompt.getText().toLegacyText() + "}";
	}
}