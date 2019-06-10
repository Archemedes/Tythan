package co.lotc.core.save.rows;

import java.util.LinkedHashMap;
import java.util.Map;

import co.lotc.core.save.MongoConnection;
import co.lotc.core.save.Publisher;

public class FlexibleUpdateRow extends FlexibleRow {
	protected final Map<String, Object> sets = new LinkedHashMap<>();
	
	public FlexibleUpdateRow(Publisher consumer, String table) {
		super(consumer, table);
	}

	@Override
	public FlexibleUpdateRow where(String column, Object value) {
		return (FlexibleUpdateRow) super.where(column, value);
	}
	
	@Override
	public FlexibleUpdateRow set(String column, Object value) {
		sets.put(column, value);
		return this;
	}
	
	@Override
	public void accept(MongoConnection con) {
		con.update(this.collecton, vars, sets);
	}
	
}
