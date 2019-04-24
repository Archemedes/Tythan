package co.lotc.core.agnostic;

import static net.md_5.bungee.api.ChatColor.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;

import com.google.common.primitives.Ints;

import co.lotc.core.Tythan;
import co.lotc.core.util.Context;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.var;
import lombok.experimental.FieldDefaults;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

@RequiredArgsConstructor
public abstract class AbstractChatStream<T extends AbstractChatStream<T>> {
	protected final Sender converser;
	protected final UUID uuid;
	protected final Context context = new Context();
	protected final List<Prompt> prompts = new ArrayList<>();
	
	protected Consumer<Context> onAbandon, onActivate;
	
	public T prompt(Prompt prompt) {
		prompts.add(prompt);
		return getThis();
	}
	
	public T prompt(String contextTag, BaseComponent promptText, Consumer<Prompt> fulfillment) {
		Prompt p = new Prompt(this, contextTag, promptText, fulfillment);
		return prompt(p);
	}
	
	public T confirmPrompt() {
		var msg = Tythan.get().chatBuilder().append("Are you sure you want to continue? Type ").color(WHITE)
				.append("YES").color(RED).bold().append(" in all capitals to confirm your choice.").bold(false).color(WHITE).build();
		return confirmPrompt(msg, "YES");
	}
	
	public T confirmPrompt(String whatTheyShouldType) {
		var msg = Tythan.get().chatBuilder().append("Are you sure you want to continue? Type ").color(WHITE)
				.append(whatTheyShouldType).color(RED).bold().append(" to confirm your choice (case-sensitive).").bold(false).color(WHITE).build();
		return confirmPrompt(msg, "YES");
	}
	
	public T confirmPrompt(BaseComponent message, String whatTheyShouldType) {
		return prompt(null, message, s->s.equals(whatTheyShouldType), $->$);
	}
	
	public T choice(String contextTag, BaseComponent message, String... options) {
		AbstractChatBuilder<? extends AbstractChatBuilder<?>> cb = Tythan.get().chatBuilder().append(message).newline();
		for(String option : options) cb.appendButton(option, option);
		message = cb.build();
		
		Function<String, String> maps = s-> (Stream.of(options).filter(o->s.equalsIgnoreCase(s)).findAny().orElse(null));
		return prompt(contextTag, message, maps);
	}
	
	public T prompt(String contextTag, String message) {
		return prompt(contextTag, new TextComponent(message));
	}
	
	public T prompt(String contextTag, BaseComponent message) {
		Predicate<String> somePredicate = $->true; //Compiler wants it explicit in generic type
		return prompt( contextTag, message, somePredicate );
	}
	
	public T prompt(String contextTag, String message, Predicate<String> filter) {
		return prompt(contextTag, new TextComponent(message), filter);
	}
	
	public T prompt(String contextTag, BaseComponent message, Predicate<String> filter) {
		return prompt(contextTag, message, filter, $->$);
	}
	
	public T prompt(String contextTag, BaseComponent message, Function<String, ?> mapper) {
		return prompt(contextTag, message, $->true, $->$);
	}
	
	public T prompt(String contextTag, String message, Predicate<String> filter, Function<String, ?> mapper) {
		return prompt(contextTag, new TextComponent(message), filter, mapper);
	}
	
	public abstract T prompt(String contextTag, BaseComponent message, Predicate<String> filter, Function<String, ?> mapper);
	
	
	public T intPrompt(String contextTag, String message) {
		return intPrompt(contextTag, new TextComponent(message));
	}
	
	public T intPrompt(String contextTag, BaseComponent message) {
		return prompt(contextTag, message, NumberUtils::isDigits, Ints::tryParse);
	}
	
	public T withContext(String key, Object value) {
		context.set(key, value);
		return getThis();
	}
	
	public T withContext(Context context) {
		context.getMap().forEach( (k,v) -> this.context.set(k, v));
		return getThis();
	}
	
	public void activate(Consumer<Context> go) {
		onActivate = go;
		Validate.isTrue(prompts.size() > 0);
		prompts.get(0).open();
	}
	
	protected abstract T getThis();
	
	@RequiredArgsConstructor
	@FieldDefaults(level=AccessLevel.PRIVATE, makeFinal=true)
	@Getter
	public static final class Prompt{
		AbstractChatStream<?> stream;
		String contextTag;
		BaseComponent text;
		
		Consumer<Prompt> fulfillment;
		
		public Sender getConverser() {
			return stream.converser;
		}
		
		void open() {
			sendPrompt();
			fulfillment.accept(this);
		}
		
		public void sendPrompt() {
			Tythan.get().chatBuilder()
			.appendButton("x", "stop", "click to exit prompt", ChatColor.RED, ChatColor.RED)
			.append(" ").reset()
			.append(text)
			.send(stream.converser);
		}
		
		public void fulfil(Object value) {
			if(StringUtils.isNotEmpty(contextTag)) stream.context.set(contextTag, value);
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
