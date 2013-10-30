package clientManager;

import infrastructure.exceptions.InvalidClientException;
import infrastructure.exceptions.InvalidQueueException;
import infrastructure.exceptions.ServerException;

import java.io.IOException;

import asl.ThorBangMQ;

public class clientRunner implements Runnable{
	
	ThorBangMQ client;
	private int messagesToSend;
	private int id;
	private int queue;
	private int userId;
	
	public clientRunner(String hostname, int port, int messagesToSend, int userId, int queue, int id){
		
		this.messagesToSend = messagesToSend;
		this.id = id;
		this.queue = queue;
		this.userId = userId;
		
		try {
			
			client = ThorBangMQ.build(hostname, port, userId);
			client.init();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < messagesToSend; i++) {
				try {
					client.SendMessage(userId, queue, 5, 0, "message no #" + i + " from " + userId + " to " + userId);
				} catch (InvalidClientException e) {
					System.out.println(String.format("Invalid client id: %d", e.id));
				} catch (NumberFormatException | InvalidQueueException
						| ServerException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("#" + id + " : Finished");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			client.stop();
		}
	}

}
