package co.lotc.core.bukkit.util;

import static co.lotc.core.bukkit.util.ReflectionUtil.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.bukkit.inventory.meta.tags.ItemTagType;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;

import co.lotc.core.CoreLog;
import lombok.NonNull;
import lombok.var;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class ItemUtil {
	private ItemUtil() {}
	
	/**
	 * Set CustomTag compliant with STRING type using a lotc namespace
	 * @param item Item to modify
	 * @param key Key, will be lowercased and must match [a-z0-9/._-]
	 * @param value Value to be stored as an NBTString
	 */
	public static void setCustomTag(@NonNull ItemStack item, String key, String value) {
		if("true".equals(value) || "false".equals(value))
			throw new IllegalArgumentException("Values true/false cannot be serialized so they are forbidden!");
		
		var meta = item.getItemMeta();
		var container = meta.getCustomTagContainer();
		container.setCustomTag(fuckYouBukkitJustGiveMeAKey(key), ItemTagType.STRING, value);
		item.setItemMeta(meta);
	}
	
	public static String getCustomTag(ItemStack item, String key) {
		if(!exists(item)) return null;
		var meta = item.getItemMeta();
		var container = meta.getCustomTagContainer();
		return container.getCustomTag(fuckYouBukkitJustGiveMeAKey(key), ItemTagType.STRING);
	}
	
	public static boolean hasCustomTag(ItemStack item, String key) {
		if(!exists(item)) return false;
		var meta = item.getItemMeta();
		var container = meta.getCustomTagContainer();
		return container.hasCustomTag(fuckYouBukkitJustGiveMeAKey(key), ItemTagType.STRING);
	}
	
	public static void removeCustomTag(@NonNull ItemStack item, String key) {
		var meta = item.getItemMeta();
		var container = meta.getCustomTagContainer();
		container.removeCustomTag(fuckYouBukkitJustGiveMeAKey(key));
		item.setItemMeta(meta);
	}
	
	@SuppressWarnings("deprecation")
	static NamespacedKey fuckYouBukkitJustGiveMeAKey(String rawKey) {
		//The data is stored as NBT tags
		//Structure is inside a compound called PublicBukkitValues
		//Key-value is keys as NamespacedKey (pluginName:key) and value as provided
		//Can be any of the NBT primitive types but storing as string is fine
		return new NamespacedKey("lotc", rawKey);
	}
	
	private static Method getRaw = null;
	public static Set<String> getCustomKeys(ItemStack item){
		CoreLog.debug("Getting the custom tag keys for an item: " + item);
		if(!exists(item)) return Collections.emptySet();
		
		var meta = item.getItemMeta();
		var container = meta.getCustomTagContainer();
		
		try {
			if(getRaw == null) getRaw = container.getClass().getMethod("getRaw");
			@SuppressWarnings("unchecked")
			var map = (Map<String, Object>) getRaw.invoke(container);

			return map.keySet().stream()
					.map(String::valueOf)//Return self
					.filter(s->s.startsWith("lotc:"))
					.map(s->s.substring(5))
					.collect(Collectors.toSet());
		}catch(ClassCastException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			CoreLog.severe("CraftCustomTagContainer changed internal structure: Failure on getRaw()!");
			e.printStackTrace();
			return Collections.emptySet();
		}
	}
	
	public static Map<String, String> getCustomTags(ItemStack item){
		return getCustomKeys(item).stream().collect(Collectors.toMap(x->x, x->getCustomTag(item, x)));
	}
	
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
		String prefix = is.getType().isBlock()? "block." : "item.";
		return prefix + k.getNamespace() + '.' + k.getKey();
	}

	/**
	 * Convenience method for checking if an ItemStack is non-null and non-AIR
	 * @param is ItemStack to check
	 * @return is != null && is.getType() != Material.AIR;
	 */
	@SuppressWarnings("deprecation")
	public static boolean exists(ItemStack is) {
		return is != null
				&& is.getType() != Material.AIR
				&& is.getType() != Material.CAVE_AIR
				&& is.getType() != Material.VOID_AIR
				&& is.getType() != Material.LEGACY_AIR;
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
