package co.lotc.core.bungee.command;

import co.lotc.core.bungee.wrapper.BungeeSender;
import co.lotc.core.command.AgnosticExecutor;
import co.lotc.core.command.ArcheCommand;
import lombok.var;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public final class BungeeCommandExecutor extends Command implements TabExecutor {
	private final AgnosticExecutor delegate;
	
	public BungeeCommandExecutor(ArcheCommand command, BungeeCommandData data) {
		super(data.getName(), data.getPermission(), data.getAliases().toArray(new String[0]));
		delegate = new AgnosticExecutor(command);
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		var bungeeSender = new BungeeSender(sender);
		return delegate.onTabComplete(bungeeSender, getName(), args);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		var bungeeSender = new BungeeSender(sender);
		delegate.onCommand(bungeeSender, getName(), args);
	}

}
