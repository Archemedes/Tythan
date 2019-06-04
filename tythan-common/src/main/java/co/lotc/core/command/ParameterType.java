package co.lotc.core.command;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang.Validate;

import com.mojang.brigadier.arguments.ArgumentType;

import co.lotc.core.CoreLog;
import co.lotc.core.agnostic.Sender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.var;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent=true)
@Setter
public class ParameterType<T> {
	private static final Map<Class<?>, ParameterType<?>> customTypes = new HashMap<>();
	
	public static ParameterType<?> getCustomType(Class<?> clazz) {
		return customTypes.get(clazz);
	}
	
	public static boolean senderTypeExists(Class<?> clazz) {
		var x = customTypes.get(clazz);
		return x != null && x.senderMapper() != null;
	}
	
	public static boolean argumentTypeExists(Class<?> clazz) {
		var x = customTypes.get(clazz);
		return x != null && (x.mapper() != null || x.mapperWithSender != null);
	}
	
	/* END OF STATICS */
	
	protected final Class<T> forClass;
	
	@Getter private Function<String, T> mapper;
	@Getter private BiFunction<Sender, String, T> mapperWithSender;
	@Getter private Function<Sender, T> senderMapper;
	@Getter private Predicate<T> filter;
	private ArgumentType<?> brigadierType;

	private CommandCompleter completer;
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
		if(forClass == String.class) return false;
		if(forClass == Sender.class) return false;
		if(forClass == String[].class) return false;
		if(forClass == Instant.class) return false;
		if(forClass == Duration.class) return false;
		if(forClass == Timestamp.class) return false;
		return true;
	}
	
	public Class<?> getTargetType(){
		return forClass;
	}
	
	public final void register() {
		Validate.notNull(forClass, "There is no class specified for this argument type");
		Validate.isTrue(isClassValid(), "The class to specify as an argument type was already handled");

		@SuppressWarnings("unchecked") //This is safe because only type T can be linked to Class<T> which is what the key was
		var existing = (ParameterType<T>) customTypes.get(forClass);
		if(existing != null) {
			CoreLog.warning("Attempted a merge on a custom command argument type for the class: " + forClass.getSimpleName());
			CoreLog.warning("This might be fine but more likely this was unintended and might lead to unexpected behavior");
			if(existing.mapper == null) existing.mapper = this.mapper;
			if(existing.senderMapper == null) existing.senderMapper = this.senderMapper;
			if(existing.filter == null) existing.filter = this.filter;
		} else {
			customTypes.put(forClass, this);
		}
	}
	
	
	public ParameterType<T> completer(CommandCompleter completer) {
		this.completer = completer;
		return this;
	}
	
	public ParameterType<T> completer( BiFunction<Sender, String, ? extends Collection<String>> suggestions) {
		this.completer = new CommandCompleter(suggestions);
		return this;
	}
	
	public ParameterType<T> completer(Supplier<? extends Collection<String>> supplier ) {
		this.completer = new CommandCompleter(supplier);
		return this;
	}
	
	public void settle(CmdArg<T> arg) {
		if(mapper != null && mapperWithSender != null)
			throw new IllegalStateException("Tried to define the mapping of an argument from string twice! Once with and once without sender! Please define only one");
		if(completer != null) arg.setCompleter(completer);
		if(filter != null) arg.setFilter(filter);
		if(mapper != null) arg.setMapper(mapper);
		if(mapperWithSender != null) arg.setMapperWithSender(mapperWithSender);
		if(brigadierType!=null) arg.setBrigadierType(brigadierType);
	}
	
	public String getDefaultName() {
		if(defaultName == null) return this.forClass.getSimpleName();
		return defaultName;
	}
	
	public String getDefaultError() {
		if(defaultError == null) return "Please provide a valid " + getDefaultName();
		return defaultError;
	}
}
