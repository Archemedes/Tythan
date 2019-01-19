package co.lotc.core.agnostic;

import net.md_5.bungee.api.chat.BaseComponent;

public interface Sender {

	boolean hasPermission(String perm);
	
	String getName();
	
	void sendMessage(String msg);
	
	void sendMessage(BaseComponent msg);
	
	void sendMessage(BaseComponent... msg);
	
	
}
