package asl;

import java.util.logging.Level;
import java.util.logging.Logger;

public class IntervalLogger implements Runnable {
	private boolean stop = false;
	private long interval;
	private Logger logger;
	private Level level;
	
	public IntervalLogger(long intervalInMilliseconds, Logger logger, Level level){
		this.interval = intervalInMilliseconds;
		this.logger = logger;
		this.level = level;
	}
	
	public void stop(){
		this.stop = true;
	}
	
	@Override
	public void run() {
		while(!stop){
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			// the first comma (,) is to make the file a CSV file.
			logger.log(this.level, ","
					+ GlobalCounters.numberOfMessagesPersisted.getAndSet(0) + ","
					+ GlobalCounters.numberOfMessagesReturned.getAndSet(0) + ","
					+ GlobalCounters.totalThinkOperations.getAndSet(0) + ","
					+ GlobalCounters.totalThinkTime.getAndSet(0) + ","
					+ GlobalCounters.totalThinkTimeInClientRequestWorker.getAndSet(0) + ","
					+ GlobalCounters.totalThinkTimeInPersistence.getAndSet(0));
		}
	}

}
