package co.lotc.core.save;

import java.util.concurrent.Executor;

import co.lotc.core.save.rows.ArcheRow;
import co.lotc.core.save.rows.FlexibleRow;

public interface Consumer {

		Executor getExecutor();

		MongoHandler getMongo();
		
    void queueRow(Runnable row);
    void queueRow(java.util.function.Consumer<MongoConnection> row);
    void queueRow(ArcheRow row);

    int getQueueSize();
    
    FlexibleRow insert(String table);
    FlexibleRow insertIgnore(String table);
    FlexibleRow replace(String table);
    FlexibleRow delete(String table);
    FlexibleRow update(String table);

    /**
     * Run until entire consumer is empty
     */
		void runForced();
		
		
		
}
