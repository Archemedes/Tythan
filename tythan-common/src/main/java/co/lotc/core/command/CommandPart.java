package co.lotc.core.command;

import java.sql.Connection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.lordofthecraft.arche.command.ArcheCommandBuilder;
import net.lordofthecraft.arche.command.CommandPart;
import net.lordofthecraft.arche.command.RanCommand;
import net.lordofthecraft.arche.save.rows.RunnerRow;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal=true)
@RequiredArgsConstructor
final class CommandPart {
	public static final CommandPart NULL_COMMAND = run($->{}, Execution.SYNC);
	static enum Execution{ SYNC, ASYNC, CONSUMER, ANY}
	
	BiConsumer<RanCommand, Connection> runner;
	Execution strategy;

	@NonFinal @Setter Plugin plugin;
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
			BukkitRunnable r = new BukkitRunnable() { @Override public void run() { next.execute(rc); }};
			if(next.strategy == Execution.SYNC) r.runTask(plugin);
			else r.runTaskAsynchronously(plugin);
			break;
		case CONSUMER:
		//Note that 'conn' is not the method param, which would be null at this point in the code
			RunnerRow wrapped = conn->next.execute(rc, conn);
			wrapped.queueAndFlush();
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
	
	public static CommandPart consume(BiConsumer<RanCommand, Connection> bic) {
		return new CommandPart(bic, Execution.CONSUMER);
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
		private boolean forTheConsumer = true;
		static <T> JoinedPart<T> forAsync(ArcheCommandBuilder b, Function<RanCommand, T> function){
			BiFunction<RanCommand, Connection, T> wrapped = (rc,$)->function.apply(rc);
			val result = new JoinedPart<>(b, wrapped);
			result.forTheConsumer = false;
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
			
			builder.sequence(new CommandPart(wrappedFunction, forTheConsumer? Execution.CONSUMER : Execution.ASYNC));
			builder.sequence(new CommandPart(wrappedConsumer, Execution.SYNC));
			
			return builder;
		}
	}
	
}
