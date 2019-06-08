package co.lotc.core.account;

import java.util.UUID;

public class AccountQuery {
	private Urgency urgency = Urgency.VIEW;
	private Selector<?> selector = null;
	private Object filter = null;
	

	public AccountQuery id(int id) {
		selector = Selector.ID;
		filter = id;
		return this;
	}
	
	public <S> AccountQuery byField(Selector<S> selector, S value) {
		this.selector = selector;
		this.filter = value;
		return this;
	}
	
	
	public static enum Urgency{
			VIEW,TRY,FORCE;
	}
	
	public static class Selector<T>{
		private Selector() {}
				
		public static Selector<Integer> ID = new Selector<>();
		public static Selector<UUID> UUID = new Selector<>();
		public static Selector<Long> FORUM_ID = new Selector<>();
		public static Selector<UUID> DISCORD_ID = new Selector<>();
		public static Selector<String> USERNAME = new Selector<>();
		public static Selector<String> IP = new Selector<>();
		
	}
}
