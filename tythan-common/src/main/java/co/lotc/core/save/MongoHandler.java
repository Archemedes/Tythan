package co.lotc.core.save;

import java.util.ArrayList;
import java.util.List;

import org.bson.codecs.Codec;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MongoHandler {
	private final String dbName;
	private final MongoClient client;
	
	private final List<Codec<?>> codecs = new ArrayList<>();
	
	public MongoHandler(String dbName) {
		this.dbName = dbName;
		client = MongoClients.create();
	}
	
	public MongoHandler(String dbName, String ip, int port) {
		this.dbName = dbName;
		client = MongoClients.create(new ConnectionString("mongodb://"+ip+":"+port));
	}
	
	public MongoHandler(String dbName, String ip, int port, String username, String password) {
		this.dbName = dbName;
		client = MongoClients.create(MongoClientSettings.builder()
				.credential(MongoCredential.createCredential(username, ip, password.toCharArray()))
				.applyConnectionString(new ConnectionString("mongodb://"+ip+":"+port))
				.build()
				);
	}

	public MongoHandler withDatabase(String databaseName) {
		return new MongoHandler(databaseName,client);
	}
	
	public MongoConnection connect() {
		return new MongoConnection(client, dbName);
	}
}
