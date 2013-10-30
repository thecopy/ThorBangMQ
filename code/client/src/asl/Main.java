package asl;

import infrastructure.exceptions.InvalidClientException;
import infrastructure.exceptions.InvalidQueueException;
import infrastructure.exceptions.ServerException;

import java.io.IOException;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Starting ThorBangMQ Client...");
		
		ThorBangMQ client = ThorBangMQ.build("127.0.0.1", 8123, 1);

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
	}
		

}
