package asl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.FailedLoginException;

import asl.Persistence.IPersistence;
import asl.infrastructure.exceptions.InvalidClientException;
import asl.infrastructure.exceptions.InvalidMessageException;
import asl.infrastructure.exceptions.InvalidQueueException;
import asl.infrastructure.exceptions.PersistenceException;
import asl.network.ITransport;

public class ClientRequestWorker implements Runnable{
	private static String SendMessageStringFormat = "MSG,%d,%d,%d,%s";
	private final String okMessage = "OK";
	private final String queueFailMessage = "FAIL QUEUE %d";
	private final String clientFailMessage = "FAIL CLIENT %d";
	private final String messageFailMessage = "FAIL MESSAGE %d";
	private final String persistenceFailMessage = "FAIL UNKNOWN -1";

	private String requestString = null;
	private Logger logger;
	private ITransport transport;
	private IPersistence persistence;

	public ClientRequestWorker(
			Logger logger, 
			IPersistence persistence,
			ITransport transport, 
			String requestString) {
		this.requestString = requestString;
		this.logger = logger;
		this.transport = transport;
		this.persistence = persistence;
	}

	@Override
	public void run() {
		long start = System.nanoTime();
		try {
			interpreter(requestString);
		} catch (IOException e) {
			logger.severe("Error while writing to client: " + e);
		}finally{
			GlobalCounters.totalThinkTimeInClientRequestWorker.addAndGet(System.nanoTime()-start);
		}
	}

	// TODO handle exceptions
	private void interpreter(String msg) throws IOException {
		logger.fine(String.format("Interpreting message: \"%s\"", msg));
		String[] methodWithArgs = msg.split(",", 2);
try{
		switch (methodWithArgs[0]) {
		case "HELLO":
			transport.Send("OK");
			break;
		case "MSG":
			storeMessage(methodWithArgs[1]);
			break;
		case "PEEKQ":
			peekQueue(methodWithArgs[1]);
			break;
		case "PEEKS":
			peekQueueWithSender(methodWithArgs[1]);
			break;
		case "POPQ":
			popQueue(methodWithArgs[1]);
			break;
		case "POPS":
			popQueueWithSender(methodWithArgs[1]);
			break;
		case "CREATEQUEUE":
			createQueue(methodWithArgs[1]);
			break;
		case "REMOVEQUEUE":
			removeQueue(methodWithArgs[1]);
			break;
		case "CREATECLIENT":
			createClient(methodWithArgs[1]);
			break;
		case "REMOVECLIENT":
			removeClient(methodWithArgs[1]);
		default:
			logger.log(Level.WARNING,"Failed to interpert client request method: " + msg);
			transport.Send("BAD REQUEST");
			break;
		}
	}catch(Exception e){
		logger.log(Level.WARNING,"Failed to interpert client message: " + e);
		transport.Send("BAD REQUEST");
	}
	}
	
	public void storeMessage(String args) throws Exception {
		String[] msgArgs = args.split(",", 6);
		
		long reciever = Long.parseLong(msgArgs[0]);
		long sender = Long.parseLong(msgArgs[1]);
		int prio = Integer.parseInt(msgArgs[3]);
		long context = Long.parseLong(msgArgs[4]);
		String content = msgArgs[5];

		try{
			for(String queueStr : msgArgs[2].split(";")){
				long queue = Long.parseLong(queueStr);

				persistence.storeMessage(sender, reciever, queue, context, prio, content);
				
				GlobalCounters.numberOfMessagesPersisted.incrementAndGet();
			}
			
			transport.Send(okMessage);

			} catch (PersistenceException e) {
					transport.Send(persistenceFailMessage);
					throw e;
			} catch (InvalidQueueException e) {
					transport.Send(String.format(this.queueFailMessage, e.id));
					throw e;
			} catch (InvalidClientException e) {
					transport.Send(String.format(this.clientFailMessage, e.id));
					throw e;
		}
	}

	// PEEKQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	public void peekQueue(String argsConcat)  throws Exception{
		try{
			String[] args = argsConcat.split(",", 3);

			long reciever = Long.parseLong(args[0]);
			long queue = Long.parseLong(args[1]);
			Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[2]) == 1;

			Message m = peekQueue(reciever, queue, getByTimestampInsteadOfPriority);
			
			GlobalCounters.numberOfMessagesReturned.incrementAndGet();
			transport.Send(formatMessage(m));
			
		}  catch (InvalidQueueException e) {
			transport.Send(String.format(this.queueFailMessage, e.id));
			throw e;
		} catch (PersistenceException e) {
			transport.Send(String.format(this.persistenceFailMessage));
			throw e;
		} catch (InvalidMessageException e) {
			transport.Send(String.format(this.messageFailMessage, e.id));
			throw e;
		}
	}

	// PEEKS,ReceiverId,QueueId,SenderId,OrderByTimestampInsteadPriority
	public void peekQueueWithSender(String argsConcat) throws Exception {
		String[] args = argsConcat.split(",", 4);

		long receiver = Long.parseLong(args[0]);
		long queue = Long.parseLong(args[1]);
		long sender = Long.parseLong(args[2]);
		Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[3]) == 1;
		try {
			Message m = persistence.getMessageBySender(queue, receiver, sender,getByTimestampInsteadOfPriority);

			transport.Send(formatMessage(m));
			GlobalCounters.numberOfMessagesReturned.incrementAndGet();
			
		} catch (InvalidClientException e) {
			transport.Send( String.format(this.clientFailMessage, e.id));
			throw e;
		} catch (InvalidQueueException e) {
			transport.Send(String.format(this.queueFailMessage, e.id));
			throw e;
		} catch (PersistenceException e) {
			transport.Send(String.format(this.persistenceFailMessage));
			throw e;
		}
	}

	// POPQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	public void popQueue(String argsConcat) throws Exception {
		String[] args = argsConcat.split(",", 3);

		long receiverId = Long.parseLong(args[0]);
		long queueId = Long.parseLong(args[1]);
		Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[2]) == 1;

		logger.finer(String.format("Popping queue %d for user %d ordered by %s", queueId, receiverId, getByTimestampInsteadOfPriority ? "time" : "prio"));
		Message m;
		try {
			m = peekQueue(receiverId, queueId, getByTimestampInsteadOfPriority);
			if(m != null)
				persistence.deleteMessage(m.id);

			GlobalCounters.numberOfMessagesReturned.incrementAndGet();

			logger.finer(String.format("Sending popped message"));

			transport.Send(this.formatMessage(m));
			
		} catch (InvalidMessageException e) {
			transport.Send(String.format(this.messageFailMessage, e.id));
			throw e;
		} catch (PersistenceException e) {
			transport.Send(String.format(this.persistenceFailMessage));
			throw e;
		} catch (InvalidQueueException e) {
			transport.Send( String.format(this.queueFailMessage, e.id));
			throw e;
		}
	}

	// POPQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	public void popQueueWithSender(String argsConcat) throws Exception {	
		String[] args = argsConcat.split(",", 4);

		long receiverId = Long.parseLong(args[0]);
		long queueId = Long.parseLong(args[1]);
		long senderId = Long.parseLong(args[2]);
		Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[3]) == 1;

		try {
			Message m = persistence.getMessageBySender(queueId, receiverId, senderId,getByTimestampInsteadOfPriority);
			if(m != null)
				persistence.deleteMessage(m.id);
			
			transport.Send(this.formatMessage(m));
			GlobalCounters.numberOfMessagesReturned.incrementAndGet();
			
		} catch (InvalidMessageException e) {
			transport.Send(String.format(this.messageFailMessage, e.id));
			throw e;
		} catch (PersistenceException e) {
			transport.Send(String.format(this.persistenceFailMessage));
			throw e;
		} catch (InvalidClientException e) {
			transport.Send(String.format(this.clientFailMessage, e.id));
			throw e;
		} catch (InvalidQueueException e) {
			transport.Send(String.format(this.queueFailMessage, e.id));
			throw e;
		}
	}

	// CREATEQUEUE,NameOfQueue
	public void createQueue(String args) throws Exception {
		// TODO: unspecified that this will return -1.
		try {
			long queueId = persistence.createQueue(args);
			transport.Send(String.valueOf(queueId));
		} catch (PersistenceException e) {
			transport.Send(String.format(this.persistenceFailMessage));
			throw e;
		}
	}

	// REMOVEQUEUE,QueueId
	public void removeQueue(String args)  throws Exception{
		try {
			persistence.removeQueue(Long.parseLong(args));
			transport.Send(okMessage);
		} catch (InvalidQueueException e) {
			transport.Send(String.format(this.queueFailMessage, e.id));
			throw e;
		} catch (PersistenceException e) {
			transport.Send(String.format(this.persistenceFailMessage));
			throw e;
		}
	}
	
	// CREATECLIENT,NameOfClient
	public void createClient(String arg) throws Exception {
		// TODO: Unspecified that this could return -1.
		try {
			Long clientId = persistence.createClient(arg);
			transport.Send(clientId.toString());
		} catch (PersistenceException e) {
			transport.Send(this.persistenceFailMessage);
			throw e;
		}
	}
	
	//REMOVECLIENT,id
	public void removeClient(String arg) throws Exception {
		
		try {
			persistence.removeClient(Long.parseLong(arg));
			transport.Send(okMessage);
		} catch (PersistenceException e) {
			transport.Send(this.persistenceFailMessage);
			throw e;
		} catch (InvalidClientException e) {
			transport.Send(String.format(this.clientFailMessage, e.id));
			throw e;
		}
	}
	
	public static String formatMessage(Message m) {
		if(m == null)
			return "MSG0";
		
		return String.format(SendMessageStringFormat, m.senderId, m.contextId,
				              m.id, m.content);
	}
	
	private Message peekQueue(long receiver, long queue, boolean getByTimestampInsteadOfPriority) throws InvalidQueueException, PersistenceException, InvalidMessageException{
		Message m;
		if (getByTimestampInsteadOfPriority) {
			m = persistence.getMessageByTimestamp(queue, receiver);
		} else {
			m = persistence.getMessageByPriority(queue, receiver);
		}
		
		return m;
	}
}
