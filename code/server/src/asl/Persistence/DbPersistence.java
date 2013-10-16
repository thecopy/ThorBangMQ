package asl.Persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.postgresql.jdbc2.optional.PoolingDataSource;

import asl.ASLServerSettings;
import asl.Client;
import asl.Message;

public class DbPersistence implements IPersistence {

	public static PoolingDataSource connectionPool = new PoolingDataSource();
	private Logger logger;

	public DbPersistence(ASLServerSettings settings, Logger logger) {
		this.logger = logger;

		connectionPool.setDatabaseName(settings.DB_DATABASE_NAME);
		connectionPool.setDataSourceName(settings.DB_DATA_SOURCE_NAME);
		connectionPool.setUser(settings.DB_USERNAME);
		connectionPool.setServerName(settings.DB_SERVER_NAME);
		connectionPool.setPassword(settings.DB_PASSWORD);
		connectionPool.setMaxConnections(settings.DB_MAX_CONNECTIONS);
	}

	@Override
	public void deleteMessage(long messageId) {
		// TODO Auto-generated method stub

	}

	@Override
	public long storeMessage(Message message) {
		final String query = "INSERT INTO messages (sender_id, receiver_id, queue_id, context_id, priority, message) " +
		        			 " VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

		return executeScalar(query, logger,
				message.senderId,
				message.receiverId,
				message.queueId,
				message.contextId,
				message.priority,
				message.content);
	}

	@Override
	public Message getMessageByPriority(long queueId, long recieverId) {
		final String query = "SELECT receiver_id, sender_id, time_of_arrival, queue_id, id, priority, context, content"
							+"FROM messages WHERE receiver_id = ? AND queue_id = ? ORDER BY priority DESC LIMIT 1";

		ArrayList<Object[]> s = executeQuery(query, logger, recieverId, queueId);

		if(s.size() > 0)
			return getMessage(s.get(0));

		return null;
	}

	@Override
	public Message getMessageByTimestamp(long queueId, long recieverId) {
		final String query = "SELECT receiver_id, sender_id, time_of_arrival, queue_id, id, priority, context, content"
				+"FROM messages WHERE receiver_id = ? AND queue_id = ? ORDER BY time_of_arrival ASC LIMIT 1";

		ArrayList<Object[]> s = executeQuery(query, logger, recieverId, queueId);

		if(s.size() > 0)
			return getMessage(s.get(0));

		return null;
	}

	@Override
	public Message getMessageBySender(long queueId, long recierId, long senderId) {
		final String query = "SELECT receiver_id, sender_id, time_of_arrival, queue_id, id, priority, context, content"
							+"FROM messages WHERE receiver_id = ? AND queue_id = ? AND sender_id = ? ORDER BY time_of_arrival ASC LIMIT 1";

		ArrayList<Object[]> s = executeQuery(query, logger, recierId, queueId, senderId);

		if(s.size() > 0)
			return getMessage(s.get(0));

		return null;
	}


	/* (non-Javadoc)
	 * @see asl.Persistence.IPersistence#createQueue(java.lang.String)
	 */
	@Override
	public long createQueue(String name) {
		// TODO: Insert logging
		System.out.println("Creating queue");
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = DbPersistence.connectionPool.getConnection();
			con.setAutoCommit(false);
			// Set up prepared statement
			stmt = con.prepareStatement("INSERT INTO queues (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);

			// Check if insert statement succeeded
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				// TODO: Insert logging
				System.out.println("0 rows affected on insert in createQueue. BAD!");
			}

			// Retrieve id from created queue.
			ResultSet createdRow = stmt.getGeneratedKeys();
			if(createdRow.next()) {
				return createdRow.getLong(1);  // Return newly created queue id.
			} else {
				// TODO: Insert logging
				System.out.println("Couldn't get id from created queue. BAD!");
			}

			return -1; // Error code.

		} catch (SQLException e) {
			// TODO: Insert logging
			e.printStackTrace();
		} finally {

			if (stmt != null) {
				// TODO: Insert logging
				try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
			}
			if (con != null) {
				// TODO: Insert logging
				try { con.commit(); con.close(); } catch (SQLException e) { e.printStackTrace(); }
			}
		}
		return -1; // Error code.
	}

	@Override
	public void removeQueue(long queueId) {
		// TODO: Insert logging
		System.out.println("Creating queue");
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = DbPersistence.connectionPool.getConnection();
			con.setAutoCommit(false);
			// Set up prepared statement
			stmt = con.prepareStatement("DELETE FROM queues WHERE id=?");
			stmt.setLong(1, queueId);

			// Check if insert statement succeeded
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				// TODO: Insert logging
				System.out.println("0 rows affected on delete in createQueue. BAD!");
			}

		} catch (SQLException e) {
			// TODO: Insert logging
			e.printStackTrace();
		} finally {

			if (stmt != null) {
				// TODO: Insert logging
				try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
			}
			if (con != null) {
				// TODO: Insert logging
				try { con.commit(); con.close(); } catch (SQLException e) { e.printStackTrace(); }
			}
		}
	}

	@Override
	public Enumeration<Client> getAllClients() {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() {
		DbPersistence.connectionPool.close();
	}

	@Override
	public Message getMessageById(long id) {
<<<<<<< HEAD
		// TODO Auto-generated method stub
=======
		// TODO Auto-generated method stub
>>>>>>> 1d5b7bd3381d766b4f02e61408e031df2534f3be
		return null;
	}

	@Override
	public long createUser(String name) {
		final String query = "INSERT INTO clients(name) VALUES(?) RETURNING id";

		return executeScalar(query, logger, name);
	}

	private static void executeStatement(String sql, Logger logger, Object... params){
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet set = null;

		try {
			con = DbPersistence.connectionPool.getConnection();
			con.setAutoCommit(false);
			// Set up prepared statement
			stmt = con.prepareStatement(sql);
			for(int i = 1; i <= params.length; i ++)
				stmt.setObject(i, params[i-1]);

			stmt.execute();

			con.commit();

		} catch (SQLException e) {
			// TODO: Insert logging
			e.printStackTrace();
		} finally {
			close(set, stmt, con, logger);
		}
	}

	private static ArrayList<Object[]>  executeQuery(String sql, Logger logger, Object... params){
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet set = null;

		try {
			con = DbPersistence.connectionPool.getConnection();
			con.setAutoCommit(false);
			// Set up prepared statement
			stmt = con.prepareStatement(sql);
			for(int i = 1; i <= params.length; i ++)
				stmt.setObject(i, params[i-1]);

			set = stmt.executeQuery();
			con.commit();

			ResultSetMetaData rsmd = set.getMetaData();
			ArrayList<Object[]> rows = new ArrayList<>();
			int columnCount = rsmd.getColumnCount();
			while(set.next())
			{
				Object[] cols = new Object[columnCount];
				for(int c = 1; c<=columnCount;c++)
					cols[c-1] = (set.getObject(c));

				rows.add(cols);
			}

			return rows;

		} catch (SQLException e) {
			// TODO: Insert logging
			e.printStackTrace();
		} finally {
			close(set, stmt, con, logger);
		}

		return null;
	}

	private static long executeScalar(String sql, Logger logger, Object... params){
		ArrayList<Object[]> r = executeQuery(sql, logger, params);

		if(r.size() > 0)
			return (long)r.get(0)[0];

		return -1;
	}

	private Message getMessage(Object[] cols){
		long receiver = (long)cols[0];
		long sender = (long)cols[1];
		long timestamp = (long)cols[2];
		long queueId = (long)cols[3];
		long id = (long)cols[4];
		int prio = (int)cols[5];
		long context = (long)cols[6];
		String content = (String)cols[7];


		return new Message(receiver, sender, timestamp, queueId, id, prio, context, content);
	}

	private static void close(ResultSet rs, Statement ps, Connection conn, Logger logger)
	{
	    if (rs!=null)
	    {
	        try
	        {
	            rs.close();

	        }
	        catch(SQLException e)
	        {
	            logger.severe("The result set cannot be closed: " + e);
	        }
	    }
	    if (ps != null)
	    {
	        try
	        {
	            ps.close();
	        } catch (SQLException e)
	        {
	            logger.severe("The statement cannot be closed: " +  e);
	        }
	    }
	    if (conn != null)
	    {
	        try
	        {
	            conn.close();
	        } catch (SQLException e)
	        {
	            logger.severe("The data source connection cannot be closed: "  + e);
	        }
	    }

	}

	public void deleteSchema(){
		String sql = "DROP SCHEMA asl CASCADE;";

		executeStatement(sql, logger);
	}

	public void createSchema(){
		String sql = "CREATE SCHEMA asl "
					+"AUTHORIZATION asl;"
					+"GRANT ALL ON SCHEMA asl TO asl;";

		executeStatement(sql, logger);
	}

	public void buildSchema(){
		String sql = " "+
				"CREATE TABLE clients "+
				"( "+
				  "id serial NOT NULL, "+
				  "name character varying(25), "+
				  "CONSTRAINT id PRIMARY KEY (id ) "+
				") "+
				"WITH ( "+
				  "OIDS=FALSE "+
				"); "+
				"ALTER TABLE clients "+
				  "OWNER TO asl; "+
				" "+
				" "+
				"CREATE TABLE queues "+
				"( "+
				  "id serial NOT NULL, "+
				  "name character varying(25), "+
				  "CONSTRAINT queues_id PRIMARY KEY (id ) "+
				") "+
				"WITH ( "+
				  "OIDS=FALSE "+
				"); "+
				"ALTER TABLE queues "+
				  "OWNER TO asl; "+
				" "+
				" "+
				" "+
				"CREATE TABLE messages "+
				"( "+
				  "id serial NOT NULL, "+
				  "sender_id bigint, "+
				  "receiver_id bigint, "+
				  "queue_id bigint NOT NULL, "+
				  "time_of_arrival timestamp without time zone NOT NULL DEFAULT now(), "+
				  "priority integer NOT NULL, "+
				  "context_id bigint, "+
				  "message text NOT NULL, "+
				  "CONSTRAINT messages_id PRIMARY KEY (id ), "+
				  "CONSTRAINT client_id FOREIGN KEY (sender_id) "+
				  	"REFERENCES clients (id) MATCH SIMPLE "+
				  	"ON UPDATE NO ACTION ON DELETE NO ACTION, "+
				  "CONSTRAINT queue_id FOREIGN KEY (queue_id) "+
				  	"REFERENCES queues (id) MATCH SIMPLE "+
				  	"ON UPDATE NO ACTION ON DELETE NO ACTION, "+
				  "CONSTRAINT sender_id FOREIGN KEY (sender_id) "+
				  	"REFERENCES clients (id) MATCH SIMPLE "+
				  	"ON UPDATE NO ACTION ON DELETE NO ACTION "+
				") "+
				"WITH ( "+
				  "OIDS=FALSE\n"+
				"); "+
				"ALTER TABLE messages "+
				  "OWNER TO asl; ";

		executeStatement(sql, logger);
	}
}
