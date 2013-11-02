package testRunner;

import java.io.BufferedReader;
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
		
		System.out.println("Available tests:");
		for(Class test : r.getTests()){
			Test t = (Test)test.newInstance();
			System.out.println("* " + t.getIdentifier() + ": " + t.getInfo());
		}
		
		String input;
		if (args.length == 0) {
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
		
		MemoryLogger logger = new MemoryLogger(true);
		r.runTest(input, settings, logger);
	}

}
