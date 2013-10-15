package asl.infrastructure;

import asl.Message;

public interface IProtocolService {
	void storeMessage(String argsConcat);
	Message peekQueue(String argsConcat);
	Message peekQueueWithSender(String argsConcat);
	Message popQueue(String argsConcat);
	Message popQueueWithSender(String argsConcat);
	long createQueue(String name);
	void removeQueue(long id);
	void sendMessage(Message m);
	String formatMessage(Message m);
}