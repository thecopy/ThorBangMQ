package asl;

import java.nio.charset.Charset;

import asl.infrastructure.PersistenceType;

public class ServerSettings {

	public Charset getCharset() {
		return Charset.forName("UTF-8");
	}

	public int LISTENING_PORT = 8123;
	
	public PersistenceType PERSISTENCE_TYPE;
	public int MESSAGE_MAX_LENGTH = 4096;
	public int NUM_CLIENTREQUESTWORKER_THREADS = 10;

	public String DB_SERVER_NAME = "localhost";
	public String DB_DATABASE_NAME = "asl";
	public String DB_DATA_SOURCE_NAME = "asl";
	public int DB_PORT_NUMBER = 5432;
	public String DB_USERNAME = "asl";
	public String DB_PASSWORD = "asl2013";
	public int DB_MAX_CONNECTIONS = 5;
	public boolean DB_DEFAULT_AUTO_COMMIT = false;
	public String APPLICATION_LOG_PATH;
	public String TEST_LOG_PATH;

}
