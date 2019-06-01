package co.lotc.core;

import java.io.File;
import java.util.logging.Logger;

import com.mongodb.client.MongoClient;

import co.lotc.core.account.AccountHandler;
import co.lotc.core.agnostic.AbstractChatBuilder;
import co.lotc.core.agnostic.Config;
import co.lotc.core.save.MongoHandler;

public interface Tythan {
	static Tythan get() { return TythanInternals.INSTANCE; }
	
	Logger getLogger();
	
	File getDataFolder();

	boolean isDebugging();
	
	Config config();
	
	MongoHandler getMongoHandler();
	
	AccountHandler getAccountHandler();
	
	AbstractChatBuilder<?> chatBuilder();
}
