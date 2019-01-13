package co.lotc.core.bukkit.util;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class LocationUtil {
	private LocationUtil() {}

	public static boolean isClose(Entity e, Location l) {
		return isClose(e, l, 5);
	}
	
	public static boolean isClose(Entity e, Location l, double maxDist) {
		return isClose(e.getLocation(), l, maxDist);
	}
	
	public static boolean isClose(Entity e1, Entity e2) {
		return isClose(e1, e2, 5);
	}
	
	public static boolean isClose(Entity e1, Entity e2, double maxDist) {
		return isClose(e1.getLocation(), e2.getLocation(), maxDist);
	}
	
	public static boolean isClose(Location l1, Location l2) {
		return isClose(l1, l2, 5);
	}
	
	public static boolean isClose(Location l1, Location l2, double maxDist) {
		return l1.getWorld() == l2.getWorld() && l1.distanceSquared(l2) <= maxDist*maxDist;
	}

	public static <T extends Entity> T getNearest(Entity e, List<T> ents) {
		ents.remove(e);
		return getNearest(e.getLocation(), ents);
	}
	
	public static <T extends Entity> T getNearest(Location l, List<T> ents) {
		double dist = 99999999999999999999999d;
		T result = null;
		for(T e : ents) {
			double dist2 = l.distanceSquared(e.getLocation());
			if(dist2 < dist) {
				dist = dist2;
				result = e;
			}
		}
		return result;
	}
	
	public static boolean isFloating(LivingEntity le) {
		return isInWater(le) || isInLava(le);
	}
	
	public static boolean isInLava(LivingEntity le) {
		Block b = le.getLocation().getBlock();
		return b.getType() == Material.LAVA;
	}
	
	public static boolean isInWater(LivingEntity le) {
		Block b = le.getLocation().getBlock();
		return b.getType() == Material.WATER;
	}
}
