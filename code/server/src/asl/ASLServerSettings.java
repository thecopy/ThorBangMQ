package asl;

import java.nio.charset.Charset;

public class ASLServerSettings {
	public static final int SOCKET_PORT = 8123;
	public static final Charset CHARSET = Charset.forName("UTF-8");

	public boolean UseInMemoryPersister;
	public int MESSAGE_MAX_LENGTH = 4096;
	public int NUM_CLIENTREQUESTWORKER_THREADS = 20;

	public String DB_SERVER_NAME = "localhost";
	public String DB_DATABASE_NAME = "asl";
	public String DB_DATA_SOURCE_NAME = "asl";
	public int DB_PORT_NUMBER = 5432;
	public String DB_USERNAME = "asl";
	public String DB_PASSWORD = "asl2013";
	public int DB_MAX_CONNECTIONS = 10;
	public boolean DB_DEFAULT_AUTO_COMMIT = false;

}
