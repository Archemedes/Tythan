package co.lotc.core.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang.Validate;

import co.lotc.core.CoreLog;
import co.lotc.core.agnostic.Command;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

//We're reaching levels of Telanir that shouldn't be even possible
@Accessors(fluent=true)
public class ArcheCommandBuilder {
	private final BiConsumer<Command, ArcheCommand> registrationHandler;
	private final ArcheCommandBuilder parentBuilder;
	private final Command command;
	
	@Getter private final String mainCommand;
	@Setter private String description;
	@Setter private String permission;

	@Getter private ParameterType<?> senderType = null;
	
	@Getter(AccessLevel.PACKAGE) private final Set<String> aliases = new HashSet<>();
	@Getter(AccessLevel.PACKAGE) private final List<CmdArg<?>> args = new ArrayList<>();
	@Getter(AccessLevel.PACKAGE) private final List<CmdFlag> flags = new ArrayList<>();
	@Getter(AccessLevel.PACKAGE) private final List<ArcheCommand> subCommands = new ArrayList<>();
	
	@Setter private Consumer<RanCommand> payload = null;
	
	//Builder state booleans
	boolean argsHaveDefaults = false; //When arg is added that has default input
	boolean noMoreArgs = false; //When an unity argument is used
	boolean buildHelpFile = true;
	
	
	public ArcheCommandBuilder(BiConsumer<Command, ArcheCommand> registration, Command command) {
		registrationHandler = registration;
		parentBuilder = null;
		this.command = command;
		
		this.mainCommand = command.getName();
		this.description = command.getDescription();
		this.permission = command.getPermission();
		
		command.getAliases().stream().map(String::toLowerCase).forEach(aliases::add);
		aliases.add(command.getName().toLowerCase());
	}
	
	ArcheCommandBuilder(ArcheCommandBuilder dad, String name, boolean inheritOptions){
		registrationHandler = null;
		parentBuilder = dad;
		command = dad.command;
		this.mainCommand = name;
		this.permission = dad.permission;
		aliases.add(name.toLowerCase());
		if(inheritOptions) {
			this.buildHelpFile = dad.buildHelpFile;
		}
	}
	
	public ArcheCommandBuilder subCommand(String name) {
		return subCommand(name, true);
	}
	
	public ArcheCommandBuilder subCommand(String name, boolean inheritOptions) {
		return new ArcheCommandBuilder(this, name, inheritOptions);
	}
	
	public ArgBuilder arg(String name) {
		return arg().name(name);
	}
	
	public ArgBuilder arg() {
		if(noMoreArgs) throw new IllegalStateException("This command cannot accept additional arguments.");
		return new ArgBuilder(this);
	}
	
	void addArg(CmdArg<?> arg) {
		if(arg.hasDefaultInput()) argsHaveDefaults = true;
		else if(argsHaveDefaults) throw new IllegalStateException("For command" + this.mainCommand + ": argument at " + (args.size()-1) + " had no default but previous arguments do");
		args.add(arg);
	}
	
	public ArgBuilder flag(String name, String... aliases) {
		Validate.isTrue(!"p".equals(name), "The flag name 'p' is reserved for custom sender args");
		return CmdFlag.make(this, name, aliases);
	}
	
	public ArgBuilder restrictedFlag(String name, String pex, String... aliases) {
		Validate.isTrue(!"p".equals(name), "The flag name 'p' is reserved for custom sender args");
		return CmdFlag.make(this, name, pex, aliases);
	}
	
	void addFlag(CmdFlag flag) {
		flags.add(flag);
	}
	
	public ArcheCommandBuilder alias(String... aliases) {
		for(String alias : aliases) this.aliases.add(alias.toLowerCase());
		return this;
	}
	
	public boolean requiresSender() {
		return senderType != null;
	}
	
	public ArcheCommandBuilder requiresSender(Class<?> senderClass) {
		if(requiresSender()) throw new IllegalStateException("Specified sender argument twice for command " + this.mainCommand());
		
		if(ParameterType.senderTypeExists(senderClass)) {
			senderType = ParameterType.getCustomType(senderClass);
			
			CoreLog.debug("cmd " + mainCommand() + " requires as its sender: " + senderClass.getSimpleName());
			if(senderType.mapper() != null) { //Can also replace the sender as Console with -p flag
				ArgBuilder b = CmdFlag.make(this, "p", "archecore.mod", new String[0]);
				b.asType(senderType.getTargetType());
			}
		} else {
			throw new IllegalStateException("This class cannot be used as a command sender: " + senderClass.getSimpleName());
		}
		
		return this;
	}
	
	public ArcheCommandBuilder noHelp() {
		buildHelpFile = false;
		return this;
	}
		
	public ArcheCommandBuilder build() {
		boolean noneSpecified = payload == null;
		if(noneSpecified) {
			if(!args.isEmpty() || subCommands.isEmpty())
				throw new IllegalStateException("Found no execution sequence for command: " + this.mainCommand
						+ ". This is only possible if the command has subcommands and no arguments specified."
						+ " It is VERY likely the command was built incorrectly.");
			payload = ArcheCommand.NULL_COMMAND;
		}
		
		CoreLog.debug("Now Building ArcheCommand: " + mainCommand + " it has " + subCommands.size()
			+ " subcommands and parent: " +(parentBuilder == null? "none":parentBuilder.mainCommand));
		ArcheCommand built = new ArcheCommand(
				mainCommand,
				Collections.unmodifiableSet(aliases),
				description,
				permission,
				senderType,
				Collections.unmodifiableList(args),
				Collections.unmodifiableList(flags),
				Collections.unmodifiableList(subCommands),
				payload);
		
		if(parentBuilder != null) {
			if(built.collides(parentBuilder.subCommands))
				throw new IllegalStateException("Detected ambiguous subcommand: "
			  + built.getMainCommand() + ". Aliases and argument range overlap with other commands!");
			parentBuilder.subCommands.add(built);
		}
		
		if(buildHelpFile) {
			HelpCommand help = new HelpCommand(built);
			this.subCommands.add(help);
			if(noneSpecified) payload = c->help.runHelp(c, 0);
			flag("h").description("Get help and subcommands").defaultInput("0").asInt();
		}
		
		//If there's no more builders up the chain we've reached the top. Means we're done and we can make an executor
		if(parentBuilder == null) registrationHandler.accept(command, built);
		
		return parentBuilder;
	}
}