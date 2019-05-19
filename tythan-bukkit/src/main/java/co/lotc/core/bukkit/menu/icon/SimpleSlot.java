package co.lotc.core.bukkit.menu.icon;

import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;

public class SimpleSlot extends Slot {
	private ItemStack item;
	
	public SimpleSlot() {
		this(null);
	}
	
	public SimpleSlot(ItemStack item) {
		this.item = item;
	}

	@Override
	public void click(MenuAction action) {}
	
	@Override
	public ItemStack getItemStack(MenuAgent a) {
		return item;
	}

}
