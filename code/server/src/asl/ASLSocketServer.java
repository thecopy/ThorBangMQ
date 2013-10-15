package asl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import asl.ASLServerSettings;
import asl.Persistence.IPersistence;
import asl.infrastructure.ProtocolService;
import asl.network.DefaultTransport;
import asl.network.ITransport;

/*
 * With inspiration from http://www.onjava.com/pub/a/onjava/2002/09/04/nio.html?page=2
 * and especially http://rox-xmlrpc.sourceforge.net/niotut/
 */
public class ASLSocketServer {
	private Selector selector;
	private ServerSocketChannel serverChannel;
	private ExecutorService executor;
	private ByteBuffer readBuffer = ByteBuffer.allocate(ASLServerSettings.MESSAGE_MAX_LENGTH);
	private LinkedList<SelectionKey> pendingWriteChannels = new LinkedList<SelectionKey>();
	private HashMap<SelectionKey, String> pendingWrites = new HashMap<SelectionKey, String>();
	private Logger logger;
	private IPersistence persistence;


	public ASLSocketServer(ExecutorService executor, Logger logger, IPersistence persistence) throws IOException {
		this.executor = executor;
		this.logger = logger;
		this.persistence = persistence;

		// Initialize server socket and the selector to accept connections.
		this.serverChannel = ServerSocketChannel.open();
		this.serverChannel.configureBlocking(false);
		this.serverChannel.socket().bind(new InetSocketAddress(ASLServerSettings.SOCKET_PORT));  // listen for connections on SOCKET_PORT
		this.selector = Selector.open();
		this.serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
	}

	public void start() throws IOException {
		System.out.println("Waiting for connections");
		while (true) {
			/*
			 * We're alternating between looking for READ and WRITE operations from client sockets.
			 * In the following, we're using in the pendingWrite 'queue' to set connection's operation to WRITE.
			 */
			synchronized(this.pendingWriteChannels) {
				Iterator<SelectionKey> itsc = pendingWriteChannels.iterator();
				SocketChannel pendingWriteConn;
				while (itsc.hasNext()) {
					pendingWriteConn = (SocketChannel)itsc.next().channel();
					itsc.remove();
					SelectionKey key = pendingWriteConn.keyFor(this.selector);
					if (key.isValid()) {
						key.interestOps(SelectionKey.OP_WRITE);
					}
				}
			}

			// Go through connections that require actions to be made.
			this.selector.select();
			Iterator<SelectionKey> itsk = this.selector.selectedKeys().iterator();
			SelectionKey conn;
			while (itsk.hasNext()) {
				conn = itsk.next();
				itsk.remove();

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

		// Accept connections and put a READ
		SocketChannel client = serverChannel.accept();
		System.out.printf("Accepting connection from %s\n", client.getRemoteAddress().toString());
		client.configureBlocking(false);
		client.register(this.selector, SelectionKey.OP_READ);
	}

	private void read(SelectionKey conn) throws IOException {
		this.readBuffer.clear();
		this.readBuffer.limit(ASLServerSettings.MESSAGE_MAX_LENGTH);
		SocketChannel clientChannel = (SocketChannel)conn.channel();

		int bytesRead = 0;
		try {
			bytesRead = clientChannel.read(this.readBuffer);
		}
		catch (IOException e) {
			// Connection unexpectedly closed.
			conn.cancel();
			conn.channel().close();
			return;
		}

		// End of stream reached. Close connection.
		if (bytesRead == -1) {
			conn.channel().close();
			conn.cancel();
			return;
		}

		ITransport transport = new DefaultTransport(this, conn);
		this.executor.execute(
				new ASLClientRequestWorker(
						logger,
						new ProtocolService(persistence, transport, logger),
						transport,
						bufferToString(bytesRead)));
	}

	private void write(SelectionKey conn) throws IOException {
		// SocketChannel to write reply to.
		SocketChannel channel = (SocketChannel)conn.channel();
		// write pending messages to client
		String reply;
		synchronized(this.pendingWriteChannels) {
			reply = this.pendingWrites.get(conn);
			System.out.printf("Replying to %s with:%s\n", ((SocketChannel)conn.channel()).getRemoteAddress().toString(),reply);
			channel.write(stringToByteBuffer(reply));
			this.pendingWrites.remove(conn);
		}

		// Done writing -- tell selector to listen for reads instead.
		conn.interestOps(SelectionKey.OP_READ);
	}

	/*
	 * Used by worker threads to put a message into the message queue,
	 * so that I/O will be handled by the selection-thread.
	 */
	public void send(SelectionKey conn, String str) {
		synchronized(this.pendingWriteChannels) {
			this.pendingWriteChannels.add(conn);
			this.pendingWrites.put(conn, str);
		}
		// Should wake up the selector in case it is sleeping, so that the message can be processed ASAP.
		this.selector.wakeup();
	}

	private String bufferToString(int bytesRead) {
		byte[] buff = new byte[bytesRead];
		for (int i = 0; i < bytesRead; i += 1) {
			buff[i] = this.readBuffer.get(i);
		}
		return new String(buff, ASLServerSettings.CHARSET);
	}

	public ByteBuffer stringToByteBuffer(String str) {
		Charset charset = ASLServerSettings.CHARSET;
		return charset.encode(str);
	}
}
