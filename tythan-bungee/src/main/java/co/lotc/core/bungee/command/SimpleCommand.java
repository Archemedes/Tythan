package co.lotc.core.bungee.command;

import java.util.List;

import co.lotc.core.agnostic.Command;
import lombok.Value;

@Value
public class SimpleCommand implements Command {
	String name, permission, description;
	List<String> aliases;
}
