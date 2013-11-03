package testRunner;

public class Settings {
	public String host;
	public int port;
	public String[] args;
	public String testName;
	
	public void printSettingsToSystemOut(){
		System.out.println("Host=" + host);
		System.out.println("Port=" + port);
		System.out.println("Test Name=" + testName);
		System.out.println("Args:");
		for(String arg : args)
			System.out.println(arg);
	}
	public String TEST_LOG_PATH = "test_log.txt";
	public String APPLICATION_LOG_PATH = "application_log.txt";
}
