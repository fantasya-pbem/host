package de.x8bit.Fantasya.Host.serialization.db;

import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.lib.legacy.ClassImposteriser;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DatabaseAdapterTest {

	private final String tablename = "someTable";


	private Mockery context = new Mockery(){{
		setImposteriser(ClassImposteriser.INSTANCE);
	}};

	private Database db = context.mock(Database.class);
	private DatabaseAdapter adapter = new DatabaseAdapter(db);

	private Map<String, String> row1 = new HashMap<String, String>();
	private Map<String, String> row2 = new HashMap<String, String>();
	private SerializedData serializedData = new SerializedData();
	private String truncation = "TRUNCATE TABLE " + tablename + ";";
	private String creation;
	private String insert1;
	private String insert2;


	@Before
	public void setup() {
		// add some data to be read.
		row1.put("id", "5");
		row1.put("value", "something");
		row2.put("id", "6");
		row2.put("value", "entryWithCharacter'ToBeEscaped");
		serializedData.add(row1);
		serializedData.add(row2);

		// assemble the insert that we expect.
		String keys = "(";
		String creationKeys = "(";
		String vals1 = "(";
		String vals2 = "(";
		for (String key : row1.keySet()) {
			keys += key + ",";
			creationKeys += key + " text,";
			vals1 += "'" + row1.get(key).replace("'", "\\'") + "',";
			vals2 += "'" + row2.get(key).replace("'", "\\'") + "',";
		}
		keys = keys.substring(0, keys.length()-1) + ")";
		creationKeys = creationKeys.substring(0, creationKeys.length()-1) + ")";
		vals1 = vals1.substring(0, vals1.length()-1) + ")";
		vals2 = vals2.substring(0, vals2.length()-1) + ")";

		creation = "CREATE TABLE " + tablename + " " + creationKeys + " ENGINE=InnoDB CHARSET=utf8;";
		insert1  = "INSERT INTO " + tablename + " " + keys + " VALUES " + vals1 + ";";
		insert2  = "INSERT INTO " + tablename + " " + keys + " VALUES " + vals2 + ";";
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresDatabase() {
		new DatabaseAdapter(null);
	}

	@Test
	public void openingIsForwardedToTheDatabase() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).open();
		}});

		adapter.open();

		context.assertIsSatisfied();
	}

	@Test(expected = RuntimeException.class)
	public void openingErrorsAreForwardedAsRuntimeExceptions() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).open();
				will(throwException(new SQLException()));
		}});

		adapter.open();

		context.assertIsSatisfied();
	}

	@Test
	public void closingIsForwardedToTheDatabase() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).isOpen();
				will(returnValue(true));
			oneOf(db).close();
		}});

		adapter.close();

		context.assertIsSatisfied();
	}

	@Test(expected = IllegalStateException.class)
	public void closingRequiresOpeningFirst() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).isOpen();
				will(returnValue(false));
		}});

		adapter.close();

		context.assertIsSatisfied();
	}

	@Test(expected = RuntimeException.class)
	public void closingErrorsAreWrapped() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).isOpen();
				will(returnValue(true));
			oneOf(db).close();
				will(throwException(new SQLException()));
		}});

		adapter.close();
	}

	@Test
	public void adapterReturnsEmptyDataIfTableDoesNotExist() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).hasTable(tablename);
				will(returnValue(false));
		}});

		assertTrue("Adapter did not return empty data set.",
				adapter.readData(tablename).isEmpty());

		context.assertIsSatisfied();
	}

	@Test
	public void adapterConvertsDataFromResultSet() throws Exception {
		final FakeResultSet fakeSet = new FakeResultSet(serializedData);

		context.checking(new Expectations() {{
			oneOf(db).hasTable(tablename);
				will(returnValue(true));
			oneOf(db).getTableData(tablename);
				will(returnValue(fakeSet));
		}});

		DataAnalyzer analyzer = new DataAnalyzer(adapter.readData(tablename));
		assertEquals("Wrong number of data items returned.", 2, analyzer.size());
		assertTrue("First row of data not returned.", analyzer.contains(row1));
		assertTrue("Second row of data not returned.", analyzer.contains(row2));

		context.assertIsSatisfied();
	}

	@Test(expected = RuntimeException.class)
	public void readErrorsAreWrappedInRuntimeException() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).hasTable(tablename);
				will(throwException(new SQLException()));
		}});

		adapter.readData(tablename);
	}

	@Test
	public void writingTruncatesExistingTable() throws Exception {
		final Sequence order = context.sequence("order");

		context.checking(new Expectations() {{
			oneOf(db).hasTable(tablename); inSequence(order);
				will(returnValue(true));
			oneOf(db).executeCommand(truncation); inSequence(order);
			allowing(db).executeCommand(with(any(String.class))); inSequence(order);
		}});

		adapter.writeData(tablename, serializedData);

		context.assertIsSatisfied();
	}

	@Test
	public void nonExistantTableIsCreated() throws Exception {
		final Sequence order = context.sequence("order");

		context.checking(new Expectations() {{
			oneOf(db).hasTable(tablename); inSequence(order);
				will(returnValue(false));
			oneOf(db).executeCommand(creation); inSequence(order);
			allowing(db).executeCommand(with(any(String.class))); inSequence(order);
		}});

		adapter.writeData(tablename, serializedData);

		context.assertIsSatisfied();
	}

	@Test
	public void correctInsertsAreProduced() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).hasTable(tablename);
				will(returnValue(true));
			oneOf(db).executeCommand(truncation);
			oneOf(db).executeCommand(insert1);
			oneOf(db).executeCommand(insert2);
		}});

		adapter.writeData(tablename, serializedData);

		context.assertIsSatisfied();
	}

	@Test(expected = RuntimeException.class)
	public void writingWrapsErrorsIntoExceptions() throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).hasTable(tablename);
				will(throwException(new SQLException()));
		}});

		adapter.writeData(tablename, serializedData);
	}
}