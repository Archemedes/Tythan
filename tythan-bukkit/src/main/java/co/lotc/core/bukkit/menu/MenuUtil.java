package co.lotc.core.bukkit.menu;

import static org.bukkit.ChatColor.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.bukkit.menu.icon.Link;
import co.lotc.core.bukkit.util.ItemBuilder;
import co.lotc.core.bukkit.util.ItemUtil;

public final class MenuUtil {

	private MenuUtil() {};
	
	private static ItemStack back() {
		return new ItemBuilder(Material.BARRIER)
				.name(RED + "Back")
				.lore(DARK_GRAY + "Return to previous menu")
				.build();
	}
	
	private static ItemStack left() {
		ItemStack is = ItemUtil.getSkullFromTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0=");
		ItemMeta m = is.getItemMeta();
		m.setDisplayName(WHITE + "Previous");
		is.setItemMeta(m);
		return is;
	}
	
	private static ItemStack right() {
		ItemStack is = ItemUtil.getSkullFromTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19");
		ItemMeta m = is.getItemMeta();
		m.setDisplayName(WHITE + "Next");
		is.setItemMeta(m);
		return is;
	}
	
	public static List<Menu> createMultiPageMenu(Menu origin, String title, List<? extends Icon> icons){
		int size = icons.size();
		
		List<Menu> result = new ArrayList<>();
		
		boolean backButton = origin != null;
		boolean onePage = size <= (45 + (backButton?0:9));
		
		if(onePage) {
			int rows = 1+((size-1)/9);
			if(backButton) rows++;
			
			MenuBuilder b = new MenuBuilder(title, rows);
			IntStream.range(0, size).forEach(i->b.icon(i, icons.get(i)));
			if(backButton) {
				int backButtonLocation =  rows * 9 - 5;
				b.icon(backButtonLocation, new Link(back(), origin));
			}
			result.add(b.build());
		} else {
			int menus = 1 + ((size-1)/45);
			
			Menu prev = null;
			for(int i = 0; i < menus; i++) {
				MenuBuilder b = new MenuBuilder(title, 6);
				for(int j = 0; j < 45; j++) {
					int index = i*45+j;
					if(index >= size) break;
					b.icon(j, icons.get(index));
				}
				if(backButton) b.icon(49, new Link(back(), origin));
				if(prev != null) b.icon(45, new Link(left(), prev));
				Menu x = b.build();
				result.add(x);
				if(prev != null) prev.setIcon(53, new Link(right(), x));
				prev = x;
			}
		}
		
		return result;
	}
}
