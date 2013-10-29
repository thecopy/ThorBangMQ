package testRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
		
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Choose test:");
		String input = br.readLine();
		
		MemoryLogger logger = new MemoryLogger(true);
		r.runTest(input, settings, logger);
	}

}
