package co.lotc.core.bukkit.menu.icon;

import org.bukkit.inventory.ItemStack;

public abstract class Slot implements Icon {

	
	
	
	@Override
	public boolean mayInteract(ItemStack moved) {
		return true;
	}
}
