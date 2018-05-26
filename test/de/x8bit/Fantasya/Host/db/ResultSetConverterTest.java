package de.x8bit.Fantasya.Host.db;


import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ResultSetConverterTest {

	private Mockery context = new Mockery();
	private ResultSetConverter converter = new ResultSetConverter();

	private List<String> columns = new ArrayList<String>();
	private List<String> values1 = new ArrayList<String>();
	private List<String> values2 = new ArrayList<String>();

	@Before
	public void setup() {
		// define the "columns" in our table
		columns.add("id");
		columns.add("value");

		// and fill two sets of values
		values1.add("someId");
		values1.add("someValue");

		values2.add("anotherId");
		values2.add("anotherValue");
	}

	@Test(expected = IllegalArgumentException.class)
	public void errorIfResultSetToConvertIsNull() throws Exception {
		converter.convert(null);
	}

	@Test
	public void conversionBasicallyWorks() throws Exception {
		// we need a few mock classes
		final String uninitialized = "not initialized";
		final String firstRow = "first";
		final String secondRow = "second";
		final String after = "after the end";

		final ResultSet rs = context.mock(ResultSet.class);
		final ResultSetMetaData meta = context.mock(ResultSetMetaData.class);
		final States rowState = context.states("rowState").startsAs(uninitialized);

		// Now, we need to define two things:
		// 1. The behaviour of our result set.
		//    It should have two columns and two rows, and return the correct values.
		// 2. The expected behaviour of the converter.
		//    It should query both "rows", and get the values for each cell.
		context.checking( new Expectations() {{
			try{
				// The meta data returns the column names.
				atLeast(1).of(meta).getColumnCount();
					will(returnValue(2));
				atLeast(1).of(meta).getColumnLabel(0);
					will(returnValue(columns.get(0)));
				atLeast(1).of(meta).getColumnLabel(1);
					will(returnValue(columns.get(1)));

				// The result set uses a state machine to determine whether the
				// first or second row is meant.
				
				// a call to next() advances the cursor by one row
				oneOf(rs).next();
					will(returnValue(true));
					when(rowState.is(uninitialized));
					then(rowState.is(firstRow));
				oneOf(rs).next();
					will(returnValue(true));
					when(rowState.is(firstRow));
					then(rowState.is(secondRow));
				oneOf(rs).next();
					will(returnValue(false));
					when(rowState.is(secondRow));
					then(rowState.is(after));

				// a call to getMetaData() returns the meta data
				atLeast(1).of(rs).getMetaData();
					will(returnValue(meta));

				// calls to getString() return the appropriate element
				atLeast(1).of(rs).getString(with(equal(columns.get(0))));
					will(returnValue(values1.get(0)));
					when(rowState.is(firstRow));
				atLeast(1).of(rs).getString(with(equal(columns.get(1))));
					will(returnValue(values1.get(1)));
					when(rowState.is(firstRow));
				atLeast(1).of(rs).getString(with(equal(columns.get(0))));
					will(returnValue(values2.get(0)));
					when(rowState.is(secondRow));
				atLeast(1).of(rs).getString(with(equal(columns.get(1))));
					will(returnValue(values2.get(1)));
					when(rowState.is(secondRow));
			} catch (Exception e) {
				fail();
			}
		}});

		// With the behaviour now defined, we can try to convert our fake
		// result set, and see if the converter called all the functions.
		SerializedData output = converter.convert(rs);

		context.assertIsSatisfied();

		// Finally, we check that the output is what we expect

		boolean firstValueFound = false;
		boolean secondValueFound = false;

		for (Map<String,String> entry : output) {
			assertEquals("Wrong key size in output.", 2, entry.keySet().size());
			assertTrue("Keys are not contained in output.",
					entry.keySet().contains(columns.get(0)) && entry.keySet().contains(columns.get(1)));

			if (!firstValueFound
					&& entry.get(columns.get(0)).equals(values1.get(0))
					&& entry.get(columns.get(1)).equals(values1.get(1))) {
				firstValueFound = true;
			}
			else if (!secondValueFound
					&& entry.get(columns.get(0)).equals(values2.get(0))
					&& entry.get(columns.get(1)).equals(values2.get(1))) {
				secondValueFound = true;
			}
			else {
				fail("Invalid data item in output.");
			}
		}

		assertTrue("Output did not contain input elements.",
				firstValueFound && secondValueFound);
	}
}