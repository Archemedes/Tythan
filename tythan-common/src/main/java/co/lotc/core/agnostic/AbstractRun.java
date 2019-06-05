package co.lotc.core.agnostic;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractRun<T> implements PluginOwned<T> {
	@Getter protected final T plugin;

	
	public abstract Executor syncExecutor();
	public abstract Executor asyncExecutor();
	
	public abstract void sync(Runnable r);
	public abstract void async(Runnable r);
	public abstract void delayed(long delay, Runnable r);
	public abstract void repeating(long delay, long timer, Runnable r);
	
	public void repeating(long timer, Runnable r) {
		repeating(1, timer, r);
	}
	
	public CompletableFuture<Void> future(Runnable r) {
		return CompletableFuture.runAsync(r, asyncExecutor());
	}
	
	public <X> CompletableFuture<X> future(Supplier<X> r) {
		return CompletableFuture.supplyAsync(r, asyncExecutor());
	}
}
