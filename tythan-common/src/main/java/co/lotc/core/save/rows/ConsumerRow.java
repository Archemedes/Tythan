package co.lotc.core.save.rows;

import co.lotc.core.save.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ConsumerRow implements ArcheRow {
	@Getter private final Consumer consumer;
	
	
  public void queue() {
  	consumer.queueRow(this);
  }
  
  public void queueAndFlush() {
  	queue();
  	consumer.getExecutor().execute(()->consumer.runForced());
  }
  
  @Override
	public final void run() {
  	//Class must be a consumer, prohibit runnable
  }
	
}
