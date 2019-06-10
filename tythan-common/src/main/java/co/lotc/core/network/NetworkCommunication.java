package co.lotc.core.network;

public interface NetworkCommunication {
	String SERVER_STRING_ALL = "ALL";
	String SERVER_STRING_ONLINE = "ONLINE";
	String SERVER_STRING_PROXY = "PROXY";
	

	void acquireLock(boolean longTerm);
	
	void relinquishLock(boolean longTerm);
	
	void awaitSignal(String reason, Runnable callback);
	
	void awaitSignal(String server, String reason, Runnable callback);
	
	void processSignal(String server, String reason);
	
}
