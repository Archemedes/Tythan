package co.lotc.core.save.rows;

import co.lotc.core.save.MongoConnection;
import co.lotc.core.save.Publisher;

public class FlexibleInsertRow extends FlexibleRow {
	
	public FlexibleInsertRow(Publisher consumer, String table) {
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
