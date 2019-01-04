package co.lotc.core.bukkit.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.var;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent=true)
@FieldDefaults(level=AccessLevel.PRIVATE)
public class ItemBuilder {
	@Setter Material material;
	@Setter int durability = 0;
	@Setter int amount = 1;
	@Setter String name;
	@Setter boolean unbreakable = false;
	
	final List<String> lore = new ArrayList<>();
	final List<ItemFlag> flags = new ArrayList<>();
	
	final Map<Enchantment, Integer> enchants = new HashMap<>();
	final Multimap<Attribute, AttributeModifier> attributes = MultimapBuilder.hashKeys().arrayListValues().build();
	
	public ItemBuilder(Material material) {
		this.material = material;
	}
	
	public ItemBuilder unbreakable() {
		return unbreakable(true);
	}
	
	public ItemBuilder lore(String... lore) {
		for(var l : lore) this.lore.add(l);
		return this;
	}
	
	public ItemBuilder flag(ItemFlag... flags) {
		for(var f : flags)  this.flags.add(f);
		return this;
	}
	
	public ItemBuilder enchantment(Enchantment enchantment) {
		return enchantment(enchantment,1);
	}
	
	public ItemBuilder enchantment(Enchantment enchantment, int level) {
		enchants.put(enchantment, level);
		return this;
	}
	
	public ItemBuilder attribute(Attribute attribute, AttributeModifier modifier) {
		attributes.put(attribute, modifier);
		return this;
	}
	
	public ItemStack build() {
		ItemStack result = new ItemStack(material, amount);
		enchants.forEach(result::addEnchantment);
		
		var meta = result.getItemMeta();
		
		if(durability > 0 && meta instanceof Damageable)
			((Damageable)meta).setDamage(durability);
		if(name != null) meta.setDisplayName(name);
		if(!lore.isEmpty()) meta.setLore(lore);
		flags.forEach(meta::addItemFlags);
		meta.setUnbreakable(unbreakable);
		meta.setAttributeModifiers(attributes);
		
		result.setItemMeta(meta);
		return result;
	}
	
}
