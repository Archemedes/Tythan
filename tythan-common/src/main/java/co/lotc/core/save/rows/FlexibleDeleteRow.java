package co.lotc.core.save.rows;

import co.lotc.core.save.MongoConnection;
import co.lotc.core.save.Publisher;

public class FlexibleDeleteRow extends FlexibleRow {
	
	public FlexibleDeleteRow(Publisher consumer, String table) {
		super(consumer, table);
	}
	
	@Override
	public void accept(MongoConnection con) {
		con.delete(this.collecton, vars);
	}

}
