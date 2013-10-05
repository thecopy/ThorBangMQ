package asl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;

import asl.ASLServerSettings;

/*
 * With inspiration from http://www.onjava.com/pub/a/onjava/2002/09/04/nio.html?page=2
 * and especially http://rox-xmlrpc.sourceforge.net/niotut/
 */
public class SocketServer {
	private Selector selector = null;
	private ServerSocketChannel serverChannel = null;
	private HashMap pendingData = new HashMap();
	
	// Allocate 4 MB for incoming text.
	// I Think it might be faster to allocate this once, instead of doing so before each message read.
	private ByteBuffer readBuffer = ByteBuffer.allocate(4096);
	
	public SocketServer() throws IOException {
		this.selector = Selector.open();
	}
	
	public void start() throws IOException {
		this.serverChannel = ServerSocketChannel.open();
		this.serverChannel.configureBlocking(false);
		this.serverChannel.socket().bind(new InetSocketAddress(ASLServerSettings.SOCKET_PORT));  // listen for connections on SOCKET_PORT
		
		Selector selector = Selector.open();
		this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		while (true) {
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			
			SelectionKey conn = null;
			while (it.hasNext()) {
				conn = it.next();
				it.remove();
								
				if (!conn.isValid()) {
					continue;
				}
				
				if (conn.isAcceptable()) {
					this.accept(conn);
				}
				else if (conn.isReadable()) {
					this.read(conn);
				}
				else if (conn.isWritable()) {
					this.write(conn);
				}
			}
		}
	}
	
	private void accept(SelectionKey conn) throws IOException {
		// Only the server channel is listening for connections, so it'll be the channel of conn here.
		ServerSocketChannel serverChannel = (ServerSocketChannel)conn.channel();
		
		SocketChannel client = serverChannel.accept();
		client.configureBlocking(false);
		client.register(this.selector, SelectionKey.OP_READ);
	}

	private void read(SelectionKey conn) throws IOException {
		this.readBuffer.clear();
		SocketChannel clientChannel = (SocketChannel)conn.channel();
		
		int bytesRead = 0; 
		try {
			bytesRead = clientChannel.read(this.readBuffer);
		}
		catch (IOException e) {
			// Connection abruptly closed.
			conn.cancel();
			conn.channel().close();
			return;
		}

		if (bytesRead == -1) {
			// End of stream reached. Close connection.
			conn.channel().close();
			conn.cancel();
			return;
		}
//		pass off interpretation of text to worker thread
	}
	
	private void write(SelectionKey conn) {
		// SocketChannel to write reply to.
		SocketChannel channel = (SocketChannel)conn.channel();
		// Use SocketChannel to fetch pending messages for client
		// write pending messages to client
	}
}
