package asl.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketTransport implements ITransport {
	private Socket socket;
	
	InputStream inputStream;
	OutputStream outputStream; 
	
	public SocketTransport(Socket socket) throws IOException{
		this.socket = socket;	
		
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
	}
	
	public void Send(String data) throws IOException{
		outputStream.write(data.getBytes());
	}
	
	public String Read() throws IOException{
		StringBuilder builder = new StringBuilder();
		
		byte[] buffer = new byte[1024];
		while(inputStream.read(buffer) > 0){
			String s = new String(buffer, "ASCII");
			builder.append(s);
		}
		
		return builder.toString();
	}
	
	public String SendAndGetResponse(String data) throws IOException{
		Send(data);
		return Read();
	}

	public Socket getSocket() {
		return socket;
	}
}
