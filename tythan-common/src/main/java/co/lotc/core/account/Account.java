package co.lotc.core.account;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import co.lotc.core.save.ArcheConsumer;
import co.lotc.core.save.Consumer;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class Account {
	private transient Consumer save = new ArcheConsumer(50, 1, 100);
	private final int id=-1;
	
	private long forumId; //e.g. 22277 = sporadic
	private long discordId; //e.g 69173255004487680=Telanir
	
	private int fatigue;
	private long timeCreatedMillis;
	
	//private final Tags<Account> tags;
	
	
	transient long timePlayed, timePlayedThisWeek, lastSeen;
	
	@Getter(AccessLevel.NONE) transient final Set<UUID> alts = new HashSet<>();
	@Getter(AccessLevel.NONE) transient final Set<String> ips = new HashSet<>();
	
	
	public Instant getTimeCreated() {
		return Instant.ofEpochMilli(timeCreatedMillis);
	}
}
