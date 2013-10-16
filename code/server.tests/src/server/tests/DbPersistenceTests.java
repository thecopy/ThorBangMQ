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

}
