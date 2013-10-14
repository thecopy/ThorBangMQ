package asl;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import asl.Persistence.IPersistence;
import asl.infrastructure.Bootstrapping;

public class Main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Logger logger = Logger.getLogger("ThorBangMQ");
		
		// Read configuration file
		Bootstrapping.StrapTheBoot(logger);

		IPersistence persistence = Bootstrapping.GetPersister();
		ExecutorService threadpool = Executors.newFixedThreadPool(ASLServerSettings.NUM_CLIENTREQUESTWORKER_THREADS);
		
		// Do stuff
		try {
			System.out.println("Starting socketServer");
			
			ASLSocketServer socketServer = new ASLSocketServer(threadpool, logger, persistence);
			socketServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Close threads gracefully. 
		threadpool.shutdown();
	}

}
