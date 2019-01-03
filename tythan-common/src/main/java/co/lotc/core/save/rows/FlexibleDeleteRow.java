package co.lotc.core.save.rows;

import co.lotc.core.save.Consumer;
import co.lotc.core.save.MongoConnection;

public class FlexibleDeleteRow extends FlexibleRow {
	
	public FlexibleDeleteRow(Consumer consumer, String table) {
		super(consumer, table);
	}
	
	@Override
	public void accept(MongoConnection con) {
		con.delete(this.collecton, vars);
	}

}
