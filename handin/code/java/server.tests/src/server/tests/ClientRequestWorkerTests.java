package server.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import asl.ClientRequestWorker;
import asl.Message;
import asl.Persistence.IPersistence;
import asl.infrastructure.exceptions.InvalidClientException;
import asl.infrastructure.exceptions.InvalidMessageException;
import asl.infrastructure.exceptions.InvalidQueueException;
import asl.infrastructure.exceptions.PersistenceException;
import asl.network.ITransport;

public class ClientRequestWorkerTests {
	
	@Test
	public void shoud_store_message() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		long reciever = 2;
		long sender = 3;
		long queue = 4;
		int prio = 5;
		long context = 6;
		String content = "message 123, comma here, comma there";
		//     MSG,ReceiverId,SenderId,QueueId,Priority,Context,Content
		String req = String.format("MSG,%d,%d,%d,%d,%d,%s", 
				reciever, sender, queue, prio, context, content);
		
		// Act
		new ClientRequestWorker(
				new FakeLogger(),
				persistence,
				transport,
				req).run();
		
		// Assert
		verify(persistence, times(1)).storeMessage(
				eq(sender), eq(reciever),
				eq(queue), eq(context),
				eq(prio), eq(content));
		verify(transport, times(1)).Send("OK");
	}
	
	@Test
	public void shoud_peek_and_send_message() throws InvalidQueueException, PersistenceException, InvalidMessageException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		String req = "PEEKQ,3,2,1";
		Message m = new Message(3,2,0,2,1,1,1,"content");
		when(persistence.getMessageByTimestamp(2, 3)).thenReturn(m);
		
		// Act
		new ClientRequestWorker(
				new FakeLogger(),
				persistence,
				transport,
				req).run();
		
		// Assert
		verify(persistence, times(1)).getMessageByTimestamp(2, 3);
		verify(transport, times(1)).Send(ClientRequestWorker.formatMessage(m));
	}
	
	@Test
	public void shoud_peek_and_send_message_from_sender() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		String req = "PEEKS,3,2,1,1";
		Message m = new Message(3,2,0,2,1,1,1,"content");
		when(persistence.getMessageBySender(2, 3, 1, true)).thenReturn(m);
		
		// Act
		new ClientRequestWorker(
				new FakeLogger(),
				persistence,
				transport,
				req).run();
		
		// Assert
		verify(persistence, times(1)).getMessageBySender(2, 3, 1, true);
		verify(transport, times(1)).Send(ClientRequestWorker.formatMessage(m));
	}
	
	@Test
	public void shoud_peek_and_send_message_from_sender_by_priority() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		String req = "PEEKS,3,2,1,0";
		Message m = new Message(3,2,0,2,1,1,1,"content");
		when(persistence.getMessageBySender(2, 3, 1, false)).thenReturn(m);
		
		// Act
		new ClientRequestWorker(
				new FakeLogger(),
				persistence,
				transport,
				req).run();
		
		// Assert
		verify(persistence, times(1)).getMessageBySender(2, 3, 1, false);
		verify(transport, times(1)).Send(ClientRequestWorker.formatMessage(m));
	}
	
	@Test
	public void shoud_peek_and_send_message_by_priority() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		String req = "PEEKQ,3,2,0";
		Message m = new Message(3,2,0,2,1,1,1,"content");
		when(persistence.getMessageByPriority(2, 3)).thenReturn(m);
		
		// Act
		new ClientRequestWorker(
				new FakeLogger(),
				persistence,
				transport,
				req).run();
		
		// Assert
		verify(persistence, times(1)).getMessageByPriority(2, 3);
		verify(transport, times(1)).Send(ClientRequestWorker.formatMessage(m));
	}
	
	@Test
	public void shoud_peek_and_delete_and_send_message() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		String req = "POPQ,3,2,1";
		long msgId = 55;
		Message m = new Message(3,2,0,2,msgId,1,1,"content");
		when(persistence.getMessageByTimestamp(2, 3)).thenReturn(m);
		
		// Act
		new ClientRequestWorker(
				new FakeLogger(),
				persistence,
				transport,
				req).run();
		
		// Assert
		verify(persistence, times(1)).getMessageByTimestamp(2, 3);
		verify(persistence, times(1)).deleteMessage(msgId);
		verify(transport, times(1)).Send(ClientRequestWorker.formatMessage(m));
	}

	
	@Test
	public void shoud_peek_and_delete_and_send_message_by_priority() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		String req = "POPQ,3,2,0";
		long msgId = 55;
		Message m = new Message(3,2,0,2,msgId,1,1,"content");
		when(persistence.getMessageByPriority(2, 3)).thenReturn(m);
		
		// Act
		new ClientRequestWorker(
				new FakeLogger(),
				persistence,
				transport,
				req).run();
		
		// Assert
		verify(persistence, times(1)).getMessageByPriority(2, 3);
		verify(persistence, times(1)).deleteMessage(msgId);
		verify(transport, times(1)).Send(ClientRequestWorker.formatMessage(m));
	}
	
	@Test
	public void shoud_peek_and_delete_and_send_message_by_sender() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		String req = "POPS,3,2,1,1";
		long msgId = 55;
		Message m = new Message(3,2,0,2,msgId,1,1,"content");
		when(persistence.getMessageBySender(2, 3, 1,true)).thenReturn(m);
		
		// Act
		new ClientRequestWorker(
				new FakeLogger(),
				persistence,
				transport,
				req).run();
		
		// Assert
		verify(persistence, times(1)).getMessageBySender(2, 3, 1,true);
		verify(persistence, times(1)).deleteMessage(msgId);
		verify(transport, times(1)).Send(ClientRequestWorker.formatMessage(m));
	}
	
	@Test
	public void shoud_peek_and_delete_and_send_message_by_sender_by_priority() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		String req = "POPS,3,2,1,0";
		long msgId = 55;
		Message m = new Message(3,2,0,2,msgId,1,1,"content");
		when(persistence.getMessageBySender(2, 3, 1,false)).thenReturn(m);
		
		// Act
		new ClientRequestWorker(
				new FakeLogger(),
				persistence,
				transport,
				req).run();
		
		// Assert
		verify(persistence, times(1)).getMessageBySender(2, 3, 1,false);
		verify(persistence, times(1)).deleteMessage(msgId);
		verify(transport, times(1)).Send(ClientRequestWorker.formatMessage(m));
	}

	@Test
	public void shoud_create_queue() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		String req = "CREATEQUEUE,ABC";
		long queueId = 543;
		when(persistence.createQueue(eq("ABC"))).thenReturn(queueId);
		
		// Act
		new ClientRequestWorker(
				new FakeLogger(),
				persistence,
				transport,
				req).run();
		
		// Assert
		verify(persistence, times(1)).createQueue(eq("ABC"));
		verify(transport, times(1)).Send(String.valueOf(queueId));
	}

	@Test
	public void shoud_delete_queue() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		long queueId = 543;
		String req = "REMOVEQUEUE," + queueId;
		
		// Act
		new ClientRequestWorker(
				new FakeLogger(),
				persistence,
				transport,
				req).run();
		
		// Assert
		verify(persistence, times(1)).removeQueue(eq(queueId));
		verify(transport, times(1)).Send("OK");
	}


	@Test
	public void shoud_create_client() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		String req = "CREATECLIENT,ABC";
		long clientId = 543;
		when(persistence.createClient(eq("ABC"))).thenReturn(clientId);
		
		// Act
		new ClientRequestWorker(
				new FakeLogger(),
				persistence,
				transport,
				req).run();
		
		// Assert
		verify(persistence, times(1)).createClient(eq("ABC"));
		verify(transport, times(1)).Send(String.valueOf(clientId));
	}

	@Test
	public void shoud_delete_client() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		long clientId = 543;
		String req = "REMOVECLIENT," + clientId;
		
		// Act
		new ClientRequestWorker(
				new FakeLogger(),
				persistence,
				transport,
				req).run();
		
		// Assert
		verify(persistence, times(1)).removeClient(eq(clientId));
		verify(transport, times(1)).Send("OK");
	}

	@Test
	public void shoud_return_MSG0_when_message_is_null_when_peeking() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		String req = "PEEKQ,3,2,1";
		when(persistence.getMessageByTimestamp(anyLong(), anyLong())).thenReturn(null);

		// Act
		new ClientRequestWorker(new FakeLogger(), persistence, transport, req)
				.run();

		// Assert
		verify(transport, times(1)).Send(eq("MSG0"));
	}

	@Test
	public void shoud_return_MSG0_when_message_is_null_when_popping() throws InvalidQueueException, PersistenceException, InvalidMessageException, InvalidClientException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ITransport transport = mock(ITransport.class);
		String req = "POPQ,3,2,1";
		when(persistence.getMessageByTimestamp(anyLong(), anyLong())).thenReturn(null);

		// Act
		new ClientRequestWorker(new FakeLogger(), persistence, transport, req)
				.run();

		// Assert
		verify(transport, times(1)).Send(eq("MSG0"));
	}
}
