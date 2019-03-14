package co.lotc.core.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import co.lotc.core.CoreLog;
import co.lotc.core.agnostic.Command;
import co.lotc.core.agnostic.Sender;
import co.lotc.core.command.RanCommand.CmdParserException;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
import co.lotc.core.command.annotate.Flag;
import co.lotc.core.command.annotate.Joined;
import co.lotc.core.command.annotate.Range;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.var;

@RequiredArgsConstructor
public class AnnotatedCommandParser {
	private final Supplier<CommandTemplate> template;
	private final Command bukkitCommand;
	
	public ArcheCommandBuilder invokeParse() {
		ArcheCommandBuilder acb = CommandHandle.builder(bukkitCommand);
		return parse(template, acb);
	}
	
	private ArcheCommandBuilder parse(Supplier<CommandTemplate> template, ArcheCommandBuilder acb) {
		Class<? extends CommandTemplate> c = template.get().getClass();
		
		addInvoke(c, template, acb);
		
		var cmds = Stream.of(c.getMethods()).filter(m->m.isAnnotationPresent(Cmd.class)).collect(Collectors.toList());
		
		for(Method method : cmds) checkForSubLayer(method, template, acb); //Note this recurses
		for(Method method : cmds) parseCommand(method, template, acb);
		
		return acb; //this::parse is recursive. So is ArcheCommandBuilder::build. Perfect synergy :)
	}
	
	private void addInvoke(Class<? extends CommandTemplate> c, Supplier<CommandTemplate> template, ArcheCommandBuilder acb) {
		//This breaks polymorphism but whatever
		for(Method m : c.getDeclaredMethods()) {
			if(m.getParameterCount() > 0 && Modifier.isPublic(m.getModifiers()) && m.getName().equals("invoke") && m.getReturnType() == Void.TYPE) {
				//This is a invoke method declared in the class, assumed this is what we want for the default invocation of the command
				//Due to logic of the ArcheCommandExecutor this still makes the no-argument default a help command
				parseCommandMethod(m, template, acb);
				return;
			}
		}
		
		//Fallback option, which does use polymorphism, specifically the CommandTemplate.invoke() method
		acb.run(rc->{ //This is default behavior when no arguments are given, usually refers to help file
			CommandTemplate t = template.get();
			t.setRanCommand(rc);
			t.invoke();
		});
	}
	
	private ArcheCommandBuilder constructSubBuilder(Method method, ArcheCommandBuilder parent) {
		String name = method.getName();
		
		Cmd anno = method.getAnnotation(Cmd.class);
		String desc = anno.value();
		String pex = anno.permission();
		
		var result = parent.subCommand(name, false);
		if(desc !=  null) result.description(desc);
		if(StringUtils.isNotEmpty(pex)) result.permission(pex);
		
		return result;
	}
	
	@SneakyThrows
	private void checkForSubLayer(Method method, Supplier<CommandTemplate> template, ArcheCommandBuilder acb) {
		if(!CommandTemplate.class.isAssignableFrom(method.getReturnType())) return;
		if(method.getParameterCount() > 0) throw new IllegalStateException("Methods returning CommandTemplate can't also have parameters");
		if(method.getName().equals("invoke")) throw new IllegalArgumentException("Don't annotate your invoke() methods. The method name is reserved!");
		
		ArcheCommandBuilder subbo = constructSubBuilder(method, acb);
		Supplier<CommandTemplate> chained = ()-> chainSupplier(method, template);
		parse(chained, subbo).build(); //We need to go deeper
	}
	
	@SneakyThrows
	private CommandTemplate chainSupplier(Method templateGetter, Supplier<CommandTemplate> theOldSupplier) {
		//Makes a NEW supplier which invokes the old supplier (which is one higher in the chain)
		//The supplied CommandTemplate has a particular method called via reflection
		//A method which we know to return CommandTemplate (checked above), so we cast it
		//This supplier is then used for subsequent checking.
		//Yes this is an abysmal piece of code. Let's never speak of it.
		return (CommandTemplate) templateGetter.invoke(theOldSupplier.get());
	}
	
	private void parseCommand(Method method, Supplier<CommandTemplate> template, ArcheCommandBuilder acb) {
		if(method.getReturnType() != Void.TYPE) return;
		if(method.getName().equals("invoke")) throw new IllegalArgumentException("Don't annotate your invoke() methods. The method name is reserved!");
		
		var subbo = constructSubBuilder(method, acb);
		parseCommandMethod(method, template, subbo);
		subbo.build();
	}

	private void parseCommandMethod(Method method, Supplier<CommandTemplate> template, ArcheCommandBuilder acb) {
		var flagsAnno = method.getAnnotation(Flag.List.class);
		if(flagsAnno != null) for(Flag flag : flagsAnno.value()) addFlag(acb, flag);
		else if(method.isAnnotationPresent(Flag.class)) addFlag(acb, method.getAnnotation(Flag.class));
		
		var params = method.getParameters();
		boolean wantsSenderAsFirstArg = false;
		for (int i = 0; i < params.length; i++) {
			var param = params[i];
			var c = param.getType();
			CoreLog.debug("Param " + i + " in method " + method.getName() + " has type " + c);
			
			if(i == 0) {
				//The first argument MIGHT be a sender argument, and often is
				//but it does not necessarily NEED to be... thus we check
				//If a SenderTemplate is registered, we go forward
				
				if(TypeRegistry.forSenders().customTypeExists(c)) {
					//TODO: Cast Sender as this?
					continue;
				} else if( Sender.class.isAssignableFrom(c)) {
					CoreLog.debug("Method " + method.getName() + " for cmd " + acb.mainCommand() + " has explicit sender arg");
					wantsSenderAsFirstArg = true;
					continue;
				}
			}
			
			var argAnno = param.getAnnotation(Arg.class);
			ArgBuilder arg = argAnno == null? acb.arg() : acb.arg(argAnno.value());
			if(argAnno != null && !argAnno.description().isEmpty()) arg.description(argAnno.description());
			
			Default defaultInput = param.getAnnotation(Default.class);
			if(defaultInput != null) {
				String def = defaultInput.value();
				Validate.notNull(def);
				arg.defaultInput(def);
			}
			
			if(param.isAnnotationPresent(Joined.class)) {
				if(param.getType() == String.class) arg.asJoinedString();
				else throw new IllegalArgumentException("All JoinedString annotations must affect a String type parameter");
			} else if (param.isAnnotationPresent(Range.class)) {
				Range rangeInput = param.getAnnotation(Range.class);
				//Retarded magical numbers bs is here. Sorry.
				boolean hasMin = rangeInput.min() != Integer.MIN_VALUE;
				boolean hasMax = rangeInput.max() != Integer.MAX_VALUE;
				if(c == int.class || c == Integer.class) {
					if(hasMin && hasMax) arg.asInt((int)rangeInput.min(), (int)rangeInput.max());
					else if(hasMin && !hasMax) arg.asInt((int) rangeInput.min());
					else throw new IllegalArgumentException("Use @Range by specifying either a min or a min and max");
				} else if(c == float.class || c == Float.class) {
					if(hasMin && hasMax) arg.asFloat((float)rangeInput.min(), (float) rangeInput.max());
					else if(hasMin && !hasMax) arg.asFloat((float) rangeInput.min());
					else throw new IllegalArgumentException("Use @Range by specifying either a min or a min and max");
				} else if(c == double.class || c == Double.class) {
					if(hasMin && hasMax) arg.asDouble(rangeInput.min(), rangeInput.max());
					else if(hasMin && !hasMax) arg.asDouble( rangeInput.min());
					else throw new IllegalArgumentException("Use @Range by specifying either a min or a min and max");
				} else {
					throw new IllegalArgumentException("Use @Range annotation only on integer, float or double!");
				}
			} else {
				arg.asType(c);
			}
		}
		
		makeCommandDoStuff(template, acb, method, wantsSenderAsFirstArg);
	}

	private void makeCommandDoStuff(Supplier<CommandTemplate> template, ArcheCommandBuilder acb, Method method, boolean wantsCommandSenderAsFirstArg) {
		//Make command actually do stuff
		acb.run(rc->{
			try {
				CommandTemplate t = template.get();
				t.setRanCommand(rc);
				Object[] args = rc.getArgResults().toArray();
				
				if(false /* TODO check if custom sender is required*/) {
					Object[] newArgs = insertFirst(args, rc.getCommand().requiresPersona()? rc.getPersona() : rc.getPlayer());
					method.invoke(t, newArgs);
				} else if (wantsCommandSenderAsFirstArg) {
					Object[] newArgs = insertFirst(args, rc.getSender());
					method.invoke(t, newArgs);
				}else {
					method.invoke(t, args);
				}
			} catch (InvocationTargetException ite) {
				if(ite.getCause() instanceof CmdParserException) {
					rc.error(ite.getCause().getMessage());
				} else {
					ite.printStackTrace();
					rc.error("An unhandled exception occurred. Contact a developer.");
				}
			} catch (Exception e) {
				e.printStackTrace();
				rc.error("An unhandled exception occurred. Contact a developer.");
			}
		});
	}
	
	private Object[] insertFirst(Object[] args, Object toAdd) {
		Object[] newArgs = new Object[args.length+1];
		System.arraycopy(args, 0, newArgs, 1, args.length);
		newArgs[0] = toAdd;
		return newArgs;
	}
	
	private void addFlag(ArcheCommandBuilder acb, Flag flag) {
		ArgBuilder flarg;
		
		String pex = flag.permission();
		if(!pex.isEmpty()) flarg = acb.restrictedFlag(flag.name(), pex, flag.aliases());
		else flarg = acb.flag(flag.name(), flag.aliases());
		
		String desc = flag.description();
		if(!desc.isEmpty()) flarg.description(desc);

		flarg.asType(flag.type());
	}
}
