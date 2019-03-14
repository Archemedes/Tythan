package co.lotc.core.command.types;

import java.util.HashMap;
import java.util.Map;
//TODO rewrite entire package
public class TypeRegistry {
	private static final TypeRegistry arguments = new TypeRegistry();
	private static final TypeRegistry senders = new TypeRegistry();
	
	public static TypeRegistry forArguments() { return arguments; }
	public static TypeRegistry forSenders() { return senders; }
	
	private final Map<Class<?>, TypeTemplate<?>> customTypes = new HashMap<>();

	public void registerCustomType(TypeTemplate<?> template) {
		customTypes.put(template.getTargetType(), template);
	}
	
	public TypeTemplate<?> getCustomType(Class<?> clazz) {
		return customTypes.get(clazz);
	}
	
	public boolean customTypeExists(Class<?> clazz) {
		return customTypes.containsKey(clazz);
	}
	
}
