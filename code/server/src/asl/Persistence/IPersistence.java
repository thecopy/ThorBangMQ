package asl.Persistence;

import java.util.Enumeration;

import asl.Client;
import asl.Message;
import asl.infrastructure.exceptions.InvalidClientException;
import asl.infrastructure.exceptions.InvalidMessageException;
import asl.infrastructure.exceptions.InvalidQueueException;
import asl.infrastructure.exceptions.PersistenceException;

public interface IPersistence {
	void deleteMessage(long messageId) throws InvalidMessageException, PersistenceException;
	long storeMessage(Message message) throws PersistenceException, InvalidQueueException, InvalidClientException;

	Message getMessageByPriority(long queueId, long recieverId) throws InvalidQueueException, PersistenceException;
	Message getMessageByTimestamp(long queueId, long recieverId) throws InvalidQueueException, PersistenceException, InvalidMessageException;
	Message getMessageBySender(long queueId, long recierId, long senderId) throws InvalidClientException, InvalidQueueException, PersistenceException;
	Message getMessageById(long id) throws InvalidMessageException, PersistenceException;

    /**
     * Create a queue.
     * @param name Name of the queue
     * @return Id of queue on success, -1 on failure.
     */
	long createQueue(String name) throws PersistenceException;
	void removeQueue(long queueId) throws InvalidQueueException, PersistenceException;

	long createClient(String name) throws PersistenceException;
	void removeClient(long clientId) throws PersistenceException, InvalidClientException;

	Enumeration<Client> getAllClients();
}
