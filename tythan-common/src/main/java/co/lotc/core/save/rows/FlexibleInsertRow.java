package co.lotc.core.save.rows;

import co.lotc.core.save.Consumer;
import co.lotc.core.save.MongoConnection;

public class FlexibleInsertRow extends FlexibleRow {
	
	public FlexibleInsertRow(Consumer consumer, String table) {
		super(consumer, table);
	}

	@Override
	public FlexibleInsertRow set(String column, Object value) {
		return (FlexibleInsertRow) this.where(column, value);
	}
	
	@Override
	public void accept(MongoConnection con) {
		con.insert(collecton, vars);
	}
	
}
