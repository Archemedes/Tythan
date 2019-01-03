package co.lotc.core.save.rows;

import java.util.function.Consumer;

import co.lotc.core.save.MongoConnection;

public interface Row extends Runnable, Consumer<MongoConnection> {

	@Override
	default void run() { }
	
	@Override
	default void accept(MongoConnection connection) { }
	
}
