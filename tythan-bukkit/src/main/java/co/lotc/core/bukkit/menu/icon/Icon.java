package co.lotc.core.bukkit.menu.icon;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.MenuAgent;

public interface Icon {
	
	/**
	 * This gives the itemstack the Icon should be displaying
	 */
	ItemStack getItemStack(MenuAgent agent);
	
	/**
	 * Called when icon is interacted with by the player
	 */
	void click(MenuAgent agent, ClickType t);
	
	/**
	 * May you move an item in this icon (is the Icon a slot?)
	 * @param into is item being moved INTO the slot (else it's moved out of slot)
	 * @param moved Item moved
	 * @return whether or not you're allowed to move the item
	 */
	default boolean mayMoveItem(boolean into, ItemStack moved) { return false; }
}
