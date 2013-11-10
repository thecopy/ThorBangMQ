package server.tests;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.Test;
import org.postgresql.jdbc2.optional.PoolingDataSource;

import asl.ServerSettings;
import asl.Message;
import asl.Persistence.PostgresPersistence;
import asl.infrastructure.exceptions.InvalidClientException;
import asl.infrastructure.exceptions.InvalidMessageException;
import asl.infrastructure.exceptions.InvalidQueueException;
import asl.infrastructure.exceptions.PersistenceException;

public class PostgresPersistenceTests {

	private ServerSettings getSettings(){
		ServerSettings settings = new ServerSettings();
		settings.DB_DATABASE_NAME = "asl";
		settings.DB_DATA_SOURCE_NAME = "asl";
		settings.DB_PASSWORD = "asl2013";
		settings.DB_SERVER_NAME = "localhost";
		settings.DB_USERNAME = "asl";
		
		return settings;
	}
	
	private static PoolingDataSource connectionPool = null;
	
	private PostgresPersistence getTestablePersistence() throws PersistenceException{
		ServerSettings settings = getSettings();
		
		if(connectionPool == null)
		{
			connectionPool = new PoolingDataSource();
			
			connectionPool.setDatabaseName(settings.DB_DATABASE_NAME);
			connectionPool.setDataSourceName(settings.DB_DATA_SOURCE_NAME);
			connectionPool.setUser(settings.DB_USERNAME);
			connectionPool.setServerName(settings.DB_SERVER_NAME);
			connectionPool.setPassword(settings.DB_PASSWORD);
			connectionPool.setMaxConnections(settings.DB_MAX_CONNECTIONS);
			
		}
		
		PostgresPersistence db = new PostgresPersistence(connectionPool, Logger.getAnonymousLogger());
		
		db.deleteSchema();
		db.createSchema();
		db.buildSchema();
		
		db.createQueue("some_queue");
		
		db.createClient("user1");
		db.createClient("user2");
		
		return db;
	}
	
	@Test
	public void shouldBeAbleToPersistMessage() throws PersistenceException, InvalidQueueException, InvalidClientException, InvalidMessageException {
		// Arrange
		PostgresPersistence persistence = getTestablePersistence();
		
		long sender = 1;
		long receiver = 2;
		long queue = 1;
		long context = 4;
		String content = "content";
		int priority = 1;
		Message messageToStore = new Message(receiver, sender, 0, queue, 0, priority, context, content);
		
		// Act
		long id = persistence.storeMessage(sender, receiver, queue, context, priority, content);
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
	
	@Test
	public void shouldBeAbleToPeekQueueByPriority() throws PersistenceException, InvalidQueueException, InvalidClientException {
		// Arrange
		PostgresPersistence persistence = getTestablePersistence();
		
		long sender = 1;
		long receiver = 2;
		long queue = 1;
		long context = 4;
		String content = "content";
		int priority = 2;
		persistence.storeMessage(sender, receiver, queue, context, priority-1, content);
		persistence.storeMessage(sender, receiver, queue, context, priority, content);
		
		// Act
		Message loaded = persistence.getMessageByPriority(queue, receiver);
		
		// Assert
		assertNotNull(loaded);
		assertEquals(content, loaded.content);
		assertEquals(context, loaded.contextId);
		assertEquals(sender, loaded.senderId);
		assertEquals(receiver, loaded.receiverId);
		assertEquals(queue, loaded.queueId);
		assertEquals(priority, loaded.priority);
	}
	
	@Test
	public void shouldBeAbleToPeekQueueByTimestamp() throws PersistenceException, InvalidQueueException, InvalidClientException, InvalidMessageException {
		// Arrange
		PostgresPersistence persistence = getTestablePersistence();
		
		long sender = 1;
		long receiver = 2;
		long queue = 1;
		long context = 4;
		String content = "content";
		int priority = 1;
		long id = persistence.storeMessage(sender, receiver, queue, context, priority, content);
		persistence.storeMessage(sender, receiver, queue, context, priority+1, content);
		
		// Act
		Message loaded = persistence.getMessageByTimestamp(queue, receiver);
		
		// Assert
		assertNotNull(loaded);
		assertEquals(id, loaded.id);
		assertEquals(content, loaded.content);
		assertEquals(context, loaded.contextId);
		assertEquals(sender, loaded.senderId);
		assertEquals(receiver, loaded.receiverId);
		assertEquals(queue, loaded.queueId);
		assertEquals(priority, loaded.priority);
	}

	
	@Test
	public void shouldBeAbleToPeekQueueBySender() throws PersistenceException, InvalidQueueException, InvalidClientException {
		// Arrange
		PostgresPersistence persistence = getTestablePersistence();
		
		long sender = 1;
		long receiver = 2;
		long queue = 1;
		long context = 4;
		String content = "content";
		int priority = 1;
		persistence.storeMessage(sender, receiver, queue, context, priority, content);
		long id = persistence.storeMessage(sender+1, receiver, queue, context, priority, content);
		persistence.storeMessage(sender, receiver, queue, context, priority+1, content);
		
		// Act
		Message loaded = persistence.getMessageBySender(queue, receiver, sender+1, false);
		
		// Assert
		assertNotNull(loaded);
		assertEquals(id, loaded.id);
		assertEquals(content, loaded.content);
		assertEquals(context, loaded.contextId);
		assertEquals(sender+1, loaded.senderId);
		assertEquals(receiver, loaded.receiverId);
		assertEquals(queue, loaded.queueId);
		assertEquals(priority, loaded.priority);
	}

	@Test
	public void shouldBeAbleToRemoveMessage() throws PersistenceException, InvalidQueueException, InvalidClientException, InvalidMessageException {
		// Arrange
		PostgresPersistence persistence = getTestablePersistence();
		
		long sender = 1;
		long receiver = 2;
		long queue = 1;
		long context = 4;
		String content = "content";
		int priority = 1;
		long id = persistence.storeMessage(sender, receiver, queue, context, priority, content);
		
		// Act
		persistence.deleteMessage(id);
		Message loaded = persistence.getMessageById(id);
		
		// Assert
		assertNull(loaded);
	}

	@Test(expected = Exception.class)
	public void shouldBeAbleToRemoveQueue() throws PersistenceException, InvalidQueueException, InvalidClientException {
		// Arrange
		PostgresPersistence persistence = getTestablePersistence();
		
		// Act
		persistence.removeQueue(1); // this is created in getTestablePersistence()
		
		// Assert
		persistence.storeMessage(1,1,1,1,1,"a"); // just remove queue id = 1
	}
}
