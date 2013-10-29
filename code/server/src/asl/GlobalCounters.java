package asl;

import java.util.concurrent.atomic.AtomicLong;

public class GlobalCounters {
	public static AtomicLong numberOfMessagesPersisted = new AtomicLong(0);
	public static AtomicLong numberOfMessagesReturned = new AtomicLong(0);
}
