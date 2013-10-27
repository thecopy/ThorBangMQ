package clientTests;

import java.io.IOException;

public abstract class ClientTest {
	String hostName;
	int port;
	int numClients;
	int numMessagesPerClient;
	
	public ClientTest(String hostName, int port, int numClients, int numMessagesPerClient) {
		this.hostName = hostName;
		this.port = port;
		this.numClients = numClients;
		this.numMessagesPerClient = numMessagesPerClient;
	}
	
	public void start() throws IOException {
		
	}
	
	public void prepare() throws IOException {
		
	}
	
	public void cleanUp() throws IOException {
		
	}
}
