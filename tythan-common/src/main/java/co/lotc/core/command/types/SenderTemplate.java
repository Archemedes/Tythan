package co.lotc.core.command.types;

import java.util.function.Function;

import org.apache.commons.lang.Validate;

import co.lotc.core.agnostic.Sender;
import co.lotc.core.command.ArgBuilder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class SenderTemplate<T> extends TypeTemplate<T> {

	public SenderTemplate(Class<T> forClass) {
		super(forClass, TypeRegistry.forSenders());
	}
	
	private Function<Sender, T> mapSender;
	private Function<String, T> mapString;
	
}
