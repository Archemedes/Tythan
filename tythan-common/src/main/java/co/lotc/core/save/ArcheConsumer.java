package co.lotc.core.save;

import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import co.lotc.core.CoreLog;
import co.lotc.core.save.rows.Row;
import co.lotc.core.save.rows.ConsumerRow;
import co.lotc.core.save.rows.FlexibleDeleteRow;
import co.lotc.core.save.rows.FlexibleInsertRow;
import co.lotc.core.save.rows.FlexibleUpdateRow;
import co.lotc.core.save.rows.LambdaRow;
import lombok.Getter;
import lombok.var;

public final class ArcheConsumer extends TimerTask implements Consumer {
	private final Queue<Row> queue = new LinkedBlockingQueue<>();
	@Getter private final MongoHandler mongo;
	@Getter private final Executor executor;
	
	private final int timePerRun;
	private final int forceToProcess;
	private final int warningSize;
	private boolean bypassForce = false;

	public ArcheConsumer(Executor executor, int timePerRun, int forceToProcess, int warningSize) {
		this.mongo = new MongoHandler("archebase"); //TODO auth data from config
		this.executor = executor;
		this.timePerRun = timePerRun;
		this.forceToProcess = forceToProcess;
		this.warningSize = warningSize;
	}

	@Override
	public FlexibleInsertRow insert(String table) {
		return new FlexibleInsertRow(this, table, FlexibleInsertRow.Mode.IGNORE);
	}
	
	@Override
	public FlexibleInsertRow replace(String table) {
		return new FlexibleInsertRow(this, table, FlexibleInsertRow.Mode.REPLACE);
	}

	@Override
	public FlexibleDeleteRow delete(String table) {
		return new FlexibleDeleteRow(this, table);
	}

	@Override
	public FlexibleUpdateRow update(String table) {
		return new FlexibleUpdateRow(this, table);
	}

	public synchronized void bypassForce() {
		bypassForce = true;
	}

	@Override
	public void queueRow(Runnable row) {
		queueRow(new LambdaRow(row));
	}
	
	@Override
	public void queueRow(java.util.function.Consumer<MongoConnection> row) {
		queueRow(new LambdaRow(row));
	}
	
	@Override
	public void queueRow(Row row) {
		queue.add(row);
	}

	@Override
	public int getQueueSize() {
		return queue.size();
	}

	@Override
	public synchronized void runForced() {
		this.bypassForce = true;
		run();
		this.bypassForce = false;
	}

	@Override
	public synchronized void run() {

		if (queue.isEmpty()) {
			CoreLog.debug("[Consumer] The consumer has no queue, not running.");
			return;
		}

		final long starttime = System.currentTimeMillis();
		if (queue.size() >= warningSize) {
			CoreLog.warning("[Consumer] Warning! The Consumer Queue is overloaded! The size of the queue is " + queue.size() + " which is " + (queue.size() - warningSize) + " over our set threshold of " + warningSize + "! We're still running, but this should be looked into!");
		}
		
		int count = 0;
		
		try(var session = mongo.open()) {
			MongoConnection connection = session.getConnection();
			while (bypassForce || System.currentTimeMillis() - starttime < timePerRun|| count < forceToProcess) {
				Row row = queue.poll();
				if (row == null) break;
				
				if(row instanceof ConsumerRow) {
					String trace = ((ConsumerRow) row).getOriginStackTrace();
					if(trace != null) {
						CoreLog.debug("ConsumerRow origin stack trace:");
						CoreLog.debug(trace);
					}
				}
				try { CoreLog.debug("[Consumer] Beginning process for " + row.toString());}
				catch(RuntimeException e) { CoreLog.debug("[Consumer] Beginning process for FAULTY " + row.getClass().getSimpleName());}

				long taskstart = System.currentTimeMillis();
				
				try { //2 calls but only 1 of them should be non-empty in normal operation
					row.run();
					row.accept(connection);
				} catch(Exception e) {
					e.printStackTrace();
					CoreLog.severe("[Consumer] Exception on " + row.getClass().getSimpleName() + ": ", e);
					CoreLog.severe("[Consumer] Statement body: " + row.toString());
				}
				
				count++;
				CoreLog.debug("[Consumer] took " + (System.currentTimeMillis() - taskstart) + "ms for " + row.getClass().getSimpleName());
			}
		} finally {
			long time = System.currentTimeMillis() - starttime;
			CoreLog.info("[Consumer] Finished handling " + count + " rows in " + time + "ms.");
			if(count == 0) CoreLog.warning("[Consumer] Ran with 0 tasks, this shouldn't happen?");
			else CoreLog.debug("[Consumer] Total rows processed: " + count + ". " + queue.size() + " rows left in queue");
		}
	}
	
	@Override
	public boolean isDebugging() {
		return CoreLog.isDebugging();
	}
}
