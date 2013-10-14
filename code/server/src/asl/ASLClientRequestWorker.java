package asl;

import java.nio.channels.SelectionKey;

import asl.Persistence.PersistenceImpl;

public class ASLClientRequestWorker implements Runnable{

	ASLSocketServer server = null;
	String message = null;
	SelectionKey conn = null;
	
	public ASLClientRequestWorker(ASLSocketServer server, String message, SelectionKey conn) {
		this.server = server;
		this.message = message;
		this.conn = conn;
		System.out.printf("%d, %s\n", message.length(), message);
	}
	
	@Override
	public void run() {
		// Interpret message
		PersistenceImpl persistence = new PersistenceImpl();
		// Use persistence to query the database
		Message response = new Message(0, 0, 0, 0, 0, 0, "0");
		this.server.send(this.conn, response);
	}
}
