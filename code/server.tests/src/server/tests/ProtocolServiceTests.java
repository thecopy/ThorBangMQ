package server.tests;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.Test;

import asl.ClientRequestWorker;
import asl.Message;
import asl.Persistence.IPersistence;
import asl.infrastructure.ProtocolService;
import asl.network.ITransport;

public class ProtocolServiceTests {

	@Test
	public void shouldPeekQueue() {
		// Arrange
		ITransport transport = mock(ITransport.class);
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence, transport);
		String args = "1,2,1"; // ReceiverId,QueueId,OrderByTimestampInsteadPriorit
		Message returnThisMessage = new Message();
		when(persistence.getMessageByTimestamp(2, 1)).thenReturn(returnThisMessage);
		
		// Act
		Message m = ps.peekQueue(args);
		
		// Assert
		verify(persistence).getMessageByTimestamp(2, 1);
		assertEquals(returnThisMessage, m);
	}

	@Test
	public void shouldPeekQueueWithSender() {
		// Arrange
		ITransport transport = mock(ITransport.class);
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence, transport);
		String args = "1,2,3,1"; // ReceiverId,QueueId,SenderId,OrderByTimestampInsteadPriorit
		Message returnThisMessage = new Message();
		when(persistence.getMessageBySender(2, 1, 3)).thenReturn(returnThisMessage);
		
		// Act
		Message m = ps.peekQueueWithSender(args);
		
		// Assert
		verify(persistence).getMessageBySender(2, 1, 3);
		assertEquals(returnThisMessage, m);
	}
	
	@Test
	public void shouldPeekQueueOrderedByPriority() {
		// Arrange
		ITransport transport = mock(ITransport.class);
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence, transport);
		String args = "1,2,0"; // ReceiverId,QueueId,OrderByTimestampInsteadPriority
		Message returnThisMessage = new Message();
		when(persistence.getMessageByPriority(2, 1)).thenReturn(returnThisMessage);
		
		// Act
		Message m = ps.peekQueue(args);
		
		// Assert
		verify(persistence).getMessageByPriority(2, 1);
		assertEquals(returnThisMessage, m);
	}
	
	@Test
	public void shouldRemoveMessageWhenPoppingQueue() {
		// Arrange
		ITransport transport = mock(ITransport.class);
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence, transport);
		String args = "1,2,0"; // ReceiverId,QueueId,OrderByTimestampInsteadPriority
		Message returnThisMessage = new Message();
		when(persistence.getMessageByPriority(2, 1)).thenReturn(returnThisMessage);
		
		// Act
		Message m = ps.popQueue(args);
		
		// Assert
		verify(persistence).deleteMessage(returnThisMessage.id);
		assertEquals(returnThisMessage, m);
	}
	
	@Test
	public void shouldRemoveMessageWhenPoppingQueueWithSender() {
		// Arrange
		ITransport transport = mock(ITransport.class);
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence, transport);
		String args = "1,2,3,0"; // ReceiverId,QueueId,SenderId,OrderByTimestampInsteadPriority
		Message returnThisMessage = new Message();
		when(persistence.getMessageBySender(2, 1,3)).thenReturn(returnThisMessage);
		
		// Act
		Message m = ps.popQueueWithSender(args);
		
		// Assert
		verify(persistence).deleteMessage(returnThisMessage.id);
		assertEquals(returnThisMessage, m);
	}
	
	@Test
	public void shouldRemoveMessageWhenPoppingQueueOrderedByPriority() {
		// Arrange
		ITransport transport = mock(ITransport.class);
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence, transport);
		String args = "1,2,0"; // ReceiverId,QueueId,SenderId,OrderByTimestampInsteadPriority
		Message returnThisMessage = new Message();
		when(persistence.getMessageByPriority(2, 1)).thenReturn(returnThisMessage);
		
		// Act
		Message m = ps.popQueue(args);
		
		// Assert
		verify(persistence).deleteMessage(returnThisMessage.id);
		assertEquals(returnThisMessage, m);
	}
	
	@Test
	public void shouldSendOkAndStoreMessage(){
		// Arrange
		ITransport transport = mock(ITransport.class);
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence, transport);
		when(persistence.createQueue("apa")).thenReturn(123L);
		
		ClientRequestWorker r = new ClientRequestWorker(Logger.getAnonymousLogger(), ps, transport, "MSG,1,1,1,1,0,HEY");
		
		// Act
		r.run();
				
		// Assert
		verify(persistence).storeMessage(any(Message.class));
		verify(transport).Send("OK");
	}
	
	@Test
	public void shouldCreateQueue() {
		// Arrange
		ITransport transport = mock(ITransport.class);
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence, transport);
		String args = "apa"; // QueueName
		when(persistence.createQueue("apa")).thenReturn(123L);
		// Act
		long id = ps.createQueue(args);
		
		// Assert
		verify(persistence).createQueue("apa");
		assertEquals(123, id);
	}
	
	@Test
	public void shouldRemoveQueue() {
		// Arrange
		ITransport transport = mock(ITransport.class);
		IPersistence persistence = mock(IPersistence.class);
		ProtocolService ps = new ProtocolService(persistence, transport);
		// Act
		ps.removeQueue(123);
		
		// Assert
		verify(persistence).removeQueue(123);
	}
}
