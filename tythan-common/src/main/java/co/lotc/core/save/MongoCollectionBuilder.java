package co.lotc.core.save;

import static com.mongodb.client.model.Filters.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.BsonType;
import org.bson.conversions.Bson;

import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ValidationAction;
import com.mongodb.client.model.ValidationLevel;
import com.mongodb.client.model.ValidationOptions;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

@RequiredArgsConstructor
public class MongoCollectionBuilder {
	private final MongoConnection connection;
	private final String name;
	
	private final List<Index> indices = new ArrayList<>();
	private final List<Bson> filters = new ArrayList<>();
	
	public MongoCollectionBuilder index(boolean unique, String... fields) {
		indices.add(new Index(new IndexOptions().unique(unique), fields));
		return this;
	}
	
	public MongoCollectionBuilder doubleField(String name, boolean required) {
		return field(name, required, BsonType.DOUBLE);
	}
	
	public MongoCollectionBuilder stringField(String name, boolean required) {
		return field(name, required, BsonType.STRING);
	}
	
	MongoCollectionBuilder field(String name, boolean required, BsonType type) {
		if(required) filters.add(exists(name));
		if(type != null) filters.add(type(name, type));
		return this;
	}
	
	public MongoConnection build() {
		val db = connection.getDatabase();
		
		if(filters.isEmpty()) db.createCollection(connection.getSession(), name);
		else db.createCollection(connection.getSession(), name, validation() );
		
		if(!indices.isEmpty()) {
			val collection = db.getCollection(name);
			for(val index : indices)
				collection.createIndex(connection.getSession(), Indexes.ascending(index.fields), index.options);
		}
		
		return connection;
	}

	private CreateCollectionOptions validation() {
		return new CreateCollectionOptions().validationOptions(new ValidationOptions()
				.validationLevel(ValidationLevel.MODERATE)
				.validationAction(ValidationAction.ERROR)
				.validator( and(filters) )
				);
	}
	
	@Value
	private static class Index {
		IndexOptions options;
		String[] fields;
	}
	
	@Value
	private static class Column {
		boolean required;
		String name;
		BsonType type;
	}
	
}
