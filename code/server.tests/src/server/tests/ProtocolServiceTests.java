package server.tests;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.Test;

import asl.ClientRequestWorker;
import asl.Message;
import asl.Persistence.IPersistence;
import asl.infrastructure.ProtocolService;
import asl.infrastructure.exceptions.InvalidClientException;
import asl.infrastructure.exceptions.InvalidMessageException;
import asl.infrastructure.exceptions.InvalidQueueException;
import asl.infrastructure.exceptions.PersistenceException;
import asl.network.ITransport;

public class ProtocolServiceTests {
	private static String sendMessageStringFormat = "MSG,%d,%d,%d,%s";
	
	@Test
	public void shouldPeekQueue() throws InvalidQueueException, PersistenceException, InvalidMessageException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence);
		String args = "1,2,1"; // ReceiverId,QueueId,OrderByTimestampInsteadPriorit
		Message returnThisMessage = new Message();
		when(persistence.getMessageByTimestamp(2, 1)).thenReturn(returnThisMessage);
		
		// Act
		String m = ps.peekQueue(args);
		
		// Assert
		verify(persistence).getMessageByTimestamp(2, 1);
		assertEquals(this.formatMessage(returnThisMessage), m);
	}

	@Test
	public void shouldPeekQueueWithSender() throws InvalidClientException, InvalidQueueException, PersistenceException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence);
		String args = "1,2,3,1"; // ReceiverId,QueueId,SenderId,OrderByTimestampInsteadPriorit
		Message returnThisMessage = new Message();
		when(persistence.getMessageBySender(2, 1, 3)).thenReturn(returnThisMessage);
		
		// Act
		String m = ps.peekQueueWithSender(args);
		
		// Assert
		verify(persistence).getMessageBySender(2, 1, 3);
		assertEquals(this.formatMessage(returnThisMessage), m);
	}
	
	@Test
	public void shouldPeekQueueOrderedByPriority() throws InvalidQueueException, PersistenceException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence);
		String args = "1,2,0"; // ReceiverId,QueueId,OrderByTimestampInsteadPriority
		Message returnThisMessage = new Message();
		when(persistence.getMessageByPriority(2, 1)).thenReturn(returnThisMessage);
		
		// Act
		String m = ps.peekQueue(args);
		
		// Assert
		verify(persistence).getMessageByPriority(2, 1);
		assertEquals(this.formatMessage(returnThisMessage), m);
	}
	
	@Test
	public void shouldRemoveMessageWhenPoppingQueue() throws InvalidQueueException, PersistenceException, InvalidMessageException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence);
		String args = "1,2,0"; // ReceiverId,QueueId,OrderByTimestampInsteadPriority
		Message returnThisMessage = new Message();
		when(persistence.getMessageByPriority(2, 1)).thenReturn(returnThisMessage);
		
		// Act
		String m = ps.popQueue(args);
		
		// Assert
		verify(persistence).deleteMessage(returnThisMessage.id);
		assertEquals(this.formatMessage(returnThisMessage), m);
	}
	
	@Test
	public void shouldRemoveMessageWhenPoppingQueueWithSender() throws InvalidMessageException, PersistenceException, InvalidClientException, InvalidQueueException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence);
		String args = "1,2,3,0"; // ReceiverId,QueueId,SenderId,OrderByTimestampInsteadPriority
		Message returnThisMessage = new Message();
		when(persistence.getMessageBySender(2, 1,3)).thenReturn(returnThisMessage);
		
		// Act
		String m = ps.popQueueWithSender(args);
		
		// Assert
		verify(persistence).deleteMessage(returnThisMessage.id);
		assertEquals(this.formatMessage(returnThisMessage), m);
	}
	
	@Test
	public void shouldRemoveMessageWhenPoppingQueueOrderedByPriority() throws InvalidQueueException, PersistenceException, InvalidMessageException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence);
		String args = "1,2,0"; // ReceiverId,QueueId,SenderId,OrderByTimestampInsteadPriority
		Message returnThisMessage = new Message();
		when(persistence.getMessageByPriority(2, 1)).thenReturn(returnThisMessage);
		
		// Act
		String m = ps.popQueue(args);
		
		// Assert
		verify(persistence).deleteMessage(returnThisMessage.id);
		assertEquals(this.formatMessage(returnThisMessage), m);
	}
	
	@Test
	public void shouldSendOkAndStoreMessage() throws PersistenceException, InvalidQueueException, InvalidClientException{
		// Arrange
		ITransport transport = mock(ITransport.class);
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence);
		when(persistence.createQueue("apa")).thenReturn(123L);
		
		ClientRequestWorker r = new ClientRequestWorker(Logger.getAnonymousLogger(), ps, transport, "MSG,1,1,1,1,0,HEY");
		
		// Act
		r.run();
				
		// Assert
		verify(persistence).storeMessage(any(Message.class));
		verify(transport).Send("OK");
	}
	
	@Test
	public void shouldCreateQueue() throws PersistenceException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence);
		String args = "apa"; // QueueName
		when(persistence.createQueue("apa")).thenReturn(123L);
		// Act
		String id = ps.createQueue(args);
		
		// Assert
		verify(persistence).createQueue("apa");
		assertEquals(123L, Long.parseLong(id));
	}
	
	@Test
	public void shouldRemoveQueue() throws InvalidQueueException, PersistenceException {
		// Arrange
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence);
		// Act
		ps.removeQueue(123);
		
		// Assert
		verify(persistence).removeQueue(123);
	}
	
	private String formatMessage(Message m) {
		return String.format(sendMessageStringFormat, m.senderId, m.contextId,
				              m.id, m.content);
	}
}
