package testRunner;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public final class Counters{
	public static AtomicLong MessagesSent = new AtomicLong(0);
	public static AtomicLong MessageRecieved = new AtomicLong(0);
}