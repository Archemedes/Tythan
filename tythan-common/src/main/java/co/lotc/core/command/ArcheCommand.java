package co.lotc.core.command;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import co.lotc.core.agnostic.Sender;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.val;
import lombok.experimental.NonFinal;
import net.lordofthecraft.arche.command.ArcheCommand;
import net.lordofthecraft.arche.command.CmdArg;
import net.lordofthecraft.arche.command.CmdFlag;
import net.lordofthecraft.arche.command.CommandPart;
import net.lordofthecraft.arche.command.HelpCommand;
import net.lordofthecraft.arche.command.JoinedArg;
import net.lordofthecraft.arche.command.RanCommand;

@Value
@NonFinal
public class ArcheCommand {
	String mainCommand;
	Set<String> aliases;
	String description;
	String permission;

	@Getter(AccessLevel.NONE) boolean requirePlayer;
	@Getter(AccessLevel.NONE) boolean requirePersona;

	List<CmdArg<?>> args;
	List<CmdFlag> flags;
	List<ArcheCommand> subCommands;

	@Getter(AccessLevel.NONE) CommandPart sequenceStart;
	
	void execute(RanCommand rc) {
		sequenceStart.execute(rc);
	}
	
	public boolean isEmptyCommand() {
		return sequenceStart == CommandPart.NULL_COMMAND;
	}
	
	public boolean hasArgs() {
		return !args.isEmpty();
	}

	public boolean requiresPlayer() {
		return requirePlayer;
	}
	
	public boolean requiresPersona() {
		return requirePersona;
	}
	
	public boolean hasPermission(Sender s) {
		return StringUtils.isEmpty(permission) || s.hasPermission(permission);
	}
	
	public boolean hasDescription() {
		return StringUtils.isNotEmpty(description);
	}
	
	public boolean isAlias(String param) {
		return aliases.contains(param);
	}
	
	public String getBestAlias(String param) {
		for(String alias : aliases) {
			if(alias.startsWith(param)) return alias;
		}
		return null;
	}
	
	private boolean aliasOverlaps(ArcheCommand other) {
		return aliases.stream().anyMatch(other::isAlias);
	}
	
	private boolean argRangeOverlaps(ArcheCommand other) { //b1 <= a2 && a1 <= b2
		return other.minArgs() <= maxArgs() && minArgs() <= other.maxArgs();
	}
	
	boolean collides(List<ArcheCommand> subbos) {
		return subbos.stream()
		.filter(this::argRangeOverlaps)
		.anyMatch(this::aliasOverlaps);
	}
	
	HelpCommand getHelp() {
		return subCommands.stream()
				.filter(HelpCommand.class::isInstance)
				.map(HelpCommand.class::cast)
				.findAny().orElse(null);
	}
	
	private int minArgs() {
		int i = 0;
		for(val arg : args) {
			if(arg.hasDefaultInput()) return i;
			i++;
		}
		
		return i;
	}
	
	private int maxArgs() {
		int s = args.size();
		if(s > 0 && args.get(s-1) instanceof JoinedArg) return 255;
		else return s;
	}
	
	public boolean fitsArgSize(int argSize) {
		return argSize >= minArgs() && argSize <= maxArgs();
	}
	
	@Override
	public String toString() {
		return "ArcheCommand:" + mainCommand;
	}
	
	
	
}
