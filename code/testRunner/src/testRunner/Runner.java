package testRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;

import testRunner.tests.DummyTest;
import testRunner.tests.SendMessages;
import testRunner.tests.SendMessagesTime;

public class Runner {
	List<Class> tests = new ArrayList<Class>();
	
	String testArgs[];
	
	public Runner(){
		tests.add(DummyTest.class);
		tests.add(SendMessages.class);
		tests.add(SendMessagesTime.class);
	}
	
	public List<Class> getTests(){
		return tests;
	}
	
	public void setArgs(String args[]) {
		this.testArgs = args;
	}

	public Test getTestFromIdentifier(String input) throws InstantiationException, IllegalAccessException {
		for(Class t : tests)
		{
			Test test = (Test)t.newInstance();
			if(test.getIdentifier().toLowerCase().equals(input.toLowerCase()))
				return test;
		}
		return null;
	}
	
	
	
	public void runTest(String identifier, Settings settings, MemoryLogger applicationLogger, MemoryLogger testLogger) throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Test test = this.getTestFromIdentifier(identifier);

		if (this.testArgs.length <= 0) {
			System.out.println("---");
			System.out.println(test.getInfo());
			System.out.println("Please provide these arguments: ");
			String[] argDescriptors = test.getArgsDescriptors();
			this.testArgs = new String[argDescriptors.length];
			
			for(int i = 0; i < argDescriptors.length; i++){
				String arg = argDescriptors[i];
				System.out.println(arg);
				System.out.print("=>");
				this.testArgs[i] = br.readLine();
			}
		}
		
		System.out.println("OK. Initing test...");
		
		System.out.println("---");
		test.setConnectionInfo(settings.host, settings.port);
		test.init(this.testArgs);
		System.out.println("---");
			
		System.out.println("OK. Running test...");
		
		System.out.println("---");
		StopWatch w = new StopWatch();
		w.start();
		test.run(applicationLogger, testLogger);
		w.stop();
		System.out.println("---");
			
		System.out.println("OK. Finished");
		System.out.println("Total Running time: " + w.getNanoTime()/1000/(float)1000 + " ms");
	}
}
