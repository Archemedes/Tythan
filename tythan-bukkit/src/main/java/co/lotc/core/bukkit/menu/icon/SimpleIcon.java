package co.lotc.core.bukkit.menu.icon;

import java.util.function.Consumer;

import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SimpleIcon extends Icon {
	ItemStack itemStack;
	Consumer<MenuAction> doThis;

	@Override
	public ItemStack getItemStack(MenuAgent agent) {
		return itemStack;
	}

	@Override
	public void click(MenuAction action) {
		doThis.accept(action);
	}

}
