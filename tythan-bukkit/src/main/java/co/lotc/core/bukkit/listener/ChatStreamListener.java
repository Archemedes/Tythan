package co.lotc.core.bukkit.listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.lotc.core.agnostic.AbstractChatStream.Prompt;
import co.lotc.core.bukkit.TythanBukkit;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
public class ChatStreamListener implements Listener {
	TythanBukkit plugin;
	Map<Prompt, Predicate<String>> chatPrompts = new ConcurrentHashMap<>();
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void handle(AsyncPlayerChatEvent e) {
		
	}
	
}
