package co.lotc.core.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import co.lotc.core.agnostic.Sender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class CmdArg<T> {
	private static final Supplier<Collection<String>> NULL_COMPLETER = ArrayList::new;
	
	@Setter private Function<String, T> mapper;
	@Setter private BiFunction<Sender, String, T> mapperWithSender = ($,s)->mapper.apply(s);
	@Setter private Predicate<T> filter = $->true;
	@Setter private Supplier<? extends Collection<String>> completer = NULL_COMPLETER;
	
	@SuppressWarnings("rawtypes")
	@Setter private ArgumentType brigadierType = StringArgumentType.word();
	
	private final String name, errorMessage, defaultInput, description;
	
	T resolveDefault(Sender s) {
		if(defaultInput == null) return null;
		return resolve(s, defaultInput);
	}
	
	T resolve(Sender s, List<String> input, int i) {
		return resolve(s, input.get(i));
	}
	
	T resolve(Sender s, String input) {
		T mapped = mapperWithSender.apply(s, input);
		if(mapped == null || !filter.test(mapped)) return null;
		
		return mapped;
	}
	
	void completeMe(String... opts) {
		setCompleter(()->Arrays.asList(opts));
	}
	
	public boolean hasCustomCompleter() {
		return this.completer != NULL_COMPLETER;
	}
	
	public boolean hasDefaultInput() {
		return defaultInput != null;
	}
	
	public boolean hasDescription() {
		return description != null;
	}
}
