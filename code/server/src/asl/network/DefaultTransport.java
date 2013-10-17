package asl.network;

import java.nio.channels.SelectionKey;

import asl.ThorBangMQServer;

public class DefaultTransport implements ITransport {
	private ThorBangMQServer server;
	private SelectionKey conn;

	public DefaultTransport(ThorBangMQServer server, SelectionKey conn){
		this.server = server;
		this.conn = conn;
		
	}

	@Override
	public void Send(String str) {
		server.send(conn, str + "\0");
	}
}
