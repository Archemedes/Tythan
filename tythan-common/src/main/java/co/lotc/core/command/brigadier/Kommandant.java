package co.lotc.core.command.brigadier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

import co.lotc.core.command.ArcheCommand;
import co.lotc.core.command.CmdArg;
import co.lotc.core.command.HelpCommand;
import lombok.RequiredArgsConstructor;
import lombok.var;

@RequiredArgsConstructor
public class Kommandant {
	private final ArcheCommand head;
	private final List<CommandNode<Object>> rootNodes = new ArrayList<>();
	
	public void addBrigadier() {
		rootNodes.add(buildNode(head, null));
	}

	public List<CommandNode<Object>> getNodes(){
		return Collections.unmodifiableList(rootNodes);
	}
	
	private CommandNode<Object> buildNode(ArcheCommand cmd, CommandNode<Object> dad) {
		var builder = LiteralArgumentBuilder.literal(cmd.getMainCommand());
		if(!cmd.hasArgs() && !cmd.isEmptyCommand()) builder.executes($->0);
		var node = builder.build();
		
		for(var sub : cmd.getSubCommands()) {
			if(sub instanceof HelpCommand) continue;
			var subNode = buildNode(sub, node); //Recurses
			node.addChild(subNode);
		}
		
		CommandNode<Object> argument = null;
		Map<String, Integer> namesUsed = new HashMap<>();
		var queue = new LinkedList<>(cmd.getArgs());
		while(!queue.isEmpty()) {
			var arg = queue.poll();
			var next = queue.peek();
			boolean executes = next == null || next.hasDefaultInput();
			
			//Adds numbers to duplicate names: prevents crashes and gray-text rubbish
			String name = arg.getName();
			Integer value = namesUsed.compute(name, (k,v)->v==null? 1 : v+1);
			if(value > 1) name = name+value;
			
			CommandNode<Object> nextArg = buildNodeForArg(name, arg, executes);
			if(argument == null) node.addChild(nextArg);
			else argument.addChild(nextArg);
			
			argument = nextArg;
		}
		 
		redirectAliases(cmd, dad, node);
		return node;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private CommandNode<Object> buildNodeForArg(String name, CmdArg<?> arg, boolean executes){
		
		ArgumentType argumentType = arg.getBrigadierType();
		boolean bungee = CommandNodeManager.getInstance().isBungee();
		
		//So basically Bungee Commands packet only lets us encode StringArgumentType
		//There are no PROPER_PROVIDERS for other types, causing an error trying to write the packet
		//Because intercepting a packet is the main handle for bungee to change the brigadier command nodes
		//We must restrict ourselves to the StringArgumentType for bungee types
		if(bungee && !(argumentType instanceof StringArgumentType))
			argumentType = StringArgumentType.word();
		
		var builder = RequiredArgumentBuilder.argument(name, argumentType);
		if(executes) builder.executes( $->0 );
		if(arg.hasCustomCompleter() && !bungee) builder.suggests(new ArcheSuggestionProvider<>(arg));
		return builder.build();
	}
	
	private void redirectAliases(ArcheCommand cmd, CommandNode<Object> parent, CommandNode<Object> theOneTrueNode) {
		for(String alias : cmd.getAliases()) {
			if(alias.equalsIgnoreCase(cmd.getMainCommand())) continue;
			var node = LiteralArgumentBuilder.literal(alias).redirect(theOneTrueNode).build();
			if(parent == null) {
				rootNodes.add(node);
			}
			else parent.addChild(node);
		}
	}
}