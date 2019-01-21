package co.lotc.core.bukkit.menu;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import co.lotc.core.Tythan;
import co.lotc.core.bukkit.TythanBukkit;
import co.lotc.core.bukkit.util.Run;
import lombok.Getter;

public class MenuAgent {
	@Getter private final Player player;
	@Getter private	Menu menu = null; //Current menu focused on, may change
	
	private final Map<String, Object> context = new HashMap<>();
	
	MenuAgent(Player player) {
		this.player = player;
	}
	
	void setInitialMenu(Menu menu) {
		Validate.isTrue(this.menu == null, "Must be initiated by the Native MenuListener!");
		
		this.menu = menu;
	}
	
	public Inventory getInventory() {
		return menu.getInventory();
	}
	
	public void openNewInventory(Menu newMenu) {
		Run.as(getPlugin()).sync(()->{
			menu = newMenu;
			player.getPlayer().openInventory(menu.getInventory());
		});
	}
	
	public boolean hasContext(String tag) {
		return context.containsKey(tag);
	}
	
	public void addContext(String tag, Object value) {
		context.put(tag, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getContext(String tag) {
		return (T) context.get(tag);
	}
	
	public String getContextString(String tag) {
		return (String) context.get(tag);
	}

	private TythanBukkit getPlugin() {
		return (TythanBukkit) Tythan.get();
	}
}
