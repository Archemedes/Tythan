package co.lotc.core;

import co.lotc.core.agnostic.Config;
import co.lotc.core.save.MongoHandler;

public class TythanInternals {
	static Tythan INSTANCE = null;
	
	public static void init(Tythan theOneTrueTythus) {
		if(INSTANCE != null) throw new IllegalStateException("Tythan was already initialized!");
		INSTANCE = theOneTrueTythus;
		CoreLog.set(INSTANCE);
	}
	
	
	public static MongoHandler makeHandler(Config config) {
		
	}
	
	private TythanInternals() {throw new UnsupportedOperationException();}
}
