package co.lotc.core;

import org.apache.commons.lang.StringUtils;

import co.lotc.core.agnostic.Config;
import co.lotc.core.save.MongoHandler;

public class TythanInternals {
	static Tythan INSTANCE = null;
	static MongoHandler mongo;
	
	public static void init(Tythan theOneTrueTythus) {
		if(INSTANCE != null) throw new IllegalStateException("Tythan was already initialized!");
		INSTANCE = theOneTrueTythus;
		CoreLog.set(INSTANCE);
		
		mongo = buildMongoHandler(theOneTrueTythus.config());
	}
	
	public static void shutdown() {
		mongo.close();
	}
	
	private static MongoHandler buildMongoHandler(Config config) {
		String dbName = config.getString("mongo.database");
		String host = config.getString("mongo.host");
		int port = config.getInt("mongo.port");
		
		String username = config.getString("mongo.username");
		String password = config.getString("mongo.password");
		
		if(StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			return new MongoHandler(dbName, host, port);
		} else {
			return new MongoHandler(dbName, host, port, username, password);
		}
	}
	
	private TythanInternals() {throw new UnsupportedOperationException();}
}
