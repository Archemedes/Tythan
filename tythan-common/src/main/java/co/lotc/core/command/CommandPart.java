package co.lotc.core.command;

import java.sql.Connection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

//TODO cleanup the parts that dont work
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal=true)
@RequiredArgsConstructor
final class CommandPart {
	public static final CommandPart NULL_COMMAND = run($->{}, Execution.SYNC);
	static enum Execution{ SYNC, ASYNC, ANY}
	
	BiConsumer<RanCommand, Connection> runner;
	Execution strategy;

	@NonFinal @Setter CommandPart next;
	
	void execute(RanCommand rc) {
		execute(rc, null);
	}
	
	protected void execute(RanCommand rc, Connection c) {
		try {
			runner.accept(rc, c);
			runNext(rc, c);
		} catch(Exception e) {
			rc.handleException(e);
		}
	}
	
	private void runNext(RanCommand rc, Connection c) {
		if(next == null) return;
	
		if(strategy == Execution.ANY || strategy == next.strategy) next.execute(rc, c);
		else switch(next.strategy) {
		case SYNC:
		case ASYNC:
			Runnable r = new Runnable() { @Override public void run() { next.execute(rc); }};
			if(next.strategy == Execution.SYNC) r.run();
			else r.run(); //TODO this needs to be async
			break;
		case ANY: throw new IllegalStateException();
		}
	}
	
	public static CommandPart messager(String message, Execution strat) {
		BiConsumer<RanCommand, Connection> cc = (rc,$)->rc.msg(message);
		return new CommandPart(cc, strat);
	}
	
	public static CommandPart tester(Predicate<RanCommand> p, String error) {
		Consumer<RanCommand> c = rc-> rc.validate(p.test(rc), error);
		return run(c, Execution.SYNC);
		
	}
	
	public static CommandPart tester(Predicate<RanCommand> p, Consumer<RanCommand> orElse) {
		Consumer<RanCommand> c = rc-> {
			if(!p.test(rc)) {
				orElse.accept(rc);
				rc.error("");
			}
		};
		return run(c, Execution.SYNC);
	}
	
	public static CommandPart run(Consumer<RanCommand> c, Execution strat) {
		BiConsumer<RanCommand, Connection> cc = (rc,$)->c.accept(rc);
		return new CommandPart(cc, strat);
	}

	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	public static class JoinedPart<T>{
		private static final String TEMP_HOLDER = "tmp_result_holder";
		private final ArcheCommandBuilder builder;
		private final BiFunction<RanCommand, Connection, T> function;
		static <T> JoinedPart<T> forAsync(ArcheCommandBuilder b, Function<RanCommand, T> function){
			BiFunction<RanCommand, Connection, T> wrapped = (rc,$)->function.apply(rc);
			val result = new JoinedPart<>(b, wrapped);
			return result;
		}
		
		public ArcheCommandBuilder andThen(BiConsumer<RanCommand, T> consumer) {
			BiConsumer<RanCommand, Connection> wrappedFunction = (rc, c)->{
				T result = function.apply(rc, c);
				rc.addContext(TEMP_HOLDER, result);
			};
			
			BiConsumer<RanCommand, Connection> wrappedConsumer = (rc,$)->{
				T result = rc.getContext(TEMP_HOLDER);
				consumer.accept(rc, result);
			};
			
			builder.sequence(new CommandPart(wrappedFunction, Execution.ASYNC));
			builder.sequence(new CommandPart(wrappedConsumer, Execution.SYNC));
			
			return builder;
		}
	}
	
}
