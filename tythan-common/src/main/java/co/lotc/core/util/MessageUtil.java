package co.lotc.core.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;

import java.util.Arrays;

import co.lotc.core.Tythan;

import static net.md_5.bungee.api.ChatColor.*;

/**
 * Set of utility methods to quickly retrieve often-used BaseComponents compositions
 * or convenience methods making the spigot ChatMessage API slightly more bearable
 * @author Sporadic
 */
public final class MessageUtil {
	private MessageUtil() {}

	public static BaseComponent legacyText(String legacy) {
		return legacyAdd(new TextComponent(), legacy);
	}
	
	public static BaseComponent legacyAdd(BaseComponent m, String toAdd) {
		Arrays.stream(TextComponent.fromLegacyText(toAdd))
			.forEach(bc->m.addExtra(bc));
		return m;
	}
	
	public static String asError(String text) {
		return DARK_RED + "Error: " + RED + text;
	}
	
	public static BaseComponent asErrorComponent(String text) {
		return Tythan.get().chatBuilder()
				.append("Error: ").color(DARK_RED)
				.append(text).color(RED)
				.build();
	}
	
	public static HoverEvent hoverEvent(String text) {
		return hoverEvent(HoverEvent.Action.SHOW_TEXT, text);
	}
	
	public static HoverEvent hoverEvent(HoverEvent.Action action, String text) {
		return new HoverEvent(action, new BaseComponent[]{new TextComponent(text)});
	}
	
	public static BaseComponent CommandButton(String text, String cmd) {
		return CommandButton(text, cmd, null, GRAY, BLUE);
	}
	
	public static BaseComponent CommandButton(String text, String cmd, String hover) {
		return CommandButton(text, cmd, hover, GRAY, BLUE);
	}
	
	public static BaseComponent CommandButton(String text, String cmd, ChatColor textcolor, ChatColor rimcolor) {
		return CommandButton(text, cmd, null, textcolor, rimcolor);
	}
		
	public static BaseComponent CommandButton(String text, String cmd, String hover, ChatColor textcolor, ChatColor rimcolor) {
		
		TextComponent tc = new TextComponent();
		tc.setColor(rimcolor);
		tc.addExtra("[");
		TextComponent sub = new TextComponent(text);
		sub.setItalic(true);
		sub.setColor(textcolor);
		sub.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
		if(hover != null) {
			BaseComponent[] hoe = new ComponentBuilder(hover)
			.color(GRAY).italic(true)
			.create();
			sub.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoe));
		}
		tc.addExtra(sub);
		tc.addExtra("]");
		
		return tc;
	}
	
	public static BaseComponent ArcheHelpButton(String topic) {
		return CommandButton(topic, "/archehelp " + topic, "Click for help");
	}

	public static void addNewlines(BaseComponent x) {
		breakUp(x, 0, null);
	}
	
	private static int breakUp(BaseComponent x, int lineLength, BaseComponent prev) {
		if(x instanceof TextComponent) {
			TextComponent tc = (TextComponent) x;
			String text = tc.getText();
			
			if(text.trim().length() == 0) {
				lineLength += text.length();
				if(lineLength >= 60) {
					tc.setText("\n");
					lineLength = 0;
				} else if(lineLength == 0) {
					tc.setText("");
				}
			} else {
				String[] parts = text.split(" ");
				StringBuilder recoveredParts = new StringBuilder(text.length() + 4);
				boolean first = true;

				for(String part : parts) {
					if(lineLength+part.length() >= 60 && part.length() != 1) {
						if(prev != null && prev instanceof TextComponent) {
							TextComponent prev_tc = (TextComponent) prev;
							String txt = prev_tc.getText();
							if(txt.length() <2) {
								prev_tc.setText('\n' + txt);
							} else {
								recoveredParts.append('\n');
							}
						} else {
							recoveredParts.append('\n');
						}
						lineLength = 0;
					} else if(!first) {
						recoveredParts.append(' ');
						lineLength++;
					}
					
					if(first) {
						first = false;
						prev = null;
					}
					
					lineLength += part.length();
					recoveredParts.append(part);
				}
				if(text.charAt(text.length() - 1) == ' ')
					recoveredParts.append(' ');
				tc.setText(recoveredParts.toString());
	
			}
		} else {
			lineLength += x.toPlainText().length();
		}
		
		prev = null;
		if(x.getExtra() != null) for(BaseComponent o : x.getExtra()) {
			lineLength = breakUp(o, lineLength, prev);
			prev = o;
		}
		
		return lineLength;
	}

}
