package co.lotc.core.command;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang.Validate;

import com.google.common.base.Supplier;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lordofthecraft.arche.command.ArgBuilder;
import net.lordofthecraft.arche.command.CmdArg;

@RequiredArgsConstructor
@Accessors(fluent=true)
@Setter
public class ArgTypeTemplate<T> {
	private final Class<T> forClass;
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
	
	Class<T> getTargetType() {
		return forClass;
	}
	
	public void register() {
		Validate.notNull(forClass, "There is no class specified for this argument type");
		Validate.isTrue(isClassValid(), "The class to specify as an argument type was already handled");
		ArgBuilder.registerCustomType(this);
	}
	
	private boolean isClassValid() {
		if(forClass.isPrimitive()) return false;
		if(forClass == Integer.class) return false;
		if(forClass == Long.class) return false;
		if(forClass == Float.class) return false;
		if(forClass == Double.class) return false;
		if(forClass == String.class) return false;
		if(forClass == Boolean.class) return false;
		if(ArgBuilder.customTypeExists(forClass)) return false;
		
		return true;
	}
	
}
