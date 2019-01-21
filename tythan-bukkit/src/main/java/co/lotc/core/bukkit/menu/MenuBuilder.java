package co.lotc.core.bukkit.menu;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.event.inventory.InventoryType;

import co.lotc.core.bukkit.menu.icon.Icon;

public class MenuBuilder {
	private final String title;
	private final InventoryType type;
	private final int size;
	
	private final List<Icon> icons;
	
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
		icons.set(i, icon);
		return this;
	}
	
	
	
	
	private int firstEmpty() {
		return icons.indexOf(null);
	}
}
