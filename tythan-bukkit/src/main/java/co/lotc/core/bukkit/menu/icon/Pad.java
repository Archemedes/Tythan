package co.lotc.core.bukkit.menu.icon;

import static net.md_5.bungee.api.ChatColor.GRAY;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.util.ItemBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Pad extends Icon {
	@Getter private final ItemStack itemStack;

	public Pad(Material m) {
		itemStack = new ItemBuilder(m).name(GRAY+"").build();
	}
		
	@Override
	public void click(MenuAction action) {}

	@Override
	public ItemStack getItemStack(MenuAgent agent) {
		return getItemStack();
	}

}
