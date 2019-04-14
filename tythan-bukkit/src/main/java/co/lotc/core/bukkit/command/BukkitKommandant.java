package co.lotc.core.bukkit.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import co.lotc.core.command.ArcheCommand;
import co.lotc.core.command.CmdArg;
import co.lotc.core.command.brigadier.Kommandant;
import lombok.var;

public class BukkitKommandant extends Kommandant {

	public BukkitKommandant(ArcheCommand built) {
		super(built);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected RequiredArgumentBuilder makeBuilderWithSuggests(String name, ArgumentType<?> type, CmdArg<?> arg) {
		var builder = RequiredArgumentBuilder.argument(name, type);
		if(arg.hasCustomCompleter()) {
			SuggestionProvider provider = new ArcheSuggestionProvider<>(arg);
			builder.suggests(provider);
		}
		return builder;
	}

}
