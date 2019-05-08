package co.lotc.core.bukkit.command;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import co.lotc.core.agnostic.Sender;
import co.lotc.core.bukkit.wrapper.BukkitSender;
import co.lotc.core.command.CmdArg;
import co.lotc.core.command.CommandCompleter.Suggestion;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArcheSuggestionProvider<T> implements SuggestionProvider<T> {
	private final CmdArg<T> arg;

	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<T> context, SuggestionsBuilder builder) throws CommandSyntaxException {
		T source = context.getSource();
		Sender sender = new BukkitSender(BrigadierProvider.get().getBukkitSender(source));
		
		for(Suggestion suggestion : arg.getCompleter().suggest(sender, builder.getRemaining())) {
			String sugg = suggestion.getLiteral();
			if (sugg.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
				if(suggestion.hasTooltip()) builder.suggest(sugg, new LiteralMessage(suggestion.getTooltip()) );
				else builder.suggest(sugg);
			}
		}

		return builder.buildFuture();
	}

}
