package asl;

import java.nio.charset.Charset;

public class ASLServerSettings {
	public static final int SOCKET_PORT = 8123;
	public static final Charset CHARSET = Charset.forName("UTF-8");
		
	public static int MESSAGE_MAX_LENGTH = 4096;
	public static int NUM_CLIENTREQUESTWORKER_THREADS = 20;

	public static String DB_SERVER_NAME = "localhost";
	public static String DB_DATABASE_NAME = "asl";
	public static String DB_DATA_SOURCE_NAME = "asl";
	public static int DB_PORT_NUMBER = 5432;
	public static String DB_USERNAME = "asl";
	public static String DB_PASSWORD = "asl2013";
	public static int DB_MAX_CONNECTIONS = 10;
	public static boolean DB_DEFAULT_AUTO_COMMIT = false;
	
}
