package co.lotc.core;

import java.io.File;
import java.util.logging.Logger;

import co.lotc.core.account.AccountHandler;
import co.lotc.core.agnostic.AbstractChatBuilder;
import co.lotc.core.agnostic.AbstractRun;
import co.lotc.core.agnostic.Config;
import co.lotc.core.save.MongoHandler;

public interface Tythan {
	static Tythan get() { return TythanInternals.INSTANCE; }
  static MongoHandler getMongoHandler() { return TythanInternals.mongo; }
	static AccountHandler getAccountHandler() { return TythanInternals.accounts; }
	
	Logger getLogger();
	
	File getDataFolder();

	boolean isDebugging();
	
	Config config();
	
	AbstractChatBuilder<?> chatBuilder();
	
	AbstractRun<?> run(Object plugin);
}
