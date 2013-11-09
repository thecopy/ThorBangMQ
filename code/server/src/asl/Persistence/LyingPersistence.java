package asl.Persistence;

import java.util.Enumeration;

import asl.Client;
import asl.Message;
import asl.infrastructure.exceptions.InvalidClientException;
import asl.infrastructure.exceptions.InvalidMessageException;
import asl.infrastructure.exceptions.InvalidQueueException;
import asl.infrastructure.exceptions.PersistenceException;

public class LyingPersistence implements IPersistence{

	@Override
	public void deleteMessage(long messageId) throws InvalidMessageException,
			PersistenceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long storeMessage(long senderId, long receiverId, long queueId, long contextId,
			int priority, String content) throws PersistenceException,
			InvalidQueueException, InvalidClientException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Message getMessageByPriority(long queueId, long recieverId)
			throws InvalidQueueException, PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message getMessageByTimestamp(long queueId, long recieverId)
			throws InvalidQueueException, PersistenceException,
			InvalidMessageException {
		// TODO Auto-generated method stub
		return new Message();
	}

	@Override
	public Message getMessageBySender(long queueId, long recierId, long senderId, boolean b)
			throws InvalidClientException, InvalidQueueException,
			PersistenceException {
		// TODO Auto-generated method stub
		return new Message();
		}

	@Override
	public Message getMessageById(long id) throws InvalidMessageException,
			PersistenceException {
		// TODO Auto-generated method stub
		return new Message();
		}

	@Override
	public long createQueue(String name) throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void removeQueue(long queueId) throws InvalidQueueException,
			PersistenceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long createClient(String name) throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void removeClient(long clientId) throws PersistenceException,
			InvalidClientException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Enumeration<Client> getAllClients() {
		// TODO Auto-generated method stub
		return null;
	}

}
