package benchmarker;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.jdbc2.optional.PoolingDataSource;

import asl.ClientRequestWorker;
import asl.ServerSettings;
import asl.Persistence.PostgresPersistence;
import asl.Persistence.IPersistence;
import asl.Persistence.LyingPersistence;
import asl.infrastructure.MemoryLogger;
import asl.infrastructure.exceptions.InvalidMessageException;
import asl.infrastructure.exceptions.InvalidQueueException;
import asl.infrastructure.exceptions.PersistenceException;
import asl.network.ITransport;

public class Main {

	public static void main(String[] args) throws Exception {
		final PoolingDataSource connectionPool = new PoolingDataSource();
		ServerSettings settings = new ServerSettings();
		connectionPool.setDatabaseName(settings.DB_DATABASE_NAME);
		connectionPool.setDataSourceName(settings.DB_DATA_SOURCE_NAME);
		connectionPool.setUser(settings.DB_USERNAME);
		connectionPool.setServerName(settings.DB_SERVER_NAME);
		connectionPool.setPassword(settings.DB_PASSWORD);
		connectionPool.setMaxConnections(90+10); // NEVER queue in here. measuring service time!!
		
		PostgresPersistence p = new PostgresPersistence(connectionPool, new FakeLogger());

		final long q = p.createQueue("queue");
		final long c = p.createClient("user");
		p.storeMessage(c, c, q, 0, 0, "hej message abc :C");
		
		long duration = 5000L;
		if(args.length > 0)
			duration = Long.parseLong(args[0]);
		MemoryLogger logger = new MemoryLogger(true);
		logger.setLevel(Level.INFO);
		logger.info("1 Client");
		benchmarkDbPersistenceWithRealDb(connectionPool,duration, 1, logger,c,q);
		logger.info("------------------------------------------");
		logger.info("2 Client");
		benchmarkDbPersistenceWithRealDb(connectionPool,duration, 2, logger,c,q);
		logger.info("------------------------------------------");
		logger.info("5 Client");
		benchmarkDbPersistenceWithRealDb(connectionPool,duration, 5, logger,c,q);
		logger.info("------------------------------------------");
		logger.info("10 Client");
		benchmarkDbPersistenceWithRealDb(connectionPool,duration, 10, logger,c,q);
		logger.info("------------------------------------------");
		logger.info("50 Client");
		benchmarkDbPersistenceWithRealDb(connectionPool,duration, 50, logger,c,q);
		logger.info("------------------------------------------");
		
		logger.dumpToFile("bench_log.txt");
	}
	
	private static void benchmarkClientRequestWorker(long duration, final String request, final Logger logger) throws InterruptedException{
		final ITransport t = new FakeTransport();
		final IPersistence ps = new LyingPersistence();
		final AtomicLong counter = new AtomicLong(0);
		RunnableWithStop test = new RunnableWithStop() {
			@Override
			public void run() {
				ClientRequestWorker crw = new ClientRequestWorker(new FakeLogger() ,ps, t, request);
				
				while(stop == false){
					crw.run();	
					counter.incrementAndGet();
				}
			}
		};
		Thread thread = new Thread(test);
		thread.start();
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		test.stop = true;
		thread.join();
	
		logger.info(String.format(
				"ClientRequestWorker:\t%.2f requests (%s) per second",
				counter.get()/duration * (float)1000, request));
		logger.info(String.format(
				"\t\t\t\t%.2f µs per request (%s)", 
				(float)duration/counter.get() * (float)1000, request));
		logger.info("");
	}
	
	private static void benchmarkDbPersistence(long duration, final Logger logger) throws InterruptedException, SQLException{
		final PoolingDataSource fakeSource = mock(PoolingDataSource.class);
		FakeConnection c = new FakeConnection();
		FakePreparedStatement stmt = new FakePreparedStatement();
		when(fakeSource.getConnection()).thenReturn(c);
		c.setPreparedStatement(stmt);

		FakeResultSet rs = new FakeResultSet();
		
		Object[] result = new Object[9];
		// "SELECT receiver_id, sender_id, time_of_arrival, queue_id, id, priority, context_id, message"
		result[1] = 1L;
		result[2] = 1L;
		result[3] = new Timestamp(1000L);
		result[4] = 1L;
		result[5] = 1L;
		result[6] = 1;
		result[7] = 1L;
		result[8] = "message";
		rs.result = result;
		rs.metaData = new FakeResultSetMetaData(8);
		stmt.setResultSet(rs);
		
		final AtomicLong counter = new AtomicLong(0);
		
		RunnableWithStop test = new RunnableWithStop() {
			@Override
			public void run() {
				PostgresPersistence p = new PostgresPersistence(fakeSource, new FakeLogger());
				try {
					while(stop == false){
							p.getMessageById(10);
						counter.incrementAndGet();
					}
				} catch (PersistenceException | InvalidMessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		Thread thread = new Thread(test);
		thread.start();
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		test.stop = true;
		thread.join();
	
		logger.info(String.format(
				"DbPersistence:\t%.2f requests (getMessageById) per second",
				counter.get()/duration * (float)1000));
		logger.info(String.format(
				"\t\t\t%.2f µs per request (getMessageById)", 
				(float)duration/counter.get() * (float)1000));
		logger.info("");
	}


	private static void benchmarkDbPersistenceWithRealDb(
			final PoolingDataSource connectionPool, 
			long duration, int clients, final Logger logger,
			final long c, final long q) throws Exception{
		
		final AtomicLong counter = new AtomicLong(0);
	
		final PostgresPersistence p = new PostgresPersistence(connectionPool, new FakeLogger());
		
		RunnableWithStop test = new RunnableWithStop() {
			@Override
			public void run() {
				try {
					while(stop == false){
							try {
								p.getMessageByTimestamp(q,c);
								counter.incrementAndGet();

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		Thread[] threads = new Thread[clients];
		for(int i = 0;i < clients; i ++){
			threads[i] = new Thread(test);
			threads[i].start();
		}
		
		try {
			Thread.sleep(duration);
			for(Thread t : threads)
				t.stop();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		logger.info(String.format(
				"DbPersistence:\t%d requests ",
				counter.get()));
		logger.info(String.format(
				"DbPersistence:\t%.5f requests (getMessageByTimestamp) per second",
				counter.get()/(duration*(float)1) * (float)1000));
		logger.info(String.format(
				"\t\t\t%.2f ms per request (getMessageByTimestamp)", 
				(float)duration/counter.get()));
		logger.info("");
	}
	
}
