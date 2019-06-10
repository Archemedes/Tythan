package co.lotc.core.account;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;

import co.lotc.core.Tythan;
import lombok.var;

public abstract class AccountHandler {

	
	protected abstract void callLoadEvent(Account account);
	
	private void setupTables() {
		try(var mongo = Tythan.getMongoHandler().connect()){
			mongo.collection("accounts")
			.intField("_id",true)
			.longField("forum_id")
			.longField("discord_id")
			.intField("fatigue")
			.longField("created")
			.index(true, "_id")
			.index(true, "forum_id")
			.index(true, "discord_id")
			.build();
			
			mongo.collection("account_ips")
			.intField("_id",true)
			.stringField("ip_address", true)
			.index(true, "_id")
			.index(false, "ip_address")
			.build();
			
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