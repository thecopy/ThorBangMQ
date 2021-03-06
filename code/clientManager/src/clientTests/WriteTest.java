package clientTests;

import infrastructure.exceptions.InvalidQueueException;
import infrastructure.exceptions.ServerException;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.time.StopWatch;

import asl.ThorBangMQ;
import clientManager.clientRunner;

public class WriteTest extends ClientTest {
	private ArrayList<Long> clients;
	private long queueId;
	
	public WriteTest(String hostName, int port, int numClients, int numMessagesPerClient) {
		super(hostName, port, numClients, numMessagesPerClient);
		clients = new ArrayList<Long>();
	}
	
	@Override
	public void prepare() throws IOException, ServerException {
		ThorBangMQ api = ThorBangMQ.build(this.hostName, this.port, 1);
		
		for(int i = 0; i < this.numClients; i += 1) {
			clients.add(api.createClient("client_" + i));
		}
		
		this.queueId = api.createQueue("writetest_queue");
		
	}
	
	@Override
	public void cleanUp() throws IOException, NumberFormatException, ServerException, InvalidQueueException {
		ThorBangMQ api = ThorBangMQ.build(this.hostName, this.port, 1);
		for (long clientId: this.clients) {
//			api.removeClient(clientId);
		}
		api.removeQueue(this.queueId);
	}
	
	@Override
	public void start() throws IOException {		
		System.out.println("Connecting " + numClients + " clients to " + this.hostName + ":" + this.port + "...");
		
		Thread[] clients = new Thread[this.numClients];
		for(int i = 0; i < this.numClients;i++){
			clients[i] = new Thread(new clientRunner(this.hostName, port, this.numMessagesPerClient, 5, (int)this.queueId, i));
		}
		
		System.out.println("OK Done! Sending " + this.numMessagesPerClient + " messages sequentially to queue 1 per client...");
		
		StopWatch w = new StopWatch();
		
		w.start();
		
		//Start client threads
		for(int i = 0; i < this.numClients;i++){
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
		
		float totalMessages = (float) (this.numClients * this.numMessagesPerClient);
		float totalTimeInMs = w.getNanoTime()/1000/1000;
		
		System.out.println("OK Done!");
		System.out.println("-------------------------------------------");

		System.out.println("Number of Clients:\t" + this.numClients+ "");
		System.out.println("Messages per Client:\t" + this.numMessagesPerClient + "");
		System.out.println("Total Messages:\t\t" + totalMessages + "");
		System.out.println("");
		System.out.println("Total Time:\t\t" + totalTimeInMs + "ms");
		System.out.println("Per Message:\t\t" + totalTimeInMs/this.numMessagesPerClient/this.numClients + "ms");
		System.out.println("Messages/second:\t" + totalMessages/totalTimeInMs * 1000);
		System.out.println("Time/message:\t\t" + totalTimeInMs/totalMessages + "ms");
		
		System.in.read();
	}
}
