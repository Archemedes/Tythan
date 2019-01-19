package co.lotc.core;

import java.io.File;
import java.util.logging.Logger;

import co.lotc.core.agnostic.AbstractChatBuilder;
import co.lotc.core.agnostic.Config;

public interface Tythan {
	static Tythan get() { return TythanProvider.INSTANCE; }
	
	Logger getLogger();
	
	File getDataFolder();

	boolean isDebugging();
	
	Config config();
	
	AbstractChatBuilder<?> chatBuilder();
}
