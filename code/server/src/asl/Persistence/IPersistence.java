package asl.Persistence;

import java.sql.SQLException;
import java.util.Enumeration;

import asl.Client;
import asl.Message;

public interface IPersistence {
	void deleteMessage(long messageId);
	long storeMessage(Message message);

	Message getMessageByPriority(long queueId, long recieverId);
	Message getMessageByTimestamp(long queueId, long recieverId);
	Message getMessageBySender(long queueId, long recierId, long senderId);
	Message getMessageById(long id);

    /**
     * Create a queue.
     * @param name Name of the queue
     * @return Id of queue on success, -1 on failure.
     */
	long createQueue(String name);
	void removeQueue(long queueId);
	
	long createUser(String name);

	Enumeration<Client> getAllClients();
}
