package co.lotc.core.bukkit.menu;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.CoreLog;
import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.bukkit.util.InventoryUtil.MovedItem;
import lombok.var;

public class MenuListener implements Listener {
	
	//Two possible interactions
	//a) SINGLE icon/slot affected, propagate a click
	//b) MULTIPLE slots affected; they all allowed a move
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void handle(InventoryCloseEvent e) {
		MenuAgent agent = getAgent(e.getInventory(), e.getPlayer());
		if(agent != null) {
			CoreLog.debug("Closing Menu with title " + e.getInventory().getTitle() + " for player: " + e.getPlayer().getName());
			agent.getMenu().clearViewer(agent);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void handle(InventoryOpenEvent e) {
		var inv = e.getInventory();
		if(inv.getHolder() instanceof Menu) {
			Menu menu = (Menu) inv.getHolder();
			menu.init();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void inv(InventoryClickEvent e) {
		MenuAgent agent = getAgent(e.getInventory(), e.getWhoClicked());
		if(agent != null) {
			var action = new MenuAction(agent, InventoryUtil.getResultOfEvent(e), e.getClick());
			boolean cancel = setCancel(e, action);
			if(cancel) { //button-like action
				if(action.getMovedItems().size() == 1) {
					asIcon(e, e.getRawSlot(), action).ifPresent(i->i.click(action));
				}
			} else { //Slot-like action?
				for(MovedItem mi : action.getMovedItems()) {
					//Should be impossible for both to be true
					asIcon(e, mi.getInitialSlot(), action).ifPresent(i->i.click(action));
					asIcon(e, mi.getFinalSlot(), action).ifPresent(i->i.click(action));
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void inv(InventoryDragEvent e) {
		MenuAgent agent = getAgent(e.getInventory(), e.getWhoClicked());
		if(agent != null) {
			var action = new MenuAction(agent, InventoryUtil.getResultOfEvent(e), ClickType.LEFT);
			
			boolean cancel = setCancel(e, action);
			if(!cancel) {
				for (Integer slot : e.getRawSlots()) {
					asIcon(e, slot, action).ifPresent(i->i.click(action));
				}
			}
		}
	}

	private Optional<Icon> asIcon(InventoryInteractEvent e, int slot, MenuAction a){
		if(slot < 0 || slot >= e.getInventory().getSize()) return Optional.empty();
		return Optional.ofNullable(a.getMenuAgent().getMenu().getIcon(slot));
	}
	
	//Returns if cancellation was invoked
	//If so, treat as click and enforce single-item moved
	private boolean setCancel(InventoryInteractEvent e, MenuAction a) {
		List<MovedItem> moved = a.getMovedItems();
		
		//Check if any immovable icons are being moved
		//if so cancel the event
		for(MovedItem mi : moved) {
			var item = mi.getItem();
			if(asIcon(e, mi.getInitialSlot(), a).filter(i->!i.mayInteract(new ItemStack(Material.AIR))).isPresent()) {
				e.setCancelled(true);
				return true;
			}
			
			if(asIcon(e, mi.getFinalSlot(), a).filter(i->!i.mayInteract(item)).isPresent()) {
				e.setCancelled(true);
				return true;
			}
		}
		return false;
	}
	
	private MenuAgent getAgent(Inventory inv, HumanEntity he) {
		var holder = inv.getHolder();
		if(holder instanceof Menu) {
			Menu menu = (Menu) holder;
			MenuAgent agent = menu.getAgent(he);
			Validate.notNull(agent);
			return agent;
		} else {
			return null;
		}
	}
	
}
