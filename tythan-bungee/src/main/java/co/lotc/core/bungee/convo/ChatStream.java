package co.lotc.core.bungee.convo;

import java.util.function.Function;
import java.util.function.Predicate;

import co.lotc.core.agnostic.AbstractChatStream;
import co.lotc.core.bungee.wrapper.BungeeSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;

public class ChatStream extends AbstractChatStream<ChatStream> {

	public ChatStream(ProxiedPlayer player) {
		super(new BungeeSender(player), player.getUniqueId());
	}
	
	public <T extends ChatEvent> ChatStream listen(String contextTag, BaseComponent message, Function<ChatEvent, Object> listener, Function<ChatEvent, ProxiedPlayer> howToGetPlayer) {
		prompt(contextTag, message, new ChatEventListener(uuid, listener, howToGetPlayer));
		return this;
	}
	
	@Override
	protected ChatStream getThis() {
		return this;
	}

	@Override
	public ChatStream prompt(String contextTag, BaseComponent message, Predicate<String> filter, Function<String, ?> mapper) {
		Function<ChatEvent, ProxiedPlayer> howToGetPlayer = event->{
			Connection con = event.getSender();
			if(con instanceof ProxiedPlayer) return (ProxiedPlayer) con;
			else return null;
		};
		
		return listen(contextTag, message, x->{
			if(filter.test(x.getMessage())) return mapper.apply(x.getMessage());
			else return null;
		}, howToGetPlayer);
	}

	@Override
	protected void resolveFinishedStream() {
		onActivate.accept(context);
	}

}
