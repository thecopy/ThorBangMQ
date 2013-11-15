package asl;

import infrastructure.MemoryLogger;
import infrastructure.exceptions.InvalidClientException;
import infrastructure.exceptions.InvalidQueueException;
import infrastructure.exceptions.ServerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Starting ThorBangMQ Client...");
		String host = "127.0.0.1";
		if(args.length > 0)
			host = args[0];
		ThorBangMQ client = ThorBangMQ.build(host, 8123, 1);
		System.out.println("Initializing connection...");
		
		try {
			client.init();
		} catch (Exception e) {
			System.out.println("Failed to init: " + e.getMessage());
			return;
		}
		
		System.out.println("Init successfull!");

		System.out.println("Sending msg to self...");
		
		try {
			client.SendMessage(1, 1, 1, 0, "HEJ");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		System.out.println("OK! Peeking...");
		Message msg = null;
		try {
			msg = client.PopMessage(1, true);
		} catch (NumberFormatException | InvalidQueueException
				| ServerException e) {
			e.printStackTrace();
		}
		
		if (msg != null) {
			System.out.println("OK! Got message: " + msg.content);
		} else{
			System.out.println("Did not get msg :(");
		}	        
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(true){
			System.out.print(":");
			String input = br.readLine();
			
			if(input.equals("help"))
			{
				String helpText = "Type any message to send to the ThorBangMQ server.\n"
						+ "Special commands: \n"
						+ "stdmsg\t\tSends a message to yourself in queue 1\n"
						+ "stdmsg <number prio>\tSend a message to yourself in queue 1 with message length number bytes\n"
						+ "pop\t\tPops queue 1\n"
						+ "r! number cmd\tRepeat <cmd> <number> of times. <cmd> can be any command including a special command. r! will log all the request durations into a log file.";
				System.out.println(helpText);
				continue;
			}else if(input.startsWith("r! ") || input.startsWith("r ")) {
				String[] parts = input.split(" ",3);
				int repeats = Integer.parseInt(parts[1]);
				String command = transformCommand(parts[2]);
				double[] times = new double[repeats];
				double avg = 0;
				boolean log = input.startsWith("r!");

				MemoryLogger mlogger = new MemoryLogger(false);
				mlogger.setLevel(Level.ALL);
				System.out.println(command);
				for(int i = 0;i<repeats;i++){
					Thread.sleep(5);
					long now = System.nanoTime();
					client.getTransport().SendAndGetResponse(command);
					times[i] = (System.nanoTime()-now) / 1000 / (double)1000;
					avg += times[i];
					if(log)
						mlogger.log("," + times[i]);
				}
				if(log){
					String fileName = "repeatlog_" + repeats + "_" + input + ".log";
					System.out.println("Durations saved to file: " + System.getProperty("user.dir") + "/" + fileName);
					mlogger.dumpToFile(fileName);
				}
				System.out.println("\nRepeated command " + repeats + " times.");
				System.out.println("Average: " + avg/(double)repeats + " ms");
				continue; 
			}else{
				input = transformCommand(input);
			}
			
			long now = System.nanoTime();
			String response = client.getTransport().SendAndGetResponse(input);
			long diff = System.nanoTime()-now;
			System.out.println("Operation duration: " + diff / 1000f / 1000 + " ms");
			System.out.println(String.format("[%d] %s", 
					response.length(), 
					response.substring(0, response.length() > 100 ? 100 : response.length())));
			
		}
	}
	
	private static String transformCommand(String input){
		if(input.startsWith("stdmsg")){
			if(input.length() > 6){
				String[] params = input.split(" ");
				//     MSG,ReceiverId,SenderId,QueueId,Priority,Context,Content
				int length = Integer.parseInt(params[1]);
				int prio = Integer.parseInt(params[2]);
				String content = fixedLenthString("M", length);
				input = String.format("MSG,%d,%d,1,%d,0,%s", 1,1,prio,content);
			}else{
				input = String.format("MSG,%d,%d,1,1,0,%s", 1,1,"Standard Message");
			}
		}else if(input.equals("pop")){
			input = "POPQ,1,1,1";
		}
		
		return input;
	}
		
	public static String fixedLenthString(String string, int length) {
	    return String.format("%1$"+length+ "s", string);
	}

}
