package co.lotc.core.account;

import java.time.Instant;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import co.lotc.core.save.ArcheConsumer;
import co.lotc.core.save.Consumer;
import co.lotc.core.util.Context;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
	private transient Consumer save = new ArcheConsumer(50, 1, 100);
	
	//Will also be the account's ID
	private final int id;
	
	private long forumId; //e.g. 22277 = sporadic
	private long discordId; //e.g 69173255004487680=Telanir
	
	private int fatigue;
	private long timeCreatedMillis;
	
	private final Context context = new Context();
	
	transient long timePlayed, timePlayedThisWeek, lastSeen;
	
	@Getter(AccessLevel.NONE) transient private final Set<UUID> alts = new HashSet<>();
	@Getter(AccessLevel.NONE) transient private final Set<String> ips = new HashSet<>();
	
	public Account(int id) {
		this.id = id;
	}
	
	
	public Instant getTimeCreated() {
		return Instant.ofEpochMilli(timeCreatedMillis);
	}
	
}
