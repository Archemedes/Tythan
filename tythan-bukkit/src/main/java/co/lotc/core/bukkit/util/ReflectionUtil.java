package co.lotc.core.bukkit.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.comphenix.protocol.utility.MinecraftReflection;

public class ReflectionUtil {
	private ReflectionUtil() {}
	
	//NBT Compound tag construction
	private static Class<?> NBTTagCompound;
	private static Constructor<?> compoundConstructor;

	//All methods needed to make ItemUtil happen
	private static Method saveToJson;
	private static Method itemNameMethod;

	static{
		try{
			NBTTagCompound = MinecraftReflection.getMinecraftClass("NBTTagCompound");
			compoundConstructor = NBTTagCompound.getConstructor();
			
			saveToJson = MinecraftReflection.getItemStackClass().getMethod("save", NBTTagCompound);
			itemNameMethod = MinecraftReflection.getItemStackClass().getMethod("j");
			
		}catch(Throwable t){t.printStackTrace();}
	}
	
	public static Constructor<?> compoundConstructor() { return compoundConstructor; }
	
	public static Method saveToJson() { return saveToJson; }
	
	public static Method itemNameMethod() { return itemNameMethod; }
	
}
