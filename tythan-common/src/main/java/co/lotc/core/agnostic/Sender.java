package co.lotc.core.agnostic;

import java.util.stream.Stream;

import net.md_5.bungee.api.chat.BaseComponent;

public interface Sender {

	boolean hasPermission(String perm);
	
	String getName();
	
	void sendMessage(String msg);
	
	void sendMessage(BaseComponent msg);
	
	default void sendMessage(BaseComponent... msg) {
		Stream.of(msg).forEach(this::sendMessage);
	}
	
}
