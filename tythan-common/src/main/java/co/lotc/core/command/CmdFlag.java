package co.lotc.core.command;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import co.lotc.core.agnostic.Sender;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level=AccessLevel.PRIVATE)
@Getter
public class CmdFlag {
	final Set<String> aliases;
	final String permission;
	@Setter CmdArg<?> arg;
	
	private CmdFlag(String name, String pex, String... flagAliases){
		this.permission = pex;
		Set<String> als = new HashSet<>();
		als.add(name);
		als.addAll(Arrays.asList(flagAliases));
		aliases = Collections.unmodifiableSet(als);
	}
	
	public static ArgBuilder make(ArcheCommandBuilder target, String name, String... flagAliases) {
		return make(target, name, null, flagAliases);
	}
	
	public static ArgBuilder make(ArcheCommandBuilder target, String name, String pex, String... flagAliases) {
		CmdFlag flag = new CmdFlag(name, pex, flagAliases);
		if(flag.collidesWithAny(target.flags())) throw new IllegalStateException("Flag aliases are overlapping for command: " + target.mainCommand());
		
		target.addFlag(flag);
		ArgBuilder builder = new ArgBuilder(target, flag).name(name);
		return builder;
	}

	public String getName() {
		return arg.getName();
	}
	
	public boolean needsPermission() {
		return this.permission != null;
	}
	
	public boolean collidesWithAny(List<CmdFlag> flags) {
		return flags.stream().anyMatch(this::collidesWith);
	}
	
	public boolean collidesWith(CmdFlag flag) {
		for(String alias : aliases) {
			if(flag.getAliases().stream().anyMatch(alias::equals)) return true;
		}
		return false;
	}
	
	boolean mayUse(Sender s) {
		return StringUtils.isEmpty(permission) || s.hasPermission(permission);
	}
	
	boolean isVoid() {
		return this.arg instanceof VoidArg;
	}
	
	
}
