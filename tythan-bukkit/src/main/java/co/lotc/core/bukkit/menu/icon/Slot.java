package co.lotc.core.bukkit.menu.icon;

import org.bukkit.inventory.ItemStack;

public abstract class Slot extends Icon {
	
	@Override
	public boolean mayInteract(ItemStack moved) {
		return true;
	}
}
