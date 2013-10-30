package asl.infrastructure;

import asl.GlobalCounters;
import asl.Message;
import asl.Persistence.IPersistence;
import asl.infrastructure.exceptions.InvalidClientException;
import asl.infrastructure.exceptions.InvalidMessageException;
import asl.infrastructure.exceptions.InvalidQueueException;
import asl.infrastructure.exceptions.PersistenceException;

public class ProtocolService implements IProtocolService {
	private static String SendMessageStringFormat = "MSG,%d,%d,%d,%s";
	private final String okMessage = "OK";
	private final String queueFailMessage = "FAIL QUEUE %d";
	private final String clientFailMessage = "FAIL CLIENT %d";
	private final String messageFailMessage = "FAIL MESSAGE %d";
	private final String persistenceFailMessage = "FAIL UNKNOWN -1";
	
	
	private IPersistence persistence;

	
	public ProtocolService(IPersistence persistence){
		this.persistence = persistence;
	}

	// MSG,ReceiverId,SenderId,QueueId,Priority,Context,Content
	@Override
	public String storeMessage(String argsConcat) {
		String[] args = argsConcat.split(",", 6);

		long receiver = Long.parseLong(args[0]);
		long sender = Long.parseLong(args[1]);
		long queue = Long.parseLong(args[2]);
		int priority = Integer.parseInt(args[3]);
		long context = Long.parseLong(args[4]);
		String content = args[5];

		Message message = new Message(receiver, sender, 0L, queue, 0L, priority, context, content);

		try {
			persistence.storeMessage(message);
		} catch (PersistenceException e) {
			return this.persistenceFailMessage;
		} catch (InvalidQueueException e) {
			return String.format(this.queueFailMessage, e.id);
		} catch (InvalidClientException e) {
			return String.format(this.clientFailMessage, e.id);
		}
		
		GlobalCounters.numberOfMessagesPersisted.incrementAndGet();
		return this.okMessage;
	}

	// PEEKQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	@Override
	public String peekQueue(String argsConcat) {
		String[] args = argsConcat.split(",", 3);

		long receiver = Long.parseLong(args[0]);
		long queue = Long.parseLong(args[1]);
		Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[2]) == 1;

		Message m;
		try {
			if (getByTimestampInsteadOfPriority) {
				m = persistence.getMessageByTimestamp(queue, receiver);
			} else {
				m = persistence.getMessageByPriority(queue, receiver);
			}
		}  catch (InvalidQueueException e) {
			return String.format(this.queueFailMessage, e.id);
		} catch (PersistenceException e) {
			return String.format(this.persistenceFailMessage);
		} catch (InvalidMessageException e) {
			return String.format(this.messageFailMessage, e.id);
		}
		
		GlobalCounters.numberOfMessagesReturned.incrementAndGet();
		
		return this.formatMessage(m);
	}

	// PEEKS,ReceiverId,QueueId,SenderId,OrderByTimestampInsteadPriority
	@Override
	public String peekQueueWithSender(String argsConcat) {
		String[] args = argsConcat.split(",", 4);

		long receiver = Long.parseLong(args[0]);
		long queue = Long.parseLong(args[1]);
		long sender = Long.parseLong(args[2]);
//		Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[3]) == 1;

		// TODO: Differentiate on getByTimestampInsteadOfPriority
		Message m;
		try {
			m = persistence.getMessageBySender(queue, receiver, sender);
		} catch (InvalidClientException e) {
			return String.format(this.clientFailMessage, e.id);
		} catch (InvalidQueueException e) {
			return String.format(this.queueFailMessage, e.id);
		} catch (PersistenceException e) {
			return String.format(this.persistenceFailMessage);
		}

		GlobalCounters.numberOfMessagesReturned.incrementAndGet();
		return this.formatMessage(m);
	}

	// POPQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	@Override
	public String popQueue(String argsConcat) {
		
		String[] args = argsConcat.split(",", 3);

		long receiverId = Long.parseLong(args[0]);
		long queueId = Long.parseLong(args[1]);
		Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[2]) == 1;
		
		Message m;
		try {
			// TODO: make this a single call.
			if (getByTimestampInsteadOfPriority) {
				m = persistence.getMessageByTimestamp(queueId, receiverId);
			} else {
				m = persistence.getMessageByPriority(queueId, receiverId);
			}
			persistence.deleteMessage(m.id);
		} catch (InvalidMessageException e) {
			return String.format(this.messageFailMessage, e.id);
		} catch (PersistenceException e) {
			return String.format(this.persistenceFailMessage);
		} catch (InvalidQueueException e) {
			return String.format(this.queueFailMessage, e.id);
		}

		GlobalCounters.numberOfMessagesReturned.incrementAndGet();
		return this.formatMessage(m);
	}

	// POPQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	@Override
	public String popQueueWithSender(String argsConcat) {	
		String[] args = argsConcat.split(",", 4);

		long receiverId = Long.parseLong(args[0]);
		long queueId = Long.parseLong(args[1]);
		long senderId = Long.parseLong(args[2]);
//		Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[3]) == 1;
		
		Message m;
		try {
			m = persistence.getMessageBySender(queueId, receiverId, senderId);
			persistence.deleteMessage(m.id);
		} catch (InvalidMessageException e) {
			return String.format(this.messageFailMessage, e.id);
		} catch (PersistenceException e) {
			return String.format(this.persistenceFailMessage);
		} catch (InvalidClientException e) {
			return String.format(this.clientFailMessage, e.id);
		} catch (InvalidQueueException e) {
			return String.format(this.queueFailMessage, e.id);
		}

		GlobalCounters.numberOfMessagesReturned.incrementAndGet();
		return this.formatMessage(m);
	}

	// CREATEQUEUE,NameOfQueue
	@Override
	public String createQueue(String name) {
		// TODO: unspecified that this will return -1.
		Long queueId;
		try {
			queueId = persistence.createQueue(name);
		} catch (PersistenceException e) {
			return String.format(this.persistenceFailMessage);
		}
		return queueId.toString();
	}

	// REMOVEQUEUE,QueueId
	@Override
	public String removeQueue(long id) {
		try {
			persistence.removeQueue(id);
		} catch (InvalidQueueException e) {
			return String.format(this.queueFailMessage, e.id);
		} catch (PersistenceException e) {
			return String.format(this.persistenceFailMessage);
		}
		return this.okMessage;
	}
	
	// CREATECLIENT,NameOfClient
	@Override
	public String createClient(String name) {
		// TODO: Unspecified that this could return -1.
		Long clientId;
		try {
			clientId = persistence.createClient(name);
		} catch (PersistenceException e) {
			return String.format(this.persistenceFailMessage);
		}
		return clientId.toString();
	}
	
	private String formatMessage(Message m) {
		return String.format(SendMessageStringFormat, m.senderId, m.contextId,
				              m.id, m.content);
	}
}
