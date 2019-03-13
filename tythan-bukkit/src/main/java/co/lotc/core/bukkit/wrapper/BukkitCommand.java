package co.lotc.core.bukkit.wrapper;


import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import co.lotc.core.agnostic.AgnosticObject;
import co.lotc.core.agnostic.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class BukkitCommand implements AgnosticObject<PluginCommand>, Command {

	@Getter
	@Delegate(types=Command.class,excludes=CommandSender.Spigot.class)
	private final PluginCommand handle;

}
