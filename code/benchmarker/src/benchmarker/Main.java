package benchmarker;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.jdbc2.optional.PoolingDataSource;

import asl.ClientRequestWorker;
import asl.Persistence.DbPersistence;
import asl.Persistence.IPersistence;
import asl.Persistence.LyingPersistence;
import asl.infrastructure.MemoryLogger;
import asl.infrastructure.exceptions.InvalidMessageException;
import asl.infrastructure.exceptions.PersistenceException;
import asl.network.ITransport;

public class Main {

	public static void main(String[] args) throws Exception {
		long duration = 10000L;
		if(args.length > 0)
			duration = Long.parseLong(args[0]);
		MemoryLogger logger = new MemoryLogger(true);
		logger.setLevel(Level.INFO);
		benchmarkClientRequestWorker(duration,"PEEKQ,1,1,1", logger);
		benchmarkClientRequestWorker(duration,"POPQ,1,1,1", logger);
		benchmarkClientRequestWorker(duration,"MSG,1,1,1,1,1,content", logger);
		System.out.println();
		benchmarkDbPersistence(duration,  logger);
		
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
				DbPersistence p = new DbPersistence(fakeSource, new FakeLogger());
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

}
