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
}
