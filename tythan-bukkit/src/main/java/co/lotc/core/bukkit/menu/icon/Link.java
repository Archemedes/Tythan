package co.lotc.core.bukkit.menu.icon;

import org.bukkit.inventory.ItemStack;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import co.lotc.core.bukkit.menu.Menu;
import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
public class Link extends Button {
	Supplier<Menu> to;
	ItemStack item;
	
	public Link(ItemStack is, Menu menu) {
		to = Suppliers.ofInstance(menu);
		item = is;
	}
	
	public Link(ItemStack is, Supplier<Menu> supplier) {
		to = supplier;
		item = is;
	}
	
	public static Link memoize(ItemStack is, Supplier<Menu> supplier) {
		return new Link(is, Suppliers.memoize(supplier));
	}

	@Override
	public ItemStack getItemStack(MenuAgent agent) {
		return item;
	}

	@Override
	public void click(MenuAction action) {
		action.getMenuAgent().open(to.get());
	}

}
