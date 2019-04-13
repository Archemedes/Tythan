package co.lotc.core.bukkit.util;

import static org.bukkit.event.inventory.InventoryAction.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Lists;

import co.lotc.core.CoreLog;
import co.lotc.core.CoreTimer;
import co.lotc.core.Tythan;
import lombok.var;

public class InventoryUtil {
	private InventoryUtil() {}
	
	/**
	 * Counts all instances of an itemstack across an inventory using {@link ItemStack#isSimilar(ItemStack)} as a comparator
	 * @param inv inventory to check in
	 * @param is item to check for
	 * @return count of item in all stacks
	 */
	public static int countInventory(Inventory inv, ItemStack is) {
		int count = 0;
		for(ItemStack other : inv.getContents()) {
			if(other == null) continue;
			if(other.isSimilar(is)) count += other.getAmount();
		}
		return count;
	}
	
	/**
	 * Checks if the inventory has no items at all
	 * @param inv Inventory to check
	 * @return if any ItemStack (non-null content) was found
	 */
	public static boolean isEmpty(Inventory inv) {
		for(ItemStack is : inv.getContents()) {
			if(is != null) return false;
		}
		return true;
	}
	
	public static void addOrDropItem(Player p, ItemStack... items) {
		addOrDropItem(p.getLocation(), p.getInventory(), items);
	}
	
	public static void addOrDropItem(Location location, Inventory inv, ItemStack... items) {
		var left = addItem(inv, items);
		left.forEach((k,is)->location.getWorld().dropItemNaturally(location, is));
	}
	
	public static List<ItemStack> getItems(Inventory inv){
		return Stream.of(inv.getContents()).filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	public static String serializeItems(Inventory inv) {
		return serializeItems(inv.getContents());
	}
	
	public static String serializeItems(ItemStack...items) {
		List<ItemStack> list = Lists.newArrayList(items);
		return serializeItems(list);
	}
	
	public static String serializeItems(List<ItemStack> items) {
		YamlConfiguration yaml = new YamlConfiguration();
		yaml.set("c", items.stream()
				.map(is->is==null?null:is.serialize())
				.collect(Collectors.toList())
				);
		return yaml.saveToString();
	}
	
	@SuppressWarnings("unchecked")
	public static List<ItemStack> deserializeItems(String listOfItems) {
		YamlConfiguration yaml = new YamlConfiguration();
		try {
			yaml.loadFromString(listOfItems);
			if(!yaml.isList("c")) throw new IllegalArgumentException("String must have list of items under key 'c'");
      return yaml.getList("c").stream()
          .map(ent -> (Map<String, Object>) ent)
          .map(ent -> ent == null ? null : ItemStack.deserialize(ent))
          .collect(Collectors.toList());
		} catch (InvalidConfigurationException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * See {@link InventoryUtil#addItem(Inventory, ItemStack...)}
	 */
    public static HashMap<Integer, ItemStack> addItem(final Inventory inv, Collection<ItemStack> items) {
    	return addItem(inv, items.toArray(new ItemStack[0]));
    }
	
	/**
	 * Like the Bukkit method, but respects max ItemStack size
	 * See also: {@link org.bukkit.inventory.Inventory#addItem(ItemStack...)}
	 * @param inv Inventory to add to
	 * @param items Items to add
	 * @return What couldn't be added
	 */
    public static HashMap<Integer, ItemStack> addItem(final Inventory inv, final ItemStack... items) {
        Validate.noNullElements(items, "Item cannot be null");
        final HashMap<Integer, ItemStack> leftover = new HashMap<>();
        for (int i = 0; i < items.length; ++i) {
            final ItemStack item = items[i];
            while (true) {
                final int firstPartial = firstPartial(item, inv);
                if (firstPartial == -1) {
                    final int firstFree = inv.firstEmpty();
                    if (firstFree == -1) {
                        leftover.put(i, item);
                        break;
                    }
                    final int max = Math.min(item.getMaxStackSize(), inv.getMaxStackSize());
                    if (item.getAmount() <= max) {
                        inv.setItem(firstFree, item);
                        break;
                    }
                    final ItemStack stack = item.clone();
                    stack.setAmount(max);
                    inv.setItem(firstFree, stack);
                    item.setAmount(item.getAmount() - max);
                }
                else {
                    final ItemStack partialItem = inv.getItem(firstPartial);
                    final int amount = item.getAmount();
                    final int partialAmount = partialItem.getAmount();
                    final int maxAmount = partialItem.getMaxStackSize();
                    if (amount + partialAmount <= maxAmount) {
                        partialItem.setAmount(amount + partialAmount);
                        break;
                    }
                    partialItem.setAmount(maxAmount);
                    item.setAmount(amount + partialAmount - maxAmount);
                }
            }
        }
        return leftover;
    }
    
    private static int firstPartial(final ItemStack item, final Inventory inv) {
        final ItemStack[] inventory = inv.getContents();
        if (item == null) {
            return -1;
        }
        final ItemStack filteredItem = item.clone();
        for (int i = 0; i < inventory.length; ++i) {
            final ItemStack cItem = inventory[i];
            if (cItem != null && cItem.getAmount() < cItem.getMaxStackSize() && cItem.isSimilar(filteredItem)) {
                return i;
            }
        }
        return -1;
    }
	
	
	/**
	 * Represents an item that was moved during a certain InventoryInteractEvent
	 * Magic values CURSOR_SLOT and DROPPED_SLOT represent items moved from
	 * and to cursor, or items dropped to the ground
	 * slots are always the raw slots
	 */
	public static class MovedItem{
		public static final int CURSOR_SLOT = -1;
		public static final int DROPPED_SLOT = -2;
		private final ItemStack is;
		private int initialSlot;
		private final int finalSlot;
		
		public MovedItem(ItemStack is, int initial, int fin) {
			this.is = is;
			initialSlot = initial;
			finalSlot = fin;
		}
		
		public ItemStack getItem() { return is; }
		public int getInitialSlot() { return initialSlot; }
		public int getFinalSlot() { return finalSlot; }
	}
	
	/**
	 * Gets a list of ItemStacks that is potentially affected by this Inventory Event if not modified or cancelled.
	 * This method is much coarser than {@link InventoryUtil#getResultOfEvent(InventoryInteractEvent)} and should execute faster, but is less accurate and provides less information
	 * Note that while this method may potentially return items that won't be moved, it should never fail to include ItemStacks that will be touched
	 * @param e The inventory event to study
	 * @return A list of ItemStacks this event MIGHT affect in vanilla. Note that the itemstacks in this list are mutable and modifying them will affect the provided event
	 */
	public static List<ItemStack> getTouchedByEvent(InventoryInteractEvent e){
		List<ItemStack> result = new ArrayList<>();
		String dbg = null;
		
		if(Tythan.get().isDebugging()) {
			dbg = "[Debug] InventoryUtil touched items " + (e instanceof InventoryClickEvent?
					((InventoryClickEvent) e).getAction() : ((InventoryDragEvent) e).getType());
			CoreTimer.startTiming(dbg);
		}
		
		if(e instanceof InventoryClickEvent) {
			InventoryClickEvent ev = (InventoryClickEvent) e;
			InventoryAction a = ev.getAction();
			switch(a) {
			case CLONE_STACK:
				//Cloning of stacks OUTSIDE of creative inventory (so player in creative mode but e.g. in chest)
				//Since this doesn't move any items (just makes them out of thin air), do nothing
				break;
			case COLLECT_TO_CURSOR:
				result.add(ev.getCursor());
				break;
			case DROP_ONE_CURSOR:
			case DROP_ALL_CURSOR:
				result.add(ev.getCursor());
				break;
			case DROP_ALL_SLOT:
			case DROP_ONE_SLOT:
				result.add(ev.getCurrentItem());
				break;
			case HOTBAR_MOVE_AND_READD:
			case HOTBAR_SWAP:
				int hotbar = ev.getHotbarButton();
				int hotbarRawSlot = e.getView().countSlots() - 14 + hotbar;
				if(ev.getView().getType() == InventoryType.CRAFTING) hotbarRawSlot += 4;
				ItemStack moved = ev.getCurrentItem();
				if(moved.getType() != Material.AIR) result.add(moved);
				moved = ev.getView().getItem(hotbarRawSlot);
				if(moved.getType() != Material.AIR) result.add(moved);
				break;
			case MOVE_TO_OTHER_INVENTORY:
				result.add(ev.getCurrentItem());
				break;
			case PICKUP_ALL:
			case PICKUP_HALF:
			case PICKUP_ONE:
			case PICKUP_SOME:
				result.add(ev.getCurrentItem());
				break;
			case PLACE_ALL:
				//For some reason this is the action for all creative menu actions
				//We don't want to mess with this or add items unnecessarily.
				if(ev.getView().getType() == InventoryType.CREATIVE) break;
			case PLACE_ONE:
			case PLACE_SOME:
				result.add(ev.getCursor());
				break;
			case SWAP_WITH_CURSOR:
				if(ev.getCurrentItem().getType() != Material.AIR) result.add(ev.getCurrentItem());
				result.add(ev.getCursor());
				break;
			default:
				break;
			}
		} else { //Must be DragEvent
			InventoryDragEvent ev = (InventoryDragEvent) e;
			result.add(ev.getCursor()); //Cursor dragged along multiple spots
		}
		
		
		CoreTimer.stopTiming(dbg);
		return result;
	}
	
	/**
	 * Predictive method to study an event and returning a list of ItemStacks being affected as well as the inventory slots they are moved from and to).
	 * The itemstacks returned by this method are non-reflective. Changing them does not alter the event or involved inventories
	 * This method should be accurate for everything except MOVE_TO_OTHER_INVENTORY
	 * which has issues for {@link InventoryType} ANVIL, BEACON, BREWING, ENCHANTING and FURNACE
	 * @param e Event to handle
	 * @return a list of MovedItems for this event given vanilla Minecraft behavior
	 */
	public static List<MovedItem> getResultOfEvent(InventoryInteractEvent e){
		String dbg = null;

		List<MovedItem> result = new ArrayList<>();
		
		try {
			if(Tythan.get().isDebugging()) {
				dbg = "[Debug] InventoryUtil moved items " + (e instanceof InventoryClickEvent?
						((InventoryClickEvent) e).getAction() : ((InventoryDragEvent) e).getType());
				CoreTimer.startTiming(dbg);
			}

			if(e.isCancelled()) return result;

			if(e instanceof InventoryClickEvent) {
				InventoryClickEvent ev = (InventoryClickEvent) e;
				if(ev.getClick() == ClickType.CREATIVE) return result;


				ItemStack is;
				int amount, raw;
				InventoryAction a = ev.getAction();
				switch(a) {
				case CLONE_STACK:
					//Cloning of stacks OUTSIDE of creative inventory (so player in creative mode but e.g. in chest)
					//Since this doesnt move any items (just makes them out of thin air), do nothing
					break;
				case COLLECT_TO_CURSOR:
					is = ev.getCursor();
					//Full stack can't collect other items
					if(is.getAmount() >= is.getMaxStackSize()) return result;

                    //Doing the double click over an occupied slot doesn't cause a collect
                    if(ev.getCurrentItem().getType() != Material.AIR) return result;
					
					InventoryView v = ev.getView();
					InventoryType type = v.getType();
					int upper = v.countSlots() - (v.getType() == InventoryType.CRAFTING? 0 : 5);
					int count = is.getAmount();
					List<Integer> collected = Lists.newArrayList();
					//Goes in 2 phases: First collect from non-maxed stacks, then also maxed stacks
					for(int phase = 0; phase < 2; phase++) {
						for(int j = 0; j < upper; j++) {
							if((type == InventoryType.CRAFTING || type == InventoryType.WORKBENCH) && j == 0)
								continue; //Can't collect from the crafting result slot
							ItemStack is2 = v.getItem(j);
							if(!collected.contains(j) && is.isSimilar(is2) && is2.getAmount() <= is.getMaxStackSize()
									&& (phase == 1 || is2.getAmount() != is2.getMaxStackSize())) {
								is2.getAmount();
								int toAdd = Math.min(is.getMaxStackSize() - is.getAmount(), is2.getAmount());
								is2 = is2.clone();
								is2.setAmount(toAdd);
								result.add(new MovedItem(is2, j, MovedItem.CURSOR_SLOT));
								count += toAdd;
								if(count >= is.getMaxStackSize()) return result;
								collected.add(j);
							}
						}
					}
					break;
				case DROP_ALL_CURSOR: case DROP_ONE_CURSOR:
					is = ev.getCursor().clone();
					if(a == DROP_ONE_CURSOR) {
						is.setAmount(1);
					}
					result.add(new MovedItem(is, MovedItem.CURSOR_SLOT, MovedItem.DROPPED_SLOT));
					break;
				case DROP_ALL_SLOT: case DROP_ONE_SLOT:
					is = ev.getCurrentItem().clone();
					if(a == DROP_ONE_SLOT) is.setAmount(1);
					result.add(new MovedItem(is, ev.getRawSlot(), MovedItem.DROPPED_SLOT));
					break;
				case HOTBAR_MOVE_AND_READD: //This is chosen instead of HOTBAR_SWAP if:
					//there's an item in the hovered-over slot &&
					//the targeted hotbar slot is not empty &&
					//the hovered-over slot is in the top inventory || slot doesn't allow the targeted hotbar item

					//Items are moved only if an exchange of 2 ItemStacks is done between top inventory to hotbar
					//And slot.isAllowed(hotbarStack) == true
				case HOTBAR_SWAP:
					int hotbar = ev.getHotbarButton();
					int hotbarRawSlot = e.getView().countSlots() - 14 + hotbar;
					if(ev.getView().getType() == InventoryType.CRAFTING) hotbarRawSlot += 4;
					raw = ev.getRawSlot();
					is = ev.getView().getItem(hotbarRawSlot);

					if(raw != hotbarRawSlot && isItemAllowed(raw, is, ev.getView())) {
						//Enchanting table / horse armor item slot only accepts count 1
						boolean enchanting = isEnchantingSlot(raw, ev.getView());
						if(is.getAmount() > 1 && enchanting) {
							if(a == HOTBAR_SWAP) {
								is = is.clone();
								is.setAmount(1);
							} else { //HOTBAR_MOVE_AND_READD.
								if(is.getAmount() > is.getMaxStackSize()) { //Very rare fringe case
									v = ev.getView();
									int spot = MovedItem.DROPPED_SLOT;
									int topSize = ev.getView().getTopInventory().getSize();
									int[] slotsToCheck = new int[36];
									ItemStack target = ev.getCurrentItem();
									//slotsToCheck[0] = ev.getWhoClicked().getInventory().getHeldItemSlot() + topSize + 27;
									for(int i = 0; i < 9; i++) slotsToCheck[i] = topSize + 27 + i;
									for(int i = 0; i < 27; i++) slotsToCheck[i+9] = topSize + i;
									for(int phase = 0; phase < 2; phase++) {
										for(int i = 0; i < slotsToCheck.length; i++) {
											int slotToCheck = slotsToCheck[i];
											if(i != 0 && slotToCheck == slotsToCheck[0]) continue;
											ItemStack toCheck = v.getItem(slotToCheck);
											if( (phase == 0 && target.isSimilar(toCheck) && toCheck.getAmount() < toCheck.getMaxStackSize())
												|| (phase == 1 &&  toCheck.getType() == Material.AIR)) {
												System.out.println("yes");
												spot = slotToCheck;
												break;
											}
										}
										
										if(spot > 0) break;
									}
									result.add(new MovedItem(target.clone(), raw, spot));
									is = is.clone();
									is.setAmount(1);
									result.add(new MovedItem(is, hotbarRawSlot, raw));
								}
								
								return result; //Both slots occupied but hotbar is full
							}
						}
						
						if(is.getType() != Material.AIR) result.add(new MovedItem(is.clone(), hotbarRawSlot, raw));
						is = ev.getCurrentItem();
						if(is.getType() != Material.AIR) result.add(new MovedItem(is.clone(), raw, hotbarRawSlot));
					}
					break;
				case MOVE_TO_OTHER_INVENTORY:
					//Just cancel this event tbh
					handleMoveToOther(result, ev.getRawSlot(), ev.getView());
					break;
				case PICKUP_ALL: case PICKUP_HALF:case PICKUP_ONE:
					is = ev.getCurrentItem().clone();
					amount = a == PICKUP_ALL? is.getAmount() :
						a == PICKUP_HALF? is.getAmount()/2 + is.getAmount()%2 : 1;
						is.setAmount(amount);
						result.add(new MovedItem(is, ev.getRawSlot(), MovedItem.CURSOR_SLOT));
						break;
				case PICKUP_SOME: //When an itemstack is oversized (rare case)
					is = ev.getCurrentItem().clone();
					int stackSize = Math.min(is.getMaxStackSize(), ev.getClickedInventory().getMaxStackSize());
					int initial = ev.getCurrentItem().getAmount(); //For this InventoryAction: initial > stackSize
					amount = -1 * (stackSize - initial);
					result.add(new MovedItem(is, ev.getRawSlot(), MovedItem.CURSOR_SLOT));
				case PLACE_ALL: case PLACE_ONE: case PLACE_SOME:
					is = ev.getCursor().clone(); //Item in cursor is gonna be placed;
					raw = ev.getRawSlot();
					if(isItemAllowed(raw, is, ev.getView())) {
						boolean enchanting = isEnchantingSlot(raw, ev.getView());
						if(enchanting && ev.getCurrentItem().getType() != Material.AIR) return result;
						amount = enchanting? 1 :
							a == PLACE_ALL? is.getAmount() :
							a == PLACE_ONE? 1 : //else it's place some
								Math.min(is.getMaxStackSize(), ev.getClickedInventory().getMaxStackSize()) - ev.getCurrentItem().getAmount();
						
						is.setAmount(amount);
						result.add(new MovedItem(is, MovedItem.CURSOR_SLOT, raw));
					}
					break;
				case SWAP_WITH_CURSOR:
					is = ev.getCursor().clone();
					result.add(new MovedItem(is, MovedItem.CURSOR_SLOT, ev.getRawSlot()));
					is = ev.getCurrentItem().clone();
					result.add(new MovedItem(is, ev.getRawSlot(), MovedItem.CURSOR_SLOT));
				default:
					break;

				}
			} else { //must be drag since only 2 subinterfaces in Bukkit
				InventoryDragEvent ev = (InventoryDragEvent) e;
				ev.getNewItems().forEach( (s,is) ->result.add(new MovedItem(is, MovedItem.CURSOR_SLOT, s)));
			}


			return result;
		}finally {
			CoreTimer.stopTiming(dbg);
		}
	}
	
	private static boolean isEnchantingSlot(int raw, InventoryView v) {
		return (raw == 0 && v.getType() == InventoryType.ENCHANTING)
				|| (raw == 1 && v.getType() == InventoryType.CHEST &&
				v.getTopInventory().getHolder() instanceof Animals);
	}
	
	private static Method civ_getHandle = null;
	private static Method con_getSlot = null;
	private static Method slot_isAllowed = null;
	
	private static boolean isItemAllowed(int rawSlot, ItemStack is, InventoryView view) {
		if(is.getType() == Material.AIR) return true;
		Object nmsItem = MinecraftReflection.getMinecraftItemStack(is);
		Object isAllowed = null;
		try {
			if(civ_getHandle == null) civ_getHandle = view.getClass().getMethod("getHandle");
			Object container = civ_getHandle.invoke(view);
			if(con_getSlot == null) con_getSlot = container.getClass().getMethod("getSlot", int.class);
			Object slot = con_getSlot.invoke(container, rawSlot);
			if(slot_isAllowed == null) slot_isAllowed = slot.getClass().getMethod("isAllowed", nmsItem.getClass());
			slot_isAllowed.setAccessible(true);
			isAllowed = slot_isAllowed.invoke(slot, nmsItem);
			return (boolean) isAllowed;
		} catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Reflection failed. MC probably changed its method handles. This is bad. Call someone", e);
		} catch (ClassCastException e) {
			String clss = isAllowed == null? "null" : isAllowed.getClass().getName();
			CoreLog.severe("bad class cast on isItemAllowed. Wanted boolean return but got " + clss + " from " + nmsItem);
			throw e;
		}
	}
	
	private static void handleMoveToOther(List<MovedItem> result, int raw, InventoryView view) {
		final int topSize = view.getTopInventory().getSize();
	
		switch(view.getType()) {
		case ANVIL:
		case BEACON:
		case BREWING:
		case ENCHANTING:
		case FURNACE:
			//Honestly for most I don't have the slightest clue what's gonna happen
			//Assume that the clicked item goes to the other inventory, but leave it there.
			int possibleTargetSpot = raw < view.getTopInventory().getSize()?
					view.getTopInventory().getSize() : 0;
			result.add(new MovedItem(view.getItem(raw).clone(), raw, possibleTargetSpot));
		case CHEST:
		case ENDER_CHEST:
		case DISPENSER:
		case DROPPER:
		case HOPPER:
		case SHULKER_BOX:
			boolean topToBottom = raw < topSize;
			int initialValue = topToBottom?  view.countSlots() - 6 : 0;
			int finalValue = topToBottom? topSize-1 : topSize;
			int modder = topToBottom? -1 : 1;
			
			//Could be donkey or lama. Some special code needed to handle this
			ItemStack hacker = null;
			int removed = -1;
			if(!topToBottom && view.getTopInventory().getHolder() instanceof Animals) {
				hacker = view.getItem(raw);
				for(int i = 0; i < 2; i++) {
					if(view.getItem(i).getType() == Material.AIR && isItemAllowed(i, hacker, view)) {
						//Single item will probably moved here
						ItemStack equipable = hacker.clone();
						result.add(new MovedItem(equipable, raw, i));
						if( i == 0 && hacker.getAmount() > 64) { //65+ saddles wtf you should never reach this unless you are trying to piss me off
							equipable.setAmount(64);
							hacker.setAmount(hacker.getAmount() - 64); //This is propagated to the InventoryView
							removed = 64;
							break; //Don't do i == 1 since there's no way the item matches both horse slots
						}else if( (i == 1 && hacker.getAmount() > 1) ) {
							equipable.setAmount(1);
							hacker.setAmount(hacker.getAmount() - 1); //This is propagated to the InventoryView
							removed = 1;
						} else {
							return;
						}
						
					}
				}
				
				initialValue = 2;
			}
			
			moveToOtherMainLoop(result, view, raw, topToBottom, initialValue, finalValue, modder);
			if(removed > 0) hacker.setAmount(hacker.getAmount()+removed);
			break;
		case CRAFTING: //This is the player inventory view
			//Behavior MOSTLY like MERCHANT and WORKBENCH
			//Exception is that some special equippables in the bottom inventory work differently
			if(raw == 0) { //Crafting result slot
				initialValue = view.countSlots() - 6;
				finalValue = topSize-1;
				modder = -1;
				moveToOtherMainLoop(result, view, raw, true, initialValue, finalValue, modder);
			} else if(raw < 9) { //topSize == 5 here, this moves a crafting ingredient slot OR an armor slot
				initialValue = 9; //Skips armor slots as target slots
				finalValue = 45; //Cannot move into the shield slot so skip slot 45
				modder = 1;
				moveToOtherMainLoop(result, view, raw, true, initialValue, finalValue, modder);
			} else {
				//Inventory slot, move hotbar to invspace or vice-versa
				ItemStack is = view.getItem(raw);
				
				//Special pre-treatment of shields, which get moved to offhand
				if(is.getType() == Material.SHIELD && view.getItem(45).getType() == Material.AIR) {
					result.add(new MovedItem(is.clone(), raw, 45));
					return;
				}
				
				//Special treatment of armor
				boolean performAnUglyHack = false;
				for(int i = 5; i < 9; i++) {
					if(view.getItem(i).getType() == Material.AIR && isItemAllowed(i, is, view)) {
						ItemStack armor = is.clone();
						result.add(new MovedItem(armor, raw, i));
						int amount = is.getAmount(); //Amount to move to the next inventory
						if(amount > 1) { //mod has stacked armor. Must keep going through  main loop
							armor.setAmount(1);
							is.setAmount(amount - 1); //This is propagated to the InventoryView
							performAnUglyHack = true;
							break;
						} else { //armor was moved to armor slot, no further action needed
							return;
						}
					}
				}
				
				//Now contraints are taken care of handle like the main loop
				int invSpaceSize = 36; //The 27 slots over the armor slot
				boolean toHotbar = raw < invSpaceSize; //clicked spots in the invspace get moved to hotbar
				initialValue = toHotbar? invSpaceSize : 9;
				finalValue = toHotbar? invSpaceSize + 9: invSpaceSize;
				moveToOtherMainLoop(result, view, raw, true, initialValue, finalValue, 1);
				if(performAnUglyHack) is.setAmount(is.getAmount() + 1);
			}
			break;
		case MERCHANT:
		case WORKBENCH:
		//Behavior of these inventories:
		//Anything in the bottom (player) inv is moved from hotbar to invspace or invspace to hotbar
		//The crafting result slot (raw slot 0) gets moved to inventory in reverse order
		if(raw == 0) { //Crafting result slot
			initialValue = view.countSlots() - 6;
			finalValue = topSize-1;
			modder = -1;
			moveToOtherMainLoop(result, view, raw, true, initialValue, finalValue, modder);
		} else if(raw < topSize) { //One of the crafting slots
			initialValue = topSize;
			finalValue = view.countSlots() - 5;
			modder = 1;
			moveToOtherMainLoop(result, view, raw, true, initialValue, finalValue, modder);
		} else { //Inventory slot, move hotbar to inspace or vice-versa
			int invSpaceSize = topSize + 27;
			boolean toHotbar = raw < invSpaceSize; //clicked spots in the invspace get moved to hotbar
			initialValue = toHotbar? invSpaceSize : topSize;
			finalValue = toHotbar? invSpaceSize + 9: invSpaceSize;
			moveToOtherMainLoop(result, view, raw, true, initialValue, finalValue, 1);
		}
		break;
		default:
			break;
		
		}
	}
	
	private static void moveToOtherMainLoop(List<MovedItem> result, InventoryView view, int raw,
			boolean topToBottom, int initialValue, int finalValue, int modder) {
		ItemStack is = view.getItem(raw);
		int amount = is.getAmount(); //Amount to move to the next inventory
		boolean oversized = amount > is.getMaxStackSize();
		int maxInvRoom = topToBottom? view.getBottomInventory().getMaxStackSize() : view.getTopInventory().getMaxStackSize();
		int maxRoom = oversized? maxInvRoom : Math.min(is.getMaxStackSize(), maxInvRoom);
				
		//Moving goes in 2 phases: First fill up existing stacks, then look for empty slots
		for(int phase = (oversized? 1 : 0); phase < 2; phase++) {
			for(int i = initialValue; i != finalValue; i += modder) {
				ItemStack slot = view.getItem(i);
				if( (phase == 0 && slot.isSimilar(is)) || (phase == 1 && slot.getType() == Material.AIR)) {
					int room = maxRoom - slot.getAmount();
					if(room > 0) {
						int toMove = Math.min(room, amount);
						ItemStack moved = is.clone();
						moved.setAmount(toMove);
						result.add(new MovedItem(moved, raw, i));
						amount -= toMove;
						if(amount <= 0) return;
					}
				}
			}
		}
	}
}
