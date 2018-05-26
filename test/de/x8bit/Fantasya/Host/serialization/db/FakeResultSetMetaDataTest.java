package de.x8bit.Fantasya.Host.serialization.db;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class FakeResultSetMetaDataTest {

	private List<String> columns = new ArrayList<String>();
	private FakeResultSetMetaData metadata;

	@Before
	public void setup() {
		for (int i = 0; i < 5; i++) {
			columns.add("Column " + i);
		}
		metadata = new FakeResultSetMetaData(columns);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresListOfColumns() {
		new FakeResultSetMetaData(null);
	}

	@Test
	public void columnCountCanBeQueried() throws Exception {
		assertEquals("Wrong number of columns.", columns.size(), metadata.getColumnCount());
	}

	@Test
	public void columnNamesCanBeQueriedInOrder() throws Exception {
		for (int i = 0; i < columns.size(); i++) {
			assertEquals("Wrong column name.", columns.get(i), metadata.getColumnName(i+1));
		}
	}
}