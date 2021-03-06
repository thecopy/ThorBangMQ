package asl.Persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.postgresql.jdbc2.optional.PoolingDataSource;

import asl.Client;
import asl.GlobalCounters;
import asl.Message;
import asl.infrastructure.exceptions.InvalidClientException;
import asl.infrastructure.exceptions.InvalidMessageException;
import asl.infrastructure.exceptions.InvalidQueueException;
import asl.infrastructure.exceptions.PersistenceException;

public class PostgresPersistence implements IPersistence {

	private PoolingDataSource connectionPool;
	private Logger logger;
	private final String messageExceptionString = "is not present in table \"messages\"";
	private final String clientExceptionString = "is not present in table \"clients\"";
	private final String queueExceptionString = "is not present in table \"queues\"";
	private Pattern readExceptionStringPattern = Pattern.compile("Key \\((?<name>[\\w_-]+)\\)=\\((?<id>\\d+)\\)");
	

	public PostgresPersistence(PoolingDataSource connectionPool, Logger logger) {
		this.logger = logger;
		this.connectionPool = connectionPool;
		System.out.println("connectionPool is null = "  + (null == connectionPool));
	}

	@Override
	public void deleteMessage(long messageId) throws PersistenceException, InvalidMessageException {
		long started = System.nanoTime();
		
		final String query = "DELETE FROM messages WHERE id = ?";
		logger.fine(String.format("Deleting message %d", messageId));

		try {
			executeStatement(query, logger, messageId);
		} catch (SQLException e) {
			long id = this.getIdOfExceptionString(e.getMessage());
			if (e.getMessage().contains(this.messageExceptionString)) {
				throw new InvalidMessageException(id);
			} else {
				throw new PersistenceException(e);
			}
		}finally{
			GlobalCounters.totalThinkTimeInPersistence.addAndGet(System.nanoTime()-started);
		}
	}

	@Override
	public long storeMessage(long senderId, long receiverId, long queueId, long contextId,
			int priority, String content) throws PersistenceException, InvalidQueueException, InvalidClientException {
		final String query = "INSERT INTO messages (sender_id, receiver_id, queue_id, context_id, priority, message) "
				+ " VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
		long started = System.nanoTime();

		try {
			return (long) executeScalar(query, logger, senderId,
					receiverId > 0 ? receiverId : null, queueId, contextId,
					priority, content);
		} catch (SQLException e) {
			long id = this.getIdOfExceptionString(e.getMessage());
			if (e.getMessage().contains(this.clientExceptionString)) {
				throw new InvalidClientException(id);
			} else if (e.getMessage().contains(this.queueExceptionString)) {
				throw new InvalidQueueException(id);
			} else {
				throw new PersistenceException(e);
			}
		}finally{
			GlobalCounters.totalThinkTimeInPersistence.addAndGet(System.nanoTime()-started);
		}
	}

	@Override
	public Message getMessageByPriority(long queueId, long recieverId) throws InvalidQueueException, PersistenceException {
		final String query = " (select receiver_id, sender_id, time_of_arrival, queue_id, id, priority, context_id, message from asl.messages "+
				"where receiver_id = ?  and queue_id = ? order by priority DESC limit 1) "
			   +"union all "
			   	+"(select receiver_id, sender_id, time_of_arrival, queue_id, id, priority, context_id, message from asl.messages "
			   	+ "where receiver_id = null and queue_id = ? order by priority DESC limit 1) order by priority DESC limit 1;";
		long started = System.nanoTime();
		
		ArrayList<Object[]> s;
		try {
			s = executeQuery(query, logger, recieverId, queueId,queueId);
		} catch (SQLException e) {
			long id = this.getIdOfExceptionString(e.getMessage());
			if (e.getMessage().contains(this.queueExceptionString)) {
				throw new InvalidQueueException(id);
			} else {
				throw new PersistenceException(e);
			}
		}finally{
			GlobalCounters.totalThinkTimeInPersistence.addAndGet(System.nanoTime()-started);
		}

		if (s.size() > 0)
			return getMessage(s.get(0));

		return null;
	}

	@Override
	public Message getMessageByTimestamp(long queueId, long recieverId) throws InvalidQueueException, InvalidMessageException, PersistenceException {
		
		final String query = " (select receiver_id, sender_id, time_of_arrival, queue_id, id, priority, context_id, message from asl.messages "+
					" where receiver_id = ?  and queue_id = ? order by time_of_arrival limit 1) "
				   +"union all "
				   	+"(select receiver_id, sender_id, time_of_arrival, queue_id, id, priority, context_id, message from asl.messages "
				   	+ "where receiver_id is null and queue_id = ? order by time_of_arrival limit 1) order by time_of_arrival limit 1;";
		logger.finer(String.format("SELECT message where queue %d for user %d by time", queueId, recieverId));
		long started = System.nanoTime();

		ArrayList<Object[]> s;
		
		try {
			s = executeQuery(query, logger, recieverId, queueId,queueId);
		} catch (SQLException e) {
			long id = this.getIdOfExceptionString(e.getMessage());
			if (e.getMessage().contains(this.queueExceptionString)) {
				throw new InvalidQueueException(id);
			} else {
				throw new PersistenceException(e);
			}
		}finally{
			GlobalCounters.totalThinkTimeInPersistence.addAndGet(System.nanoTime()-started);
		}
		if (s.size() > 0)
			return getMessage(s.get(0));

		return null;
	}

	@Override
	public Message getMessageBySender(long queueId, long receiverId, long senderId, boolean getByTimestampInsteadOfPriority) throws InvalidQueueException, InvalidClientException, PersistenceException {
		final String query = String.format(" (select receiver_id, sender_id, time_of_arrival, queue_id, id, priority, context_id, message from asl.messages"+
				" where receiver_id = ?  and queue_id = ? and sender_id = ? order by %s limit 1) "
				   +"union all "
				   	+"(select receiver_id, sender_id, time_of_arrival, queue_id, id, priority, context_id, message from asl.messages "
				   	+ " where receiver_id = null and queue_id = ? and sender_id = ? order by %s limit 1) order by %s limit 1;",
				getByTimestampInsteadOfPriority ? "time_of_arrival ASC" : "priority DESC",
				getByTimestampInsteadOfPriority ? "time_of_arrival ASC" : "priority DESC",
				getByTimestampInsteadOfPriority ? "time_of_arrival ASC" : "priority DESC");
		long started = System.nanoTime();

		ArrayList<Object[]> s;
		try {
			s = executeQuery(query, logger, receiverId, queueId, senderId, queueId, senderId);
		} catch (SQLException e) {
			long id = this.getIdOfExceptionString(e.getMessage());
			if (e.getMessage().contains(this.queueExceptionString)) {
				throw new InvalidQueueException(id);
			} else if(e.getMessage().contains(this.clientExceptionString)) {
				throw new InvalidClientException(id);
			}
			else {
				throw new PersistenceException(e);
			}
		}finally{
			GlobalCounters.totalThinkTimeInPersistence.addAndGet(System.nanoTime()-started);
		}

		if (s.size() > 0)
			return getMessage(s.get(0));

		return null;
	}

	@Override
	public long createQueue(String name) throws PersistenceException {
		final String query = "INSERT INTO queues(name) VALUES(?) RETURNING id";
		long started = System.nanoTime();

		try {
			long id =  (long)executeScalar(query, logger, name);
			return id;
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}finally{
			GlobalCounters.totalThinkTimeInPersistence.addAndGet(System.nanoTime()-started);
		}
	}

	@Override
	public void removeQueue(long queueId) throws PersistenceException, InvalidQueueException {
		final String query = "DELETE FROM queues WHERE id = ?";
		long started = System.nanoTime();

		try {
			executeStatement(query, logger, queueId);
		} catch (SQLException e) {
			long id = this.getIdOfExceptionString(e.getMessage());
			if (e.getMessage().contains(this.queueExceptionString)) {
				throw new InvalidQueueException(id);
			} else {
				throw new PersistenceException(e);
			}
		}finally{
			GlobalCounters.totalThinkTimeInPersistence.addAndGet(System.nanoTime()-started);
		}
	}

	@Override
	public Enumeration<Client> getAllClients() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message getMessageById(long messageId) throws PersistenceException, InvalidMessageException {
		final String query = "SELECT receiver_id, \"sender_id\", \"time_of_arrival\", \"queue_id\","
				+ "id, priority, \"context_id\", message "
				+ " FROM messages WHERE id = ?";
		long started = System.nanoTime();

		ArrayList<Object[]> s;
		try {
			s = executeQuery(query, logger, messageId);
		} catch (SQLException e) {
			long id = this.getIdOfExceptionString(e.getMessage());
			if (e.getMessage().contains(this.messageExceptionString)) {
				throw new InvalidMessageException(id);
			} else {
				throw new PersistenceException(e);
			}
		}finally{
			GlobalCounters.totalThinkTimeInPersistence.addAndGet(System.nanoTime()-started);
		}
		if (s.size() > 0)
			return getMessage(s.get(0));

		return null;
	}

	@Override
	public long createClient(String name) throws PersistenceException {
		final String query = "INSERT INTO clients(name) VALUES(?) RETURNING id";

		try {
			
			return (long)executeScalar(query, logger, name);
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}
	
	@Override
	public void removeClient(long clientId) throws InvalidClientException, PersistenceException {
		final String query = "DELETE FROM clients WHERE id = ?";
		
		try {
			executeStatement(query, logger, clientId);
		} catch (SQLException e) {
			long id = this.getIdOfExceptionString(e.getMessage());
			if (e.getMessage().contains(this.clientExceptionString)) {
				throw new InvalidClientException(id);
			} else {
				throw new PersistenceException(e);
			}
		}
	}

	private void executeStatement(String sql, Logger logger, Object... params) throws SQLException {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet set = null;

		try {
			con = connectionPool.getConnection();
			con.setAutoCommit(false);
			// Set up prepared statement
			stmt = con.prepareStatement(sql);
			for (int i = 1; i <= params.length; i++)
				stmt.setObject(i, params[i - 1]);

			stmt.execute();

			con.commit();

		} finally {
			close(set, stmt, con, logger);
		}
	}

	private ArrayList<Object[]> executeQuery(String sql, Logger logger, Object... params) throws SQLException {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet set = null;
		try {
			con = connectionPool.getConnection();
			con.setAutoCommit(false);
			// Set up prepared statement
			stmt = con.prepareStatement(sql);
			for (int i = 1; i <= params.length; i++)
				stmt.setObject(i, params[i - 1]);
			
			set = stmt.executeQuery();
			con.commit();

			ResultSetMetaData rsmd = set.getMetaData();
			ArrayList<Object[]> rows = new ArrayList<>();
			int columnCount = rsmd.getColumnCount();
			while (set.next()) {
				Object[] cols = new Object[columnCount];
				for (int c = 1; c <= columnCount; c++)
					cols[c - 1] = (set.getObject(c));

				rows.add(cols);
			}

		return rows;
		} finally {
			close(set, stmt, con, logger);
		}
	}

	private Object executeScalar(String sql, Logger logger, Object... params) throws SQLException {
		ArrayList<Object[]> r = executeQuery(sql, logger, params);
		if (r == null) {			
			return -1L;
		}
		
		if (r.size() > 0)
			return r.get(0)[0];

		return -1L;
	}

	private Message getMessage(Object[] cols) {
		long receiver = (long) (cols[0] == null ? -1L : (long)cols[0]);
		long sender = (long) cols[1];
		long timestamp = ((Timestamp) cols[2]).getTime();
		long queueId = (long) cols[3];
		long id = (long) cols[4];
		int prio = (int) cols[5];
		long context = (long) cols[6];
		String content = (String) cols[7];

		return new Message(receiver, sender, timestamp, queueId, id, prio,
				context, content);
	}

	private static void close(ResultSet rs, Statement ps, Connection conn,
			Logger logger) throws SQLException {
		if (rs != null) {
			try {
				rs.close();

			} catch (SQLException e) {
				logger.severe("The result set cannot be closed: " + e);
			}
		}
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				logger.severe("The statement cannot be closed: " + e);
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.severe("The data source connection cannot be closed: "
						+ e);
			}
		}

	}

	public void deleteSchema() throws PersistenceException {
		String sql = "DROP SCHEMA asl CASCADE;";

		try {
			executeStatement(sql, logger);
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	public void createSchema() throws PersistenceException {
		String sql = "CREATE SCHEMA asl " + "AUTHORIZATION asl;"
				+ "GRANT ALL ON SCHEMA asl TO asl;";

		try {
			executeStatement(sql, logger);
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}

	public void buildSchema() throws PersistenceException {
		String sql = "CREATE TABLE clients "
				+ "( "
				+ "id bigserial NOT NULL, "
				+ "name character varying(25), "
				+ "CONSTRAINT id PRIMARY KEY (id ) "
				+ ") "
				+ "WITH ( "
				+ "OIDS=FALSE "
				+ "); "
				+ "ALTER TABLE clients "
				+ "OWNER TO asl; "
				+ " "
				+ " "
				+ " "
				+ "CREATE TABLE queues "
				+ "( "
				+ "id bigserial NOT NULL, "
				+ "name character varying(25), "
				+ "CONSTRAINT queues_id PRIMARY KEY (id ) "
				+ ") "
				+ "WITH ( "
				+ "OIDS=FALSE "
				+ "); "
				+ "ALTER TABLE queues "
				+ "OWNER TO asl; "
				+ " "
				+ " "
				+ "CREATE TABLE messages "
				+ "( "
				+ "id bigserial NOT NULL, "
				+ "sender_id bigint, "
				+ "receiver_id bigint, "
				+ "queue_id bigint NOT NULL, "
				+ "time_of_arrival timestamp without time zone NOT NULL DEFAULT now(), "
				+ "priority integer NOT NULL, " + "context_id bigint, "
				+ "message text NOT NULL, "
				+ "CONSTRAINT messages_id PRIMARY KEY (id ), "
				+ "CONSTRAINT client_id FOREIGN KEY (sender_id) "
				+ "REFERENCES clients (id) MATCH SIMPLE "
				+ "ON UPDATE NO ACTION ON DELETE NO ACTION, "
				+ "CONSTRAINT queue_id FOREIGN KEY (queue_id) "
				+ "REFERENCES queues (id) MATCH SIMPLE "
				+ "ON UPDATE NO ACTION ON DELETE NO ACTION, "
				+ "CONSTRAINT sender_id FOREIGN KEY (sender_id) "
				+ "REFERENCES clients (id) MATCH SIMPLE "
				+ "ON UPDATE NO ACTION ON DELETE NO ACTION " + ") " + "WITH ( "
				+ "OIDS=FALSE " + "); " + "ALTER TABLE messages "
				+ "OWNER TO asl; " + " " +
				"CREATE INDEX receiver_queue_toa "+
					"ON asl.messages "+
					" USING btree "+
					" (receiver_id, queue_id,time_of_arrival);" +
				"CREATE INDEX receiver_queue_prio "+
					"ON asl.messages "+
					" USING btree "+
					" (receiver_id, queue_id,priority);" +
				"CREATE INDEX sender "+
					"ON asl.messages "+
					" USING btree "+
					" (sender_id);";
		try {
			executeStatement(sql, logger);
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}
	
	public void fillDb(int numMessages) throws PersistenceException{

		int numLoops = 4;
		String msgsPerLoop = String.valueOf(numMessages / numLoops);
		
		try {
		String sql = "DO "+
					"$do$ "+
					"BEGIN  "+
					"FOR i IN 1.." + msgsPerLoop + " LOOP "+
					"   INSERT INTO asl.messages (receiver_id,sender_id,queue_id,context_id,priority,message) "+
					"	VALUES(2,1,1,1,1,'some message'); "+
					"END LOOP; "+
					"END "+
					"$do$";
			executeStatement(sql, logger);
			sql = "DO "+
					"$do$ "+
					"BEGIN  "+
					"FOR i IN 1.." + msgsPerLoop + " LOOP "+
					"   INSERT INTO asl.messages (receiver_id,sender_id,queue_id,context_id,priority,message) "+
					"	VALUES(2,2,1,1,1,'some message'); "+
					"END LOOP; "+
					"END "+
					"$do$";
			executeStatement(sql, logger);
			sql = "DO "+
					"$do$ "+
					"BEGIN  "+
					"FOR i IN 1.." + msgsPerLoop + " LOOP "+
					"   INSERT INTO asl.messages (receiver_id,sender_id,queue_id,context_id,priority,message) "+
					"	VALUES(1,2,1,1,1,'some message'); "+
					"END LOOP; "+
					"END "+
					"$do$";
			executeStatement(sql, logger);sql = "DO "+
					"$do$ "+
					"BEGIN  "+
					"FOR i IN 1.." + msgsPerLoop + " LOOP "+
					"   INSERT INTO asl.messages (receiver_id,sender_id,queue_id,context_id,priority,message) "+
					"	VALUES(1,1,1,1,1,'some message'); "+
					"END LOOP; "+
					"END "+
					"$do$";
			executeStatement(sql, logger);
		} catch (SQLException e) {
			throw new PersistenceException(e);
		}
	}
	
	private long getIdOfExceptionString(String exceptionString) {
		Matcher match = this.readExceptionStringPattern.matcher(exceptionString);
		if (match.find()) {
			logger.severe(String.format("Invalid id: %s: %s", match.group("name"), match.group("id")));
			return Long.parseLong(match.group("id"));
		}
		return -1;
	}
}
