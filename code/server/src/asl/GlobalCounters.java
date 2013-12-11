package asl;

import java.util.concurrent.atomic.AtomicLong;

import asl.infrastructure.MemoryLogger;

public class GlobalCounters {
	public static AtomicLong numberOfMessagesPersisted = new AtomicLong(0);
	public static AtomicLong numberOfMessagesReturned = new AtomicLong(0);
	public static AtomicLong totalThinkOperations = new AtomicLong(0);

	public static AtomicLong totalThinkTime = new AtomicLong(0);
	public static AtomicLong totalThinkTimeInClientRequestWorker = new AtomicLong(0);
	public static AtomicLong totalThinkTimeInPersistence = new AtomicLong(0);
	
	public static MemoryLogger crwServiceTime = new MemoryLogger(false);
	public static MemoryLogger dbServiceTime = new MemoryLogger(false);

}
