package testRunner.tests;

import testRunner.MemoryLogger;

public class DummyTest extends testRunner.Test {

	@Override
	public void init(String[] args) {
		System.out.println("DummyTest initiated with " + args.length + " arguments:");
		for(Object arg : args)
			System.out.println("* " + arg);
	}


	@Override
	public String[] getArgsDescriptors() {
		String[] descriptors = new String[3];
		descriptors[0] = "number of clients";
		descriptors[1] = "messages per client";
		descriptors[2] = "message to send";
		
		return descriptors;
	}
	
	@Override
	public String getInfo() {
		return "Simple dummy test";
	}
	@Override
	public String getIdentifier() {
		return "dummy";
	}

	@Override
	public void run(MemoryLogger applicationLogger, MemoryLogger testLogger) {
		applicationLogger.log("Running dummy test! :]");
		applicationLogger.log("Host: " + super.host);
		applicationLogger.log("Port: " + super.port);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ignore) {}
		
		applicationLogger.log("Yaaaaay");
	}

}
