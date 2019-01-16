package co.lotc.core.bungee.util;


import co.lotc.core.AbstractChatBuilder;
import net.md_5.bungee.api.CommandSender;

public class ChatBuilder extends AbstractChatBuilder<ChatBuilder> {

	public ChatBuilder() {
		super("");
	}
	
	public ChatBuilder(String initial) {
		super(initial);
	}
	
	public ChatBuilder send(CommandSender s) {
		//ProxiedPlayer extends this. Should work for all
		s.sendMessage(this.build());
		return getThis();
	}
	
	@Override
	protected ChatBuilder getThis() {
		return this;
	}

}
