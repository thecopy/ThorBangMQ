package asl;

import infrastructure.exceptions.InvalidClientException;
import infrastructure.exceptions.InvalidQueueException;
import infrastructure.exceptions.ServerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

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
		} catch (NumberFormatException | InvalidQueueException
				| InvalidClientException | ServerException e1) {
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
						+ "stdmsg number\tSend a message to yourself in queue 1 with message length number bytes\n"
						+ "pop\t\tPops queue 1\n"
						+ "r number cmd\tRepeat <cmd> <number> of times. <cmd> can be any command including a special command";
				System.out.println(helpText);
				continue;
			}else if(input.startsWith("r! ") || input.startsWith("r ")) {
				String[] parts = input.split(" ",3);
				int repeats = Integer.parseInt(parts[1]);
				String command = transformCommand(parts[2]);
				double[] times = new double[repeats];
				double avg = 0;
				boolean print = input.equals("r!");
				for(int i = 0;i<repeats;i++){
					StopWatch w = new StopWatch();
					w.start();
					client.getTransport().SendAndGetResponse(command);
					w.stop();
					times[i] = w.getNanoTime() / 1000 / (double)1000;
					avg += times[i];
					if(print)
						System.out.print(times[i] + " ");
				}
				System.out.println("\nRepeated command " + repeats + " times.");
				System.out.println("Average: " + avg/(double)repeats + " ms");
				System.out.println("Max: " + Collections.max(Arrays.asList(ArrayUtils.toObject(times))) + " ms");
				System.out.println("Min: " + Collections.min(Arrays.asList(ArrayUtils.toObject(times))) + " ms");
				continue; 
			}else{
				input = transformCommand(input);
			}
			
			StopWatch w = new StopWatch();
			w.start();
			String response = client.getTransport().SendAndGetResponse(input);
			w.stop();
			System.out.println("Operation duration: " + w.getNanoTime() / 1000f / 1000 + " ms");
			System.out.println(String.format("[%d] %s", 
					response.length(), 
					response.substring(0, response.length() > 100 ? 100 : response.length())));
			
		}
	}
	
	private static String transformCommand(String input){
		if(input.startsWith("stdmsg")){
			if(input.length() > 6){
				//     MSG,ReceiverId,SenderId,QueueId,Priority,Context,Content
				int length = Integer.parseInt(input.substring(7));
				String content = StringUtils.leftPad("", length, 'M');
				input = String.format("MSG,%d,%d,1,1,0,%s", 1,1,content);
			}else{
				input = String.format("MSG,%d,%d,1,1,0,%s", 1,1,"Standard Message");
			}
		}else if(input.equals("pop")){
			input = "POPQ,1,1,1";
		}
		
		return input;
	}
		

}
