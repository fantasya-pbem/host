package de.x8bit.Fantasya.Host.serialization.db;

import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import de.x8bit.Fantasya.Host.util.TestingDatabase;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Test;

/**
 *  Checks that new tables are automatically created by the DatabaseAdapter
 */
public class DatabaseAdapterTableTest {

	private final String tablename = "ArbitraryTestTable";
	private Database database = TestingDatabase.setUpDatabase();


	@After
	public void teardown() throws Exception {
		// remove the table again.
		database.open();
		database.executeCommand("DROP TABLE " + tablename + ";");
		database.close();
	}

	@Test
	public void tablesAreAutomaticallyCreated() throws Exception {
		// create some testing data to save
		Map<String,String> item = new HashMap<String,String>();
		item.put("someKey", "someValue");

		// auto-create a new non-existent table.
		DatabaseAdapter adapter = new DatabaseAdapter(database);
		adapter.open();
		adapter.writeData(tablename, new SerializedData(item));
		adapter.close();

		database.open();
		assertTrue("New table was not automatically created.", database.hasTable(tablename));
		database.close();

		// read out the data again and make sure it was properly written and can be read again.
		adapter.open();
		DataAnalyzer analyzer = new DataAnalyzer(adapter.readData(tablename))	;
		adapter.close();

		assertEquals("Wrong amount of data read out.", 1, analyzer.size());
		assertTrue("Wrong data read out.", analyzer.contains(item));
	}
}
