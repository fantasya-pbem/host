package de.x8bit.Fantasya.Host.serialization.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;

public class DatabaseTest {

	private Table table = new Table("someTableName", new ArrayList<Row>());
	private Database database = new Database(Collections.singletonList(table));

	@Test(expected = IllegalArgumentException.class)
	public void errorWhenRequestingInvalidTable() {
		database.getTable("tableCertainlyDoesNotExist");
	}

	@Test
	public void correctTableIsReturnedOnRequest() {
		assertSame("Incorrect table returned.", table, database.getTable(table.getName()));
	}

	@Test
	public void correctListOfTablesIsReturned() {
		Set<String> tableNames = database.getTableNames();

		assertEquals("Wrong number of table names returned.", 1, tableNames.size());
		assertTrue("Wrong table listed.", tableNames.contains(table.getName()));
	}
}