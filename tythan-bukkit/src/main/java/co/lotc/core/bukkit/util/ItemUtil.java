package co.lotc.core.bukkit.util;

import static co.lotc.core.bukkit.util.ReflectionUtil.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class ItemUtil {
	private ItemUtil() {}
	
	/**
	 * Method to easily make Minecraft skulls from arbitrary skin files
	 * @param theTexture A base-64 encoded nbt compound containing skin info
	 * @return a textured Minecraft SKULL_ITEM
	 */
	public static ItemStack getSkullFromTexture(String theTexture) {
		NbtCompound tag = NbtFactory.ofCompound("tag");
		NbtCompound skullOwner = NbtFactory.ofCompound("SkullOwner");
		NbtCompound properties = NbtFactory.ofCompound("Properties");
		NbtCompound property = NbtFactory.ofCompound("");
		
		char[] uid = UUID.nameUUIDFromBytes(theTexture.getBytes()).toString().toCharArray();
		uid[14] = '4';
		uid[19] = 'a';
		
		property.put("Value", theTexture);
		skullOwner.put("Id", String.valueOf(uid));
		NbtList<NbtCompound> list = NbtFactory.ofList("textures", property);
		properties.put(list);
		skullOwner.put(properties);
		tag.put(skullOwner);
		
		ItemStack is = MinecraftReflection.getBukkitItemStack(new ItemStack(Material.PLAYER_HEAD, 1));
		NbtFactory.setItemTag(is, tag);
		return is;
	}
	
	/**
	 * Method to easily make Minecraft skulls from arbitrary skin files
	 * @param profile The profile to get a skin from
	 * @return a textured Minecraft SKULL_ITEM
	 */
	public static ItemStack getSkullFromProfile(WrappedGameProfile profile) {
		String value = profile.getProperties().get("textures").iterator().next().getValue();
		return getSkullFromTexture(value);
	}
	
	/**
	 * Transforms an itemstack into its JSON equivalent. Useful for HoverEvent SHOW_ITEM or uncommon serialization needs
	 * @param is The item to convert
	 * @return A JSON string (can be turned to JSON object if desired)
	 */
	public static String getItemJson(ItemStack is) {
		try {
			Object nms = MinecraftReflection.getMinecraftItemStack(is);
			Object compound = compoundConstructor().newInstance();
			
			saveToJson().invoke(nms, compound);
			return compound.toString();
		}catch(Throwable t){t.printStackTrace();}
		return null;
	}
	
	/**
	 * Name of ItemStack as displayed by en_US language
	 * @param is ItemStack to check
	 * @return the translated name
	 */
	public static String getItemEnglishName(ItemStack is){
		TranslatableComponent comp = new TranslatableComponent();
		comp.setTranslate(getItemLocaleName(is));
		return comp.toPlainText();
	}
	
	/**
	 * Returns best-fitting name of ItemStack.
	 * Either the display name from Item Meta, or else the en_US english name of the item
	 * @param is ItemStack to check
	 * @return the display name
	 */
	public static String getDisplayName(ItemStack is){
		if(is.hasItemMeta()) {
			ItemMeta m = is.getItemMeta();
			if(m.hasDisplayName()) return m.getDisplayName();
		}
		
		return getItemEnglishName(is);
	}

	/**
	 * Retrieve item name recognized by MCs Locale translations
	 * @param is Item to check
	 * @return The internal ItemStack translatable name
	 */
	public static String getItemLocaleName(ItemStack is){
		NamespacedKey k = is.getType().getKey();
		return "item." + k.getNamespace() + '.' + k.getKey();
	}

	/**
	 * Convenience method for checking if an ItemStack is non-null and non-AIR
	 * @param is ItemStack to check
	 * @return is != null && is.getType() != Material.AIR;
	 */
	public static boolean exists(ItemStack is) {
		return is != null && is.getType() != Material.AIR;
	}

	public static ItemStack make(Material mat, short durability, String displayName, String... lore) {
		ItemStack is = new ItemStack(mat);
		ItemMeta m = is.getItemMeta();
		if(m instanceof Damageable) {
			((Damageable) m).setDamage(durability);
			is.setItemMeta(m);
		}
		return decorate(is, displayName, lore);
	}

	public static ItemStack make(Material mat, short durability, int amount, String displayName, String... lore) {
		ItemStack is = new ItemStack(mat);
		ItemMeta m = is.getItemMeta();
		if(m instanceof Damageable) {
			((Damageable) m).setDamage(durability);
			is.setItemMeta(m);
		}
		is.setAmount(amount);
		return decorate(is, displayName, lore);
	}
	
	public static ItemStack makePotion(Color color, String displayName, String... lore) {
		ItemStack is = new ItemStack(Material.POTION);
		PotionMeta m = (PotionMeta) is.getItemMeta();
		m.setColor(color);
		is.setItemMeta(m);
		
		return decorate(is, displayName, lore);
	}
	
	/**
	 * Conveniently make a new item.
	 * @param mat Material of button
	 * @param displayName Name (not italicized by default)
	 * @param lore Lore lines
	 * @return the Item
	 */
	public static ItemStack make(Material mat, String displayName, String... lore) {
		ItemStack is = new ItemStack(mat);
		return decorate(is, displayName, lore);
	}
	
	/**
	 * Decorate an item with displayName and Lore
	 * @param is Item
	 * @param displayName Name (not italicized by default)
	 * @param lore Lore lines
	 * @return the Item, now decorated
	 */
	public static ItemStack decorate(ItemStack is, String displayName, String... lore) {
        Predicate<String> colored = s -> s.indexOf(ChatColor.COLOR_CHAR) >= 0;
        if (!colored.test(displayName)) displayName = ChatColor.WHITE + displayName;
        List<String> listLore = Arrays.stream(lore)
                .map(s -> colored.test(s) ? s : ChatColor.GRAY + s)
                .collect(Collectors.toList());

		ItemMeta m = is.getItemMeta();
        m.setDisplayName(displayName);
        m.setLore(listLore);
        is.setItemMeta(m);
        return is;
	}
}
