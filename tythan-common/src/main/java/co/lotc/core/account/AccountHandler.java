package co.lotc.core.account;

import static com.mongodb.client.model.Filters.eq;

import org.bson.BsonType;
import org.bson.Document;

import co.lotc.core.Tythan;
import lombok.var;

public abstract class AccountHandler {
	
	protected abstract void callLoadEvent(Account account);
	
	protected abstract boolean hasLock(int accountId);
	
	public void setupTables() {
		try(var mongo = Tythan.getMongoHandler().connect()){
			
			mongo.collection("account_ips")
			.intField("_id",true)
			.stringField("ip_address", true)
			.index(true, "_id")
			.index(false, "ip_address")
			.build();
			
			mongo.collection("account_uuids")
			.field("_id", true, BsonType.BINARY)
			.intField("account_id", true)
			.index(false, "account_id")
			.build();
			
			/*mongo.collection("player_names")
			.stringField("player_name", true)
			.field("player_uuid", true, BsonType.BINARY)
			.index(false, "player_name")
			.index(false, "player_uuid")
			.build();*/
		}
	}
	
	
	private Account fetch(int id) {
		try(var mongo = Tythan.getMongoHandler().connect()){
			Document candidate = mongo.getDatabase().getCollection("accounts").find(eq("_id", id)).first();
			if(candidate == null) return null;
			
			
		}
		
		return null;
	}
}
