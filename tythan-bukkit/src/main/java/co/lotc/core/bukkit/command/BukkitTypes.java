package co.lotc.core.bukkit.command;

import co.lotc.core.agnostic.Sender;
import co.lotc.core.command.ParameterType;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class BukkitTypes {

	public static final BiFunction<Sender, String, World> UNWRAP_WORLD = (sender, worldName) -> {
		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			world = Bukkit.getWorld(UUID.fromString(worldName));
		}
		return world;
	};
	public static final Supplier<List<String>> WORLD_COMPLETER = () -> Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());

	public static void registerWorldType() {
		new ParameterType<>(World.class)
				.defaultName("World")
				.defaultError("Could not locate world")
				.mapperWithSender(UNWRAP_WORLD)
				.completer(WORLD_COMPLETER)
				.register();
	}
}
