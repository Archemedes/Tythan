package co.lotc.core.bukkit.wrapper;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import co.lotc.core.agnostic.AgnosticObject;
import co.lotc.core.agnostic.Command;
import co.lotc.core.agnostic.PluginOwned;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class BukkitCommand implements AgnosticObject<PluginCommand>, Command, PluginOwned<Plugin> {

	@Getter
	@Delegate(types=Command.class,excludes=CommandSender.Spigot.class)
	private final PluginCommand handle;

	@Override
	public Plugin getPlugin() {
		return handle.getPlugin();
	}
}
