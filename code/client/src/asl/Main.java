package asl;

import java.io.IOException;
import java.net.Socket;

import asl.network.ITransport;
import asl.network.SocketTransport;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Starting ThorBangMQ Client...");
		
		Socket socket = new Socket("127.0.0.1", 8123);
		ITransport transport = new SocketTransport(socket);
		
		ThorBangMQ client = new ThorBangMQ(transport, 123);

		System.out.println("Initializing connection...");
		
		try {
			client.init();
		} catch (Exception e) {
			System.out.println("Failed to init: " + e.getMessage());
			return;
		}
		
		System.out.println("Init successfull!");

		System.out.println("Sending msg to self...");
		client.SendMessage(123, 1, "", "HEJ");
		
		System.out.println("OK! Peeking...");
		Message msg  = client.PeekMessage(1, true);
		
		if(msg != null){
			System.out.println("OK! Got message: " + msg.content);
		}else{
			System.out.println("Did not get msg :(");
		}
	}
		

}
