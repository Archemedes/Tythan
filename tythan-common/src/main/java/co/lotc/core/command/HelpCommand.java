package co.lotc.core.command;

import static net.md_5.bungee.api.ChatColor.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.primitives.Ints;

import co.lotc.core.Tythan;
import co.lotc.core.agnostic.AbstractChatBuilder;
import co.lotc.core.agnostic.Sender;
import lombok.val;
import lombok.var;
import net.md_5.bungee.api.ChatColor;

public class HelpCommand extends ArcheCommand {
	private static final ChatColor[] colors = new ChatColor[] {AQUA, YELLOW, GREEN, LIGHT_PURPLE, GOLD};
	
	private final ArcheCommand parent;
	
	HelpCommand(ArcheCommand ac) {
	  super("help",
	  		Collections.emptySet(),
	  		"prints help",
	  		ac.getPermission(),
	  		false,
	  		false,
	  		Arrays.asList(helpPageArg()),
	  		Collections.emptyList(),
	  		Collections.emptyList(),
	  		null);
	  
	  parent = ac;
	  
	}
	
	private static CmdArg<Integer> helpPageArg(){
		val c = new CmdArg<Integer>("page", "not a valid integer", "0", null);
		c.setFilter(i->i>=0);
		c.setMapper(Ints::tryParse);
		return c;
	}
	
	@Override
	void execute(RanCommand c) {
		int page = c.getArg(0);
		runHelp(c, page);
	}
	
	public void runHelp(RanCommand c, int page) {

		if(page > 0) {
			int min = 6 + (page-1)*7;
			if(parent.hasDescription()) min--;
			int max = min + 7;
			outputSubcommands(c, min, max);
		}
		else {
			outputBaseHelp(c);
		}
	}
	
	private void outputBaseHelp(RanCommand c) {
		Sender s = c.getSender();
		var b = Tythan.get().chatBuilder();
		if(parent.getPermission() != null) b.append("[P]").color(YELLOW).hover("Permission required: " + GREEN + parent.getPermission()).append(" ");
		if(!parent.getFlags().isEmpty()) {
			b.append("[F]").color(LIGHT_PURPLE);
			StringBuilder b2 = new StringBuilder();
			b2.append(YELLOW).append("Accepted Command Flags:");
			for (CmdFlag flag : parent.getFlags()) {
				b2.append('\n');
				b2.append(WHITE).append("-").append(flag.getName());
				String flagPex = flag.getPermission();
				String flagDesc = flag.getArg().getDescription();
				
				if(flagDesc != null) b2.append(": ").append(GRAY).append(flagDesc);
				if(flagPex != null) b2.append(' ').append(YELLOW).append('(').append(flagPex).append(')');
			}
			b.hover(b2.toString()).append(" ");
			b.reset();
		}
		
		commandHeadline(b,c).send(s);
		
		if(parent.hasDescription()) c.msg(GRAY+""+ITALIC+parent.getDescription());
		
		int max = parent.hasDescription()? 5:6;
		outputSubcommands(c, 0, max);
	}
	
	private AbstractChatBuilder<?> commandHeadline(AbstractChatBuilder<?> b, RanCommand c) {
		String alias = "/" + c.getUsedAlias();
		if(alias.endsWith("help")) alias = alias.substring(0, alias.length() - 5);
		b.append(alias);
		b.color(ChatColor.GOLD).suggest(alias + ' ');
		fillArgs(parent, alias, b, true);
		return b;
	}
	
	private void fillArgs(ArcheCommand whichFriendo, String alias, AbstractChatBuilder<?> b, boolean useColor) {
		int i = 0;
		for(CmdArg<?> a : whichFriendo.getArgs()) {
			boolean optional = a.hasDefaultInput();
			b.append(" ");
			if(useColor) b.color(colorCoded(i++));
			b.append(optional? "[":"<");
			if(a.hasDescription()) b.hover(a.getDescription());
			else b.retainColors().suggest(alias + ' ');
			b.append(a.getName())
			.append(optional? "]":">");
		}
	}

	private ChatColor colorCoded(int i) {
		return colors[i%colors.length];
	}
	
	void outputSubcommands(RanCommand c, int min, int max) {
		Sender s = c.getSender();
		List<ArcheCommand> subs = parent.getSubCommands().stream()
				.filter(sub->sub!=this)
				.filter(sub->sub.hasPermission(s))
				.collect(Collectors.toList());
		
		String alias = "/" + c.getUsedAlias();
		if(alias.endsWith("help")) alias = alias.substring(0, alias.length() - 5);
		
		if(subs.size() <= min) {
			//No error message when on main help file :)
			if(min > 0) s.sendMessage(RanCommand.ERROR_PREFIX + "Invalid help page!");
			return; //haha fuck you readability
		} else {
			String trailing = alias.substring(alias.lastIndexOf(" ")+1);
			AbstractChatBuilder<?> b = Tythan.get().chatBuilder().append("-== Possible sub-commands for ").color(DARK_AQUA)
					.append(trailing).color(GRAY).append(" ==-").color(DARK_AQUA);
			
			if(min > 0) b.append(" [").hover("Previous Page").color(RED).command(alias + " -h " + (min/7)).append('\u2190').append("]");
			if(subs.size() >= max) b.append(" [").hover("Next Page").color(RED).command(alias + " -h " + (min/7+2)).append('\u2192').append("]");
			
			c.msg(b.build());
		}
		
		//Arrays start at 1 fight me
		for(int i = min; i < max; i++) {
			if(subs.size() <= i) break;
			ArcheCommand sub = subs.get(i);
			String subber = sub.getMainCommand();
			
			AbstractChatBuilder<?> b = Tythan.get().chatBuilder().append(subber).color(GOLD);
			if(sub.getHelp() != null) b.command(alias + ' ' + subber + " -h 0").hover("Click for help on this subcommand!");
			else b.suggest(alias + ' ' + subber + ' ').hover("Click to run this command");
			fillArgs(sub, alias + ' ' + subber, b, false);
			
			if(sub.hasDescription()) {
				int room = 57 - b.toPlainText().length();
				if(room > 0)  {
					b.append(": ");
					String desc = sub.getDescription();
					if(desc.length() > room) desc = desc.substring(0, room-1) + '\u2026';
					b.append(desc).color(GRAY);
				}
			}
			
			b.send(s);
		}

	}
}
