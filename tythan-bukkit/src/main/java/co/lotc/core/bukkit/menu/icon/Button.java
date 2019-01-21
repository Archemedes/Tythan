package co.lotc.core.bukkit.menu.icon;

import org.bukkit.inventory.ItemStack;

public abstract class Button extends Icon {

	@Override
	public final boolean mayInteract(ItemStack moved) {
		return false;
	}
}
