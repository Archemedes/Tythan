package co.lotc.core.bukkit.menu.icon;

import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.MenuAction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Pad extends Icon {
	private final ItemStack itemStack;

	@Override
	public ItemStack getItemStack(MenuAction action) {
		return itemStack;
	}
	
	@Override
	public void click(MenuAction action) {}

}
