package de.x8bit.Fantasya.Host.serialization.db;

import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class FakeResultSetTest {

	private SerializedData data = new SerializedData();
	private FakeResultSet resultSet;

	@Before
	public void setup() {
		for (int i = 0; i < 4; i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("someColumn", "value" + i);
			item.put("anotherColumn", "anotherValue" + i);

			data.add(item);
		}
		resultSet = new FakeResultSet(data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresData() {
		new FakeResultSet(null);
	}

	@Test
	public void resultSetCanBeClosed() throws Exception {
		assertFalse("Fake result set started out as closed.", resultSet.isClosed());
		resultSet.close();
		assertTrue("Fake result set was not marked as closed.", resultSet.isClosed());
	}

	@Test
	public void metaDataIsCorrectlyConstructed() throws Exception {
		ResultSetMetaData metadata = resultSet.getMetaData();
		Set<String> columns = new HashSet<String>(data.keySet());

		// check that everything is ok.
		assertEquals("Wrong number of columns in metadata.",
				columns.size(), metadata.getColumnCount());
		for (int i = 0; i < metadata.getColumnCount(); i++) {
			assertTrue("Column name was not in faked data.",
					columns.contains(metadata.getColumnName(i+1)));
			columns.remove(metadata.getColumnName(i+1));
		}
	}

	@Test(expected = SQLException.class)
	public void exceptionIfMetadataIsRequestedFromClosedResultSet() throws Exception {
		resultSet.close();
		resultSet.getMetaData();
	}

	@Test
	public void internalCounterIsMovedForward() throws Exception {
		for (Map<String, String> unused : data) {
			assertTrue("FakeResultSet does not declare row as valid.", resultSet.next());
		}

		assertFalse("FakeResultSet does not reach end of rows.", resultSet.next());
	}

	@Test(expected = SQLException.class)
	public void counterCannotBeMovedOnClosedResultSets() throws Exception {
		resultSet.close();
		resultSet.next();
	}

	@Test(expected = SQLException.class)
	public void exceptionIfDataRequestedFromClosedResultSet() throws Exception {
		resultSet.next();
		resultSet.close();
		resultSet.getString(1);
	}

	@Test(expected = SQLException.class)
	public void exceptionRaisedIfInvalidRowIsRequested() throws Exception {
		resultSet.getString(1);
	}

	@Test(expected = SQLException.class)
	public void exceptionRaisedWhenQueryingInvalidColumn() throws Exception {
		resultSet.next();
		resultSet.getString(resultSet.getMetaData().getColumnCount()+1);
	}

	@Test
	public void gettingTheDataWorksInPrinciple() throws Exception {
		Iterator<Map<String, String>> iterator = data.iterator();
		while (iterator.hasNext()) {
			resultSet.next();
			Map<String, String> item = iterator.next();

			int i = 0;
			for (String key : data.keySet()) {
				assertEquals("Retrieved data does not match input.",
						item.get(key), resultSet.getString(i+1));
				i++;
			}
		}
	}
}