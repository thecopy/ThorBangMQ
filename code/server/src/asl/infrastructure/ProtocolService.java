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
	public void storeMessage(long reciever, long sender, long queue, int priority, long context, String content) throws PersistenceException, InvalidQueueException, InvalidClientException {

		Message message = new Message(reciever, sender, 0L, queue, 0L, priority, context, content);

		persistence.storeMessage(message);
		
		GlobalCounters.numberOfMessagesPersisted.incrementAndGet();
	}

	// PEEKQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	@Override
	public Message peekQueue(long reciever, long queue, boolean getByTimestampInsteadOfPriority) throws InvalidQueueException, PersistenceException, InvalidMessageException {
	
		Message m;
			if (getByTimestampInsteadOfPriority) {
				m = persistence.getMessageByTimestamp(queue, reciever);
			} else {
				m = persistence.getMessageByPriority(queue, reciever);
			}
		
		
		GlobalCounters.numberOfMessagesReturned.incrementAndGet();
		return m;
	}

	// PEEKS,ReceiverId,QueueId,SenderId,OrderByTimestampInsteadPriority
	@Override
	public Message peekQueueWithSender(long reciever, long queue, long sender) throws InvalidClientException, InvalidQueueException, PersistenceException {
		
		Message m = persistence.getMessageBySender(queue, reciever, sender);

		GlobalCounters.numberOfMessagesReturned.incrementAndGet();
		return m;	
	}

	// CREATEQUEUE,NameOfQueue
	@Override
	public long createQueue(String name) throws PersistenceException {
		// TODO: unspecified that this will return -1.
		return persistence.createQueue(name);
	}

	// REMOVEQUEUE,QueueId
	@Override
	public void removeQueue(long id) throws InvalidQueueException, PersistenceException {
		persistence.removeQueue(id);
	}
	
	// CREATECLIENT,NameOfClient
	@Override
	public long createClient(String name) throws PersistenceException {
		// TODO: Unspecified that this could return -1.
		return persistence.createClient(name);
	}
	
	//REMOVECLIENT,id
	public void removeClient(long clientId) throws PersistenceException, InvalidClientException {
		persistence.removeClient(clientId);
	}

	@Override
	public void deleteMessage(long id) throws InvalidMessageException, PersistenceException {
persistence.deleteMessage(id);		
	}
}
