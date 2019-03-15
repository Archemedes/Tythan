package co.lotc.core.bukkit.command;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Function;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class ItemArg {
	private static final BrigadierProvider provider = BrigadierProvider.get();

	public static void buildMaterialParameter(){
		Commands.defineArgumentType(Material.class)
		.brigadierType(provider.argumentItemStack())
		.mapper( parse.andThen(ItemStack::getType) )
		.defaultName("item")
		.defaultError("please specify a valid item name")
		.register();
	}
	
	public static void buildItemStackParameter(){
		Commands.defineArgumentType(ItemStack.class)
			.brigadierType(provider.argumentItemStack())
			.mapper(parse)
			.defaultName("itemstack")
			.defaultError("please specify a valid item name")
			.register();
	}
	
	private static Function<String, ItemStack> parse = input->{
		try {
			Object argumentPredicateItemStack = provider.argumentItemStack().parse(new StringReader(input));
			Object nmsStack = provider.getItemStackParser().invoke(argumentPredicateItemStack, 1, false);
			ItemStack is = MinecraftReflection.getBukkitItemStack(nmsStack);
			return is;
		} catch (CommandSyntaxException e) { //User Parsing error
			return null;
		} catch (Exception e) { //Unexpected error
			e.printStackTrace();
			return null;
		}
	};
	
}
