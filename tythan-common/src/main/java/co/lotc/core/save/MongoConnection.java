package co.lotc.core.save;

import java.io.Closeable;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoConnection implements Closeable {
	private final MongoDatabase db;
	private final ClientSession session;
	
	private boolean closed = false;
	
	MongoConnection(MongoClient client, String dbName) {
		db = client.getDatabase(dbName);
		session = client.startSession();
	}

	public void insert(String collectionName, Map<String, Object> map) {
		checkOpen();
		db.getCollection(collectionName).insertOne(session, new Document(map));
	}
	
	public <T> void insert(String collectionName, Object anyObject) {
		checkOpen();                                 //L
		@SuppressWarnings("unchecked")               //M
		Class<T> o = (Class<T>) anyObject.getClass();//A
		T any = o.cast(anyObject);                   //O
		db.getCollection(collectionName, o).insertOne(session, any);
	}
	
	@Override
	public void close() {
		session.close();
		closed = true;
	}

	private void checkOpen() {
		if(closed) throw new IllegalStateException("This connection has already been closed!");
	}
	
}
