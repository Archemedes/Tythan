package co.lotc.core.bukkit.menu;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.CoreLog;
import co.lotc.core.bukkit.menu.icon.Icon;
import lombok.var;

public class Menu implements InventoryHolder{
	private final Map<UUID, MenuAgent> viewers = new LinkedHashMap<>();

	private final Icon[] icons;
	private final Inventory inventory;

	private boolean initialized = false;
	
	public static Menu fromIcons(String title, List<? extends Icon> icons) {
		return MenuUtil.createMultiPageMenu(null, title, icons).get(0);
	}
	
	public static Menu fromIcons(Menu origin, String title, List<? extends Icon> icons) {
		return MenuUtil.createMultiPageMenu(origin, title, icons).get(0);
	}
	
	Menu(MenuBuilder builder) {
		icons = builder.icons.toArray(new Icon[builder.size]);
		
		if(builder.type == InventoryType.CHEST)
			inventory = Bukkit.createInventory(this, builder.size, builder.title);
		else
			inventory = Bukkit.createInventory(this, builder.type, builder.title);
	}
	
	/**
	 * this is the correct way to open a new menu session. This will flush the Agent.
	 * Navigating between menus shouldn't be done with this one but with MenuAgent#open
	 * @param p Player to open the Menu for
	 */
	public MenuAgent openSession(Player p) {
		MenuAgent a = new MenuAgent(this, p);
		viewers.put(p.getUniqueId(), a);
		p.openInventory(inventory);
		return a;
	}
	
	void init() {
		if(!initialized) {
			initialized = true;
			@SuppressWarnings("deprecation")
			String title = inventory.getTitle();
			CoreLog.debug("Initializing menu with title: " + title);
			updateIconItems();
		}
	}
	
	@SuppressWarnings("deprecation")
	public String getTitle() {
		return inventory.getTitle();
	}
	
	public int getSize() {
		return icons.length;
	}
	
	public void updateIconItem(Icon icon) {
		updateIconItem(icon, false);
	}
	
	public void updateIconItem(int index) {
		if(icons[index] != null) {
			inventory.setItem(index, itemOrBust(icons[index]));
		}
	}
	
	public void updateIconItem(Icon icon, boolean multiple) {
		Validate.notNull(icon);
		for(int i = 0; i < icons.length; i++){
			if(icons[i] == icon) {
				inventory.setItem(i, itemOrBust(icon));
				if(!multiple) break;
			}
		}
	}
	
	public void updateIconItems() {
		for(int i = 0; i < icons.length; i++) {
			inventory.setItem(i, itemOrBust(icons[i]));
		}
	}
	
	public void addIcon(Icon icon) {
		int where = firstEmpty();
		if(where == -1) throw new IllegalStateException("Menu is full!");
		
		icons[where] = icon;
		inventory.setItem(where, itemOrBust(icon));
	}

	public void setIcon(int i, Icon icon) {
		Validate.isTrue(i >= 0 && i < icons.length);
		icons[i] = icon;
		inventory.setItem(i, itemOrBust(icon));
	}
	
	public Icon getIcon(int i) {
		return icons[i];
	}
	
	public int getIndexOf(Icon icon) {
		Validate.notNull(icon);
		for(int i = 0; i < icons.length; i++) {
			if(icons[i] == icon) return i;
		}
		return -1;
	}
	
	public int firstEmpty() {
		for(int i = 0; i < icons.length; i++) {
			if(icons[i] == null) return i;
		}
		return -1;
	}
	
	@Override
	public Inventory getInventory() {
		return inventory;
	}
	
	public MenuAgent getAgent(HumanEntity p) {
		MenuAgent aa = viewers.get(p.getUniqueId());
		Validate.isTrue(aa == null || aa.getMenu() == this);
		return aa;
	}
	
	void clearViewer(MenuAgent agent) {
		viewers.remove(agent.getPlayer().getUniqueId());
	}
	
	void addViewer(MenuAgent agent) {
		viewers.put(agent.getPlayer().getUniqueId(), agent);
	}
	
	private ItemStack itemOrBust(Icon i) {
		try {
			var opt = viewers.values().stream().findFirst().map(i::getItemStack);
			if(opt.isPresent()) return opt.get();
			else initialized = false;
		} catch(Exception e) {
			CoreLog.warning(" User error trying to get ItemStack from Icon of type " + i.getClass().getSimpleName());
			e.printStackTrace();
		}
		
		return null;
	}
}
