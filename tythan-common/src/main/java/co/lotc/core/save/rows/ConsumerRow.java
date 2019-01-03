package co.lotc.core.save.rows;

import co.lotc.core.save.Consumer;
import co.lotc.core.save.MongoConnection;
import lombok.Getter;

public abstract class ConsumerRow implements Row {
	@Getter private final Consumer consumer;
	private String briefStackTrace = null;
	
	public ConsumerRow(Consumer consumer) {
		this.consumer = consumer;
  	if(consumer.isDebugging()) {
  		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
  		int i = 0;
  		while(!stackTrace[i].getClassName().equals(this.getClass().getName())) i++;
  		
  		briefStackTrace = stackTrace[i+1].toString() + '\n' + stackTrace[i+2].toString();
  	}
	}
	
  public void queue() {
  	consumer.queueRow(this);
  }
  
  public void queueAndFlush() {
  	queue();
  	consumer.getExecutor().execute(()->consumer.runForced());
  }
  
  public String getOriginStackTrace() {
  	return briefStackTrace;
  }
  
  @Override
	public final void run() {
  	//Class must be a consumer, prohibit runnable
  }
  
  @Override //Force rows to implement again
  public abstract void accept(MongoConnection connection);
	
}
