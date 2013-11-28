package testRunner.tests;

import infrastructure.exceptions.InvalidClientException;
import infrastructure.exceptions.InvalidQueueException;
import infrastructure.exceptions.InvalidRequestException;
import infrastructure.exceptions.ServerException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.time.StopWatch;

import asl.Message;
import asl.ThorBangMQ;
import testRunner.Counters;
import testRunner.IntervalLogger;
import testRunner.MemoryLogger;

public class DistributedStandardTest extends testRunner.Test {

	
	private String[] hosts;
	private long lengthOfExperiment;
	private int numberOfClients;
	private int ratio = 3;
	
	
	@Override
	public String[] getArgsDescriptors() {
		String[] descriptors = new String[3];
		descriptors[0] = "Number of clients";
		descriptors[1] = "Length of experiment";
		descriptors[2] = "Hosts seperated by a semicolon, e.g. host1;host2";
		
		return descriptors;
	}

	@Override
	public void init(String[] args) throws Exception {
		this.numberOfClients = Integer.parseInt(args[0]);
		this.lengthOfExperiment = Long.parseLong(args[1]);
		this.hosts = args[2].split(";");
	}

	@Override
	public void run(MemoryLogger applicationLogger, MemoryLogger testLogger) throws Exception {
		int numberOfTwoWayClients = numberOfClients/ratio;
		int numberOfOneWayClients = numberOfClients-numberOfTwoWayClients;
		
		applicationLogger.info("Number of 1-way clients: "+ numberOfOneWayClients);
		applicationLogger.info("Number of 2-way clients: "+ numberOfTwoWayClients);
		
		ThorBangMQ setupClient = ThorBangMQ.build(hosts[0], this.port, 1);
		
		long queue1 = setupClient.createQueue("oneWayQueue");
		long queue2 = setupClient.createQueue("twoWayQueue");
		
		clientRunner[] oneWayClients = new clientRunner[numberOfOneWayClients];
		clientRunner[] twoWayClients = new clientRunner[numberOfTwoWayClients];
		Thread[] oneWayClientThreads = new Thread[numberOfOneWayClients];
		Thread[] twoWayClientThreads = new Thread[numberOfTwoWayClients];
		long firstOneWayClientUserId = -1; // Create and start one way clients
		
		IntervalLogger intervalLogger = new IntervalLogger(250, testLogger, Level.INFO);
		Thread intervalLoggingThread = new Thread(intervalLogger);
		intervalLoggingThread.start();
		for(int i = 0; i < numberOfOneWayClients; i++)
		{
			int hostIndex = i % hosts.length;
			String host = hosts[hostIndex];
			long userId = setupClient.createClient("onewayclient" + i);
			if(firstOneWayClientUserId == -1)
				firstOneWayClientUserId = userId;
			
			oneWayClients[i] = new clientRunner
					(host, 
					port, 
					userId, 
					queue1, 
					numberOfOneWayClients, 
					true /* is one way client */,
					i == 0 /* is the first one way client */, 
					firstOneWayClientUserId,
					applicationLogger);
			
			oneWayClientThreads[i] = new Thread(oneWayClients[i]);
			oneWayClientThreads[i].start();
		}
		
		// Create Two Way and start clients
		for(int i = 0; i < numberOfTwoWayClients; i++)
		{
			int hostIndex = i % hosts.length;
			String host = hosts[hostIndex];
			long userId = setupClient.createClient("twowayclient" + i);
			twoWayClients[i] = new clientRunner
					(host, 
					port, 
					userId, 
					queue1, 
					numberOfOneWayClients, 
					false /* is one way client */,
					false /* is the first one way client */, 
					firstOneWayClientUserId,
					applicationLogger);
			

			twoWayClientThreads[i] = new Thread(twoWayClients[i]);
			twoWayClientThreads[i].start();
		}
		
		applicationLogger.info("Created and started all clients!");
		applicationLogger.info("Waiting " + lengthOfExperiment + "ms");
		Thread.sleep(lengthOfExperiment);

		applicationLogger.info("Ok, waited " + lengthOfExperiment + "ms. Shutting down clients...");
		intervalLoggingThread.interrupt();
		for(int i = 0; i < numberOfOneWayClients; i++)
		{
			oneWayClients[i].keepRunning = false;
				oneWayClientThreads[i].interrupt();
		}
		for(int i = 0; i < numberOfTwoWayClients; i++)
		{
			twoWayClients[i].keepRunning = false;
				twoWayClientThreads[i].interrupt();
		}
		
		applicationLogger.info("Ok, done :)");
	}

	@Override
	public String getInfo() {
		return "Creates one way and two way clients distributed on many middleware";
	}

	@Override
	public String getIdentifier() {
		return "distributedStandardTest";
	}

	class clientRunner implements Runnable{
		
		ThorBangMQ client;
		private long queue;
		private long userId;

		public volatile long messageCounter = 0;
		
		public Boolean keepRunning = true;
		private boolean oneWay;
		private int numOfOneWayers;
		private boolean sendTheFirst;
		private long firstOneWayer;
		private Logger applicationLogger;
		
		
		public clientRunner(String hostname, int port, long userId, long queue, int numOfOneWayers, boolean oneWay, 
				boolean sendTheFirst, long firstOneWayer, Logger applicationLogger){
			this.queue = queue;
			this.userId = userId;
			this.oneWay = oneWay;
			this.numOfOneWayers = numOfOneWayers;
			this.sendTheFirst = sendTheFirst;
			this.firstOneWayer = firstOneWayer;
			try {
				
				client = ThorBangMQ.build(hostname, port, userId);
				client.init();
				
			} catch (Exception e) {
				applicationLogger.severe(e.getMessage());
			}
			this.applicationLogger = applicationLogger;
			applicationLogger.info("Built client runner with host "  + hostname + ":" + port);
		}

		@Override
		public void run() {
			
			Random r = new Random();
			try {
				if(sendTheFirst){
					long target = (firstOneWayer + r.nextInt(numOfOneWayers));
					sendMessage(target, queue, 1, 0, String.valueOf(++messageCounter));
					Counters.RequestsPerformed.incrementAndGet();
				}
				
				while (keepRunning) {
					if(oneWay){
						Message msg = null;
						do{
							Thread.sleep(200);
							msg = popMessage(queue, true);
							Counters.RequestsPerformed.incrementAndGet();
						}while(msg == null);
						messageCounter = Long.parseLong(msg.content);

						long target = -1;
						do{
							target = (firstOneWayer + r.nextInt(numOfOneWayers));
						}while(target == userId);
						
						sendMessage(target, queue, 1, 0, String.valueOf(++messageCounter));
						Counters.RequestsPerformed.incrementAndGet();
					}else{ // Two-Way
						if(userId%2 == 1){ // Sender
							long context = r.nextLong();
							sendMessage(userId+1, queue, 1, context, String.valueOf(++messageCounter));
							Counters.RequestsPerformed.incrementAndGet();
							Message msg = null;
							do{
								Thread.sleep(200);
								msg = popMessage(queue, true);
								Counters.RequestsPerformed.incrementAndGet();
							}while(msg == null || msg.context != context);
							
						}else{ // Receiver
							Message msg = null;
							do{
								Thread.sleep(300);
								msg = popMessage(queue, true);
								Counters.RequestsPerformed.incrementAndGet();
							}while(msg == null);
							messageCounter = Long.parseLong(msg.content);
							sendMessage(msg.sender, queue, 1, msg.context, String.valueOf(++messageCounter));
							Counters.RequestsPerformed.incrementAndGet();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				applicationLogger.info("Error in client " + userId + ": " + e.getClass() + " " + e.getMessage());
			} finally {
				client.stop();
			}
		}
		
		private StopWatch w = new StopWatch();
		private Message popMessage(long queue, boolean timestamp) throws IOException, InvalidQueueException, ServerException{
			w.reset();
			w.start();
			Message m = client.PopMessage(queue, true);
			w.stop();
			Counters.ResponseTimeLogger.log("," + w.getNanoTime());
			return m;
		}
		private void sendMessage(long receiver, long queue, int prio, long context, String content) throws IOException, InvalidQueueException, InvalidClientException, ServerException, InvalidRequestException
		{
			w.reset();
			w.start();
			client.SendMessage(receiver, queue, prio, context, content);
			w.stop();
			Counters.ResponseTimeLogger.log("," + w.getNanoTime());
		}

	}
}
