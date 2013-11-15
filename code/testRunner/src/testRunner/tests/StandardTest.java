package testRunner.tests;

import infrastructure.exceptions.InvalidClientException;
import infrastructure.exceptions.InvalidQueueException;
import infrastructure.exceptions.ServerException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.lang3.time.StopWatch;

import asl.Message;
import asl.ThorBangMQ;
import testRunner.Counters;
import testRunner.MemoryLogger;

public class StandardTest extends testRunner.Test {
	int numberOfClients = 0;
	int lengthOfExperiment = 0;
	int poppers = 1;
	ArrayList<Long> clients;
	long queueId;
	private int numberOfOneWayClients;
	private int numberOfTwoWayClients;
	private Long oneWayQueueId;
	private Long twoWayQueueId;
	
	@Override
	public String[] getArgsDescriptors() {
		String[] descriptors = new String[3];
		descriptors[0] = "Number of one way clients";
		descriptors[1] = "Number of two way client (must be even)";
		descriptors[2] = "Length of experiment";
		
		return descriptors;
	}

	@Override
	public void init(String[] args) throws Exception {
		numberOfOneWayClients = Integer.parseInt(args[0]);
		numberOfTwoWayClients = Integer.parseInt(args[1]);
		lengthOfExperiment = Integer.parseInt(args[2]);
		clients = new ArrayList<Long>();

		ThorBangMQ api = ThorBangMQ.build(this.host, this.port, 1);		
		for(int i = 0; i < (numberOfOneWayClients+numberOfTwoWayClients); i++) {
			clients.add(api.createClient("client_" + i));
		}
		
		this.oneWayQueueId = api.createQueue("oneway_queue");
		this.twoWayQueueId = api.createQueue("twoway_queue");
	}

	@Override
	public void run(MemoryLogger applicationLogger, MemoryLogger testLogger) throws Exception {
		applicationLogger.log(String.format("numberOfOneWayClients: %d", this.numberOfOneWayClients));
		applicationLogger.log(String.format("numberOfTwoWayClients: %d", this.numberOfTwoWayClients));
		applicationLogger.log(String.format("lengthOfExperiment: %d", this.lengthOfExperiment));
		try{
			ArrayList<clientRunner> oneWayClients = new ArrayList<clientRunner>(numberOfOneWayClients);
			ArrayList<clientRunner> twoWayClients = new ArrayList<clientRunner>(numberOfTwoWayClients);
			
			for(int i = 0; i < numberOfOneWayClients; i++){
				oneWayClients.add(new clientRunner(host,port,clients.get(i) ,
						oneWayQueueId, numberOfOneWayClients, true, i == 0, clients.get(0)));
			}
			for(int i = numberOfOneWayClients; i < (numberOfOneWayClients+numberOfTwoWayClients); i++){
				twoWayClients.add(new clientRunner(host,port,clients.get(i), 
						twoWayQueueId, numberOfTwoWayClients, false, false, -1));
			}
			
			applicationLogger.log("Starting all clients...");
			ArrayList<Thread> threads = new ArrayList<Thread>(numberOfOneWayClients + numberOfTwoWayClients);
			for(int i = 0; i < numberOfOneWayClients; i++){
				Thread t = (new Thread(oneWayClients.get(i)));
				threads.add(t);
				t.start();
			}
			for(int i = 0; i < numberOfTwoWayClients; i++){
				Thread t = (new Thread(twoWayClients.get(i)));
				threads.add(t);
				t.start();
			}
			System.out.println("Done! Waiting for " + lengthOfExperiment + " ms");
			Thread.sleep(lengthOfExperiment);
			applicationLogger.log("OK DONE:) Shutting down all clients...");
			
			for(int i = 0;i<threads.size();i++)
				threads.get(i).interrupt();
			
			applicationLogger.log("OK");
		}catch(Exception ignore){
			ignore.printStackTrace();
		}
	}

	@Override
	public String getInfo() {
		return "Creates one way and two way clients";
	}

	@Override
	public String getIdentifier() {
		return "standardTest";
	}

	class clientRunner implements Runnable{
		
		ThorBangMQ client;
		private long queue;
		private long userId;

		public volatile long popcount = 0;
		public volatile long pushcount = 0;
		
		public volatile long messageCounter = 0;
		
		public Boolean keepRunning = true;
		private boolean oneWay;
		private int numOfOneWayers;
		private boolean sendTheFirst;
		final MemoryLogger l = new MemoryLogger(false);
		private long firstOneWayer;
		
		public void dumpLog() throws FileNotFoundException{
			l.dumpToFile("log_client" + userId + ".log");
		}
		
		public clientRunner(String hostname, int port, long userId, long queue, int numOfOneWayers, boolean oneWay, 
				boolean sendTheFirst, long firstOneWayer){
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
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(true){
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						l.log("," + pushcount + "," + popcount);
						pushcount = 0;
						popcount = 0;
					}
				}
			});
			t.start();
			Random r = new Random();
			try {
				if(sendTheFirst){
					long target = (firstOneWayer + r.nextInt(numOfOneWayers));
					client.SendMessage(target, queue, 1, 0, String.valueOf(++messageCounter));
					pushcount++;
				}
				
				while (keepRunning) {
					if(oneWay){
						Message msg = null;
						do{
							Thread.sleep(200);
							msg = client.PopMessage(queue, true);
							popcount++;
							
						}while(msg == null);
						messageCounter = Long.parseLong(msg.content);

						long target = -1;
						do{
							target = (firstOneWayer + r.nextInt(numOfOneWayers));
						}while(target == userId);
							client.SendMessage(target, queue, 1, 0, String.valueOf(++messageCounter));
							pushcount++;
					}else{ // Two-Way
						if(userId%2 == 1){ // Sender
							long context = r.nextLong();
							client.SendMessage(userId+1, queue, 1, context, String.valueOf(++messageCounter));
							pushcount++;
							Message msg = null;
							do{
								Thread.sleep(200);
								msg = client.PopMessage(queue, true);
								popcount++;
							}while(msg == null || msg.context != context);
							
						}else{ // Receiver
							Message msg = null;
							do{
								Thread.sleep(300);
								msg = client.PopMessage(queue, true);
								popcount++;
							}while(msg == null);
							messageCounter = Long.parseLong(msg.content);
							client.SendMessage(msg.sender, queue, 1, msg.context, String.valueOf(++messageCounter));
							pushcount++;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				client.stop();
				try {
					l.dumpToFile("log_client" + userId + ".log");
					t.interrupt();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
}
