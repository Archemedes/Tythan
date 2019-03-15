package co.lotc.core.bungee.command;

import java.util.List;

import co.lotc.core.agnostic.Command;
import co.lotc.core.agnostic.PluginOwned;
import lombok.Value;
import net.md_5.bungee.api.plugin.Plugin;

@Value
public class BungeeCommandData implements Command, PluginOwned<Plugin> {
	Plugin plugin;
	String name, permission, description;
	List<String> aliases;
}
