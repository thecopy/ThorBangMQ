package testRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Level;


public class Main {

	public static void main(String[] args) throws Exception {
		Settings settings = new Settings();
		settings.host = "localhost";
		settings.port = 8123;
		Thread.sleep(7000);
		System.out.println("ThorBangMQ test runner prototype");
		
		Runner r = new Runner();
		
		String input;
		if (args.length == 0) {
			
			System.out.println("Available tests:");
			for(Class test : r.getTests()){
				Test t = (Test)test.newInstance();
				System.out.println("* " + t.getIdentifier() + ": " + t.getInfo());
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Choose test:");
			input = br.readLine();
		} else {
			/* Assuming that user has given args correctly in command line:
			 * <host name> <test name> [<args..>]
			 */
			r.setArgs(Arrays.copyOfRange(args, 2, args.length));
			settings.host = args[0];
			input = args[1];
		}
		System.out.println("Test= " + input);
		Counters.ResponseTimeLogger.setLevel(Level.ALL);
		MemoryLogger applicationLogger = new MemoryLogger(true);
		MemoryLogger testLogger = new MemoryLogger(false);
		addShutdownHookForSavingLog(Counters.ResponseTimeLogger, "resp_times_" + settings.TEST_LOG_PATH + ".log");
		addShutdownHookForSavingLog((MemoryLogger)applicationLogger, settings.APPLICATION_LOG_PATH);
		addShutdownHookForSavingLog((MemoryLogger)testLogger, settings.TEST_LOG_PATH);
		r.runTest(input, settings, applicationLogger, testLogger);
		
	}
	
	private static void addShutdownHookForSavingLog(final MemoryLogger logger, final String pathToStoreLog){
		Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                try {
                	logger.info("Dumping file due to interrupt signal!");
					logger.dumpToFile(pathToStoreLog);
				} catch (Exception e) {
					PrintWriter out;
					try {
						out = new PrintWriter(pathToStoreLog);
						out.println("Could not write log: " + e.getClass());
						out.println("Could not write log: " + e.getMessage());
						out.println("Could not write log: " + e.toString());
						out.close();
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
            }
        });
	}

}
