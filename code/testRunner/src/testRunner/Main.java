package testRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream.GetField;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) throws Exception {
		Settings settings = new Settings();
		getInformationFromArgs(args, settings);
		
		System.out.println("ThorBangMQ test runner prototype");
		
		settings.printSettingsToSystemOut();
		
		Runner r = new Runner();
		
		Test t = r.getTestFromIdentifier(settings.testName);
		r.runTest(t, settings, new MemoryLogger(true), settings.args);
		
	}
	
	private static void getInformationFromArgs(String[] args, Settings settings){
		for(int i = 0; i < args.length; i++){
			String arg = args[i];
			if(arg.startsWith("host="))
				settings.host = arg.substring(5);
			else if(arg.startsWith("port="))
				settings.port = Integer.parseInt(arg.substring(5));
			else if(arg.startsWith("test="))
				settings.testName = arg.substring(5);
			else{
				settings.args = Arrays.copyOfRange(args, i, args.length);
				break;
			}
		}
	}

}
