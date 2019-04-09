package co.lotc.core.bukkit.convo;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class ClickStream {

	private ClickStream() {}
	
	
	public static void selectBlock(Player p, Consumer<Block> callback) {
		selectBlock(p, callback, b->true);
	}
	
	public static void selectBlock(Player p, Consumer<Block> callback, Predicate<Block> filter) {
		//TODO
	}
	
	public static void selectPlayer(Player p, Consumer<Player> callback) {
		selectEntity(p, e->callback.accept((Player) e), Player.class::isInstance);
	}
	
	public static void selectEntity(Player p, Consumer<Entity> callback) {
		selectEntity(p, callback, e->true);
	}
	
	public static void selectEntity(Player p, Consumer<Entity> callback, Predicate<Entity> filter) {
		//TODO
	}

}
