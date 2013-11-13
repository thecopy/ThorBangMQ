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

public class SendAndPopMessages extends testRunner.Test {
	int numberOfClients = 0;
	int lengthOfExperiment = 0;
	int poppers = 1;
	ArrayList<Long> clients;
	long queueId;
	
	@Override
	public String[] getArgsDescriptors() {
		String[] descriptors = new String[3];
		descriptors[0] = "Number of clients";
		descriptors[1] = "Length of experiment";
		descriptors[2] = "Number of clients who 'pop'. If this argument is 3 every third client will pop";
		
		return descriptors;
	}

	@Override
	public void init(String[] args) throws Exception {
		numberOfClients = Integer.parseInt(args[0]);
		lengthOfExperiment = Integer.parseInt(args[1]);
		poppers = Integer.parseInt(args[2]);
		clients = new ArrayList<Long>();
		
		ThorBangMQ api = ThorBangMQ.build(this.host, this.port, 1);		
		for(int i = 0; i < numberOfClients; i += 1) {
			clients.add(api.createClient("client_" + i));
		}
		this.queueId = api.createQueue("writetest_queue");

	}

	@Override
	public void run(MemoryLogger applicationLogger, MemoryLogger testLogger) throws Exception {
		applicationLogger.log(String.format("numberOfClients: %d", this.numberOfClients));
		applicationLogger.log(String.format("lengthOfExperiment: %d", this.lengthOfExperiment));
		applicationLogger.log(String.format("poppers: %d", this.poppers));
		applicationLogger.log("Connecting " + numberOfClients + " clients to " + host + ":" + port + "...");

		Thread[] clients = new Thread[numberOfClients];
		for(int i = 0; i < numberOfClients;i++){
			clients[i] = new Thread(new clientRunner(host, port, 1, 1, i%poppers==0));
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
		return "Sends and pops messages withing a specific time limit";
	}

	@Override
	public String getIdentifier() {
		return "sendAndPopMessages";
	}

	class clientRunner implements Runnable{
		
		ThorBangMQ client;
		private int queue;
		private int userId;
		
		public int numberOfMessagesSent = 0;
		public int numberOfMessagesPoped = 0;
		
		public Boolean keepRunning = true;
		private boolean popOrSend;
		public clientRunner(String hostname, int port, int userId, int queue, boolean popOrSend){
			this.queue = queue;
			this.userId = userId;
			this.popOrSend = popOrSend;
			
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
					if(popOrSend)
					{
						client.PopMessage(queue, true);
						numberOfMessagesPoped++;
					}
					else
					{
						client.SendMessage(userId, queue, 1, 0, "message");
					
						numberOfMessagesSent++;
					}
				}
			} catch (IOException | InvalidQueueException | InvalidClientException | ServerException e) {
				e.printStackTrace();
			} finally {
				client.stop();
			}
		}

	}
}
