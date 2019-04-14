package co.lotc.core.bungee.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import co.lotc.core.command.ArcheCommand;
import co.lotc.core.command.CmdArg;
import co.lotc.core.command.brigadier.Kommandant;
import lombok.var;
import net.md_5.bungee.protocol.packet.Commands;

public class BungeeKommandant extends Kommandant {

	public BungeeKommandant(ArcheCommand built) {
		super(built);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected RequiredArgumentBuilder makeBuilderWithSuggests(String name, ArgumentType<?> argumentType, CmdArg<?> arg) {
		//So basically Bungee Commands packet only lets us encode StringArgumentType
		//There are no PROPER_PROVIDERS for other types, causing an error trying to write the packet
		//Because intercepting a packet is the main handle for bungee to change the brigadier command nodes
		//We must restrict ourselves to the StringArgumentType for bungee types
		if(!(argumentType instanceof StringArgumentType))
			argumentType = StringArgumentType.word();
		
		var builder = RequiredArgumentBuilder.argument(name, argumentType);
		if(arg.hasCustomCompleter()) builder.suggests(Commands.SuggestionRegistry.ASK_SERVER);
		return builder;
	}

}
