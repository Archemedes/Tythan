package co.lotc.core.save.rows;

import java.util.LinkedHashMap;
import java.util.Map;

import co.lotc.core.save.Consumer;

//Because duck your overcomplicated optimizations of BS that runs async
public abstract class FlexibleRow extends ConsumerRow {
	protected final String collecton;
	
	protected final Map<String, Object> vars = new LinkedHashMap<>();
	
	public FlexibleRow(Consumer consumer, String table) {
		super(consumer);
		this.collecton = table;
	}
	
	public FlexibleRow where(String column, Object value) {
		vars.put(column, value);
		return this;
	}
	
	public FlexibleRow set(String column, Object value) {
		throw new UnsupportedOperationException();
	}
	
}