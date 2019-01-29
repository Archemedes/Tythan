package co.lotc.core.bukkit.menu;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.icon.Icon;

public class Menu implements InventoryHolder{
	private final Map<UUID, MenuAgent> viewers = new LinkedHashMap<>();

	private final Icon[] icons;
	private final Inventory inventory;

	private boolean initialized = false;
	
	Menu(MenuBuilder builder) {
		icons = builder.icons.toArray(new Icon[builder.size]);
		
		if(builder.type == InventoryType.CHEST)
			inventory = Bukkit.createInventory(this, builder.size, builder.title);
		else
			inventory = Bukkit.createInventory(this, builder.type, builder.title);
	}
	
	public void openSession(Player p) {
		MenuAgent a = new MenuAgent(this, p);
		viewers.put(p.getUniqueId(), a);
		p.openInventory(inventory);
		if(!initialized) {
			updateIconItems();
			initialized = true;
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
			inventory.setItem(index, icons[index].getItemStack(eldest()));
		}
	}
	
	public void updateIconItem(Icon icon, boolean multiple) {
		Validate.notNull(icon);
		for(int i = 0; i < icons.length; i++){
			if(icons[i] == icon) {
				inventory.setItem(i, icon.getItemStack(eldest()));
				if(!multiple) break;
			}
		}
	}
	
	public void updateIconItems() {
		for(int i = 0; i < icons.length; i++) {
			if(icons[i] != null) {
				ItemStack is1 = icons[i].getItemStack(eldest()), is2 = inventory.getItem(i);
				if(!ObjectUtils.equals(is1, is2)) inventory.setItem(i, is1);
			}
		}
	}
	
	public void addIcon(Icon icon) {
		int where = firstEmpty();
		if(where == -1) throw new IllegalStateException("Menu is full!");
		
		icons[where] = icon;
		inventory.setItem(where, icon.getItemStack(eldest()));
	}

	public void setIcon(int i, Icon icon) {
		Validate.isTrue(i >= 0 && i < icons.length);
		icons[i] = icon;
		inventory.setItem(i, icon.getItemStack(eldest()));
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
	
	private MenuAgent eldest() {
		return viewers.values().stream().findFirst().get();
	}
	
	
}
