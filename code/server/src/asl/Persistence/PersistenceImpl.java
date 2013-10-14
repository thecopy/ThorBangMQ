package asl.Persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import org.postgresql.jdbc2.optional.PoolingDataSource;

import asl.ASLServerSettings;
import asl.Client;
import asl.Message;

public class PersistenceImpl implements IPersistence {

	public static PoolingDataSource connectionPool = new PoolingDataSource();

	public PersistenceImpl() {
		connectionPool.setDatabaseName(ASLServerSettings.DB_DATABASE_NAME);
		connectionPool.setDataSourceName(ASLServerSettings.DB_DATA_SOURCE_NAME);
		connectionPool.setUser(ASLServerSettings.DB_USERNAME);
		connectionPool.setServerName(ASLServerSettings.DB_SERVER_NAME);
		connectionPool.setPassword(ASLServerSettings.DB_PASSWORD);
		connectionPool.setMaxConnections(ASLServerSettings.DB_MAX_CONNECTIONS);
	}

	@Override
	public void deleteMessage(long messageId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void storeMessage(Message message) {
		// TODO: Insert logging
		System.out.println("Creating queue");
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = PersistenceImpl.connectionPool.getConnection();
			con.setAutoCommit(false);
			// Set up prepared statement
			stmt = con.prepareStatement(
					"INSERT INTO messages (sender_id, receiver_id, queue_id, context_id, priority, message) " + 
			        " VALUES (?, ?, ?, ?, ?, ?)");
			stmt.setLong(1, message.senderId);
			stmt.setLong(2, message.receiverId);
			stmt.setLong(3, message.queueId);
			stmt.setLong(4, message.contextId);
			stmt.setInt(5, message.priority);
			stmt.setString(6, message.content);
			
			// Check if insert statement succeeded
			int affectedRows = stmt.executeUpdate();
			if (affectedRows == 0) {
				// TODO: Insert logging
				System.out.println("0 rows affected on insert in storeMessage. BAD!");
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
	public Message getMessageByPriority(long queueId, long recieverId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message getMessageByTimestamp(long queueId, long recieverId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message getMessageBySender(long queueId, long recierId, long senderId) {
		// TODO Auto-generated method stub
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
			con = PersistenceImpl.connectionPool.getConnection();
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
			con = PersistenceImpl.connectionPool.getConnection();
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
		PersistenceImpl.connectionPool.close();
	}

}
