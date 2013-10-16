package server.tests;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.Test;

import asl.ASLServerSettings;
import asl.Message;
import asl.Persistence.DbPersistence;

public class DbPersistenceTests {

	private ASLServerSettings getSettings(){
		ASLServerSettings settings = new ASLServerSettings();
		settings.DB_DATABASE_NAME = "asl-test";
		settings.DB_DATA_SOURCE_NAME = "asl";
		settings.DB_PASSWORD = "";
		settings.DB_SERVER_NAME = "localhost";
		settings.DB_USERNAME = "asl";
		
		return settings;
	}
	
	private DbPersistence getTestablePersistence(){
		ASLServerSettings settings = getSettings();
		DbPersistence db = new DbPersistence(settings, Logger.getAnonymousLogger());
		
		db.deleteSchema();
		db.createSchema();
		db.buildSchema();
		
		db.createQueue("some_queue");
		
		db.createUser("user1");
		db.createUser("user2");
		
		return db;
	}
	
	@Test
	public void shouldBeAbleToPersistMessage() {
		// Arrange
		DbPersistence persistence = getTestablePersistence();
		
		long sender = 1;
		long receiver = 2;
		long queue = 1;
		long context = 4;
		String content = "content";
		int priority = 1;
		Message messageToStore = new Message(receiver, sender, 0, queue, 0, priority, context, content);
		
		// Act
		long id = persistence.storeMessage(messageToStore);
		Message messageLoaded = persistence.getMessageById(id);
		
		// Assert
		assertNotNull(messageLoaded);
		assertEquals(messageToStore.content, messageLoaded.content);
		assertEquals(messageToStore.contextId, messageLoaded.contextId);
		assertEquals(messageToStore.senderId, messageLoaded.senderId);
		assertEquals(messageToStore.receiverId, messageLoaded.receiverId);
		assertEquals(messageToStore.queueId, messageLoaded.queueId);
		assertEquals(messageToStore.priority, messageLoaded.priority);
		assertNotEquals(0, messageLoaded.timestamp);
	}
	
	public void shouldBeAbleToPeekQueueByPriority() {
		// Arrange
		DbPersistence persistence = getTestablePersistence();
		
		long sender = 1;
		long receiver = 2;
		long queue = 1;
		long context = 4;
		String content = "content";
		int priority = 1;
		Message first = new Message(receiver, sender, 0, queue, 0, priority, context, content);
		persistence.storeMessage(first);
		Message second = new Message(receiver, sender, 0, queue, 0, priority+1, context, content);
		persistence.storeMessage(second);
		
		// Act
		Message loaded = persistence.getMessageByPriority(queue, receiver);
		
		// Assert
		assertNotNull(loaded);
		assertEquals(second.content, loaded.content);
		assertEquals(second.contextId, loaded.contextId);
		assertEquals(second.senderId, loaded.senderId);
		assertEquals(second.receiverId, loaded.receiverId);
		assertEquals(second.queueId, loaded.queueId);
		assertEquals(second.priority, loaded.priority);
	}
	
	public void shouldBeAbleToPeekQueueByTimestamp() {
		// Arrange
		DbPersistence persistence = getTestablePersistence();
		
		long sender = 1;
		long receiver = 2;
		long queue = 1;
		long context = 4;
		String content = "content";
		int priority = 1;
		Message first = new Message(receiver, sender, 0, queue, 0, priority, context, content);
		long id = persistence.storeMessage(first);
		Message second = new Message(receiver, sender, 0, queue, 0, priority+1, context, content);
		persistence.storeMessage(second);
		
		// Act
		Message loaded = persistence.getMessageByPriority(queue, receiver);
		
		// Assert
		assertNotNull(loaded);
		assertEquals(id, loaded.id);
		assertEquals(first.content, loaded.content);
		assertEquals(first.contextId, loaded.contextId);
		assertEquals(first.senderId, loaded.senderId);
		assertEquals(first.receiverId, loaded.receiverId);
		assertEquals(first.queueId, loaded.queueId);
		assertEquals(first.priority, loaded.priority);
	}

	public void shouldBeAbleToPeekQueueBySender() {
		// Arrange
		DbPersistence persistence = getTestablePersistence();
		
		long sender = 1;
		long receiver = 2;
		long queue = 1;
		long context = 4;
		String content = "content";
		int priority = 1;
		Message first = new Message(receiver, sender, 0, queue, 0, priority, context, content);
			persistence.storeMessage(first);
		Message second = new Message(receiver, sender+1, 0, queue, 0, priority+1, context, content);
			long id = persistence.storeMessage(second);
		Message third = new Message(receiver, sender+2, 0, queue, 0, priority+2, context, content);
			persistence.storeMessage(third);
		
		// Act
		Message loaded = persistence.getMessageBySender(queue, receiver, sender+1);
		
		// Assert
		assertNotNull(loaded);
		assertEquals(id, loaded.id);
		assertEquals(second.content, loaded.content);
		assertEquals(second.contextId, loaded.contextId);
		assertEquals(second.senderId, loaded.senderId);
		assertEquals(second.receiverId, loaded.receiverId);
		assertEquals(second.queueId, loaded.queueId);
		assertEquals(second.priority, loaded.priority);
	}


	public void shouldBeAbleToRemoveMessage() {
		// Arrange
		DbPersistence persistence = getTestablePersistence();
		
		long sender = 1;
		long receiver = 2;
		long queue = 1;
		long context = 4;
		String content = "content";
		int priority = 1;
		Message msg = new Message(receiver, sender, 0, queue, 0, priority, context, content);
		long id = persistence.storeMessage(msg);
		
		// Act
		persistence.deleteMessage(id);
		Message loaded = persistence.getMessageById(id);
		
		// Assert
		assertNull(loaded);
	}
}
