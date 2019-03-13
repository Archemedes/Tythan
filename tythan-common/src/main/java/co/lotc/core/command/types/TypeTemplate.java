package co.lotc.core.command.types;

import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang.Validate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent=true)
@Setter
public abstract class TypeTemplate<T> {
	protected final Class<T> forClass;
	private final TypeRegistry registry;
	
	@Getter private Function<String, T> mapper;
	@Getter private Predicate<T> filter;
	
	public final void register() {
		Validate.notNull(forClass, "There is no class specified for this argument type");
		Validate.isTrue(isClassValid(), "The class to specify as an argument type was already handled");
		registry.registerCustomType(this);
	}
	
	private boolean isClassValid() {
		if(forClass.isPrimitive()) return false;
		if(forClass == Integer.class) return false;
		if(forClass == Long.class) return false;
		if(forClass == Float.class) return false;
		if(forClass == Double.class) return false;
		if(forClass == String.class) return false;
		if(forClass == Boolean.class) return false;
		if(registry.customTypeExists(forClass)) return false;
		
		return true;
	}
	
	public Class<?> getTargetType(){
		return forClass;
	}
}
