package co.lotc.core.bukkit.menu;

import static org.bukkit.ChatColor.WHITE;
import static org.bukkit.Material.AIR;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.bukkit.menu.icon.Link;
import co.lotc.core.bukkit.menu.icon.Pad;
import co.lotc.core.bukkit.menu.icon.SimpleIcon;
import co.lotc.core.bukkit.util.ItemUtil;

public class MenuBuilder {
	final String title;
	final InventoryType type;
	final int size;
	
	final List<Icon> icons;
	
	public MenuBuilder(String title, int rows) {
		this.title = title;
		type = InventoryType.CHEST;
		size = rows*9;
		
		icons = Arrays.asList(new Icon[size]);
	}
	
	public MenuBuilder(String title, InventoryType type) {
		Validate.isTrue(type.isCreatable(), "Invalid inventory type for menus: " + type);
		this.title = title;
		this.type = type;
		this.size = type.getDefaultSize();
		
		icons = Arrays.asList(new Icon[size]);
	}
	
	public MenuBuilder icon(Icon icon) {
		return icon(firstEmpty(), icon);
	}
	
	public MenuBuilder icon(int i, Icon icon) {
		checkDuplicates(icon);
		icons.set(i, icon);
		return this;
	}
	
	public MenuBuilder icon(ItemStack picture, Consumer<MenuAction> whatItDoes) {
		return icon(firstEmpty(), picture, whatItDoes);
	}
	
	public MenuBuilder icon(int i, ItemStack picture, Consumer<MenuAction> whatItDoes) {
		icons.set(i, new SimpleIcon(picture, whatItDoes));
		return this;
	}
	
	public MenuBuilder pad(Material m) {
		return icon(new Pad(m));
	}
	
	public MenuBuilder pad(int index, Material m) {
		return icon(index, new Pad(m));
	}
	
	public MenuBuilder pad(ItemStack is) {
		return pad(firstEmpty(), is);
	}
	
	public MenuBuilder pad(int i, ItemStack is) {
		icons.set(i, new Pad(is));
		return this;
	}
	
	public MenuBuilder folder(String name, Menu to) {
		return folder(firstEmpty(), name, to);
	}
	
	public MenuBuilder folder(int i, String name, Menu to) {
		return folder(i, name, Suppliers.ofInstance(to), false);
	}
	
	public MenuBuilder folder(String name, Supplier<Menu> to) {
		return folder(firstEmpty(), name, to);
	}
	
	public MenuBuilder folder(int i, String name, Supplier<Menu> to) {
		return folder(i, name, to, true);
	}
	
	public MenuBuilder folder(int i, String name, Supplier<Menu> to, boolean memoize) {
		ItemStack is = ItemUtil.getSkullFromTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTYzMzBhNGEyMmZmNTU4NzFmYzhjNjE4ZTQyMWEzNzczM2FjMWRjYWI5YzhlMWE0YmI3M2FlNjQ1YTRhNGUifX19");
		ItemMeta m = is.getItemMeta();
		m.setDisplayName(WHITE + name);
		is.setItemMeta(m);
		
		Icon icon = memoize? Link.memoize(is, to) : new Link(is, to);
		
		icon(i, icon);
		return this;
	}
	
	public MenuBuilder fill(Material m) {
		return fill(new Pad(m));
	}
	
	public MenuBuilder fill(Icon icon) {
		for(int i = 0; i < icons.size(); i++) {
			if(icons.get(i) == null) icons.set(i, icon);
		}
		return this;
	}
	
	public Menu build() {
		icons.replaceAll(i->i==null? new Pad(AIR) : i);
		Menu menu = new Menu(this);
		
		return menu;
	}
	
	private int firstEmpty() {
		return icons.indexOf(null);
	}
	
	private void checkDuplicates(Icon x) {
		if(x instanceof Pad) return;
		if(icons.contains(x)) throw new IllegalArgumentException("");
	}
}
