package asl;

<<<<<<< HEAD
import java.nio.channels.SelectionKey;

public class ClientRequestWorker implements Runnable{

	public ClientRequestWorker(String message, SelectionKey conn) {

=======
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
>>>>>>> c1805e09c4269f3d3ffd6ad9e09197b783e595dc
	}
	
	@Override
	public void run() {
<<<<<<< HEAD
		
=======
		logger.log(Level.INFO, "ClientRequestWorker RUN."
				+ "\n Socket: " + clientSocket
				+ "\n IPersistence: " + persistence);
>>>>>>> c1805e09c4269f3d3ffd6ad9e09197b783e595dc
	}

}
