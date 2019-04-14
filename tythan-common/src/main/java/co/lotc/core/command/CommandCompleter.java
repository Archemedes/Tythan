package co.lotc.core.command;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import co.lotc.core.agnostic.Sender;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandCompleter {
	public static final CommandCompleter NULL_COMPLETER = new CommandCompleter(($1,$2)->Collections.emptyList());
	
	private final BiFunction<Sender, String, ? extends Collection<String>> suggestions;
	
	public CommandCompleter(Supplier<? extends Collection<String>> supplier) {
		suggestions = ($1,$2) -> supplier.get();
	}
	
	public CommandCompleter(Function<Sender, ? extends Collection<String>> function) {
		suggestions = (s,$)-> function.apply(s);
	}
	
	public Collection<String> suggest(Sender sender, String input){
		return suggestions.apply(sender, input);
	}
}
