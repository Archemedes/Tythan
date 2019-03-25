package co.lotc.core.bukkit.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import lombok.Getter;

/**
 * Represents a block that does not keep strong references to any CraftBukkit or Minecraft objects, allowing for
 * worry-free storage of them into Collections.
 */
public class WeakBlock implements ConfigurationSerializable {
	@Getter private final String world;
	@Getter private final int x,y,z;

	public WeakBlock(World world, int x, int y, int z){
		this.world = world.getName();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public WeakBlock(String world, int x, int y, int z){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	

	public WeakBlock(Block b){
		this(b.getWorld(), b.getX(), b.getY(), b.getZ());
	}
	
	public static WeakBlock deserialize(Map<String, Object> map) {
		return new WeakBlock(map.get("world").toString(),
				(int) map.get("x"),
				(int) map.get("y"),
				(int) map.get("z")
				);
	}

	public WeakBlock(Location location) {
		this(location.getBlock());
	}

	public int getChunkX() {
		return x>>4;
	}
	
	public int getChunkZ() {
		return z>>4;
	}
	
	@Override
	public int hashCode(){
		return (this.y << 24 ^ this.x ^ this.z) + (world == null? 0 : 31 * world.hashCode());
	}

	public Location toLocation(){
		World w = Bukkit.getWorld(getWorld());
		int x = getX();
		int y = getY();
		int z = getZ();
		return new Location(w, x, y, z);
	}

	public boolean isValid() {
		return Bukkit.getWorld(world) != null;
	}

	@Override
	public boolean equals(Object o){
		if(!(o instanceof WeakBlock)) return false;
		WeakBlock other = (WeakBlock) o;

		return this.x == other.x && this.y == other.y && this.z == other.z && ObjectUtils.equals(this.world, other.world);
	}
	
	public World getBukkitWorld() {
		return Bukkit.getWorld(this.world);
	}
	
	@Override
	public String toString() {
		return world + ':' + x + ':' + y + ':' + z;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> r = new LinkedHashMap<>();
		r.put("world", world);
		r.put("x", x);
		r.put("y", y);
		r.put("z", z);
		return r;
	}
	
}
