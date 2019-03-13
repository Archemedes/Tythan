package co.lotc.core.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import co.lotc.core.CoreLog;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;
import net.lordofthecraft.arche.command.ArcheCommandBuilder;
import net.lordofthecraft.arche.command.ArgTypeTemplate;
import net.lordofthecraft.arche.command.CmdArg;
import net.lordofthecraft.arche.command.CmdFlag;
import net.lordofthecraft.arche.command.JoinedArg;
import net.lordofthecraft.arche.command.VoidArg;
import net.md_5.bungee.api.ChatColor;

@Accessors(fluent= true)
public class ArgBuilder {
	private final static Map<Class<?>, ArgTypeTemplate<?>> customTypes = new HashMap<>();
	static void registerCustomType(ArgTypeTemplate<?> template) { customTypes.put(template.getTargetType(), template);}
	static boolean customTypeExists(Class<?> clazz) { return customTypes.containsKey(clazz);}
	
	//Either one of these is set, depending on what kind of arg
	private final ArcheCommandBuilder command;
	@Getter(AccessLevel.PACKAGE) private final CmdFlag flag;

	@Setter private String defaultInput;
	@Setter private String name = null;
	@Setter private String errorMessage = null;
	@Setter private String description = null;
	
	ArgBuilder(ArcheCommandBuilder command) {
		this(command, null);
	}
	
	ArgBuilder(ArcheCommandBuilder command, CmdFlag flag) {
		this.command = command;
		this.flag = flag;
	}

	public ArcheCommandBuilder asInt(){
		asIntInternal();
		return command;
	}
	
	public ArcheCommandBuilder asInt(int min){
		defaults("#","Must be a valid integer of %d or higher", min);
		val arg = asIntInternal();
		arg.setFilter(i->i>=min);
		arg.setBrigadierType(IntegerArgumentType.integer(min));
		return command;
	}
	
	public ArcheCommandBuilder asInt(int min, int max){
		defaults("#","Must be a valid integer between %d ad %d", min, max);
		val arg = asIntInternal();
		arg.setFilter(i->(i>=min && i <= max));
		arg.setBrigadierType(IntegerArgumentType.integer(min, max));
		return command;
	}
	
	public ArcheCommandBuilder asInt(IntPredicate filter) {
		val arg = asIntInternal();
		arg.setFilter(i->filter.test(i));
		return command;
	}
	
	
	private CmdArg<Integer> asIntInternal(){
		defaults("#","Not an accepted integer");
		CmdArg<Integer> arg = build(Integer.class);
		arg.setMapper(Ints::tryParse);
		arg.setBrigadierType(IntegerArgumentType.integer());
		return arg;
	}
	
	public ArcheCommandBuilder asLong(){
		asLongInternal();
		return command;
	}
	
	private CmdArg<Long> asLongInternal() {
		defaults("#l","Not an accepted longinteger");
		CmdArg<Long> arg = build(Long.class);
		arg.setMapper(Longs::tryParse);
		return arg;
	}
	
	public ArcheCommandBuilder asDouble() {
		asDoubleInternal();
		return command;
	}
	
	public ArcheCommandBuilder asDouble(double min){
		val arg = asDoubleInternal();
		arg.setFilter(d->d>=min);
		arg.setBrigadierType(DoubleArgumentType.doubleArg(min));
		return command;
	}
	
	public ArcheCommandBuilder asDouble(double min, double max){
		val arg = asDoubleInternal();
		arg.setFilter(d->(d>=min && d <= max));
		arg.setBrigadierType(DoubleArgumentType.doubleArg(min,max));
		return command;
	}
	
	public ArcheCommandBuilder asDouble(DoublePredicate filter) {
		val arg = asDoubleInternal();
		arg.setFilter(i->filter.test(i));
		return command;
	}
	
	private CmdArg<Double> asDoubleInternal(){
		defaults("#.#","Not an accepted number");
		CmdArg<Double> arg = build(Double.class);
		arg.setMapper(Doubles::tryParse);
		arg.setBrigadierType(DoubleArgumentType.doubleArg());
		return arg;
	}
	
	public ArcheCommandBuilder asFloat() {
		asFloatInternal();
		return command;
	}
	
	public ArcheCommandBuilder asFloat(float min) {
		val arg = asFloatInternal();
		arg.setFilter(d->d>=min);
		arg.setBrigadierType(FloatArgumentType.floatArg(min));
		return command;
	}
	
	public ArcheCommandBuilder asFloat(float min, float max) {
		val arg = asFloatInternal();
		arg.setFilter(d->(d>=min && d <= max));
		arg.setBrigadierType(FloatArgumentType.floatArg(min, max));
		return command;
	}
	
	public CmdArg<Float> asFloatInternal() {
		defaults("#.#","Not an accepted number");
		CmdArg<Float> arg = build(Float.class);
		arg.setMapper(Floats::tryParse);
		arg.setBrigadierType(FloatArgumentType.floatArg());
		return arg;
	}
	
	public ArcheCommandBuilder asString(){
		defaults("*","Provide an argument");
		val arg = build(String.class);
		arg.setMapper($->$);
		return command;
	}
	
	public ArcheCommandBuilder asString(String... options){
		defaults("*","Must be one of these: " + ChatColor.WHITE + StringUtils.join(options, ", "));
		val arg = build(String.class);
		arg.setFilter( s-> Stream.of(options).filter(s2->s2.equalsIgnoreCase(s)).findAny().isPresent() );
		arg.setMapper($->$);
		arg.completeMe(options);
		return command;
	}
	
	public <T extends Enum<T>> ArcheCommandBuilder asEnum(Class<T>  clazz) {
		defaults(clazz.getSimpleName(),"Not a valid " + clazz.getSimpleName());
		val arg = build(clazz);
		arg.setMapper(s->{
			try{ return Enum.valueOf(clazz, s.toUpperCase()); }
			catch(IllegalArgumentException e) {return null;}
		});
		arg.setCompleter(()->Arrays.stream(clazz.getEnumConstants())
				.map(Enum::name)
				.map(String::toLowerCase)
				.collect(Collectors.toList()));
		return command;
	}
	
	public ArcheCommandBuilder asBoolean() {
		return asBoolean(false);
	}
	
	public ArcheCommandBuilder asBoolean(boolean def) {
		this.defaultInput = def? "y":"n";
		defaults("y/n","Please provide either true/false.");
		val arg = build(Boolean.class);
		arg.setMapper(s -> {
			if(Stream.of("true","yes","y").anyMatch(s::equalsIgnoreCase)) return true;
			else if(Stream.of("false","no","n").anyMatch(s::equalsIgnoreCase)) return false;
			else return null;
		});
		return command;
	}
	
	public ArcheCommandBuilder asVoid() {
		if(flag == null) throw new IllegalStateException("This makes no sense to use for anything but flags");
		VoidArg arg = new VoidArg(name, errorMessage, description);
		flag.setArg(arg);
		return command;
	}
	
	public ArcheCommandBuilder asJoinedString() {
		if(flag != null) throw new IllegalStateException("Cannot use joined arguments for parameters/flags");
		
		defaults("**", "Provide any sentence, spaces allowed.");
		JoinedArg arg = new JoinedArg(name, errorMessage, defaultInput, description);
		command.noMoreArgs = true;
		command.addArg(arg);
		return command;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <X> ArcheCommandBuilder asType(Class<X> c) {
		if( c == Void.class) {
			asVoid(); //Flags only
		}else if( c == String.class) {
			asString();
		}else if(c==int.class || c==Integer.class) {
			asInt();
		}else if(c==long.class || c==Long.class) {
			asLong();
		} else if(c==Float.class || c==float.class) {
			asFloat();
		} else if(c==Double.class || c==double.class) {
			asDouble();
		} else if(c==Boolean.class || c==boolean.class) {
			asBoolean();
		} else if(c.isEnum() && !customTypes.containsKey(c)) {
			asEnum((Class<Enum>) c);
		} else {
			asCustomType(c);
		}
		
		return command;
	}
	
	private <X> ArcheCommandBuilder asCustomType(Class<X> clazz) {
		if(!customTypes.containsKey(clazz)) throw new IllegalArgumentException("This class was not registered as a CUSTOM argument type: " + clazz.getSimpleName());
		@SuppressWarnings("unchecked")
		ArgTypeTemplate<X> result = (ArgTypeTemplate<X>) customTypes.get(clazz);
		String defaultName = result.defaultName == null? clazz.getSimpleName() : result.defaultName;
		String defaultError = result.defaultError == null? "Please provide a valid " + clazz.getSimpleName() : result.defaultError;
		defaults(defaultName, defaultError);
		
		CmdArg<X> arg = build(clazz);
		result.settle(arg);
		return command;
	}
	
	private void defaults(String name, String err, Object... formats) {
		if(this.name == null) this.name = name;
		if(errorMessage == null) this.errorMessage = String.format(err, formats);
	}
	
	private <T> CmdArg<T> build(Class<T> clazz){
		CoreLog.debug("Building arg for class: " + clazz.getSimpleName() + " for command: " + command.mainCommand());
		
		CmdArg<T> arg = new CmdArg<>(name, errorMessage, defaultInput, description);
		if(flag == null) command.addArg(arg);
		else flag.setArg(arg);
		return arg;
	}
}
