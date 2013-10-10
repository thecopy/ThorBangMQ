package asl.network;

import java.io.IOException;

public interface ITransport {
	void Send(String data) throws IOException;
	String Read() throws IOException;
	String SendAndGetResponse(String data) throws IOException;
}
