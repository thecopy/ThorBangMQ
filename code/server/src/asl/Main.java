package asl;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExecutorService threadpool = Executors.newFixedThreadPool(ASLServerSettings.NUM_CLIENTREQUESTWORKER_THREADS);
		// Do stuff
		try {
			SocketServer socketServer = new SocketServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Close threads gracefully. 
		threadpool.shutdown();
	}

}
