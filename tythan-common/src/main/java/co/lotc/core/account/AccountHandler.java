package co.lotc.core.account;

import java.util.function.Consumer;

import co.lotc.core.Tythan;

public class AccountHandler {

	public AccountHandler() {
		// TODO Auto-generated constructor stub
	}

	public void tryLoadAccount(Consumer<Account> callback) {
		
	}

	public void forceLoadAccount(Consumer<Account> callback) {
		
	}
	
	public void viewAccount(Consumer<Account> callback) {
		
	}
	
	
	private Account fetch(int id) {
		Tythan.get();
		return null;
	}
}
