package de.x8bit.Fantasya.Host.serialization.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.jmock.Expectations;
import org.jmock.Mockery;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DatabaseTest {

	private String username = "myuser";
	private String password = "topsecret";
	private String tablename = "someTable";

	private String simpleCommand = "do something";
	private char nonAscii = 153;
	private String commandToProcess = "do something =C4,Ä,Ö,Ü,ß,ä,ö,ü," + nonAscii;
	private String replacedCommand = "do something Ae,Ae,Oe,Ue,ss,ae,oe,ue,";

	private Mockery context = new Mockery();
	private DataSource source = context.mock(DataSource.class);
	private Connection connection = context.mock(Connection.class, "primary connection");
	private Connection secondConnection = context.mock(Connection.class, "Second");

	private Statement statement = context.mock(Statement.class);
	private DatabaseMetaData dbMeta = context.mock(DatabaseMetaData.class);
	private ResultSet rs = context.mock(ResultSet.class);

	private Database cleanDatabase = new Database(source, username, password);
	private Database openDatabase = new Database(source, username, password);

	@Before
	public void setup() throws SQLException {
		context.checking(new Expectations() {{
			oneOf(source).getConnection(username, password);
				will(returnValue(connection));
		}});

		openDatabase.open();
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresDataSource() {
		new Database(null, username, password);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresValidUsername() {
		new Database(source, null, password);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresValidPassword() {
		new Database(source, username, null);
	}

	@Test
	public void newConnectionRequestedWhenOpening() throws SQLException {
		context.checking( new Expectations() {{
			oneOf(source).getConnection(username, password);
				will(returnValue(connection));
		}});

		cleanDatabase.open();

		context.assertIsSatisfied();
	}

	@Test
	public void openAgainIfAlreadyOpened() throws SQLException {
		context.checking( new Expectations() {{
			oneOf(connection).close();
			oneOf(source).getConnection(username, password);
				will(returnValue(secondConnection));
		}});

		openDatabase.open();

		context.assertIsSatisfied();
	}

	@Test
	public void connectionsAreClosedOnClosing() throws SQLException {
		context.checking( new Expectations() {{
			oneOf(connection).close();
		}});

		openDatabase.close();

		context.assertIsSatisfied();
	}

	@Test
	public void openingStatusCanBeQueried() throws SQLException {
		assertFalse("Closed database was not correctly identified.",
				cleanDatabase.isOpen());
		assertTrue("Open database was not correctly identified.",
				openDatabase.isOpen());
	}

	@Test(expected = IllegalStateException.class)
	public void tableRequestRequiresOpenDatabase() throws SQLException {
		cleanDatabase.hasTable(tablename);
	}

	@Test
	public void noAvailableTablesAreRecognized() throws SQLException {
		context.checking(new Expectations() {{
			oneOf(connection).getMetaData();
				will(returnValue(dbMeta));
			oneOf(dbMeta).getTables(null, null, tablename, null);
				will(returnValue(rs));
			oneOf(rs).next();
				will(returnValue(false));
		}});

		assertFalse("Database did not recognize properly not available table.",
				openDatabase.hasTable(tablename));

		context.assertIsSatisfied();
	}

	@Test
	public void availableTablesAreRecognized() throws SQLException {
		context.checking(new Expectations() {{
			oneOf(connection).getMetaData();
				will(returnValue(dbMeta));
			oneOf(dbMeta).getTables(null, null, tablename, null);
				will(returnValue(rs));
			oneOf(rs).next();
				will(returnValue(true));
			oneOf(rs).next();
				will(returnValue(false));
		}});

		assertTrue("Available table was not correctly recognized.",
				openDatabase.hasTable(tablename));

		context.assertIsSatisfied();
	}

	@Test(expected = IllegalStateException.class)
	public void databaseMustBeOpenForDataRequest() throws SQLException {
		cleanDatabase.getTableData(tablename);
	}

	@Test
	public void requestedTableDataIsReturned() throws SQLException {
		context.checking(new Expectations() {{
			oneOf(connection).createStatement();
				will(returnValue(statement));
			oneOf(statement).executeQuery(with(equal("SELECT * from " + tablename)));
				will(returnValue(rs));
		}});

		assertEquals("Incorrect result set returned.",
				rs, openDatabase.getTableData(tablename));

		context.assertIsSatisfied();
	}

	@Test(expected = IllegalStateException.class)
	public void databaeMustBeOpenedForCommandExecution() throws SQLException {
		cleanDatabase.executeCommand(commandToProcess);
	}

	@Test
	public void commandsAreSentToTheDatabase() throws SQLException {
		context.checking(new Expectations() {{
			// execution of the first command
			oneOf(connection).createStatement();
				will(returnValue(statement));
			oneOf(statement).execute(simpleCommand);
				will(returnValue(true));
		}});

		openDatabase.executeCommand(simpleCommand);

		context.assertIsSatisfied();
	}
	@Test
	public void commandsAreProcessedForSpecialCharacters() throws SQLException {
		context.checking(new Expectations() {{
			oneOf(connection).createStatement();
				will(returnValue(statement));
			oneOf(statement).execute(replacedCommand);
		}});

		openDatabase.executeCommand(commandToProcess);
		context.assertIsSatisfied();
	}
}