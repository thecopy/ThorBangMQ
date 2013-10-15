package asl.infrastructure;

import java.util.logging.Logger;

import asl.Message;
import asl.Persistence.IPersistence;
import asl.network.ITransport;

public class ProtocolService implements IProtocolService {
	private static String SendMessageStringFormat = "MSG,%d,%s,%d,%s";
	
	private IPersistence persistence;
	private Logger logger;
	private ITransport transport;
	
	public ProtocolService(IPersistence persistence, ITransport transport, Logger logger){
		this.persistence = persistence;
		this.logger = logger;
		this.transport = transport;
	}

	// MSG,ReceiverId,SenderId,QueueId,Priority,Context,Conten
	@Override
	public void storeMessage(String argsConcat) {
		String[] args = argsConcat.split(",", 6);

		long receiver = Long.parseLong(args[0]);
		long sender = Long.parseLong(args[1]);
		long queue = Long.parseLong(args[2]);
		int priority = Integer.parseInt(args[3]);
		long context = Long.parseLong(args[4]);
		String content = args[5];

		Message message = new Message(receiver, sender, 0, queue, 0, priority,
				context, content);

		persistence.storeMessage(message);
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
			m = persistence.getMessageByTimestamp(queue, receiver);
		else
			m = persistence.getMessageByPriority(queue, receiver);

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
		m = persistence.getMessageBySender(queue, receiver, sender);

		return m;
	}

	// POPQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	@Override
	public Message popQueue(String argsConcat) {
		Message m = peekQueue(argsConcat);

		if (m != null)
			persistence.deleteMessage(m.id);

		return m;
	}

	// POPQ,ReceiverId,QueueId,OrderByTimestampInsteadPriority
	@Override
	public Message popQueueWithSender(String argsConcat) {
		Message m = peekQueueWithSender(argsConcat);

		if (m != null)
			persistence.deleteMessage(m.id);

		return m;
	}

	// CREATEQUEUE,NameOfQueue
	@Override
	public long createQueue(String name) {
		return persistence.createQueue(name);
	}

	// REMOVEQUEUE,QueueId
	@Override
	public void removeQueue(long id) {
		persistence.removeQueue(id);
	}
	
	
	@Override
	public void sendMessage(Message m) {
		if (m == null)
			transport.Send("MSG0");
		else
			transport.Send(formatMessage(m));
	}
	
	@Override
	public String formatMessage(Message m) {
		return String.format(SendMessageStringFormat, m.senderId, m.content,
				m.id, m.content);
	}
}
