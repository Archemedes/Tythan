package co.lotc.core.bukkit.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import co.lotc.core.bukkit.TythanBukkit;
import co.lotc.core.bukkit.util.Run;
import co.lotc.core.util.Context;
import lombok.AccessLevel;
import lombok.Getter;

public class MenuAgent {
	@Getter private final Player player;
	@Getter private	Menu menu = null; //Current menu focused on, may change

	//We need this state to discern between player closing inventory manually and us closing the inventory to navigate
	@Getter(AccessLevel.PACKAGE) private boolean navigating;
	
	private final Context context = new Context();
	private final List<Consumer<Context>> closure = new ArrayList<>();
	
	MenuAgent(Menu menu, Player player) {
		this.menu = menu;
		this.player = player;
	}
	
	public Inventory getInventory() {
		return menu.getInventory();
	}
	
	public void open(Menu newMenu) {
		Run.as(TythanBukkit.get()).sync(()->{
			//The ordering here is very important. Any change to it will break stuff
			//This is due to interleaving of the Open and Close events...
			//which interact and read state from this object
			navigating = true; //lets InventoryCloseEvent know the session shouldn't end
			player.closeInventory(); //InventoryCloseEvent is called here --> Will remove the viewer too
			navigating = false;
			menu = newMenu;
			menu.addViewer(this);
			player.getPlayer().openInventory(menu.getInventory());
		});
	}
	
	public void onSessionClose(Consumer<Context> callback) {
		closure.add(callback);
	}
	
	void runSessionClosureCallbacks() {
		closure.forEach(c->c.accept(context));
	}
	
	public void close() {
		Run.as(TythanBukkit.get()).sync(()->player.closeInventory());
	}
	
	public boolean hasContext(String tag) {
		return context.has(tag);
	}
	
	public void addContext(String tag, Object value) {
		context.set(tag, value);
	}
	
	public <T> T getContext(String tag) {
		return context.get(tag);
	}
	
	public String getContextString(String tag) {
		return context.getString(tag);
	}
}
