package clientManager;

import java.io.IOException;

import org.apache.commons.lang3.time.StopWatch;

import asl.ThorBangMQ;

public class main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		String hostname = "localhost";
		int port = 8123;
		int numberOfClients = 200;
		int numberOfMessagesPerClient = 100;
		int queue = 1;
		
		System.out.println("Connecting " + numberOfClients + " clients to " + hostname + ":" + port + "...");
		
		Thread[] clients = new Thread[numberOfClients];
		for(int i = 0; i < numberOfClients;i++){
			clients[i] = new Thread(new clientRunner(hostname, port, numberOfMessagesPerClient, i+1, queue, i));
		}
		
		System.out.println("OK Done! Sending " + numberOfMessagesPerClient + " messages sequentially to queue 1 per client...");
		
		StopWatch w = new StopWatch();
		
		w.start();
		
		//Start client threads
		for(int i = 0; i < numberOfClients;i++){
			clients[i].start();
		}
		
		//Wait for clients to finish
		for (Thread client : clients) {
			client.join();
		}

		w.stop();
		
		System.out.println("OK Done!");
		System.out.println("-------------------------------------------");

		System.out.println("Number of Clients:\t" + numberOfClients+ "");
		System.out.println("Messages per Client:\t" + numberOfMessagesPerClient + "");
		System.out.println("Total Messages:\t\t" + numberOfClients*numberOfMessagesPerClient+ "");
		System.out.println("");
		System.out.println("Total Time:\t\t" + w.getNanoTime()/1000/1000 + "ms");
		System.out.println("Per Message:\t\t" + w.getNanoTime()/1000/1000/(float)numberOfMessagesPerClient/numberOfClients + "ms");
		
		System.in.read();
	}
		

}
