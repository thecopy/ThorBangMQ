package testRunner.tests;

import infrastructure.exceptions.InvalidClientException;
import infrastructure.exceptions.InvalidQueueException;
import infrastructure.exceptions.ServerException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.commons.lang3.time.StopWatch;

import asl.ThorBangMQ;
import testRunner.MemoryLogger;

public class SendMessagesTime extends testRunner.Test {
	int numberOfClients = 0;
	int lengthOfExperiment = 0;
	ArrayList<Long> clients;
	long queueId;
	
	@Override
	public String[] getArgsDescriptors() {
		String[] descriptors = new String[2];
		descriptors[0] = "Number of clients";
		descriptors[1] = "Length of experiment";
		
		return descriptors;
	}

	@Override
	public void init(String[] args) throws Exception {
		numberOfClients = Integer.parseInt(args[0]);
		lengthOfExperiment = Integer.parseInt(args[1]);
		clients = new ArrayList<Long>();
		
		ThorBangMQ api = ThorBangMQ.build(this.host, this.port, 1);		
		for(int i = 0; i < numberOfClients; i += 1) {
			clients.add(api.createClient("client_" + i));
		}
		this.queueId = api.createQueue("writetest_queue");

	}

	@Override
	public void run(MemoryLogger applicationLogger, MemoryLogger testLogger) throws Exception {
		applicationLogger.log("Connecting " + numberOfClients + " clients to " + host + ":" + port + "...");
		
		Thread[] clients = new Thread[numberOfClients];
		for(int i = 0; i < numberOfClients;i++){
			clients[i] = new Thread(new clientRunner(host, port, 1, 1));
		}
		
		applicationLogger.log("OK Done! Sending messages...");
		
		StopWatch w = new StopWatch();
		
		
		//Start client threads
		for(int i = 0; i < numberOfClients;i++){
			clients[i].start();
		}
		w.start();
		ArrayList<String> splitTimes = new ArrayList<String>(lengthOfExperiment/500+1);
		Boolean stop = false;
		while(!stop){
			Thread.sleep(500);
			w.split();
			long millis = w.getSplitNanoTime() / 1000000;
			applicationLogger.log("Checking of " + millis + " > " + lengthOfExperiment);
			if(millis /*ms*/> lengthOfExperiment)
				stop = true;
			w.unsplit();
		}
		
		for (Thread client : clients) {
			try{
			client.interrupt();
			}catch(Exception ignore) {}
		}
		
		w.stop();
		float totalTimeInMs = w.getNanoTime()/1000/1000;
		
		applicationLogger.log("OK Done!");
		applicationLogger.log("-------------------------------------------");

		applicationLogger.log("Number of Clients:\t" + numberOfClients + "");
		applicationLogger.log("Total Time:\t\t" + totalTimeInMs + "ms");
	}

	@Override
	public String getInfo() {
		return "Sends messages to the server withing a specific time limit";
	}

	@Override
	public String getIdentifier() {
		return "sendMessagesTime";
	}

	class clientRunner implements Runnable{
		
		ThorBangMQ client;
		private int queue;
		private int userId;
		
		public int numberOfMessagesSent = 0;
		
		public Boolean keepRunning = true;
		public clientRunner(String hostname, int port, int userId, int queue){
			this.queue = queue;
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
				while (keepRunning) {
					client.SendMessage(userId, queue, 1, 0, "message");
					numberOfMessagesSent++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				client.stop();
			}
		}

	}
}
