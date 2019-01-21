package co.lotc.core.bukkit.menu.icon;

import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.MenuAction;

public abstract class Icon {
	
	Icon(){}
	
	/**
	 * This gives the itemstack the Icon should be displaying
	 */
	public abstract ItemStack getItemStack(MenuAction action);
	
	/**
	 * Called when icon is interacted with by the player
	 */
	public abstract void click(MenuAction action);
	
	/**
	 * May you move an item in this icon (is the Icon a slot?)
	 * @param moved Item moved in (AIR if slot was emptied)
	 * @return whether or not you're allowed to move the item
	 */
	public boolean mayInteract(ItemStack moved) { return false; }
}