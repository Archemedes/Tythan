package co.lotc.core.bungee.command;

import java.util.ArrayList;
import java.util.List;

import co.lotc.core.agnostic.Command;
import co.lotc.core.agnostic.PluginOwned;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.md_5.bungee.api.plugin.Plugin;

@Value
@AllArgsConstructor
public class BungeeCommandData implements Command, PluginOwned<Plugin> {
	Plugin plugin;
	String name, permission, description;
	List<String> aliases;
	
	public BungeeCommandData(Plugin plugin, String name) {
		this(plugin, name, null);
	}
	
	public BungeeCommandData(Plugin plugin, String name, String permission) {
		this(plugin, name, permission, null);
	}
	
	public BungeeCommandData(Plugin plugin, String name, String permission, String description) {
		this(plugin, name, permission, description, new ArrayList<>());
	}
}
