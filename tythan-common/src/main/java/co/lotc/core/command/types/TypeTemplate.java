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
	@Getter protected final Class<T> targetType;
	private final TypeRegistry registry;
	
	@Getter private Function<String, T> mapper;
	@Getter private Predicate<T> filter;
	
	public final void register() {
		Validate.notNull(targetType, "There is no class specified for this argument type");
		Validate.isTrue(isClassValid(), "The class to specify as an argument type was already handled");
		registry.registerCustomType(this);
	}
	
	private boolean isClassValid() {
		if(targetType.isPrimitive()) return false;
		if(targetType == Integer.class) return false;
		if(targetType == Long.class) return false;
		if(targetType == Float.class) return false;
		if(targetType == Double.class) return false;
		if(targetType == String.class) return false;
		if(targetType == Boolean.class) return false;
		if(registry.customTypeExists(targetType)) return false;
		
		return true;
	}
}
