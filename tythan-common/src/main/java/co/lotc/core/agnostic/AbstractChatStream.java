package co.lotc.core.agnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang.Validate;

import co.lotc.core.Tythan;
import co.lotc.core.util.Context;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

@RequiredArgsConstructor
public abstract class AbstractChatStream {
	protected final Sender converser;
	protected final Context context = new Context();
	protected final List<Prompt> prompts = new ArrayList<>();
	
	protected Consumer<Context> onAbandon, onActivate;
	
	public AbstractChatStream prompt(String contextTag, BaseComponent promptText, Consumer<Prompt> fulfillment) {
		Prompt p = new Prompt(this, contextTag, promptText, fulfillment);
		prompts.add(p);
		return this;
	}
	
	public void activate(Consumer<Context> go) {
		onActivate = go;
		Validate.isTrue(prompts.size() > 0);
		prompts.get(0).open();
	}
	
	
	@RequiredArgsConstructor
	@FieldDefaults(level=AccessLevel.PRIVATE, makeFinal=true)
	@Getter
	public static final class Prompt{
		AbstractChatStream stream;
		String contextTag;
		BaseComponent text;
		
		Consumer<Prompt> fulfillment;
		
		void open() {
			sendPrompt();
			fulfillment.accept(this);
		}
		
		public void sendPrompt() {
			Tythan.get().chatBuilder()
			.appendButton("x", "stop", "click to exit prompt", ChatColor.RED, ChatColor.RED)
			.append(text)
			.send(stream.converser);
		}
		
		public void fulfil(Object value) {
			if(contextTag != null) stream.context.set(contextTag, value);
			next();
		}
		
		void next() {
			int i = stream.prompts.indexOf(this) + 1;
			if(i >= stream.prompts.size()) {
				stream.onActivate.accept(stream.context);
			} else {
				stream.prompts.get(i).open();
			}
		}
	}
	
}
