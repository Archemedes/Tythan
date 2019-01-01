package co.lotc.core;

import java.io.File;
import java.util.logging.Logger;

public interface Tythan {
	static Tythan get() { return TythanProvider.INSTANCE; }
	
	
	
	Logger getLogger();
	
	File getDataFolder();

	boolean isDebugging();
}
