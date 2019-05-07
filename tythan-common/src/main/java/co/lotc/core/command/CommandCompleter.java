package co.lotc.core.command;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import co.lotc.core.agnostic.Sender;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

public class CommandCompleter {
	public static final CommandCompleter NULL_COMPLETER = new CommandCompleter(($1,$2)->Collections.emptyList());
	
	private final BiFunction<Sender, String, ? extends Collection<Suggestion>> suggestions;
	
	public static CommandCompleter suggestWithTooltips(Supplier<? extends Collection<Suggestion>> supplier) {
		return suggestWithTooltips(($1,$2)->supplier.get() );
	}
	
	public static CommandCompleter suggestWithTooltips(Function<Sender, ? extends Collection<Suggestion>> function) {
		return suggestWithTooltips( (s,$) -> function.apply(s));
	}
	
	public static CommandCompleter suggestWithTooltips(BiFunction<Sender, String, ? extends Collection<Suggestion>> function) {
		return new CommandCompleter(function, false);
	}
	
	public CommandCompleter(Supplier<? extends Collection<String>> supplier) {
		suggestions = ($1,$2) -> asSuggestions(supplier.get());
	}
	
	public CommandCompleter(Function<Sender, ? extends Collection<String>> function) {
		suggestions = (s,$)-> asSuggestions(function.apply(s));
	}
	
	private CommandCompleter(BiFunction<Sender, String, ? extends Collection<Suggestion>> function, boolean unused) {
		this.suggestions = function;
	}
	
	public CommandCompleter(BiFunction<Sender, String, ? extends Collection<String>> function) {
		suggestions = (s,i)->asSuggestions(function.apply(s, i));
	}
	
	public Collection<Suggestion> suggest(Sender sender, String input){
		return suggestions.apply(sender, input);
	}
	
	private Collection<Suggestion> asSuggestions(Collection<String> input){
		return input.stream().map(Suggestion::new).collect(Collectors.toList());
	}
	
	@Value
	@AllArgsConstructor
	public static class Suggestion {
		String literal, tooltip;
		
		private Suggestion(String literal) {
			this(literal, null);
		}
		
		public boolean hasTooltip() {
			return tooltip != null;
		}
	}
}
