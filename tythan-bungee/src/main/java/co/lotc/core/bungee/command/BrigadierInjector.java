package co.lotc.core.bungee.command;

import co.lotc.core.command.brigadier.CommandNodeManager;
import de.exceptionflug.protocolize.api.event.PacketReceiveEvent;
import de.exceptionflug.protocolize.api.handler.PacketAdapter;
import de.exceptionflug.protocolize.api.protocol.Stream;
import lombok.var;
import net.md_5.bungee.protocol.packet.Commands;

public class BrigadierInjector extends PacketAdapter<Commands> {
	
	public BrigadierInjector() {
		super(Stream.DOWNSTREAM, Commands.class);
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void receive(final PacketReceiveEvent<Commands> event) {
		var cmds = event.getPacket();
		var rootNode = cmds.getRoot();
		CommandNodeManager.getInstance().inject(rootNode);
	}

}
