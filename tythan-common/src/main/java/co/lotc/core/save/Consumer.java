package co.lotc.core.save;

public interface Consumer {

    /**
     * Adds a new row to the Consumer to process for SQL. This is for SQL Update/Insert/Delete/Drop tasks only.
     * This is not to be used for Queries.
     *
     * @param row The ArcheRow to be queued.
     */
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
