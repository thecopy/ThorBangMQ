package asl.infrastructure;

import asl.GlobalCounters;
import asl.Message;
import asl.Persistence.IPersistence;
import asl.infrastructure.exceptions.InvalidClientException;
import asl.infrastructure.exceptions.InvalidMessageException;
import asl.infrastructure.exceptions.InvalidQueueException;
import asl.infrastructure.exceptions.PersistenceException;
import asl.network.ITransport;

public class ProtocolService implements IProtocolService {
	private static String SendMessageStringFormat = "MSG,%d,%d,%d,%s";
	
	private IPersistence persistence;
	private ITransport transport;
	
	public ProtocolService(IPersistence persistence, ITransport transport){
		this.persistence = persistence;
		this.transport = transport;
	}

	// MSG,ReceiverId,SenderId,QueueId,Priority,Context,Content
	@Override
	public void storeMessage(String argsConcat) {
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
			e.printStackTrace();
		} catch (InvalidQueueException e) {
			e.printStackTrace();
		} catch (InvalidClientException e) {
			e.printStackTrace();
		}
		
		GlobalCounters.numberOfMessagesPersisted.incrementAndGet();
	}

	// PEEKQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	@Override
	public Message peekQueue(String argsConcat) {
		String[] args = argsConcat.split(",", 3);

		long receiver = Long.parseLong(args[0]);
		long queue = Long.parseLong(args[1]);
		Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[2]) == 1;

		Message m = null;
		if (getByTimestampInsteadOfPriority)
			try {
				m = persistence.getMessageByTimestamp(queue, receiver);
			} catch (InvalidQueueException e) {
				e.printStackTrace();
			} catch (PersistenceException e) {
				e.printStackTrace();
			} catch (InvalidMessageException e) {
				e.printStackTrace();
			}
		else
			try {
				m = persistence.getMessageByPriority(queue, receiver);
			} catch (InvalidQueueException e) {
				e.printStackTrace();
			} catch (PersistenceException e) {
				e.printStackTrace();
			}

		GlobalCounters.numberOfMessagesReturned.incrementAndGet();
		
		return m;
	}

	// PEEKS,ReceiverId,QueueId,SenderId,OrderByTimestampInsteadPriority
	@Override
	public Message peekQueueWithSender(String argsConcat) {
		String[] args = argsConcat.split(",", 4);

		long receiver = Long.parseLong(args[0]);
		long queue = Long.parseLong(args[1]);
		long sender = Long.parseLong(args[2]);
		Boolean getByTimestampInsteadOfPriority = Integer.parseInt(args[3]) == 1;

		// TODO: Differentiate on getByTimestampInsteadOfPriority
		Message m = null;
		try {
			m = persistence.getMessageBySender(queue, receiver, sender);
		} catch (InvalidClientException e) {
			e.printStackTrace();
		} catch (InvalidQueueException e) {
			e.printStackTrace();
		} catch (PersistenceException e) {
			e.printStackTrace();
		}

		GlobalCounters.numberOfMessagesReturned.incrementAndGet();
		return m;
	}

	// POPQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	@Override
	public Message popQueue(String argsConcat) {
		Message m = peekQueue(argsConcat);

		if (m != null) {
			try {
				persistence.deleteMessage(m.id);
			} catch (InvalidMessageException e) {
				e.printStackTrace();
			} catch (PersistenceException e) {
				e.printStackTrace();
			}
		}

		GlobalCounters.numberOfMessagesReturned.incrementAndGet();
		return m;
	}

	// POPQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	@Override
	public Message popQueueWithSender(String argsConcat) {
		Message m = peekQueueWithSender(argsConcat);

		if (m != null) {
			try {
				persistence.deleteMessage(m.id);
			} catch (InvalidMessageException e) {
				e.printStackTrace();
			} catch (PersistenceException e) {
				e.printStackTrace();
			}
		}

		GlobalCounters.numberOfMessagesReturned.incrementAndGet();
		return m;
	}

	// CREATEQUEUE,NameOfQueue
	@Override
	public long createQueue(String name) {
		// TODO: unspecified that this will return -1.
		long queueId = -1;
		try {
			queueId = persistence.createQueue(name);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return queueId;
	}

	// REMOVEQUEUE,QueueId
	@Override
	public void removeQueue(long id) {
		try {
			persistence.removeQueue(id);
		} catch (InvalidQueueException e) {
			e.printStackTrace();
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}
	
	// CREATECLIENT,NameOfClient
	@Override
	public long createClient(String name) {
		// TODO: Unspecified that this could return -1.
		long clientId = -1;
		try {
			clientId = persistence.createClient(name);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return clientId;
	}
	
	@Override
	public void sendMessage(Message m) {
		if (m == null) {
			transport.Send("MSG0");
		}
		else {
			transport.Send(formatMessage(m));
		}
	}
	
	@Override
	public String formatMessage(Message m) {
		return String.format(SendMessageStringFormat, m.senderId, m.contextId,
				m.id, m.content);
	}
}
