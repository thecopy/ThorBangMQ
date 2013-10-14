package asl;

import java.io.IOException;

import asl.network.ITransport;

public class ThorBangMQ {
	private String SendMessageStringFormat = "MSG,%d,%d,%d,%s,%s";
	private String PeekStringFormat = "PEEK,%d,%d,%d";
	
	private ITransport transport;
	private long userId;
	public ThorBangMQ(ITransport transport, long userId){
		this.transport = transport;
		this.userId = userId;
	}
	
	public void init() throws Exception{
		String response = this.transport.SendAndGetResponse("HELLO");
		
		if(!response.equals("OK"))
			throw new Exception("Connection failed! Expected server to respond with OK (server full?). Got: " + response);
	}
	
	public void SendMessage(long recieverId, long queueId, String context, String content) throws IOException {
		transport.Send(String.format(SendMessageStringFormat, 
				recieverId,
				userId,
				queueId,
				context,
				content));
	}
	
	public Message PeekMessage(long queueId, Boolean getByTime) throws IOException {
		String msg = transport.SendAndGetResponse(String.format(PeekStringFormat, 
						userId,
						queueId,
						getByTime ? 1 : 0));
		
		if(msg.startsWith("MSG0"))
			return null;
		
		String[] msgParts = msg.split(",", 5);
		
		long sender = Long.parseLong(msgParts[1]);
		String context = msgParts[2];
		long id = Long.parseLong(msgParts[3]);
		String content = msgParts[4];
		
		return new Message(sender, context, id, content);
	}
}
