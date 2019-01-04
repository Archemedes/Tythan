package co.lotc.core.save;

import java.util.Map;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.var;

@Getter(AccessLevel.PACKAGE)
public class MongoConnection{
	private final MongoDatabase db;
	
	MongoConnection(MongoClient client, String dbName) {
		db = client.getDatabase(dbName);
	}

	public void index(String collectionName, boolean unique, String... variables) {
		
		Document doc = new Document();
		for(var var : variables) {
			doc.append(var, 1);
		}
		db.getCollection(collectionName).createIndex(doc, new IndexOptions().unique(unique));
	}
	
	public void insert(String collectionName, Map<String, Object> map) {
		db.getCollection(collectionName).insertOne(new Document(map));
	}
	
	public void delete(String collectionName, Map<String, Object> map) {
		db.getCollection(collectionName).deleteMany(new Document(map));
	}
	
	public void update(String collectionName, Map<String, Object> criteria, Map<String, Object> map) {
		db.getCollection(collectionName).updateMany(new Document(criteria), new Document(map));
	}
	
}
