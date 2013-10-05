package asl;

import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import asl.Persistence.IPersistence;

public class ClientRequestWorker implements Runnable {
	private Logger logger;
	private SocketChannel clientSocket;
	private IPersistence persistence;	
	
	public ClientRequestWorker(Logger logger, SocketChannel clientSocket, IPersistence persistence){
		this.logger = logger;
		this.clientSocket = clientSocket;
		this.persistence = persistence;
	}
	
	@Override
	public void run() {
		logger.log(Level.INFO, "ClientRequestWorker RUN."
				+ "\n Socket: " + clientSocket
				+ "\n IPersistence: " + persistence);
	}

}
