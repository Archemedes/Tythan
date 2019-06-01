package co.lotc.core.save;

import java.util.Map;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.var;

@Getter(AccessLevel.PACKAGE)
public class MongoConnection implements AutoCloseable{
	private final MongoDatabase database;
	private final ClientSession session;
	
	MongoConnection(MongoClient client, String dbName, CodecRegistry codecs) {
		database = client.getDatabase(dbName).withCodecRegistry(codecs);
		session = client.startSession();
		session.startTransaction();
	}

	public void index(String collectionName, boolean unique, String... variables) {
		
		Document doc = new Document();
		for(var var : variables) {
			doc.append(var, 1);
		}
		database.getCollection(collectionName).createIndex(doc, new IndexOptions().unique(unique));
	}
	
	public void insert(String collectionName, Map<String, Object> map) {
		database.getCollection(collectionName).insertOne(session, new Document(map));
	}
	
	public void delete(String collectionName, Map<String, Object> map) {
		database.getCollection(collectionName).deleteMany(session, new Document(map));
	}
	
	public void update(String collectionName, Map<String, Object> criteria, Map<String, Object> map) {
		database.getCollection(collectionName).updateMany(session, new Document(criteria), new Document(map));
	}

	@Override
	public void close(){
		session.commitTransaction();
		session.close();
	}
	
}
