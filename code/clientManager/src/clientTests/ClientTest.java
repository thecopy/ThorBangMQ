package clientTests;

import infrastructure.exceptions.InvalidQueueException;
import infrastructure.exceptions.ServerException;

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
	
	public abstract void start() throws IOException;
	
	public abstract void prepare() throws IOException, ServerException;
	
	public abstract void cleanUp() throws IOException, NumberFormatException, ServerException, InvalidQueueException;
}
