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
		//addProperSerializers();
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void receive(final PacketReceiveEvent<Commands> event) {
		var cmds = event.getPacket();
		var rootNode = cmds.getRoot();
		CommandNodeManager.getInstance().inject(rootNode);
	}

/*	@SuppressWarnings("unchecked")
	public void addProperSerializers(){
		for(val clazz : Commands.class.getDeclaredClasses()) {
			if(clazz.getSimpleName().equals("ArgumentRegistry")) {
				@SuppressWarnings("rawtypes")
				Map map = reflect(clazz, "PROPER_PROVIDERS");
				
				map.put(IntegerArgumentType.class, reflect(clazz, "INTEGER"));
				map.put(DoubleArgumentType.class, reflect(clazz, "DOUBLE"));
				map.put(BoolArgumentType.class, reflect(clazz, "BOOLEAN"));
				map.put(FloatArgumentType.class, reflect(clazz, "FLOAT"));
				return;
			}
		}
		
		CoreLog.severe("Couldn't find the ArgumentRegistry in Bungee's Commands packet wrapper");
	}
	
	@SneakyThrows
	@SuppressWarnings("unchecked")
	private <T> T reflect(Class<?> clazz, String name) {
			Field f = clazz.getDeclaredField(name);
			f.setAccessible(true);
			return (T) f.get(null);
	}*/
}
