package co.lotc.core.bukkit.menu;

import static org.bukkit.Material.AIR;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.bukkit.menu.icon.Pad;
import co.lotc.core.bukkit.menu.icon.SimpleIcon;

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
