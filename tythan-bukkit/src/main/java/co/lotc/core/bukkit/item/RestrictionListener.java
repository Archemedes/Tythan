package co.lotc.core.bukkit.item;

import static co.lotc.core.bukkit.item.ItemRestriction.*;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import co.lotc.core.bukkit.TythanBukkit;
import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.bukkit.util.Run;
import lombok.val;

public class RestrictionListener  implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void inv(InventoryClickEvent e) {
		if(e.getInventory().getType() == InventoryType.CRAFTING)
			return;
		
		InventoryUtil.getTouchedByEvent(e).stream()
		.filter(TRADE::isPresent)
		.findAny().ifPresent($->e.setCancelled(true));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void inv(InventoryDragEvent e) {
		InventoryUtil.getTouchedByEvent(e).stream()
		.filter(TRADE::isPresent)
		.findAny().ifPresent($->e.setCancelled(true));
	}

	@EventHandler
	public void use(PlayerInteractEvent e) {
		ItemStack is = e.getItem();
		if(ItemUtil.exists(is) && USE.isPresent(is))
			e.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void use(PlayerInteractAtEntityEvent e) {
		EquipmentSlot hand = e.getHand();
		PlayerInventory inv = e.getPlayer().getInventory();
		ItemStack is = hand == EquipmentSlot.HAND? inv.getItemInMainHand() : inv.getItemInOffHand();
		if(ItemUtil.exists(is) && USE.isPresent(is))
			e.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void dmg(EntityDamageByEntityEvent e) {
		Entity entity = e.getEntity();
		if(entity instanceof Player) {
			Player p = (Player) entity;
			ItemStack is = p.getInventory().getItemInMainHand();
			if(ItemUtil.exists(is) && USE.isPresent(is))
				e.setCancelled(true);
		}
	}
	
	
	@EventHandler(ignoreCancelled = true, priority=EventPriority.HIGHEST)
	public void handle(PlayerDropItemEvent e) {
		ItemStack is = e.getItemDrop().getItemStack();
		if(LOSE.isPresent(is)){
			e.setCancelled(true);
		} else if(TRADE.isPresent(is)) {
			Run.as(TythanBukkit.get()).sync(()->e.getItemDrop().remove());
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void craft(PrepareItemCraftEvent e) {
		CraftingInventory i = e.getInventory();
		Stream.of(i.getMatrix())
			.filter(Objects::nonNull)
			.filter(USE::isPresent)
			.findAny().ifPresent($->i.setResult(null));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void die(PlayerDeathEvent e) {
		val items = e.getDrops().stream()
				.filter(TRADE::isPresent)
				.collect(Collectors.toList());
		
		items.forEach(e.getDrops()::remove);
	}
}
