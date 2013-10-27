package clientManager;

import java.io.IOException;

import org.apache.commons.lang3.time.StopWatch;

import clientTests.ClientTest;
import clientTests.WriteTest;

public class main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.out.println("Arguments are: <hostname> <number of clients> <messages per client> <test name>");
			System.exit(-1);
		}
		
		int port = 8123;
		String hostName = args[0];
		int numClients = Integer.parseInt(args[1]);
		int numMessagesPerClient = Integer.parseInt(args[2]);
		String testName = args[3];
		
		ClientTest test = null;
		switch (testName.toLowerCase()) {
			case "writetest":
				test = new WriteTest(hostName, port, numClients, numMessagesPerClient);
				break;
			default:
				System.out.printf("Test '%s' doesn't exist!", testName);
				System.exit(0);
		}
		System.out.printf("Starting test '%s'", testName);
		test.prepare();
		test.start();
		test.cleanUp();
	}
		

}
