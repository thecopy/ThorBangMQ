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
	int numQueues = 1;
	int sleepBetweenRequests = 0;
	
	ArrayList<Integer> clientIds;
	ArrayList<Long> queueIds;
	
	@Override
	public String[] getArgsDescriptors() {
		String[] descriptors = new String[5];
		descriptors[0] = "Number of clients";
		descriptors[1] = "Length of experiment";
		descriptors[2] = "Message size";
		descriptors[3] = "Number of queues";
		descriptors[4] = "Sleep time between requests";
		
		return descriptors;
	}

	@Override
	public void init(String[] args) throws Exception {
		this.numberOfClients = Integer.parseInt(args[0]);
		this.lengthOfExperiment = Integer.parseInt(args[1]);
		try {
			this.msgSize = Integer.parseInt(args[2]);
		} catch (Exception e) {
			this.msgSize = 1024;
		}

		try {
			this.numQueues = Integer.parseInt(args[3]);
		} catch (Exception e) {
			this.numQueues = 1;
		}
		
		try {
			this.sleepBetweenRequests = Integer.parseInt(args[4]);
		} catch (Exception e) {
			this.sleepBetweenRequests = 0;
		}

		ThorBangMQ api = ThorBangMQ.build(this.host, this.port, 1);
		
		clientIds = new ArrayList<Integer>();
		for(int i = 0; i < this.numberOfClients; i += 1) {
			clientIds.add((int)api.createClient("client_" + i));
		}
		
		this.queueIds = new ArrayList<Long>();
		for (int i = 0; i < this.numQueues; i += 1) {
			this.queueIds.add(api.createQueue("writetest_queue_" + i));
		}
	}

	@Override
	public void run(MemoryLogger applicationLogger, MemoryLogger testLogger) throws Exception {
		applicationLogger.log(String.format("numberOfClients: %d", this.numberOfClients));
		applicationLogger.log(String.format("lengthOfExperiment: %d", this.lengthOfExperiment));
		applicationLogger.log(String.format("msgSize: %d", this.msgSize));
		applicationLogger.log(String.format("numQueues: %d", this.numQueues));
		applicationLogger.log(String.format("sleepBetweenRequests: %d", this.sleepBetweenRequests));
		applicationLogger.log("Connecting " + numberOfClients + " clients to " + host + ":" + port + "...");

		Thread[] clients = new Thread[numberOfClients];
		clientRunner[] runners = new clientRunner[numberOfClients];
		for(int i = 0; i < numberOfClients;i++){
			long queueId = this.queueIds.get((i + 1) % this.numQueues);
			runners[i] = new clientRunner(host, port, clientIds.get(i), (int)queueId, this.msgSize, this.sleepBetweenRequests);
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
		
		logIndividualResults(testLogger, clients, runners);
	}

	@Override
	public String getInfo() {
		return "Sends and pops messages withing a specific time limit";
	}

	@Override
	public String getIdentifier() {
		return "sendAndPopSameClient";
	}
	
	private void logIndividualResults(MemoryLogger testLogger, Thread[] clients, clientRunner[] runners) {
		for (int i = 0; i < clients.length;i++) {
			float totalSendTime = 0;
			float minSendTime = Long.MAX_VALUE;
			float maxSendTime = Long.MIN_VALUE;
			for (int z = 0; z < runners[i].sendTime.size(); z += 1) {
			    long cSendTime = runners[i].sendTime.get(z);
			    totalSendTime += cSendTime;
			    if (cSendTime < minSendTime) {
			        minSendTime = cSendTime;
			    }
			    if (cSendTime > maxSendTime) {
			        maxSendTime = cSendTime;
			    }
			}
			float avgSendTime = totalSendTime / (float)runners[i].sendTime.size();
			
			float totalPopTime = 0;
			float minPopTime = Long.MAX_VALUE;
			float maxPopTime = Long.MIN_VALUE;
			for (int z = 0; z < runners[i].popTime.size(); z += 1) {
				float cPopTime = runners[i].popTime.get(z);
				totalPopTime += cPopTime;
				if (cPopTime < minPopTime) {
					minPopTime = cPopTime;
				}
				if (cPopTime > maxPopTime) {
					maxPopTime = cPopTime;
				}
			}
			float avgPopTime = totalPopTime / (float)runners[i].popTime.size();
			
			testLogger.info(String.format("Client: %s, #send: %d, total time: %f, avg: %f, min: %f, max: %f",
					                      clients[i].getName(), runners[i].sendTime.size(), totalSendTime / (float)1000000, avgSendTime / (float)1000000, minSendTime / (float)1000000, maxSendTime / (float)1000000));
			testLogger.info(String.format("Client: %s, #pop: %d, total time: %f, avg: %f, min: %f, max: %f",
	                                      clients[i].getName(), runners[i].popTime.size(), totalPopTime / (float)1000000, avgPopTime / (float)1000000, minPopTime / (float)1000000, maxPopTime / (float)1000000));
			testLogger.info(String.format("Client: %s, #fails: %d", clients[i].getName(), runners[i].numberOfFails));
		}
	}

	class clientRunner implements Runnable{
		
		ThorBangMQ client;
		private int queueId;
		private int userId;
		
		public ArrayList<Long> popTime = new ArrayList<Long>();
		public ArrayList<Long> sendTime = new ArrayList<Long>();
		public int msgSize = 0;
		public int numberOfFails = 0;
		public int sleepBetweenRequests = 0;
		
		public Boolean keepRunning = true;
		public clientRunner(String hostname, int port, int userId, int queueId, int msgSize, int sleepBetweenRequests){
			this.queueId = queueId;
			this.userId = userId;
			this.msgSize = msgSize;
			this.sleepBetweenRequests = sleepBetweenRequests;
			
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
				StopWatch w = new StopWatch();
				while (keepRunning) {
					w.reset();
					try {
						w.start();
						client.SendMessage(userId, queueId, 1, 0, msg);
						w.stop();
						this.sendTime.add(w.getNanoTime());
					} catch (InvalidQueueException | InvalidClientException | ServerException | IOException e) {
						this.numberOfFails += 1;
						w.stop();
					}
					if (this.sleepBetweenRequests > 0)  {
						Thread.sleep(this.sleepBetweenRequests);
					}
					w.reset();
					try {
						w.start();
						client.PopMessage(queueId, true);
						w.stop();
						this.popTime.add(w.getNanoTime());
					} catch (InvalidQueueException | ServerException | IOException e) {
						this.numberOfFails += 1;
						w.stop();
					}
					if (this.sleepBetweenRequests > 0)  {
						Thread.sleep(this.sleepBetweenRequests);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				client.stop();
			}
		}

	}
}
