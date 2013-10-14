package asl;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import asl.Persistence.PersistenceImpl;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExecutorService threadPool = Executors.newFixedThreadPool(ASLServerSettings.NUM_CLIENTREQUESTWORKER_THREADS);
		PersistenceImpl persistence = new PersistenceImpl();
		long queueId = persistence.createQueue("cool queue");
		Message msg = new Message(1, 1, -1, queueId, 0, 5, "this is the first message!");
		persistence.storeMessage(msg);
		// Do stuff
		try {
			System.out.println("Starting socketServer");
			ASLSocketServer socketServer = new ASLSocketServer(threadPool);
			socketServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Close threads gracefully. 
		threadPool.shutdown();
		persistence.close();
	}

}
