package co.lotc.core;

public class TythanProvider {
	static Tythan INSTANCE = null;
	
	public static void init(Tythan theOneTrueTythus) {
		if(INSTANCE != null) throw new IllegalStateException("Tythan was already initialized!");
		INSTANCE = theOneTrueTythus;
		CoreLog.set(INSTANCE);
	}
	
	private TythanProvider() {throw new UnsupportedOperationException();}

}
