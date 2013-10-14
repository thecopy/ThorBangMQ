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

	private void interperter(String msg) throws IOException{
		String[] methodWithArgs = msg.split(",", 2);

		switch(methodWithArgs[0]){
		case "HELLO":
			transport.Send("OK");
			break;
		case "SEND":
			storeMessage(methodWithArgs[1]);
			break;
		case "PEEKQ":
			Message m = peekMessage(methodWithArgs[1]);
			if(m == null)
			{
				transport.Send("MSG0");
			}else{
				transport.Send(String.format(SendMessageStringFormat, m.senderId, m.content, m.id, m.content));
			}
			break;
		default:
			logger.log(Level.WARNING, "Failed to interpert client message: " + msg);
			break;
		}
	}

	private void storeMessage(String argsConcat){
		String[] args = argsConcat.split(",", 6);

		long receiver = Long.parseLong(args[0]);
		long sender = Long.parseLong(args[1]);
		long queue = Long.parseLong(args[2]);
		long context = Long.parseLong(args[3]);
		int priority = Integer.parseInt(args[4]);
		String content = args[5];

		Message message = new Message(receiver, sender, 0, queue, 0, priority, context, content);

		persistence.storeMessage(message);
	}

	private Message peekMessage(String argsConcat){
		String[] args = argsConcat.split(",", 3);

		long receiver = Long.parseLong(args[0]);
		long queue = Long.parseLong(args[1]);
		Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[2]) == 1;

		if(getByTimestampInsteadOfPriority)
			return persistence.getMessageByTimestamp(queue, receiver);
		else
			return persistence.getMessageByPriority(queue, receiver);
	}
}
