package testRunner;

public abstract class Test {
	protected String host;
	protected int port;
	protected Boolean hasConnectionInfoSet = false;
	public void setConnectionInfo(String hostname, int port){
		host = hostname;
		this.port = port;
		hasConnectionInfoSet = true;
	}
	
	public abstract String[] getArgsDescriptors();
	public abstract void init(String[] args) throws Exception;
	public abstract void run(MemoryLogger applicationLogger, MemoryLogger testLogger) throws Exception;
	public abstract String getInfo();
	public abstract String getIdentifier();
}
