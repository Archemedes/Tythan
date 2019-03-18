package co.lotc.core.command;

import static net.md_5.bungee.api.ChatColor.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import co.lotc.core.CoreLog;
import co.lotc.core.agnostic.Sender;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.md_5.bungee.api.chat.BaseComponent;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Getter
public class RanCommand implements CommandHandle {
	public  static final String ERROR_PREFIX = DARK_RED + "Error: " + RED;
	public  static final String ERROR_UNSPECIFIED = " An unhandled error occurred when processing the command.";
	private static final String ERROR_FLAG_ARG = "Not a valid flag argument provided for: " + WHITE;
	private static final String DUPLICATE_FLAG = "You've provided a duplicate flag: " + WHITE;
	private static final String ERROR_SENDER_UNRESOLVED = "This command can only be ran for: " + WHITE;
	
	final ArcheCommand command;
	final String usedAlias;
	
	final Sender sender;
	Object resolvedSender;
	
	List<Object> argResults = Lists.newArrayList();
	Map<String, Object> context = Maps.newHashMap();
	Map<String, Object> flags = Maps.newHashMap();
	
	@SuppressWarnings("unchecked")
	public <T> T getArg(int i) { //Static typing is for PUSSIES
		return (T) argResults.get(i);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getFlag(String flagName) {
		return (T) flags.get(flagName);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getContext(String key) {
		return (T) context.get(key);
	}
	
	@Override
	public boolean hasFlag(String flagName) {
		return flags.containsKey(flagName);
	}
	
	public void addContext(String key, Object value) {
		context.put(key, value);
	}
	
	@Override
	public void msg(String message, Object... format) {
		String formatted = String.format(message, format);
		sender.sendMessage(formatted);
	}
	
	@Override
	public void msg(BaseComponent message) {
		sender.sendMessage(message);
	}
	
	@Override
	public void msg(Object o) {
		msgRaw(String.valueOf(o));
	}
	
	
	public void msgFormat(String message, Object... format) {
		String formatted = String.format(message, format);
		sender.sendMessage(formatted);
	}
	
	@Override
	public void msgRaw(String message) {
		sender.sendMessage(message);
	}
	
	void parseAll(List<String> args) {
			parseFlags(args);
			if(hasFlag("h") && command.getHelp() != null) {
				CoreLog.debug("Found a help flag! No further parsing needed!");
				return;
			}
			parseCommandSender();
			parseArgs(args);
			
			CoreLog.debug("Parsed " + argResults.size() + " args and " + flags.size() + " flags.");
	}
	
	private <S> void parseCommandSender() throws CmdParserException {
		@SuppressWarnings("unchecked") //Always allowed, doesnt confine anything yet
		ParameterType<S> senderType = (ParameterType<S>) command.getSenderType();
		if(senderType != null) {
			if(hasFlag("p")) {
				resolvedSender = getFlag("p");
			} else {
				S resolved = senderType.senderMapper().apply(sender);
				if(resolved != null && (senderType.filter() == null || senderType.filter().test(resolved)) ) {
					resolvedSender = resolved;
					flags.put("p", resolved);
				} else {
					error(ERROR_SENDER_UNRESOLVED + senderType.getDefaultName());
				}
			}
		} else { //No custom sender type required
			resolvedSender = sender;
		}
		Validate.notNull(resolvedSender);
	}
	
	void handleException(Exception e) {
		if(e instanceof CmdParserException) {
			String err = e.getMessage();
			if(StringUtils.isEmpty(err)) CoreLog.info("An empty CmdParserException for command: " + usedAlias + " from " + sender + ". This might be intentional.");
			else msgRaw(ERROR_PREFIX + e.getMessage());
		} else {
			msgRaw(ERROR_PREFIX + ERROR_UNSPECIFIED);
			e.printStackTrace();
		}
	}
	
	private void parseFlags(List<String> args) throws CmdParserException {
	//Father forgive me for I have sinned
		List<CmdFlag> f = command.getFlags().stream().filter(fl->fl.mayUse(sender))
				.collect(Collectors.toCollection(ArrayList::new));
		for(int i = 0; i < args.size(); i++) {
			String a = args.get(i);
			if(a.startsWith("-")) {
				CmdFlag flag = matchFlag(a, f);
				if(flag != null) { //Weird shit happens to i here. Plan accordingly.
					args.remove(i--); //this shortens the list of args. Index shift
					
					if(flag.isVoid()) { //Some flags cant possibly take any arguments. Give special treatment for client flexibility
						putFlag(flag, "I_EXIST");
						continue;
					}
					
					String flagArg;
					if((i+1) < args.size() && !args.get(i+1).startsWith("-")) { //next arg is now under the cursor
						flagArg = args.remove(1+i); //Additional index shift since we took 2 args now
					} else {
						flagArg = flag.getArg().getDefaultInput();
					}
					
					Object resolved = Optional.of(flagArg).map(flag.getArg()::resolve).orElse(null);
					if(resolved == null) error(ERROR_FLAG_ARG + flag.getName());
					else putFlag(flag, resolved);
				}
			}
		}
	}
	
	private void putFlag(CmdFlag flag, Object resolved) {
		if(flags.containsKey(flag.getName())) error(DUPLICATE_FLAG + flag.getName());
		else flags.put(flag.getName(), resolved);
	}
	
	private CmdFlag matchFlag(String input, List<CmdFlag> flags) {
		String xput = input.substring(1).toLowerCase();
		for(CmdFlag flag : flags) {
			boolean flagFound = flag.getAliases().stream().filter(f->f.equals(xput)).findAny().isPresent();
			if(flagFound) return flag;
		}
		
		return null;
	}
	
	private void parseArgs(List<String> args) throws CmdParserException {
		List<CmdArg<?>> cmdArgs = command.getArgs();
		
		HelpCommand help = command.getHelp();
		if(args.size() == 0 && cmdArgs.size() > 0 && help != null) {
			CoreLog.debug("Found 0 args for a command that takes more. Defaulting to help output.");
			flags.put("h", 0);
			return;
		}
		
		for(int i = 0; i < cmdArgs.size(); i++) {
			CmdArg<?> arg = cmdArgs.get(i);
			Object o = null;
			if(i >= args.size()) o = arg.resolveDefault();
			else o = arg.resolve(args, i);
			
			if(o == null) error("at argument " + (i+1) + ": " + arg.getErrorMessage());
			else argResults.add(o);
		}
	}
	
	@Override
	public void error(String err) {
		throw new CmdParserException(err);
	}
	
	@Override
	public void validate(boolean condition, String error) {
		if(!condition) error(error);
	}
	
	static class CmdParserException extends RuntimeException{
		private static final long serialVersionUID = 5283812808389224035L;

		private CmdParserException(String err) {
			super(err);
		}
	}
	
}
