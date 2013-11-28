package testRunner;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public final class Counters{
	public static AtomicLong RequestsPerformed = new AtomicLong(0);
	public static MemoryLogger ResponseTimeLogger = new MemoryLogger(false);
}