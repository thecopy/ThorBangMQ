package asl;

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
		
		client.SendMessage(1, 1, 1, 0, "HEJ");
		
		System.out.println("OK! Peeking...");
		Message msg  = client.PopMessage(1, true);
		
		if(msg != null){
			System.out.println("OK! Got message: " + msg.content);
		}else{
			System.out.println("Did not get msg :(");
		}
	}
		

}
