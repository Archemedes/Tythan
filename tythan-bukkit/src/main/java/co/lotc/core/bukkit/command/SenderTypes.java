package co.lotc.core.bukkit.command;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import co.lotc.core.agnostic.Sender;
import co.lotc.core.bukkit.wrapper.BukkitSender;

public final class SenderTypes {

	private SenderTypes() { }

	private static Function<Sender, CommandSender> function = s->((BukkitSender) s).getHandle();
	private static Supplier<List<String>> playerCompleter = ()->Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
	
	public static void registerCommandSenderType() {
		Commands.defineArgumentType(CommandSender.class).senderMapper(function).register();
	}
	
	public static void registerPlayerType() {
		Commands.defineArgumentType(Player.class)
			.senderMapper(function.andThen(s-> (s instanceof Player)? ((Player)s):null))
			.mapper(s->{
				if(s.length() == 36) {
					try {return Bukkit.getPlayer(UUID.fromString(s));}
					catch(IllegalArgumentException e) {return null;}
				} else {
					return Bukkit.getPlayer(s);
				}
			})
			.completer(playerCompleter)
			.register();
	}
	
	@SuppressWarnings("deprecation")
	public static void registerOfflinePlayerType() {
		Commands.defineArgumentType(OfflinePlayer.class)
		.mapper(s->{
			UUID u = uuidFromString(s);
			
			OfflinePlayer op = null;
			if(u != null) {
				op = Bukkit.getOfflinePlayer(u);
			} else {
				op = Bukkit.getOfflinePlayer(s); //Deprecated
			}
			//TODO: maybe do this in AC for the more powerful name registry?
			if(op != null && op.hasPlayedBefore()) return op;
			else return null;
		})
		.completer(playerCompleter)
		.register();
	}
	
	private static UUID uuidFromString(String s) {
		if(s.length() == 32)
			s = s.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5");
			
		if(s.length() == 36) {
			try {return UUID.fromString(s);}
			catch(IllegalArgumentException e) {return null;}
		}
		return null;
	}
	
}
