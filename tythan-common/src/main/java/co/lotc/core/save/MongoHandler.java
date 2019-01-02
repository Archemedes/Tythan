package co.lotc.core.save;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoHandler {

	private final MongoClient client;
	
	public MongoHandler() {
		client = MongoClients.create();
	}
	
	public MongoHandler(String ip, int port) {
		client = MongoClients.create(new ConnectionString("mongodb://"+ip+":"+port));
	}
	
	public MongoHandler(String ip, int port, String username, String password) {
		client = MongoClients.create(MongoClientSettings.builder()
				.credential(MongoCredential.createCredential(username, ip, password.toCharArray()))
				.applyConnectionString(new ConnectionString("mongodb://"+ip+":"+port))
				.build()
				);
	}
	
}
