import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import asl.network.SocketTransport;

@RunWith(JUnit4.class)
public class SocketTransportTests {

    @Test
    public void ShouldBeAbleToSendAndRecieveData() throws UnknownHostException, IOException {
    	// Arrange
    	Socket s = new Socket("maddox.xmission.com", 80);
    	SocketTransport transport = new SocketTransport(s); 
    	String stringToSend = "GET / HTTP/1.0\r\nHost: maddox.xmission.com\r\n\r\n";
    	
    	// Act
    	String result = transport.SendAndGetResponse(stringToSend);
    	
    	// Assert
    	Assert.assertTrue(result.contains("200 OK"));
    }
}