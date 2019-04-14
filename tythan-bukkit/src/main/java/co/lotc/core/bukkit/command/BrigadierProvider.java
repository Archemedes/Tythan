package co.lotc.core.bukkit.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;

import co.lotc.core.CoreLog;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.var;

public class BrigadierProvider {
	private static final BrigadierProvider INSTANCE = new BrigadierProvider();
	public static BrigadierProvider get() { return INSTANCE; }
	
	@Getter private boolean functional = true;

	private Method getServer;
	private Method getMCDispatcher;
	private Method getBrigadier;
	private Method getItemStack;
	
	private Constructor<ArgumentType<Object>> itemStackArgument;
	
	private Method getBukkitSender = null;
	
	@SuppressWarnings("unchecked")
	private BrigadierProvider() {
		try {
			var serverClass = MinecraftReflection.getMinecraftServerClass();
			getServer = serverClass.getMethod("getServer");
			getMCDispatcher = serverClass.getDeclaredMethod("getCommandDispatcher");
			getMCDispatcher.setAccessible(true);
			getBrigadier = reflectBrigadierGetter();
			getItemStack = reflectItemStackGetter();
			
			itemStackArgument = (Constructor<ArgumentType<Object>>) MinecraftReflection.getMinecraftClass("ArgumentItemStack").getConstructor();
			
		} catch(Exception e) {
			CoreLog.severe("We were unable to set up the BrigadierProvider. Likely a reflection error!");
			functional = false;
			e.printStackTrace();
		}
	}
	
	private Method reflectBrigadierGetter() throws Exception {
		var dispatcherClass = MinecraftReflection.getMinecraftClass("CommandDispatcher");
		for(var xx : dispatcherClass.getDeclaredMethods()) {
			if(xx.getParameterCount() == 0 && CommandDispatcher.class.isAssignableFrom(xx.getReturnType()))
				return xx;
		}
		
		throw new NoSuchMethodError("CommandDispatcher getter in Minecraft");
	}
	
	private Method reflectItemStackGetter() throws Exception {
		var dispatcherClass = MinecraftReflection.getMinecraftClass("ArgumentPredicateItemStack");
		var itemStackClass = MinecraftReflection.getItemStackClass();
		for(var xx : dispatcherClass.getDeclaredMethods()) {
			if(xx.getParameterCount() == 2
					&& itemStackClass.isAssignableFrom(xx.getReturnType())
					&& xx.getParameters()[0].getType() == int.class
					&& xx.getParameters()[1].getType() == boolean.class)
				return xx;
		}
		
		throw new NoSuchMethodError("ArgumentPredicateItemStack getter for ItemStack");
	}
	
	@SuppressWarnings("unchecked")
	public CommandDispatcher<Object> getBrigadier() {
		Validate.isTrue(functional);
		try {
			var server = getServer.invoke(null); //Static MinecraftServer getter
			var dispatch = getMCDispatcher.invoke(server);
			return (CommandDispatcher<Object>) getBrigadier.invoke(dispatch);
		} catch(Exception e) {
			CoreLog.severe("Brigadier Decided to crash on us after startup time");
			functional = false;
			return null;
		}
	}
	
	public ArgumentType<Object> argumentItemStack(){
		try {
			return itemStackArgument.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Method getItemStackParser() {
		return getItemStack;
	}
	
	@SneakyThrows
	public CommandSender getBukkitSender(Object commandListenerWrapper) {
		if(getBukkitSender == null) {
			getBukkitSender = commandListenerWrapper.getClass().getMethod("getBukkitSender");
		}
		
		return (CommandSender) getBukkitSender.invoke(commandListenerWrapper);
	}
	

}
