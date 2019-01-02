package co.lotc.core.save;

import java.util.Map;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoHandler {
	private final MongoClient client;
	private final String dbName;
	
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
	
	public void insert(String collectionName, Map<String, Object> map) {
		db().getCollection(collectionName).insertOne(new Document(map));
	}
	
	public <T> void insert(String collectionName, Object anyObject) {
		@SuppressWarnings("unchecked")                       //L
		Class<T> o = (Class<T>) anyObject.getClass();        //M
		T any = o.cast(anyObject);                           //A
		db().getCollection(collectionName, o).insertOne(any);//O
	}
	
	
	private MongoDatabase db() {
		return client.getDatabase(dbName);
	}
}
