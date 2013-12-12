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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.time.StopWatch;

import asl.Message;
import asl.ThorBangMQ;
import testRunner.Counters;
import testRunner.IntervalLogger;
import testRunner.MemoryLogger;

public class DistributedStressTest extends testRunner.Test {

	
	private String[] hosts;
	private long lengthOfExperiment;
	private int numberOfClients;
	private long sleep;
	
	@Override
	public String[] getArgsDescriptors() {
		String[] descriptors = new String[3];
		descriptors[0] = "Number of clients";
		descriptors[1] = "Sleep in ms";
		descriptors[2] = "Length of experiment";
		descriptors[3] = "Hosts seperated by a semicolon, e.g. host1:port;host2:port";
		
		return descriptors;
	}

	@Override
	public void init(String[] args) throws Exception {
		this.numberOfClients = Integer.parseInt(args[0]);
		this.sleep = Long.parseLong(args[1]);
		this.lengthOfExperiment = Long.parseLong(args[2]);
		this.hosts = args[3].split(";");
	}

	@Override
	public void run(MemoryLogger applicationLogger, MemoryLogger testLogger) throws Exception {
		applicationLogger.info("Number of clients: "+ numberOfClients);
		applicationLogger.info("Sleeping: "+ sleep);

		String[] h = hosts[0].split(":");
		int port = h.length == 1 ? this.port : Integer.parseInt(h[1]);
		ThorBangMQ setupClient = ThorBangMQ.build(h[0], port, 1);
		
		long queue = setupClient.createQueue("stressTestQueue");
		
		clientRunner[] clients = new clientRunner[numberOfClients];
		Thread[] clientThreads = new Thread[numberOfClients];
		
		IntervalLogger intervalLogger = new IntervalLogger(250, testLogger, Level.INFO);
		Thread intervalLoggingThread = new Thread(intervalLogger);
		intervalLoggingThread.start();
		
		ExecutorService parallelInitiator = Executors.newFixedThreadPool(50); // assume 50 worker threads on server
		
		for(int i = 0; i < numberOfClients; i++)
		{
			int hostIndex = i % hosts.length;
			h = hosts[hostIndex].split(":");
			port = h.length == 1 ? this.port : Integer.parseInt(h[1]);
			long userId = setupClient.createClient("onewayclient" + i);
			
			clients[i] = new clientRunner
					(h[0], 
					port, 
					userId, 
					queue,
					applicationLogger);
			
			parallelInitiator.submit(new clientInitiator(clients[i],i));
		}
		
		parallelInitiator.shutdown();
		parallelInitiator.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		
		applicationLogger.info("Initiated  all clients!");
		for(int i = 0; i < numberOfClients; i++){
			clientThreads[i] = new Thread(clients[i]);
			clientThreads[i].start();
		}
		applicationLogger.info("Started all clients!");
		
		applicationLogger.info("Waiting " + lengthOfExperiment + "ms");
		Thread.sleep(lengthOfExperiment);

		applicationLogger.info("Ok, waited " + lengthOfExperiment + "ms. Shutting down clients...");
		intervalLoggingThread.interrupt();
		for(int i = 0; i < numberOfClients; i++)
		{
			clients[i].keepRunning = false;
			clientThreads[i].interrupt();
		}
		
		applicationLogger.info("Ok, done :)");
	}

	@Override
	public String getInfo() {
		return "Creates non-thinking clients distributed on many middleware";
	}

	@Override
	public String getIdentifier() {
		return "distributedStressTest";
	}

	class clientRunner implements Runnable{
		
		ThorBangMQ client;
		private long queue;
		private long userId;

		public volatile long messageCounter = 0;
		
		public Boolean keepRunning = true;
		private Logger applicationLogger;
		private String hostname;
		private int port;
		
		
		public clientRunner(String hostname, int port, long userId, long queue, Logger applicationLogger){
			this.queue = queue;
			this.userId = userId;
			this.hostname = hostname;
			this.port = port;
			this.applicationLogger = applicationLogger;
			
			try {
				
				client = ThorBangMQ.build(hostname, port, userId);
				applicationLogger.info("Constructed client with host "  + this.hostname + ":" + this.port);

			} catch (Exception e) {
				applicationLogger.severe(e.getMessage());
			}
		}
		
		public void init(){
			try {
				client.init();
			} catch (Exception e) {
				applicationLogger.severe(e.getMessage());
			}
			applicationLogger.info("Built client with host "  + this.hostname + ":" + this.port);

		}

		@Override
		public void run() {
			try {
				applicationLogger.info("Client Id " + this.userId + " started (" + this.hostname + ":" +
			this.port + ")!");
				while (keepRunning) {
					Thread.sleep(sleep);
					sendMessage(this.userId, this.queue, 2, 0, "some message 123");
					Thread.sleep(sleep);
					popMessage(this.queue, true /* get by time */);
				}
			} catch (Exception e) {
				e.printStackTrace();
				applicationLogger.severe("Error in client " + userId + ": " + e.getClass() + " " + e.getMessage());
			} finally {
				client.stop();
			}
		}
		
		private StopWatch w = new StopWatch();
		private Message popMessage(long queue, boolean timestamp) throws IOException, InvalidQueueException, ServerException{
			w.reset();
			w.start();
			Message m = client.PopMessage(queue, timestamp);
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
	
	public class clientInitiator implements Runnable {
		private clientRunner client;
		private int i;
		public clientInitiator(clientRunner client, int i){
			this.client = client;
			this.i = i;
		}
		@Override
		public void run() {
			client.init();			
			System.out.println("Inited client " + i);
		}

	}
}
