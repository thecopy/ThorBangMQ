package asl;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import asl.network.ITransport;
import asl.network.SocketTransport;

public class ThorBangMQ {
	// MSG,ReceiverId,SenderId,QueueId,Priority,Context,Content
	private final static String SendMessageStringFormat = "MSG,%d,%d,%d,%d,%d,%s";
	
	private final static String PeekQueueStringFormat = "PEEKQ,%d,%d,%d";
	private final static String PeekQueueWithSenderStringFormat = "PEEKS,%d,%d,%d,%d";
	
	private final static String PopQueueStringFormat = "POPQ,%d,%d,%d";
	private final static String PopQueueWithSenderStringFormat = "POPS,%d,%d,%d,%d";
	
	private final static String CreateQueueStringFormat = "CREATEQUEUE,%s";
	private final static String RemoveQueueStringFormat = "REMOVEQUEUE,%d";
	
	private ITransport transport;
	private long userId;
	private long pollingInterval = 2000;
	
	public ThorBangMQ(ITransport transport, long userId){
		this.transport = transport;
		this.userId = userId;
	}
	
	public static ThorBangMQ build(String hostname, int port, long userId) throws UnknownHostException, IOException{
		Socket socket = new Socket(hostname, port);
		ITransport transport = new SocketTransport(socket);
		
		return new ThorBangMQ(transport, userId);
	}
	
	public void setPollingInterval(long milliseconds){ pollingInterval = milliseconds;}
	public long getPollingInterval() 				 { return pollingInterval; }
	
	public void init() throws Exception{
		String response = this.transport.SendAndGetResponse("HELLO");
		
		if(!response.equals("OK"))
			throw new Exception("Connection failed! Expected server to respond with OK (server full?). Got: " + response);
	}
	
	public void stop(){
		try {
			transport.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void SendMessage(long recieverId, long queueId, long priority, long context, String content) throws IOException {
		transport.SendAndGetResponse(String.format(SendMessageStringFormat, 
				recieverId,
				userId,
				queueId,
				priority,
				context,
				content));
	}
	
	public Message PeekMessage(long queueId, Boolean getByTime) throws IOException {
		String msg = transport.SendAndGetResponse(String.format(PeekQueueStringFormat, 
						userId,
						queueId,
						getByTime ? 1 : 0));
		
		if(msg.startsWith("MSG0"))
			return null;
		
		return parseMessage(msg);
	}
	
	public Message peekMessageFromSender(long queueId, long sender, Boolean getByTime) throws IOException {
		String msg = transport.SendAndGetResponse(String.format(PeekQueueWithSenderStringFormat, 
						userId,
						queueId,
						sender,
						getByTime ? 1 : 0));
		
		if(msg.startsWith("MSG0"))
			return null;
		
		return parseMessage(msg);
	}
	
	public Message PopMessage(long queueId, Boolean getByTime) throws IOException {
		String msg = transport.SendAndGetResponse(String.format(PopQueueStringFormat, 
						userId,
						queueId,
						getByTime ? 1 : 0));
		
		if(msg.startsWith("MSG0"))
			return null;
		
		return parseMessage(msg);
	}
	
	public Message popMessageFromSender(long queueId, long sender, Boolean getByTime) throws IOException {
		String msg = transport.SendAndGetResponse(String.format(PopQueueWithSenderStringFormat, 
						userId,
						queueId,
						sender,
						getByTime ? 1 : 0));
		
		if(msg.startsWith("MSG0"))
			return null;
		
		return parseMessage(msg);
	}
	
	public Long createQueue(String name) throws IOException {
		String queueId = transport.SendAndGetResponse(String.format(CreateQueueStringFormat, name));
		
		return Long.parseLong(queueId);
	}
	
	public void removeQueue(long id) throws IOException {
		String response = transport.SendAndGetResponse(String.format(RemoveQueueStringFormat, id));
		// TODO: throw exception or log if respone is FAIL
	}
	
	private Message parseMessage(String msg){
		String[] msgParts = msg.split(",", 5);
		
		long sender = Long.parseLong(msgParts[1]);
		String context = msgParts[2];
		long id = Long.parseLong(msgParts[3]);
		String content = msgParts[4];
		
		return new Message(sender, context, id, content);
	}
}
