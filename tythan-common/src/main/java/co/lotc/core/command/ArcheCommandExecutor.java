package co.lotc.core.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.google.common.collect.Lists;

import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.command.ArcheCommand;
import net.lordofthecraft.arche.command.HelpCommand;
import net.lordofthecraft.arche.command.RanCommand;
import net.lordofthecraft.arche.interfaces.OfflinePersona;

@RequiredArgsConstructor
public class ArcheCommandExecutor implements TabExecutor {
	private final ArcheCommand rootCommand;
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command $, String label, String[] args) {
		List<String> listArgs = new ArrayList<>();
		for (String arg : args) listArgs.add(arg);
		runCommand(sender, rootCommand, label, listArgs);
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command $, String alias, String[] args) {
		try{List<String> listArgs = new ArrayList<>();
			for (String arg : args) listArgs.add(arg);
			return getCompletions(sender, rootCommand, listArgs);
		} catch(ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return Lists.newArrayList();
		}
	}
	
	private List<String> getCompletions(CommandSender sender, ArcheCommand command, List<String> args) {
		ArcheCommand subCommand = wantsSubCommand(command, args);
		if(subCommand != null && subCommand.hasPermission(sender)) {
			args.remove(0);
			return getCompletions(sender, subCommand, args);
		} else {
			List<String> options;
			if(args.isEmpty()) return Lists.newArrayList();
			int index = args.size() - 1;
			String last = args.get(index).toLowerCase();
			if(args.size() == 1) options = subCompletions(sender, command, last);
			else options = new ArrayList<>();
			
			if(index < command.getArgs().size()) command.getArgs().get(index).getCompleter().get().forEach(options::add);
			
			return options.stream().filter(s->s.toLowerCase().startsWith(last)).collect(Collectors.toList());
		}
	}

	private void runCommand(CommandSender sender, ArcheCommand command, String usedAlias, List<String> args) {
		ArcheCommand subCommand = wantsSubCommand(command, args);
		CoreLog.debug("catching alias " + usedAlias + ". SubCommand found: " + subCommand);
		if(!args.isEmpty()) CoreLog.debug("These are its arguments: " + StringUtils.join(args, ", "));
		if(subCommand != null) {
			runSubCommand(sender, subCommand, usedAlias, args);
		} else if (!command.hasPermission(sender)) {
			sender.sendMessage(RanCommand.ERROR_PREFIX + "You do not have permission to use this");
		} else {
			RanCommand c = new RanCommand(command, usedAlias, sender);
			
			try{
				c.parseAll(args);
			} catch(Exception e) {
				c.handleException(e);
				return;
			}
			
			HelpCommand help = command.getHelp();
			if(help != null && c.hasFlag("h")) {
				help.runHelp(c, c.getFlag("h"));
			} else if(command.requiresPersona() && c.getPersona() == null){
				//The case where a persona flag was offered, meaning the command is not YET in error.
				OfflinePersona pers = c.getFlag("p");
				getPersonaAndExecute(sender, command, c, args, pers);
			} else {
				executeCommand(command, c);
			}
		}
	}
	
	private void runSubCommand(CommandSender sender, ArcheCommand subCommand, String usedAlias, List<String> args) {
		String usedSubcommandAlias = args.remove(0).toLowerCase();
		String newAlias = usedAlias + ' ' + usedSubcommandAlias;
		runCommand(sender, subCommand, newAlias, args);
	}
	
	private void executeCommand(ArcheCommand command, RanCommand c) {
		command.execute(c);
	}
	
	private void getPersonaAndExecute( CommandSender sender, ArcheCommand ac, RanCommand c, List<String> args, OfflinePersona o) {
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Persona not loaded. Will try to load it now. Please wait...");
		o.load().then(persona->{
      c.rectifySenders(persona);
      executeCommand(ac, c);
		});
	}
	
	private List<String> subCompletions(CommandSender sender, ArcheCommand cmd, String argZero){
		List<String> result = new ArrayList<>();
		String lower = argZero.toLowerCase();
		cmd.getSubCommands().stream()
			.filter(s->s.hasPermission(sender))
			.map(s->s.getBestAlias(lower))
			.filter(Objects::nonNull)
			.forEach(result::add);

		return result;
	}
	
	private ArcheCommand wantsSubCommand(ArcheCommand cmd, List<String> args) {
		if(args.isEmpty()) return null;
		String subArg = args.get(0).toLowerCase();
		
		List<ArcheCommand> matches = new ArrayList<>();
		cmd.getSubCommands().stream()
		  .filter(s->s.isAlias(subArg))
		  .forEach(matches::add);
		if(matches.isEmpty()) return null;
		else if(matches.size() == 1) return matches.get(0);
		
		int s = args.size() - 1;
		for(ArcheCommand match : matches) {
			if(match.fitsArgSize(s)) return match;
		}
		
		//Fallback, no subcommand wants the amount of given arguments
		return null;
	}


	
}
