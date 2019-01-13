package co.lotc.core;

import co.lotc.core.util.MessageUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Wrapper class around ComponentBuilder with lots of syntactic sugar.
 * Doesn't extend {@link net.md_5.bungee.api.chat.ComponentBuilder} because it is final.
 * Unfortunately this means we lose their javadoc on these methods
 */
public abstract class AbstractChatBuilder<E extends AbstractChatBuilder<E>> {
	protected final ComponentBuilder handle;
	
	protected AbstractChatBuilder(String initial) {
		handle = new ComponentBuilder(initial);
	}
	
	public E newline() {
		handle.append(System.lineSeparator(), FormatRetention.NONE);
		return getThis();
	}
	
	public E append(char ch) {
		handle.append(String.valueOf(ch));
		return getThis();
	}
	
	public E append(Object o) {
		handle.append(String.valueOf(o));
		return getThis();
	}
	
	public E append(int i) {
		handle.append(String.valueOf(i));
		return getThis();
	}
	
	public E append(long l) {
		handle.append(String.valueOf(l));
		return getThis();
	}
	
	public E append(String text) {
		handle.append(text);
		return getThis();
	}
	
	public E append(String text, FormatRetention retention) {
		handle.append(text, retention);
		return getThis();
	}
	
	public E append(BaseComponent component) {
		handle.append(component);
		return getThis();
	}
	
	public E append(BaseComponent[] components) {
		handle.append(components);
		return getThis();
	}
	
	public E append(BaseComponent component, FormatRetention retention){
		handle.append(component, retention);
		return getThis();
	}
	
	public E append(BaseComponent[] components, FormatRetention retention){
		handle.append(components, retention);
		return getThis();
	}
	
	public E appendButton(String text, String cmd) {
		return append(MessageUtil.CommandButton(text, cmd));
	}
	
	public E appendButton(String text, String cmd, String hover) {
		return append(MessageUtil.CommandButton(text, cmd, hover));
	}
	
	public E appendButton(String text, String cmd, ChatColor textcolor, ChatColor rimcolor) {
		return append(MessageUtil.CommandButton(text, cmd, textcolor, rimcolor));
	}
	
	public E appendButton(String text, String cmd, String hover, ChatColor textcolor, ChatColor rimcolor) {
		return append(MessageUtil.CommandButton(text, cmd, hover, textcolor, rimcolor));
	}
	
	public E reset() {
		handle.reset();
		return getThis();
	}
	
	public E retainEvents() {
		return retain(FormatRetention.EVENTS);
	}
	
	public E retainColors() {
		return retain(FormatRetention.FORMATTING);
	}
	
	public E retain(FormatRetention retention) {
		handle.retain(retention);
		return getThis();
	}
	
	public E bold() {
		return bold(true);
	}
	
	public E bold(boolean bold) {
		handle.bold(bold);
		return getThis();
	}
	
	public E italic() {
		return italic(true);
	}
	
	public E italic(boolean italic) {
		handle.italic(italic);
		return getThis();
	}
	
	public E obfuscated() {
		return obfuscated(true);
	}
	
	public E obfuscated(boolean obfuscated) {
		handle.obfuscated(obfuscated);
		return getThis();
	}
	
	public E strikethrough() {
		return strikethrough(true);
	}
	
	public E strikethrough(boolean strikethrough) {
		handle.strikethrough(strikethrough);
		return getThis();
	}
	
	public E underlined() {
		return underlined(true);
	}
	
	public E underlined(boolean underlined) {
		handle.underlined(underlined);
		return getThis();
	}
	
	public E color(ChatColor color) {
		handle.color(color);
		return getThis();
	}
	
	public E hover(String text) {
		return event(HoverEvent.Action.SHOW_TEXT, text);
	}

	public E event(HoverEvent.Action action, String text) {
		return event(new HoverEvent(action, new BaseComponent[]{new TextComponent(text)}));
	}
	
	public E event(HoverEvent event) {
		handle.event(event);
		return getThis();
	}
	
	public E command(String command) {
		return event(ClickEvent.Action.RUN_COMMAND, command);
	}
	
	public E suggest(String suggestion) {
		return event(ClickEvent.Action.SUGGEST_COMMAND, suggestion);
	}
	
	public E event(ClickEvent.Action action, String text) {
		return event(new ClickEvent(action, text));
	}
	
	public E event(ClickEvent event) {
		handle.event(event);
		return getThis();
	}
	
	public E insertion(String insertion) {
		handle.insertion(insertion);
		return getThis();
	}
	
	public BaseComponent[] create() {
		return handle.create();
	}
	
	public BaseComponent build() {
		return new TextComponent(handle.create());
	}
	
	public String toLegacyText() {
		return BaseComponent.toLegacyText(handle.create());
	}
	
	public String toPlainText() {
		return BaseComponent.toPlainText(handle.create());
	}
	
	
	protected abstract E getThis();
}
