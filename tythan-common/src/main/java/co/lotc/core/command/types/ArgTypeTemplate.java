package co.lotc.core.command.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang.Validate;

import com.google.common.base.Supplier;

import co.lotc.core.agnostic.Sender;
import co.lotc.core.command.CmdArg;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.var;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent=true)
@Setter
public class ArgTypeTemplate<T> {
	private static final Map<Class<?>, ArgTypeTemplate<?>> customTypes = new HashMap<>();
	
	public static ArgTypeTemplate<?> getCustomType(Class<?> clazz) {
		return customTypes.get(clazz);
	}
	
	public static boolean senderTypeExists(Class<?> clazz) {
		var x = customTypes.get(clazz);
		return x != null && x.mapper() != null;
	}
	
	public static boolean argumentTypeExists(Class<?> clazz) {
		var x = customTypes.get(clazz);
		return x != null && x.senderMapper() != null;
	}
	
	/* END OF STATICS */
	
	protected final Class<T> forClass;
	
	@Getter private Function<String, T> mapper;
	@Getter private Function<Sender, T> senderMapper;
	@Getter private Predicate<T> filter;

	private Supplier<Collection<String>> completer;
	private String defaultName;
	private String defaultError;
	
	
	private boolean isClassValid() {
		if(forClass.isPrimitive()) return false;
		if(forClass == Integer.class) return false;
		if(forClass == Long.class) return false;
		if(forClass == Float.class) return false;
		if(forClass == Double.class) return false;
		if(forClass == String.class) return false;
		if(forClass == Boolean.class) return false;
		
		return true;
	}
	
	public Class<?> getTargetType(){
		return forClass;
	}
	
	public final void register() {
		Validate.notNull(forClass, "There is no class specified for this argument type");
		Validate.isTrue(isClassValid(), "The class to specify as an argument type was already handled");
		//TODO check if have to merge with pre-existing type
		customTypes.put(forClass, this);
	}
	
	public void settle(CmdArg<T> arg) {
		if(completer != null) arg.setCompleter(completer);
		if(filter != null) arg.setFilter(filter);
		if(mapper != null) arg.setMapper(mapper);
	}
	
	public String getDefaultName() { return defaultName; }
	
	public String getDefaultError() { return defaultError; }
}
