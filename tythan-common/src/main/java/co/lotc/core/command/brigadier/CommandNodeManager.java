package co.lotc.core.command.brigadier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import lombok.var;

public class CommandNodeManager {
	private static final CommandNodeManager INSTANCE = new CommandNodeManager();
	public static CommandNodeManager getInstance() { return INSTANCE; }
	
	private final List<CommandNode<Object>> nodes = Collections.synchronizedList(new ArrayList<>());
	
	private CommandNodeManager() {
		//This space intentionally left blank
	}
	
	public void register(Kommandant kommandant) {
		kommandant.getNodes().forEach(nodes::add);
	}
	
	public void add(CommandNode<Object> node) {
		nodes.add(node);
	}
		
	public void inject(RootCommandNode<Object> root) {
		for(var node : nodes) {
			String name = node.getName();
			despigot(root, name);
			root.addChild(node);
		}
	}
	
	private void despigot(RootCommandNode<Object> root, String alias) {
		var iter = root.getChildren().iterator();
		while(iter.hasNext()) {
			var kid = iter.next(); //Search spigot's attempt at registering the argument
			if(kid.getName().equals(alias))
				iter.remove(); //Killing the skeleton of spigot gives us full Brigadier power
		}
	}
	
}
