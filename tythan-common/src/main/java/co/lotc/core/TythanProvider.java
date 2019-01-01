package co.lotc.core;

public class TythanProvider {
	static Tythan INSTANCE;
	
	public static void init(Tythan theOneTrueTythus) {
		INSTANCE = theOneTrueTythus;
	}
	
	
	private TythanProvider() {throw new UnsupportedOperationException();}

}
