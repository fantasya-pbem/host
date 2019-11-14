package de.x8bit.Fantasya.Host.serialization.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/** Provides slightly more convenient access to a database.
 *
 * This class intends to provide a weak facade to the SQL interface in the Java
 * standard library which is neither lean nor convenient to use. This class
 * does not provide proper handling in several respects. For example,
 * SQLExceptions simply fall through the functions, and no sanity checks are
 * done on various SQL queries.
 */

public class Database {

	private DataSource resource;
	private String username;
	private String password;

	private Connection connection;

	/** Creates a new database object for accessing a database resource.
	 *
	 * @param resource  the database resource to access.
	 * @param username  the username to use for access of the database
	 * @param password  the password to use for access of the database
	 * @throws IllegalArgumentException if any of the parameters is null.
	 */
	public Database(DataSource resource, String username, String password) {
		if (resource == null || username == null || password == null) {
			throw new IllegalArgumentException("Database parameters not supplied.");
		}

		this.resource = resource;
		this.username = username;
		this.password = password;
	}

	/** Opens the connection to the database resource.
	 *
	 * A call to this function is required to use the database.
	 * @throws SQLException if there is an error accessing the database resource.
	 */
	public void open() throws SQLException {
		if (isOpen()) {
			close();
		}
		connection = resource.getConnection(username, password);
	}

	/** Closes the connection to the database.
	 *
	 * @throws SQLException if there is an error accessing the database resource.
	 * @throws IllegalStateException if the database was never opened.
	 */
	public void close() throws SQLException {
		connection.close();
		connection = null;
	}

	/** Returns whether the database has been opened. */
	public boolean isOpen() {
		return connection != null;
	}

	/** Returns whether the database resource contains a table with the given name. */
	public boolean hasTable(String table) throws SQLException {
		if (!isOpen()) {
			throw new IllegalStateException("Database has not been opened.");
		}

		ResultSet rs = connection.getMetaData().getTables(null, null, table, null);
		int numTables = 0;
		while (rs.next()) {
			numTables++;
		}

		return numTables != 0;
	}

	/** Returns the data contained in the table.
	 *
	 * Note that no checking of the table name is done. If you try to access a
	 * non-existant table, this is directly passed to the underlying database
	 * resource, and will likely produce an SQLException.
	 *
	 * @param table  the name of the table to be queried.
	 * @return a ResultSet containing all the data in the table.
	 * @throws SQLException if there is an error with the request.
	 */
	public ResultSet getTableData(String table) throws SQLException {
		if (!isOpen()) {
			throw new IllegalStateException("Database has not been opened.");
		}

		Statement statement = connection.createStatement();
		return statement.executeQuery("SELECT * from " + table);
	}

	/** Execute an arbitrary SQL command.
	 *
	 * @param sqlCommand  the command to execute.
	 * @return whether the command succeeded.
	 * @throws SQLException if there is an error with the command.
	 */
	public void executeCommand(String sqlCommand) throws SQLException {
		if (!isOpen()) {
			throw new IllegalStateException("Database has not been opened.");
		}

		// finally, execute it.
		Statement statement = connection.createStatement();
		statement.execute(sqlCommand);
	}
}
