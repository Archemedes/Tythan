package co.lotc.core.bungee.convo;

import java.util.UUID;
import java.util.function.Function;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.event.EventHandler;

public class ChatEventListener extends PromptListener<ChatEvent> {

	public ChatEventListener(UUID uuid, Function<ChatEvent, Object> function, Function<ChatEvent, ProxiedPlayer> howToGetPlayer) {
		super(uuid, ChatEvent.class, function, howToGetPlayer);
	}
	
	@Override
	@EventHandler
	public void execute(ChatEvent e) {
		super.execute(e);
	}

}
