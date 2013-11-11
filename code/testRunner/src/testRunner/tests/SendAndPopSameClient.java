package testRunner.tests;

import infrastructure.exceptions.InvalidClientException;
import infrastructure.exceptions.InvalidQueueException;
import infrastructure.exceptions.ServerException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import asl.ThorBangMQ;
import testRunner.MemoryLogger;

public class SendAndPopSameClient extends testRunner.Test {
	int numberOfClients = 0;
	int lengthOfExperiment = 0;
	int poppers = 1;
	int msgSize = 0;
	ArrayList<Integer> clientIds;
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
		this.numberOfClients = Integer.parseInt(args[0]);
		this.lengthOfExperiment = Integer.parseInt(args[1]);
		this.msgSize = Integer.parseInt(args[2]);
		clientIds = new ArrayList<Integer>();
		
		ThorBangMQ api = ThorBangMQ.build(this.host, this.port, 1);		
		for(int i = 0; i < numberOfClients; i += 1) {
			clientIds.add((int)api.createClient("client_" + i));
		}
		this.queueId = api.createQueue("writetest_queue");

	}

	@Override
	public void run(MemoryLogger applicationLogger, MemoryLogger testLogger) throws Exception {
		applicationLogger.log(String.format("numberOfClients: %d", this.numberOfClients));
		applicationLogger.log(String.format("lengthOfExperiment: %d", this.lengthOfExperiment));
		applicationLogger.log(String.format("msgSize: %d", this.msgSize));
		applicationLogger.log("Connecting " + numberOfClients + " clients to " + host + ":" + port + "...");

		Thread[] clients = new Thread[numberOfClients];
		clientRunner[] runners = new clientRunner[numberOfClients];
		for(int i = 0; i < numberOfClients;i++){
			runners[i] = new clientRunner(host, port, clientIds.get(i), (int)queueId, this.msgSize);
			clients[i] = new Thread(runners[i]);
		}
		
		applicationLogger.log("OK Done! Sending messages...");
		
		StopWatch w = new StopWatch();
		
		
		//Start client threads
		for(int i = 0; i < numberOfClients;i++){
			clients[i].start();
		}
		w.start();
		
		Thread.sleep(lengthOfExperiment);
		
		for (int i = 0; i < numberOfClients;i++) {
			try{
				runners[i].keepRunning = false;
				clients[i].join();
			}catch(Exception ignore) {
				ignore.printStackTrace();
			}
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
		return "sendAndPopSameClient";
	}

	class clientRunner implements Runnable{
		
		ThorBangMQ client;
		private int queue;
		private int userId;
		
		public int numberOfMessagesSent = 0;
		public int numberOfMessagesPoped = 0;
		public int msgSize = 0;
		
		public Boolean keepRunning = true;
		public clientRunner(String hostname, int port, int userId, int queue, int msgSize){
			this.queue = queue;
			this.userId = userId;
			this.msgSize = msgSize;
			
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
				String msg = StringUtils.repeat("*", this.msgSize);
				while (keepRunning) {
					client.SendMessage(userId, queue, 1, 0, msg);
					numberOfMessagesSent++;
					client.PopMessage(queue, true);
					numberOfMessagesPoped++;
				}
			} catch (IOException | InvalidQueueException | InvalidClientException | ServerException e) {
				e.printStackTrace();
			} finally {
				client.stop();
			}
		}

	}
}
