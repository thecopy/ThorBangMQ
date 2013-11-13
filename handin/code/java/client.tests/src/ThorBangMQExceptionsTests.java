import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import asl.ThorBangMQ;
import asl.network.SocketTransport;

@RunWith(JUnit4.class)
public class ThorBangMQExceptionsTests {

    @Test
    public void sendMessageShouldThrowInvalidClientException() throws UnknownHostException, IOException {
    	// Arrange
    	ThorBangMQ client = ThorBangMQ.build("localhost", 8123, 0);
    	// Act
    	
    	// Assert
    }
}