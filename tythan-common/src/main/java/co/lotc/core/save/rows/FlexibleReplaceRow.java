package co.lotc.core.save.rows;

import java.util.LinkedHashMap;
import java.util.Map;

import co.lotc.core.save.MongoConnection;
import co.lotc.core.save.Publisher;

public class FlexibleReplaceRow extends FlexibleRow {
	protected final Map<String, Object> sets = new LinkedHashMap<>();
	
	public FlexibleReplaceRow(Publisher consumer, String table) {
		super(consumer, table);
	}

	@Override
	public FlexibleReplaceRow where(String column, Object value) {
		return (FlexibleReplaceRow) super.where(column, value);
	}
	
	@Override
	public FlexibleReplaceRow set(String column, Object value) {
		sets.put(column, value);
		return this;
	}
	
	@Override
	public void accept(MongoConnection con) {
		con.replace(this.collecton, vars, sets);
	}

}
