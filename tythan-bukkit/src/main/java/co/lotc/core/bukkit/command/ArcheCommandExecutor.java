package co.lotc.core.bukkit.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import co.lotc.core.bukkit.wrapper.BukkitSender;
import co.lotc.core.command.AgnosticExecutor;
import co.lotc.core.command.ArcheCommand;

public class ArcheCommandExecutor extends AgnosticExecutor implements TabExecutor {

	public ArcheCommandExecutor(ArcheCommand rootCommand) {
		super(rootCommand);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		BukkitSender bukkitSender = new BukkitSender(sender);
		return super.onTabComplete(bukkitSender, alias, args);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		BukkitSender bukkitSender = new BukkitSender(sender);
		return super.onCommand(bukkitSender, label, args);
	}

}
