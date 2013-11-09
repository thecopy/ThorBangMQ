package benchmarker;

import asl.Message;
import asl.infrastructure.IProtocolService;
import asl.infrastructure.exceptions.InvalidClientException;
import asl.infrastructure.exceptions.InvalidMessageException;
import asl.infrastructure.exceptions.InvalidQueueException;
import asl.infrastructure.exceptions.PersistenceException;

public class FakeProtocolService implements IProtocolService {

	@Override
	public void storeMessage(long reciever, long sender, long queue,
			int priority, long context, String content)
			throws PersistenceException, InvalidQueueException,
			InvalidClientException {
		// TODO Auto-generated method stub

	}

	@Override
	public Message peekQueue(long reciever, long queue,
			boolean getByTimestampInsteadOfPriority)
			throws InvalidQueueException, PersistenceException,
			InvalidMessageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message peekQueueWithSender(long reciever, long queue, long sender)
			throws InvalidClientException, InvalidQueueException,
			PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long createQueue(String name) throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void removeQueue(long id) throws InvalidQueueException,
			PersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public long createClient(String name) throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void removeClient(long id) throws PersistenceException,
			InvalidClientException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteMessage(long id) throws InvalidMessageException,
			PersistenceException {
		// TODO Auto-generated method stub

	}

}
