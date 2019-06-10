package co.lotc.core.save;

import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import co.lotc.core.CoreLog;
import co.lotc.core.Tythan;
import co.lotc.core.save.rows.ConsumerRow;
import co.lotc.core.save.rows.FlexibleDeleteRow;
import co.lotc.core.save.rows.FlexibleInsertRow;
import co.lotc.core.save.rows.FlexibleReplaceRow;
import co.lotc.core.save.rows.FlexibleUpdateRow;
import co.lotc.core.save.rows.LambdaRow;
import co.lotc.core.save.rows.Row;

public final class Publisher extends TimerTask {
	private final Queue<Row> queue = new LinkedBlockingQueue<>();
	private final MongoHandler mongo;
	
	private final int timePerRun;
	private final int forceToProcess;
	private final int warningSize;
	private boolean bypassForce = false;

	public Publisher(int timePerRun, int forceToProcess, int warningSize) {
		this.mongo = Tythan.getMongoHandler();
		
		this.timePerRun = timePerRun;
		this.forceToProcess = forceToProcess;
		this.warningSize = warningSize;
	}

	public FlexibleInsertRow insert(String table) {
		return new FlexibleInsertRow(this, table);
	}
	
	public FlexibleReplaceRow replace(String table) {
		return new FlexibleReplaceRow(this, table);
	}

	public FlexibleDeleteRow delete(String table) {
		return new FlexibleDeleteRow(this, table);
	}

	public FlexibleUpdateRow update(String table) {
		return new FlexibleUpdateRow(this, table);
	}

	public synchronized void bypassForce() {
		bypassForce = true;
	}

	public void queueRow(Runnable row) {
		queueRow(new LambdaRow(row));
	}
	
	public void queueRow(Consumer<MongoConnection> row) {
		queueRow(new LambdaRow(row));
	}
	
	public void queueRow(Row row) {
		queue.add(row);
	}

	public int getQueueSize() {
		return queue.size();
	}

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
		
		try(MongoConnection connection = mongo.connect()){
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
		}

			long time = System.currentTimeMillis() - starttime;
			CoreLog.info("[Consumer] Finished handling " + count + " rows in " + time + "ms.");
			if(count == 0) CoreLog.warning("[Consumer] Ran with 0 tasks, this shouldn't happen?");
			else CoreLog.debug("[Consumer] Total rows processed: " + count + ". " + queue.size() + " rows left in queue");
	}
	
	public boolean isDebugging() {
		return CoreLog.isDebugging();
	}
}
