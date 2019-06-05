package co.lotc.core.bungee.util;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import co.lotc.core.CoreLog;
import co.lotc.core.agnostic.AbstractRun;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;

public class Run extends AbstractRun<Plugin> {

	public Run(Plugin plugin) {
		super(plugin);
	}

	@Override
	public Executor syncExecutor() {
		return r->r.run();
	}

	@Override
	public Executor asyncExecutor() {
		return r->scheduler().runAsync(plugin, r);
	}

	@Override
	public void sync(Runnable r) {
		CoreLog.warning("Runnable synchronous for Bungee: " + r.getClass().getSimpleName() + " for Plugin: " + plugin.getClass().getSimpleName());
		r.run();
	}

	@Override
	public void async(Runnable r) {
		
	}

	@Override
	public void delayed(long delay, Runnable r) { //In ticks because Bukkit-like behavior
		scheduler().schedule(plugin, r, 50*delay, TimeUnit.MILLISECONDS);
	}

	@Override
	public void repeating(long delay, long timer, Runnable r) {
		scheduler().schedule(plugin, r, 50*delay, 50*timer, TimeUnit.MILLISECONDS);
	}
	
	private TaskScheduler scheduler() {
		return plugin.getProxy().getScheduler();
	}

}
