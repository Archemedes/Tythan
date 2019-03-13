package co.lotc.core.command.types;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.Supplier;

import co.lotc.core.command.CmdArg;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent=true)
@Setter
public class ArgTypeTemplate<T> extends TypeTemplate<T> {
	
	public ArgTypeTemplate(Class<T> forClass) {
		super(forClass, TypeRegistry.forArguments());
	}

	private Function<String, T> mapper;
	private Predicate<T> filter;
	private Supplier<Collection<String>> completer;
	String defaultName;
	String defaultError;
	
	void settle(CmdArg<T> arg) {
		if(completer != null) arg.setCompleter(completer);
		if(filter != null) arg.setFilter(filter);
		if(mapper != null) arg.setMapper(mapper);
	}	
}
