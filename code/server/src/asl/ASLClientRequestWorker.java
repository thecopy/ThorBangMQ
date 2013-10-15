package asl;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import asl.Persistence.IPersistence;
import asl.network.ITransport;

public class ASLClientRequestWorker implements Runnable{
	private static String SendMessageStringFormat = "MSG,%d,%s,%d,%s";

	private String requestString = null;
	private IPersistence persistence;
	private Logger logger;
	private ITransport transport;

	public ASLClientRequestWorker(Logger logger, IPersistence persistence, ITransport transport, String requestString) {
		this.requestString = requestString;
		this.logger = logger;
		this.persistence = persistence;
		this.transport = transport;

		logger.info(String.format("Processing Request: %s\n", requestString));
	}

	@Override
	public void run() {
		try {
			interperter(requestString);
		} catch (IOException e) {
			logger.severe("Error while writing to client: " + e);
		}
	}

	// TODO handle exceptions
	private void interperter(String msg) throws IOException{
		String[] methodWithArgs = msg.split(",", 2);

		switch(methodWithArgs[0]){
		case "HELLO":
			transport.Send("OK");
			break;
		case "MSG":
			storeMessage(methodWithArgs[1]);
			break;
		case "PEEKQ":
			sendMessage(peekQueue(methodWithArgs[1]));
			break;
		case "PEEKS":
			sendMessage(peekQueueWithSender(methodWithArgs[1]));
			break;
		case "POPQ":
			sendMessage(popQueue(methodWithArgs[1]));
			break;
		case "POPS":
			sendMessage(popQueueWithSender(methodWithArgs[1]));
			break;
		case "CREATEQUEUE":
			transport.Send(String.valueOf(createQueue(methodWithArgs[1])));
			break;
		case "REMOVEQUEUE":
			removeQueue(Long.parseLong(methodWithArgs[1]));
			transport.Send("OK");
			break;
		default:
			logger.log(Level.WARNING, "Failed to interpert client message: " + msg);
			break;
		}
	}

	// MSG,ReceiverId,SenderId,QueueId,Priority,Context,Content
	private void storeMessage(String argsConcat){
		String[] args = argsConcat.split(",", 6);

		long receiver = Long.parseLong(args[0]);
		long sender = Long.parseLong(args[1]);
		long queue = Long.parseLong(args[2]);
		int priority = Integer.parseInt(args[3]);
		long context = Long.parseLong(args[4]);
		String content = args[5];

		Message message = new Message(receiver, sender, 0, queue, 0, priority, context, content);

		persistence.storeMessage(message);
	}

	// PEEKQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	private Message peekQueue(String argsConcat){
		String[] args = argsConcat.split(",", 3);

		long receiver = Long.parseLong(args[0]);
		long queue = Long.parseLong(args[1]);
		Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[2]) == 1;

		Message m = null;
		if(getByTimestampInsteadOfPriority)
			m = persistence.getMessageByTimestamp(queue, receiver);
		else
			m = persistence.getMessageByPriority(queue, receiver);
		
		return m;
	}
	
	
	// PEEKS,ReceiverId,QueueId,SenderId,OrderByTimestampInsteadPriority
	private Message peekQueueWithSender(String argsConcat){
		String[] args = argsConcat.split(",", 4);

		long receiver = Long.parseLong(args[0]);
		long queue = Long.parseLong(args[1]);
		long sender = Long.parseLong(args[2]);
		Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[3]) == 1;

		// TODO: Differentiate on getByTimestampInsteadOfPriority
		Message m = null;
		m = persistence.getMessageBySender(queue, receiver, sender);
		
		return m;
	}
	
	// POPQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	private Message popQueue(String argsConcat){
		Message m = peekQueue(argsConcat);
		
		if(m != null)
			persistence.deleteMessage(m.id);
		
		return m;
	}

	// POPQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	private Message popQueueWithSender(String argsConcat){
		Message m = peekQueueWithSender(argsConcat);
		
		if(m != null)
			persistence.deleteMessage(m.id);
		
		return m;
	}
	
	
	// CREATEQUEUE,NameOfQueue
	private long createQueue(String name){
		return persistence.createQueue(name);
	}
	
	// REMOVEQUEUE,QueueId
	private void removeQueue(long id){
		persistence.removeQueue(id);
	}
	
	private void sendMessage(Message m){
		if(m == null)
			transport.Send("MSG0");
		else
			transport.Send(formatMessage(m));
	}
	
	private String formatMessage(Message m){
		return String.format(SendMessageStringFormat, m.senderId, m.content, m.id, m.content);
	}
}
