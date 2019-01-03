package co.lotc.core.save.rows;

import co.lotc.core.save.Consumer;
import co.lotc.core.save.MongoConnection;

public class FlexibleInsertRow extends FlexibleRow {
	private final Mode mode;
	
	public FlexibleInsertRow(Consumer consumer, String table, Mode mode) {
		super(consumer, table);
		this.mode = mode;
	}

	public enum Mode{
		REPLACE,
		IGNORE
	}
	
	@Override
	public FlexibleInsertRow set(String column, Object value) {
		return (FlexibleInsertRow) this.where(column, value);
	}
	
	@Override
	public void accept(MongoConnection con) {
		con.insert(collecton, vars); //TODO mode: ignore, replace
	}
	
}
