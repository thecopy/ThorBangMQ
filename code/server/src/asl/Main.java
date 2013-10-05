package asl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExecutorService threadpool = Executors.newFixedThreadPool(ASLServerSettings.NUM_CLIENTREQUESTWORKER_THREADS);
		// Do stuff
		
		// Close threads gracefully. 
		threadpool.shutdown();
	}

}
