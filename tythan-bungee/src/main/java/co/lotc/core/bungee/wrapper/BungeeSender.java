package co.lotc.core.bungee.wrapper;

import co.lotc.core.agnostic.AgnosticObject;
import co.lotc.core.agnostic.Sender;
import lombok.experimental.Delegate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.CommandSender;

@RequiredArgsConstructor
public class BungeeSender implements Sender, AgnosticObject<CommandSender> {
	
	@Getter
	@Delegate(types=Sender.class)
	private final CommandSender handle;
	
}
