package testRunner;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public final class Counters{
	public Counters(Logger logger){
		
	}
	
	public void incrementSend(long delta){
		MessagesSent.addAndGet(delta);
	}
	
	public void incrementRecieved(long delta){
		MessageRecieved.addAndGet(delta);
	}
	
	public AtomicLong MessagesSent = new AtomicLong(0);
	public AtomicLong MessageRecieved = new AtomicLong(0);
}