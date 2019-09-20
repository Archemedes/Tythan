package co.lotc.core.bukkit.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.lotc.core.bukkit.util.ItemUtil;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public enum ItemRestriction {
	/**
	 * Can't use this item (place if block, equip, consume, craft)
	 */
		USE(0x01),
		
		/**
		 * Can't move this item out of your inventory. Vanishes on death and drop
		 */
		TRADE(0x02),
		
		/**
		 * Item is kept on death and can't be dropped.
		 */
		LOSE(0x04),
		
		/**
		 * Advise plugins to ignore this item in all circumstances (but this is implementation-specific)
		 */
		PLUGIN(0x08);

	public static String ITEM_KEY = "tythan_itemrestriction";
	private final int flag;
		
	
	public void apply(ItemStack is) {
		val meta = is.getItemMeta();
		apply(meta);
		is.setItemMeta(meta);
	}

	public void apply(ItemMeta meta) {
		String tag = ItemUtil.getCustomTag(meta, ITEM_KEY);
		int currentFlag = tag == null? 0 : Integer.parseInt(tag);
		int newFlag = currentFlag | this.flag;
		ItemUtil.setCustomTag(meta, ITEM_KEY, String.valueOf(newFlag));
	}
	
	public void remove(ItemStack is) {
		val meta = is.getItemMeta();
		remove(meta);
		is.setItemMeta(meta);
	}
	
	public void remove(ItemMeta meta) {
		String tag = ItemUtil.getCustomTag(meta, ITEM_KEY);
		if(tag == null) return; //Nothing to remove
		
		int currentFlag = Integer.parseInt(tag);
		if( (currentFlag & this.flag) == 0) return; //Also nothing to remove
		int newFlag = currentFlag & ~this.flag;
		ItemUtil.setCustomTag(meta, ITEM_KEY, String.valueOf(newFlag));
	}
	
	public boolean isPresent(ItemStack item) {
		return isPresent(item.getItemMeta());
	}
	
	public boolean isPresent(ItemMeta meta) {
		String tag = ItemUtil.getCustomTag(meta, ITEM_KEY);
		if(tag == null) return false;
		int currentFlag = Integer.parseInt(tag);
		return (currentFlag & this.flag) != 0;
	}
	
}
