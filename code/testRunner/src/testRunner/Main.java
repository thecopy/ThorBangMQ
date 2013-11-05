package testRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;


public class Main {

	public static void main(String[] args) throws Exception {
		Settings settings = new Settings();
		settings.host = "localhost";
		settings.port = 8123;
		
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

		MemoryLogger applicationLogger = new MemoryLogger(true);
		MemoryLogger testLogger = new MemoryLogger(true);
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
					logger.dumpToFile(pathToStoreLog);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
            }
        });
	}

}
