import infrastructure.exceptions.InvalidClientException;
import infrastructure.exceptions.InvalidRequestException;
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
	public void should_send_message() throws Exception, InvalidQueueException, InvalidClientException, ServerException {
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
	public void should_send_message_to_many_queues() throws Exception, InvalidQueueException, InvalidClientException, ServerException {
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
	public void should_broadcast_mesage_to_many_queues() throws Exception, InvalidQueueException, InvalidClientException, ServerException {
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
	public void should_broadcast_mesage() throws Exception, InvalidQueueException, InvalidClientException, ServerException {
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
	public void should_create_client() throws Exception, InvalidQueueException, InvalidClientException, ServerException {
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
	
	@Test
	public void should_pop_msg_by_prio() throws IOException, InvalidQueueException, InvalidClientException, ServerException {
		//Arrange
		ITransport transport = mock(ITransport.class);
		ThorBangMQ mq = new ThorBangMQ(transport,1);
		when(transport.SendAndGetResponse(anyString())).thenReturn("MSG0");
		
		//Act
		Message m = mq.PopMessage(2,false);
		
		//Assert
		verify(transport).SendAndGetResponse("POPQ,1,2,0");
	}
	
	@Test
	public void should_pop_msg_by_time() throws IOException, InvalidQueueException, InvalidClientException, ServerException {
		//Arrange
		ITransport transport = mock(ITransport.class);
		ThorBangMQ mq = new ThorBangMQ(transport,1);
		when(transport.SendAndGetResponse(anyString())).thenReturn("MSG0");
		//Act
		Message m = mq.PopMessage(2,true);
		
		//Assert
		verify(transport).SendAndGetResponse("POPQ,1,2,1");
	}
	
	@Test
	public void should_parse_msg() throws IOException, InvalidQueueException, InvalidClientException, ServerException {
		//Arrange
		ITransport transport = mock(ITransport.class);
		ThorBangMQ mq = new ThorBangMQ(transport,1);
		// MSG,SenderId,Context,MessageId,Content
		when(transport.SendAndGetResponse(anyString())).thenReturn("MSG,1,2,3,4,5");
		//Act
		Message m = mq.PopMessage(2,true);
		
		//Assert
		assertEquals( m.sender, 1);
		assertEquals( m.context, 2 );
		assertEquals( m.id, 3);
		assertEquals( m.content, "4,5");
	}

	@Test(expected = InvalidClientException.class)
	public void should_throw_invalid_client_exception() throws Exception, InvalidQueueException, InvalidClientException, ServerException {
		
		//Arrange
		ITransport transport = mock(ITransport.class);
		ThorBangMQ mq = new ThorBangMQ(transport,1);
		when(transport.SendAndGetResponse(anyString())).thenReturn("FAIL CLIENT 3");
		
		//Act
		mq.SendMessage(1, 2, 3, 4, "");
		
		//Assert
		// The assertion is handled by JUnit, see method attribute: @Test(expected = ...
	}

	@Test(expected = InvalidQueueException.class)
	public void should_throw_invalid_queue_exception() throws Exception, InvalidQueueException, InvalidClientException, ServerException {
		
		//Arrange
		ITransport transport = mock(ITransport.class);
		ThorBangMQ mq = new ThorBangMQ(transport,1);
		when(transport.SendAndGetResponse(anyString())).thenReturn("FAIL QUEUE 3");
		
		//Act
		mq.SendMessage(1, 2, 3, 4, "");
		
		//Assert
		// The assertion is handled by JUnit, see method attribute: @Test(expected = ...
	}

	@Test(expected = ServerException.class)
	public void should_throw_server_exception() throws Exception, InvalidQueueException, InvalidClientException, ServerException {
		
		//Arrange
		ITransport transport = mock(ITransport.class);
		ThorBangMQ mq = new ThorBangMQ(transport,1);
		when(transport.SendAndGetResponse(anyString())).thenReturn("FAIL UNKNOWN");
		
		//Act
		mq.SendMessage(1, 2, 3, 4, "");
		
		//Assert
		// The assertion is handled by JUnit, see method attribute: @Test(expected = ...
	}

	@Test(expected = InvalidRequestException.class)
	public void should_throw_invalid_request_exception() throws Exception, InvalidQueueException, InvalidClientException, ServerException {
		
		//Arrange
		ITransport transport = mock(ITransport.class);
		ThorBangMQ mq = new ThorBangMQ(transport,1);
		when(transport.SendAndGetResponse(anyString())).thenReturn("BAD REQUEST");
		
		//Act
		mq.SendMessage(1, 2, 3, 4, "");
		
		//Assert
		// The assertion is handled by JUnit, see method attribute: @Test(expected = ...
	}
}
