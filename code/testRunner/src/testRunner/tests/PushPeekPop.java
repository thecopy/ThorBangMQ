package testRunner.tests;

import infrastructure.exceptions.InvalidClientException;
import infrastructure.exceptions.InvalidQueueException;
import infrastructure.exceptions.ServerException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.lang3.time.StopWatch;

import asl.Message;
import asl.ThorBangMQ;
import testRunner.Counters;
import testRunner.MemoryLogger;

public class PushPeekPop extends testRunner.Test {
	private int repeats;
	private int chars;
	private long userId;
	private Long queue;
	
	@Override
	public String[] getArgsDescriptors() {
		String[] descriptors = new String[3];
		descriptors[0] = "Number of repeats";
		descriptors[1] = "Size of message to push (# of characters)";
		
		return descriptors;
	}

	@Override
	public void init(String[] args) throws Exception {
		repeats = Integer.parseInt(args[0]);
		chars = Integer.parseInt(args[1]);

		ThorBangMQ api = ThorBangMQ.build(this.host, this.port, 1);		
		this.userId = api.createClient("someclient");
		this.queue = api.createQueue("somequeue");
	}

	@Override
	public void run(MemoryLogger applicationLogger, MemoryLogger testLogger) throws Exception {
		try{
			clientRunner r = new clientRunner(host, port, 1, queue, repeats, chars, testLogger);
			Thread t = new Thread(r);
			t.start();
			
			applicationLogger.log("Started test, hold on...");
			
			t.join();
			
			applicationLogger.log("OK! :)");
		}catch(Exception ignore){
			ignore.printStackTrace();
		}
	}

	@Override
	public String getInfo() {
		return "Pushes, peeks and pops";
	}

	@Override
	public String getIdentifier() {
		return "pushPeekPop";
	}

	class clientRunner implements Runnable{
		
		ThorBangMQ client;
		private long queue;
		private long userId;
		
		public volatile long messageCounter = 0;
		
		public Boolean keepRunning = true;
		private int repeats;
		private int chars;
		private MemoryLogger logger;
		
		public clientRunner(String hostname, int port, long userId, long queue, int repeats, int chars, final MemoryLogger logger ){
			this.queue = queue;
			this.userId = userId;
			this.repeats = repeats;
			this.chars = chars;
			this.logger = logger;
			
			try{
				
				client = ThorBangMQ.build(hostname, port, userId);
				client.init();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				String content = "";
				for(int i = 0; i < chars; i++)
					content += "*";
				
				StopWatch w = new StopWatch();
				for(int i = 0; i < repeats; i ++){
					w.reset();
					w.start();
					client.SendMessage(userId, queue, 1, 0, content);
					w.stop();
					logger.log("," + w.getNanoTime()/(float)1000*1000);
				}
				logger.dumpToFile("push.log");
				logger.clear();

				for(int i = 0; i < repeats; i ++){
					w.reset();
					w.start();
					client.PeekMessage(queue, i%2==0);
					w.stop();
					logger.log("," + w.getNanoTime());
				}
				logger.dumpToFile("peek.log");
				logger.clear();

				for(int i = 0; i < repeats; i ++){
					w.reset();
					w.start();
					client.PopMessage(queue, i%2==0);
					w.stop();
					logger.log("," + w.getNanoTime());
				}
				logger.dumpToFile("pop.log");
				logger.clear();
				
			} catch (IOException | InvalidQueueException | InvalidClientException | ServerException e) {
				e.printStackTrace();
			} finally {
				client.stop();
			}
		}

	}
}
