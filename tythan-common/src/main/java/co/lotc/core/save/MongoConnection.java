package co.lotc.core.save;

import java.util.Map;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

import lombok.Getter;

public class MongoConnection implements AutoCloseable{
	@Getter private final MongoDatabase database;
	
	MongoConnection(MongoClient client, String dbName, CodecRegistry codecs) {
		database = client.getDatabase(dbName).withCodecRegistry(codecs);
	}

	public MongoCollectionBuilder collection(String name) {
		return new MongoCollectionBuilder(this, name);
	}
	
	public void insert(String collectionName, Map<String, Object> map) {
		database.getCollection(collectionName).insertOne(new Document(map));
	}
	
	public void replace(String collectionName, Map<String, Object> criteria, Map<String, Object> map) {
		database.getCollection(collectionName).replaceOne(new Document(criteria), new Document(map), new ReplaceOptions().upsert(true));
	}
	
	public void delete(String collectionName, Map<String, Object> map) {
		database.getCollection(collectionName).deleteMany(new Document(map));
	}
	
	public void update(String collectionName, Map<String, Object> criteria, Map<String, Object> map) {
		database.getCollection(collectionName).updateMany(new Document(criteria), new Document(map));
	}

	@Override
	public void close(){
		
	}
	
}
