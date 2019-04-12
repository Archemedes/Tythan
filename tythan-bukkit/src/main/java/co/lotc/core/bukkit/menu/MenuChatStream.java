package co.lotc.core.bukkit.menu;

import java.util.function.Consumer;

import co.lotc.core.bukkit.convo.ChatStream;
import co.lotc.core.util.Context;

class MenuChatStream extends ChatStream {
	private final MenuAgent agent;
	
	public MenuChatStream(MenuAgent a) {
		super(a.getPlayer());
		agent = a;
	}

	@Override
	public void activate(Consumer<Context> go) {
		Consumer<Context> chained = c->{
			agent.mergeContext(c);
			go.accept(c);
			agent.getPlayer().openInventory(agent.getMenu().getInventory());
		};
		
		super.activate(chained);
	}
	
}
