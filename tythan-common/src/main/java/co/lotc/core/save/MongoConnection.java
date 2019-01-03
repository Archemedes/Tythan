package co.lotc.core.save;

import java.util.Map;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PACKAGE)
public class MongoConnection{
	private final MongoDatabase db;
	
	MongoConnection(MongoClient client, String dbName) {
		db = client.getDatabase(dbName);
	}
	
	public void insert(String collectionName, Map<String, Object> map) {
		db.getCollection(collectionName).insertOne(new Document(map));
	}
	
	public <T> void insert(String collectionName, Object anyObject) {
																								 //L
		@SuppressWarnings("unchecked")               //M
		Class<T> o = (Class<T>) anyObject.getClass();//A
		T any = o.cast(anyObject);                   //O
		db.getCollection(collectionName, o).insertOne(any);
	}
	
	public void delete(String collectionName, Map<String, Object> map) {
		db.getCollection(collectionName).deleteMany(new Document(map));
	}
	
	public void update(String collectionName, Map<String, Object> criteria, Map<String, Object> map) {
		db.getCollection(collectionName).updateMany(new Document(criteria), new Document(map));
	}
	
}
