package co.lotc.core.bukkit.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import co.lotc.core.bukkit.TythanBukkit;
import co.lotc.core.bukkit.util.Run;
import co.lotc.core.util.Context;
import lombok.Getter;

public class MenuAgent {
	@Getter private final Player player;
	@Getter private	Menu menu = null; //Current menu focused on, may change
	
	private final Context context = new Context();
	
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
			player.getOpenInventory().close(); //Will remove the viewer too
			menu = newMenu;
			menu.addViewer(this);
			player.getPlayer().openInventory(menu.getInventory());
		});
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
