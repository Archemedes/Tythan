package co.lotc.core.save.rows;

import java.util.function.Consumer;

import co.lotc.core.save.MongoConnection;

/**
 * Be either a consumer or runnable, but not both
 */
public class LambdaRow implements Row {
	private final Consumer<MongoConnection> consumer;
	private final Runnable runnable;
	
	public LambdaRow(Runnable runnable) {
		this.runnable = runnable;
		this.consumer = $->{};
	}
	
	public LambdaRow(Consumer<MongoConnection> consumer) {
		this.consumer = consumer;
		this.runnable = ()->{};
	}
	
	@Override
	public void run() {
		runnable.run();
	}
	
	@Override
	public void accept(MongoConnection connection) {
		consumer.accept(connection);
	}
	
}
