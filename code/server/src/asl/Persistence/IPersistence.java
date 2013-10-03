package asl.Persistence;

import java.util.Enumeration;

import asl.Client;
import asl.Message;

public interface IPersistence {
	void deleteMessage(long messageId);
	void storeMessage(Message message);
	
	Message getMessageByPriority(long queueId, long recieverId);
	Message getMessageByTimestamp(long queueId, long recieverId);
	Message getMessageBySender(long queueId, long recierId, long senderId);
	
	long createQueue(String name);
	void removeQueue(long queueId);
	
	Enumeration<Client> getAllClients();
}
