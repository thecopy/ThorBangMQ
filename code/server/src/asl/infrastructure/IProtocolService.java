package asl.infrastructure;

import asl.Message;
import asl.infrastructure.exceptions.InvalidClientException;
import asl.infrastructure.exceptions.InvalidMessageException;
import asl.infrastructure.exceptions.InvalidQueueException;
import asl.infrastructure.exceptions.PersistenceException;

public interface IProtocolService {
	void storeMessage(long reciever, long sender, long queue, int priority, long context, String content) throws PersistenceException, InvalidQueueException, InvalidClientException;
	Message peekQueue(long reciever, long queue, boolean getByTimestampInsteadOfPriority) throws InvalidQueueException, PersistenceException, InvalidMessageException;
	Message peekQueueWithSender(long reciever, long queue, long sender) throws InvalidClientException, InvalidQueueException, PersistenceException;
	long createQueue(String name) throws PersistenceException;
	void removeQueue(long id) throws InvalidQueueException, PersistenceException;
	long createClient(String name) throws PersistenceException;
	void removeClient(long id) throws PersistenceException, InvalidClientException;
	void deleteMessage(long id) throws InvalidMessageException, PersistenceException;
}