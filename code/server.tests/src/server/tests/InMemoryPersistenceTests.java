package server.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import asl.Message;
import asl.Persistence.InMemoryPersistence;

public class InMemoryPersistenceTests {

	@Test
	public void shouldReturnTheEarliestMessage() {
		
		// Arrange
		ArrayList<Message> messages = new ArrayList<Message>();
		long queueId = 3;
		long myId = 2;
		messages.add(new Message(myId,2,1000,queueId,-1,1, 0, "hej"));
		messages.add(new Message(myId,3,800,queueId,-1,1, 0, "hej2"));
		messages.add(new Message(myId,4,900,queueId,-1,1, 0, "hej3"));
		InMemoryPersistence persistence = new InMemoryPersistence(messages);
		
		// Act
		Message message = persistence.getMessageByTimestamp(queueId, myId);
		
		// Assert
		assertEquals(messages.get(1), message);
	}
	
	@Test
	public void shouldReturnTheMostUrgentMessage() {
		
		// Arrange
		ArrayList<Message> messages = new ArrayList<Message>();
		long queueId = 3;
		long myId = 2;
		// Note priority 5 on line below
		messages.add(new Message(myId,2,1000,queueId,-1,/*priority: */5, 0, "hej"));
		messages.add(new Message(myId,3,800,queueId,-1,1, 0, "hej2"));
		messages.add(new Message(myId,4,900,queueId,-1,1, 0, "hej3"));
		InMemoryPersistence persistence = new InMemoryPersistence(messages);
		
		// Act
		Message message = persistence.getMessageByPriority(queueId, myId);
		
		// Assert
		assertEquals(messages.get(0), message);
	}

	
	@Test
	public void shouldReturnSpecificSendersMessage() {
		
		// Arrange
		ArrayList<Message> messages = new ArrayList<Message>();
		long queueId = 3;
		long myId = 2;
		long senderId = 10;
		messages.add(new Message(myId,2,1000,queueId,-1,5, 0, "hej")); // highest priority
		messages.add(new Message(myId,3,800,queueId,-1,1, 0, "hej2"));			  // earliest
		messages.add(new Message(myId,senderId,900,queueId,-1,1, 0, "hej3"));	  // specific sender
		InMemoryPersistence persistence = new InMemoryPersistence(messages);
		
		// Act
		Message message = persistence.getMessageBySender(queueId, myId, senderId);
		
		// Assert
		assertEquals(messages.get(2), message);
	}
	

	@Test
	public void shouldNotReturnAMessageFromAnEmptyQueue() {
		
		// Arrange
		ArrayList<Message> messages = new ArrayList<Message>();
		long queueId = 3;
		long myId = 2;
		long senderId = 10;
		messages.add(new Message(myId,2,1000,queueId,-1,5, 0, "hej")); // highest priority
		messages.add(new Message(myId,3,800,queueId,-1,1, 0, "hej2"));			  // earliest
		messages.add(new Message(myId,senderId,900,queueId,-1,1, 0, "hej3"));	  // specific sender
		InMemoryPersistence persistence = new InMemoryPersistence(messages);
		 
		// Act
		Message message = persistence.getMessageBySender(1+queueId, myId, senderId);
		Message message1 = persistence.getMessageByPriority(1+queueId, myId);
		Message message2 = persistence.getMessageByTimestamp(1+queueId, myId);
		
		// Assert
		assertNull(message);
		assertNull(message1);
		assertNull(message2);
	}
	

	@Test
	public void shouldStoreMessage() {
		
		// Arrange
		InMemoryPersistence persistence = new InMemoryPersistence();
		Message messageToStore = new Message(1,2,3,4,-1,6, 0, "content");
		
		// Act
		persistence.storeMessage(messageToStore);
		Message gottenMessage = persistence.getMessageById(messageToStore.id);
		
		// Assert
		assertNotEquals(-1, messageToStore.id);
		assertEquals(messageToStore, gottenMessage);
	}

}