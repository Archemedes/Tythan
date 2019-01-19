package co.lotc.core.bukkit.wrapper;

import org.bukkit.command.CommandSender;

import co.lotc.core.agnostic.AgnosticObject;
import co.lotc.core.agnostic.Sender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import net.md_5.bungee.api.chat.BaseComponent;

@RequiredArgsConstructor
public class BukkitSender implements Sender,AgnosticObject<CommandSender> {
	
	@Getter
	@Delegate(types=Sender.class,excludes=CommandSender.Spigot.class)
	private final CommandSender handle;

	@Override
	public void sendMessage(BaseComponent msg) {
		handle.spigot().sendMessage(msg);
	}

	@Override
	public void sendMessage(BaseComponent... msg) {
		handle.spigot().sendMessage(msg);
	}
}
