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
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.postgresql.jdbc2.optional.PoolingDataSource;

import asl.ServerSettings;
import asl.Persistence.DbPersistence;
import asl.Persistence.IPersistence;
import asl.Persistence.InMemoryPersistence;
import asl.infrastructure.ProtocolService;
import asl.network.DefaultTransport;
import asl.network.ITransport;

/*
 * With inspiration from http://www.onjava.com/pub/a/onjava/2002/09/04/nio.html?page=2
 * and especially http://rox-xmlrpc.sourceforge.net/niotut/
 */
public class ThorBangMQServer {
	private Selector selector;
	private ServerSocketChannel serverChannel;
	private ExecutorService executor;
	private ByteBuffer readBuffer;
	private LinkedList<SelectionKey> pendingWriteChannels = new LinkedList<SelectionKey>();
	private HashMap<SelectionKey, String> pendingWrites = new HashMap<SelectionKey, String>();
	private Logger logger;
	private IPersistence persistence;
	private int connectedClients;
	private ServerSettings settings;

	public ThorBangMQServer(ServerSettings settings, ExecutorService executor, Logger logger, IPersistence persistence) throws IOException {
		this.executor = executor;
		this.logger = logger;
		this.persistence = persistence;
		this.settings = settings;
		this.readBuffer = ByteBuffer.allocate(settings.MESSAGE_MAX_LENGTH);

		// Initialize server socket and the selector to accept connections.
		this.serverChannel = ServerSocketChannel.open();
		this.serverChannel.configureBlocking(false);
		this.serverChannel.socket().bind(new InetSocketAddress(ServerSettings.SOCKET_PORT));  // listen for connections on SOCKET_PORT
		this.selector = Selector.open();
		this.serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
		this.connectedClients = 0;
	}

	public static ThorBangMQServer build(ServerSettings settings, Logger logger) throws IOException{
		PoolingDataSource connectionPool = new PoolingDataSource();
		connectionPool.setDatabaseName(settings.DB_DATABASE_NAME);
		connectionPool.setDataSourceName(settings.DB_DATA_SOURCE_NAME);
		connectionPool.setUser(settings.DB_USERNAME);
		connectionPool.setServerName(settings.DB_SERVER_NAME);
		connectionPool.setPassword(settings.DB_PASSWORD);
		connectionPool.setMaxConnections(settings.DB_MAX_CONNECTIONS);
		IPersistence persistence = settings.UseInMemoryPersister
				? new InMemoryPersistence()
				: new DbPersistence(connectionPool,logger);

		ExecutorService threadpool = Executors.newFixedThreadPool(settings.NUM_CLIENTREQUESTWORKER_THREADS);

<<<<<<< HEAD

=======
>>>>>>> fd940f182f11b0f251324241a4327c8122416ea8
		return new ThorBangMQServer(settings, threadpool, logger, persistence);
	}

	public void start() {
		logger.info("Waiting for connections..");
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
			try {
				this.selector.select();
			} catch (IOException e) {
				logger.severe("Selector gave IO error!");
				e.printStackTrace();
			}
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

	private void accept(SelectionKey conn) {
		// Only the server channel is listening for connections, so it'll be the channel of conn here.
		ServerSocketChannel serverChannel = (ServerSocketChannel)conn.channel();

		String clientAddress;
		// Accept connections and attach a READ selector
		try {
			SocketChannel client = serverChannel.accept();
			this.clientConnect();
			client.configureBlocking(false);
			client.register(this.selector, SelectionKey.OP_READ);
			clientAddress = client.getRemoteAddress().toString();
		} catch (IOException e) {
			// Connection wasn't made. Skip!
			return;
		}
		logger.info(String.format("Accepted connection from %s\n", clientAddress));
	}

	/**
	 * Read data from a client socket. Pass the data on to a worker thread which acts upon it.
	 * @param conn
	 */
	private void read(SelectionKey conn) {
		this.readBuffer.clear();
		this.readBuffer.limit(settings.MESSAGE_MAX_LENGTH);
		SocketChannel clientChannel = (SocketChannel)conn.channel();

		int bytesRead = 0;
		String clientAddress = "NO_CLIENT";  // Initialize such that variable can be used if there is an exception.
		try {
			clientAddress = clientChannel.getRemoteAddress().toString();
			bytesRead = clientChannel.read(this.readBuffer);
		}
		catch (IOException e) {
			this.unexpectedDisconnect(conn, clientAddress);
			return;
		}

		// End of stream reached. Close connection.
		if (bytesRead == -1) {
			this.disconnect(conn, clientAddress);
			return;
		}

		ITransport transport = new DefaultTransport(this, conn);

		this.executor.execute(
				new ClientRequestWorker(
						logger,
						new ProtocolService(this.persistence, transport),
						transport,
						bufferToString(bytesRead)));
	}

	/**
	 * Write data back on a client socket.
	 * @param conn
	 */
	private void write(SelectionKey conn) {
		// Client's SocketChannel.
		SocketChannel channel = (SocketChannel)conn.channel();

		// Write pending message to client.
		String reply, clientAddress = "NO_CLIENT";  // Initialize such that variable can be used if there is an exception.
		synchronized(this.pendingWriteChannels) {
			reply = this.pendingWrites.get(conn);
			try {
				clientAddress = ((SocketChannel)conn.channel()).getRemoteAddress().toString();
				channel.write(stringToByteBuffer(reply));
			} catch (IOException e) {
				this.unexpectedDisconnect(conn, clientAddress);
			}
			this.pendingWrites.remove(conn);
			logger.info(String.format("Replied to %s with:%s\n", clientAddress, reply));
		}

		// Done writing -- tell selector to listen for reads instead.
		conn.interestOps(SelectionKey.OP_READ);
	}

	/**
	 * Used by worker threads to put messages into a pendingMessages queue, which will be sent in the future.
	 * This makes sure that all network IO stays in the same thread as the selector.
	 * @param conn
	 * @param str
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
		return new String(buff, ServerSettings.CHARSET);
	}

	private ByteBuffer stringToByteBuffer(String str) {
		Charset charset = ServerSettings.CHARSET;
		return charset.encode(str);
	}

	/**
	 * Call every time a client disconnects.
	 */
	private void clientDisconnect() {
		connectedClients -= 1;
		logger.info(String.format("Client disconnect. There are now %d clients connected.", connectedClients));
	}

	/**
	 * Call every time a client connects.
	 */
	private void clientConnect() {
		connectedClients += 1;
		logger.info(String.format("Client connect. There are now %d clients connected.", connectedClients));
	}

	/**
	 * Handle client socket when an unexpected disconnect happens.
	 * @param conn
	 * @param clientAddress
	 */
	private void unexpectedDisconnect(SelectionKey conn, String clientAddress) {
		logger.warning(String.format("Connection from %s was unexpectedly closed!", clientAddress));
		this.disconnect(conn, clientAddress);
	}

	private void disconnect(SelectionKey conn, String clientAddress) {
		try {
			conn.channel().close();
		} catch (IOException e) { } // Ignore error. We're closing the connection anyway.
		finally {
			conn.cancel();
		}
		this.clientDisconnect();
	}
}
