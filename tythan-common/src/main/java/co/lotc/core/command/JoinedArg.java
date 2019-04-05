package co.lotc.core.command;

import java.util.List;

import com.mojang.brigadier.arguments.StringArgumentType;

import co.lotc.core.agnostic.Sender;

public class JoinedArg extends CmdArg<String> {

	public JoinedArg(String name, String errorMessage, String defaultInput, String description) {
		super(name, errorMessage, defaultInput, description);
		this.setMapper(s->s);
		this.setBrigadierType(StringArgumentType.greedyString());
	}

	@Override
	String resolve(Sender s, List<String> input, int i) {
		List<String> relevantInput = input.subList(i, input.size());
		String joined = String.join(" ", relevantInput);
		return resolve(s, joined);
	}
	
}
