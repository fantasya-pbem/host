package de.x8bit.Fantasya.Host.db;

import de.x8bit.Fantasya.Host.serialization.complex.ComplexHandler;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DatabaseHandlerTest {

	Mockery context = new Mockery();

	DatabaseHandler dbHandler;

	ComplexHandler complexHandler;
	String tableName = "someTableName";
	FakeResultSetConverter converter = new FakeResultSetConverter();

	SerializedData data = new SerializedData();
	Map<String, String> item1 = new HashMap<String, String>();
	Map<String, String> item2 = new HashMap<String, String>();

	@Before
	public void setup() {
		complexHandler = context.mock(ComplexHandler.class);
		dbHandler = new DatabaseHandler(complexHandler, tableName, converter);

		// fill the data with some values.
		item1.put("someKey", "someValue");
		item2.put("someKey", "someValue");
		data.add(item1);
		data.add(item2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void handlerMustNotBeNull() {
		new DatabaseHandler(null, "tableName", converter);
	}

	@Test(expected = IllegalArgumentException.class)
	public void tableNameMustNotBeNull() {
		new DatabaseHandler(complexHandler, null, converter);
	}

	@Test(expected = IllegalArgumentException.class)
	public void tableNameMustNotBeEmpty() {
		new DatabaseHandler(complexHandler, "", converter);
	}

	@Test(expected = IllegalArgumentException.class)
	public void converterMustNotBeEmpty() {
		new DatabaseHandler(complexHandler, "tableName", null);
	}

	@Test
	public void loadingWorksProperly() throws Exception {
		final DatabaseReader reader = context.mock(DatabaseReader.class);
		final ResultSet rs = context.mock(ResultSet.class);

		// make sure that the reader is queried for the result set with the
		// correct table name, the fake converter returns the correct serialized
		// data, and the handler is called with this data. Finally, the result
		// set must also be cleaned up.
		converter.data = data;
		context.checking(new Expectations() {{
			try {
				oneOf(reader).selectAll(with(equal(tableName)));
					will(returnValue(rs));
				oneOf(reader).cleanup();
				oneOf(complexHandler).loadAll(data);
			} catch (Exception e) {
				fail();
			}
		}});

		// load from the "database"
		dbHandler.loadFromDB(reader);

		// check that everything was called according to plan.
		context.assertIsSatisfied();
		assertEquals("ResultSet was not properly supplied to the converter.",
				rs, converter.resultSet);
	}

	@Test
	public void savingWorksProperly() throws Exception {
		final Sequence writing = context.sequence("writing order");
		final DatabaseWriter writer = context.mock(DatabaseWriter.class);

		context.checking( new Expectations() {{
			// 1. The complex handler returns a serialized data object
			oneOf(complexHandler).saveAll();
				will(returnValue(data));

			// 2. The table is truncated (first), then the data is written out,
			//    then the table is updated.
			oneOf(writer).truncate(with(equal(tableName)));	inSequence(writing);
			oneOf(writer).insert(tableName, item1); inSequence(writing);
			oneOf(writer).insert(tableName, item2); inSequence(writing);
			oneOf(writer).update(); inSequence(writing);
		}});

		dbHandler.saveToDB(writer);

		context.assertIsSatisfied();
	}
}
