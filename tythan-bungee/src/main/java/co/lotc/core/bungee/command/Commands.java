package co.lotc.core.bungee.command;

import java.util.function.Supplier;

import co.lotc.core.bungee.TythanBungee;
import co.lotc.core.command.AnnotatedCommandParser;
import co.lotc.core.command.ArcheCommandBuilder;
import co.lotc.core.command.CommandTemplate;
import co.lotc.core.command.ParameterType;
import co.lotc.core.command.brigadier.CommandNodeManager;
import co.lotc.core.command.brigadier.Kommandant;
import lombok.var;

public class Commands {

	/**
	 * Creates an ArcheCommandBuilder for you that parses an annotated class into a CommandExecutor
	 * @param command The PluginCommand you intend to wrap
	 * @param template A supplier for the CommandTemplate subclass you wish to use. Should Supply unique instances if intending to use Runnables
	 * @return
	 */
	public static ArcheCommandBuilder builder(BungeeCommandData wrapper, Supplier<CommandTemplate> template) {
		return new AnnotatedCommandParser(template, wrapper).invokeParse((command)->{
			Kommandant kommandant = new BungeeKommandant(command);
			kommandant.addBrigadier();
			CommandNodeManager.getInstance().register(kommandant);
			
			var sc = (BungeeCommandData) wrapper;
			var exec = new BungeeCommandExecutor(command, sc);
			TythanBungee.get().getProxy().getPluginManager().registerCommand(sc.getPlugin(), exec);
		});
	}
	
	/**
	 * Calls {{@link #build(PluginCommand, Supplier)} .build(), finalizing the command build process
	 */
	public static void build(BungeeCommandData command, Supplier<CommandTemplate> template) {
		builder(command, template).build();
	}

	public static <T> ParameterType<T> defineArgumentType(Class<T> forClass){
		return new ParameterType<>(forClass);
	}
	
}
