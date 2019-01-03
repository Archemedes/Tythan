package co.lotc.core.save;

import java.io.Closeable;
import java.util.Map;
import java.util.function.Supplier;

import org.bson.Document;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter(AccessLevel.PACKAGE)
public class MongoConnection{
	private final MongoSessionHolder session;
	private final MongoDatabase db;
	
	MongoConnection(MongoClient client, String dbName) {
		db = client.getDatabase(dbName);
		session = new MongoSessionHolder(this, client.startSession());
	}

	public void insert(String collectionName, Map<String, Object> map) {
		checkOpen();
		db.getCollection(collectionName).insertOne(session.get(), new Document(map));
	}
	
	public <T> void insert(String collectionName, Object anyObject) {
		checkOpen();                                 //L
		@SuppressWarnings("unchecked")               //M
		Class<T> o = (Class<T>) anyObject.getClass();//A
		T any = o.cast(anyObject);                   //O
		db.getCollection(collectionName, o).insertOne(session.get(), any);
	}
	
	public void delete(String collectionName, Map<String, Object> map) {
		checkOpen();
		db.getCollection(collectionName).deleteMany(session.get(), new Document(map));
	}
	
	public void update(String collectionName, Map<String, Object> criteria, Map<String, Object> map) {
		checkOpen();
		db.getCollection(collectionName).updateMany(session.get(), new Document(criteria), new Document(map));
	}
	
	private void checkOpen() {
		if(session.closed) throw new IllegalStateException("This connection has already been closed!");
	}
	
	@RequiredArgsConstructor
	static class MongoSessionHolder implements Closeable, Supplier<ClientSession>{
		@Getter private final MongoConnection connection;
		private final ClientSession session;
		private boolean closed = false;
		
		@Override
		public void close() {
			session.close();
			
			closed = true;
		}
		
		@Override
		public ClientSession get(){ return session; }
	}
}
