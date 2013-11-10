import infrastructure.exceptions.InvalidClientException;
import infrastructure.exceptions.InvalidQueueException;
import infrastructure.exceptions.ServerException;

import java.io.IOException;

import org.junit.Test;

import asl.Message;
import asl.ThorBangMQ;
import asl.network.ITransport;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class ThorBangMQTests {

	@Test
	public void should_send_message() throws IOException, InvalidQueueException, InvalidClientException, ServerException {
		//Arrange
		ITransport transport = mock(ITransport.class);
		ThorBangMQ mq = new ThorBangMQ(transport,1);
		when(transport.SendAndGetResponse(anyString())).thenReturn("OK");
		//Act
		mq.SendMessage(2,3,4,5, "content 123");
		
		//Assert
		verify(transport).SendAndGetResponse("MSG,2,1,3,4,5,content 123");
	}
	
	@Test
	public void should_send_message_to_many_queues() throws IOException, InvalidQueueException, InvalidClientException, ServerException {
		//Arrange
		ITransport transport = mock(ITransport.class);
		ThorBangMQ mq = new ThorBangMQ(transport,1);
		when(transport.SendAndGetResponse(anyString())).thenReturn("OK");
		//Act
		mq.SendMessage(2,4,5, "content 123",5,6,7);
		
		//Assert
		verify(transport).SendAndGetResponse("MSG,2,1,5;6;7,4,5,content 123");
	}
	
	@Test
	public void should_broadcast_mesage_to_many_queues() throws IOException, InvalidQueueException, InvalidClientException, ServerException {
		//Arrange
		ITransport transport = mock(ITransport.class);
		ThorBangMQ mq = new ThorBangMQ(transport,1);
		when(transport.SendAndGetResponse(anyString())).thenReturn("OK");
		//Act
		mq.BroadcastMessage(4,5, "content 123",5,6,7);
		
		//Assert
		verify(transport).SendAndGetResponse("MSG,-1,1,5;6;7,4,5,content 123");
	}
	
	@Test
	public void should_broadcast_mesage() throws IOException, InvalidQueueException, InvalidClientException, ServerException {
		//Arrange
		ITransport transport = mock(ITransport.class);
		ThorBangMQ mq = new ThorBangMQ(transport,1);
		when(transport.SendAndGetResponse(anyString())).thenReturn("OK");
		//Act
		mq.BroadcastMessage(4,5, "content 123",5);
		
		//Assert
		verify(transport).SendAndGetResponse("MSG,-1,1,5,4,5,content 123");
	}
	
	@Test
	public void should_create_client() throws IOException, InvalidQueueException, InvalidClientException, ServerException {
		//Arrange
		ITransport transport = mock(ITransport.class);
		ThorBangMQ mq = new ThorBangMQ(transport,1);
		when(transport.SendAndGetResponse(anyString())).thenReturn("55");
		//Act
		long id = mq.createClient("name");
		
		//Assert
		verify(transport).SendAndGetResponse("CREATECLIENT,name");
		assertEquals(id, 55);
	}
	
	@Test
	public void should_create_queue() throws IOException, InvalidQueueException, InvalidClientException, ServerException {
		//Arrange
		ITransport transport = mock(ITransport.class);
		ThorBangMQ mq = new ThorBangMQ(transport,1);
		when(transport.SendAndGetResponse(anyString())).thenReturn("55");
		//Act
		long id = mq.createQueue("name");
		
		//Assert
		verify(transport).SendAndGetResponse("CREATEQUEUE,name");
		assertEquals(id, 55);
	}
	
}
