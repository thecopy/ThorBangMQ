package asl.infrastructure;

public interface IProtocolService {
	String storeMessage(String argsConcat);
	String peekQueue(String argsConcat);
	String peekQueueWithSender(String argsConcat);
	String popQueue(String argsConcat);
	String popQueueWithSender(String argsConcat);
	String createQueue(String name);
	String removeQueue(long id);
	String createClient(String name);
}