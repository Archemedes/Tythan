package co.lotc.core.command;

import java.util.List;

import com.mojang.brigadier.arguments.StringArgumentType;

import co.lotc.core.agnostic.Sender;

public class ArrayArgs extends CmdArg<String[]> {

	public ArrayArgs(String name, String errorMessage, String defaultInput, String description) {
		super(name, errorMessage, defaultInput, description);
		this.setMapper(s->s.split(" "));
		this.setBrigadierType(StringArgumentType.greedyString());
	}

	//Goes from a list of strings to a single string joined by whitespace
	//Which is then turned into a String array. Not my proudest work
	
	@Override
	String[] resolve(Sender s, List<String> input, int i) {
		List<String> relevantInput = input.subList(i, input.size());
		String joined = String.join(" ", relevantInput);
		return resolve(s, joined);
	}
	

}
