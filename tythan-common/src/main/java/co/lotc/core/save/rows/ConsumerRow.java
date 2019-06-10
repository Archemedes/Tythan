package co.lotc.core.save.rows;

import co.lotc.core.Tythan;
import co.lotc.core.save.MongoConnection;
import co.lotc.core.save.Publisher;
import lombok.Getter;

public abstract class ConsumerRow implements Row {
	@Getter private final Publisher consumer;
	private String briefStackTrace = null;
	
	public ConsumerRow(Publisher consumer) {
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
  	Tythan.get().run(Tythan.get()).async(consumer::runForced);
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
