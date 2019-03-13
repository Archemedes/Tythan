package co.lotc.core.command.types;

import java.util.function.Function;

import co.lotc.core.agnostic.Sender;

public class SenderTemplate<T> extends TypeTemplate<T> {

	public SenderTemplate(Class<T> forClass) {
		super(forClass, TypeRegistry.forSenders());
	}
	
	private Function<Sender, T> mapSender;
	private Function<String, T> mapString;
	
}
