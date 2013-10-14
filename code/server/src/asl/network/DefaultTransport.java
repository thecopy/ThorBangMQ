package asl.network;

import java.nio.channels.SelectionKey;

import asl.ASLSocketServer;

public class DefaultTransport implements ITransport {
	private ASLSocketServer server;
	private SelectionKey conn;

	public DefaultTransport(ASLSocketServer server, SelectionKey conn){
		this.server = server;
		this.conn = conn;
		
	}

	@Override
	public void Send(String str) {
		server.send(conn, str + "\0");
	}
}
