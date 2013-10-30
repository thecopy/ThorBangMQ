package testRunner.tests;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.commons.lang3.time.StopWatch;

import asl.ThorBangMQ;
import testRunner.MemoryLogger;

public class SendMessages extends testRunner.Test {
	int numberOfClients = 0;
	int messagesPerClient = 0;
	ArrayList<Long> clients;
	long queueId;
	
	@Override
	public String[] getArgsDescriptors() {
		String[] descriptors = new String[3];
		descriptors[0] = "Number of clients";
		descriptors[1] = "Messages per client";
		
		return descriptors;
	}

	@Override
	public void init(String[] args) throws Exception {
		numberOfClients = Integer.parseInt(args[0]);
		messagesPerClient = Integer.parseInt(args[1]);
		clients = new ArrayList<Long>();
		
		ThorBangMQ api = ThorBangMQ.build(this.host, this.port, 1);		
		for(int i = 0; i < numberOfClients; i += 1) {
			clients.add(api.createClient("client_" + i));
		}
		this.queueId = api.createQueue("writetest_queue");

	}

	@Override
	public void run(MemoryLogger logger) {
		logger.log("Connecting " + numberOfClients + " clients to " + host + ":" + port + "...");
		
		Thread[] clients = new Thread[numberOfClients];
		for(int i = 0; i < numberOfClients;i++){
			clients[i] = new Thread(new clientRunner(host, port, messagesPerClient, i+1, (int)this.queueId, i, logger));
		}
		
		logger.log("OK Done! Sending " + messagesPerClient + " messages sequentially to queue 1 per client...");
		
		StopWatch w = new StopWatch();
		
		w.start();
		
		//Start client threads
		for(int i = 0; i < numberOfClients;i++){
			clients[i].start();
		}
		
		//Wait for clients to finish
		for (Thread client : clients) {
			try {
				client.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		w.stop();
		
		float totalMessages = (float) (numberOfClients * messagesPerClient);
		float totalTimeInMs = w.getNanoTime()/1000/1000;
		
		logger.log("OK Done!");
		logger.log("-------------------------------------------");

		logger.log("Number of Clients:\t" + numberOfClients + "");
		logger.log("Messages per Client:\t" + messagesPerClient + "");
		logger.log("Total Messages:\t\t" + totalMessages + "");
		logger.log("");
		logger.log("Total Time:\t\t" + totalTimeInMs + "ms");
		logger.log("Per Message:\t\t" + totalTimeInMs/messagesPerClient/numberOfClients + "ms");
		logger.log("Messages/second:\t" + totalMessages/totalTimeInMs * 1000);
		logger.log("Time/message:\t\t" + totalTimeInMs/totalMessages + "ms");
	}

	@Override
	public String getInfo() {
		return "Sends a fixed number of messages to the server";
	}

	@Override
	public String getIdentifier() {
		return "sendMessages";
	}

	class clientRunner implements Runnable{
		
		ThorBangMQ client;
		private int messagesToSend;
		private int id;
		private int queue;
		private int userId;
		private MemoryLogger logger;
		
		public clientRunner(String hostname, int port, int messagesToSend, int userId, int queue, int id, MemoryLogger logger){
			
			this.messagesToSend = messagesToSend;
			this.id = id;
			this.queue = queue;
			this.logger = logger;
			this.userId = userId;
			
			try {
				
				client = ThorBangMQ.build(hostname, port, userId);
				client.init();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				for (int i = 0; i < messagesToSend; i++) {
					client.SendMessage(userId, queue, 1, 0, "message no #" + i + " from " + userId + " to " + userId);
				}
				logger.log("#" + id + " : Finished");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				client.stop();
			}
		}

	}
}