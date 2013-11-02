package asl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.FailedLoginException;

import asl.Persistence.IPersistence;
import asl.infrastructure.IProtocolService;
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
	private IProtocolService ps;
	private IPersistence persistence;

	public ClientRequestWorker(Logger logger, IProtocolService ps, ITransport transport, String requestString) {
		this.requestString = requestString;
		this.logger = logger;
		this.transport = transport;
		this.ps = ps;
		//this.persistence = persistence;
	}

	@Override
	public void run() {
		try {
			interpreter(requestString);
		} catch (IOException e) {
			logger.severe("Error while writing to client: " + e);
		}
	}

	// TODO handle exceptions
	private void interpreter(String msg) throws IOException {
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
			logger.log(Level.WARNING,"Failed to interpert client message: " + msg);
			transport.Send("BAD REQUEST");
			break;
		}
	}catch(Exception e){
		logger.log(Level.WARNING,"Failed to interpert client message: " + e);
		transport.Send("BAD REQUEST");
	}
	}
	
	public void storeMessage(String args) {
		String[] msgArgs = args.split(",", 6);
		
		long reciever = Long.parseLong(msgArgs[0]);
		long sender = Long.parseLong(msgArgs[1]);
		int prio = Integer.parseInt(msgArgs[3]);
		long context = Long.parseLong(msgArgs[4]);
		String content = msgArgs[5];

		try{
			for(String queueStr : msgArgs[2].split(";")){
				long queue = Long.parseLong(queueStr);
				ps.storeMessage(reciever, sender, queue, prio, context, content);
			}
			
			transport.Send(okMessage);

			} catch (PersistenceException e) {
					transport.Send(persistenceFailMessage);
			} catch (InvalidQueueException e) {
					transport.Send(String.format(this.queueFailMessage, e.id));
			} catch (InvalidClientException e) {
					transport.Send(String.format(this.clientFailMessage, e.id));
		}
	}

	// PEEKQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	public void peekQueue(String argsConcat) {
		try{
			String[] args = argsConcat.split(",", 3);

			long reciever = Long.parseLong(args[0]);
			long queue = Long.parseLong(args[1]);
			Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[2]) == 1;
			Message m = ps.peekQueue(reciever, queue, getByTimestampInsteadOfPriority);
			transport.Send(formatMessage(m));
			
		}  catch (InvalidQueueException e) {
			transport.Send(String.format(this.queueFailMessage, e.id));
		} catch (PersistenceException e) {
			transport.Send(String.format(this.persistenceFailMessage));
		} catch (InvalidMessageException e) {
			transport.Send(String.format(this.messageFailMessage, e.id));
		}
	}

	// PEEKS,ReceiverId,QueueId,SenderId,OrderByTimestampInsteadPriority
	public void peekQueueWithSender(String argsConcat) {
		String[] args = argsConcat.split(",", 4);

		long receiver = Long.parseLong(args[0]);
		long queue = Long.parseLong(args[1]);
		long sender = Long.parseLong(args[2]);
		
		try {
			Message m = persistence.getMessageBySender(queue, receiver, sender);

			transport.Send(formatMessage(m));
			GlobalCounters.numberOfMessagesReturned.incrementAndGet();
			
		} catch (InvalidClientException e) {
			transport.Send( String.format(this.clientFailMessage, e.id));
		} catch (InvalidQueueException e) {
			transport.Send(String.format(this.queueFailMessage, e.id));
		} catch (PersistenceException e) {
			transport.Send(String.format(this.persistenceFailMessage));
		}
	}

	// POPQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	public void popQueue(String argsConcat) {
		
		String[] args = argsConcat.split(",", 3);

		long receiverId = Long.parseLong(args[0]);
		long queueId = Long.parseLong(args[1]);
		Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[2]) == 1;
		
		Message m;
		try {
			m = ps.peekQueue(receiverId, queueId, getByTimestampInsteadOfPriority);
			if(m != null)
				ps.deleteMessage(m.id);

			GlobalCounters.numberOfMessagesReturned.incrementAndGet();
			transport.Send(this.formatMessage(m));
			
		} catch (InvalidMessageException e) {
			transport.Send(String.format(this.messageFailMessage, e.id));
		} catch (PersistenceException e) {
			transport.Send(String.format(this.persistenceFailMessage));
		} catch (InvalidQueueException e) {
			transport.Send( String.format(this.queueFailMessage, e.id));
		}
	}

	// POPQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	public void popQueueWithSender(String argsConcat) {	
		String[] args = argsConcat.split(",", 4);

		long receiverId = Long.parseLong(args[0]);
		long queueId = Long.parseLong(args[1]);
		long senderId = Long.parseLong(args[2]);
		
		try {
			Message m = ps.peekQueueWithSender(queueId, receiverId, senderId);
			if(m != null)
				persistence.deleteMessage(m.id);
			
			transport.Send(this.formatMessage(m));
			GlobalCounters.numberOfMessagesReturned.incrementAndGet();
			
		} catch (InvalidMessageException e) {
			transport.Send(String.format(this.messageFailMessage, e.id));
		} catch (PersistenceException e) {
			transport.Send(String.format(this.persistenceFailMessage));
		} catch (InvalidClientException e) {
			transport.Send(String.format(this.clientFailMessage, e.id));
		} catch (InvalidQueueException e) {
			transport.Send(String.format(this.queueFailMessage, e.id));
		}
	}

	// CREATEQUEUE,NameOfQueue
	public void createQueue(String args) {
		// TODO: unspecified that this will return -1.
		Long queueId;
		try {
			queueId = ps.createQueue(args);
			transport.Send(queueId.toString());
		} catch (PersistenceException e) {
			transport.Send(String.format(this.persistenceFailMessage));
		}
	}

	// REMOVEQUEUE,QueueId
	public void removeQueue(String args) {
		try {
			ps.removeQueue(Long.parseLong(args));
			transport.Send(okMessage);
		} catch (InvalidQueueException e) {
			transport.Send(String.format(this.queueFailMessage, e.id));
		} catch (PersistenceException e) {
			transport.Send(String.format(this.persistenceFailMessage));
		}
	}
	
	// CREATECLIENT,NameOfClient
	public void createClient(String arg) {
		// TODO: Unspecified that this could return -1.
		Long clientId;
		try {
			clientId = ps.createClient(arg);
			transport.Send(clientId.toString());
		} catch (PersistenceException e) {
			transport.Send(this.persistenceFailMessage);
		}
	}
	
	//REMOVECLIENT,id
	public void removeClient(String arg) {
		
		try {
			ps.removeClient(Long.parseLong(arg));
			transport.Send(okMessage);
		} catch (PersistenceException e) {
			transport.Send(this.persistenceFailMessage);
		} catch (InvalidClientException e) {
			transport.Send(String.format(this.clientFailMessage, e.id));
		}
	}
	
	private String formatMessage(Message m) {
		if(m == null)
			return "MSG0";
		
		return String.format(SendMessageStringFormat, m.senderId, m.contextId,
				              m.id, m.content);
	}
}
