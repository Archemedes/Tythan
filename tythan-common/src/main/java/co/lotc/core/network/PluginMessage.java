package co.lotc.core.network;

import java.io.DataOutputStream;

public interface PluginMessage {
	
	String getChannel();
	
	String getSubchannel();
	
	String getRecipient();
	
	void send();
	
	DataOutputStream getPayload();
}
