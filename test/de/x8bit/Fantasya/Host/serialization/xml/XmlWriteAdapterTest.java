package de.x8bit.Fantasya.Host.serialization.xml;

import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class XmlWriteAdapterTest {

	private final File output = new File("output.xml");

	private SerializedData data = new SerializedData();
	private XmlWriteAdapter adapter = new XmlWriteAdapter(output.getName());

	@Before
	public void setup() {
		// fill the SerializedData object with some entries.
		for (int i = 0; i < 5; i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("firstKey", String.valueOf(i));
			item.put("secondKey", String.valueOf(i+1));
			data.add(item);
		}
	}

	@After
	public void teardown() {
		output.delete();
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresNonNullFilename() {
		new XmlWriteAdapter(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresNonEmptyFilename() {
		new XmlWriteAdapter("");
	}

	@Test(expected = IllegalStateException.class)
	public void openingFailsIfFileMayNotBeWritten() throws Exception {
		output.createNewFile();
		output.setReadOnly();

		adapter.open();
	}

	@Test
	public void openingWorksIfFileIsNotThereOrWritable() throws Exception {
		output.createNewFile();
		adapter.open();

		output.delete();
		adapter.open();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void readingIsNotAllowed() {
		adapter.open();
		adapter.readData("some table name");
	}

	@Test(expected = IllegalStateException.class)
	public void adapterMustBeOpenedForWriting() {
		adapter.writeData("sometable", new SerializedData());
	}

	@Test(expected = IllegalStateException.class)
	public void adapterMayNotBeClosedForWriting() {
		adapter.open();
		adapter.close();
		adapter.writeData("sometable", new SerializedData());
	}

	@Test
	public void dataIsCorrectlyWrittenOut() throws Exception {
		String emptyTableName = "emptyEmpty";
		String tablename = "whatever";

		// write out everything
		adapter.open();
		adapter.writeData(emptyTableName, new SerializedData());
		adapter.writeData(tablename, data);
		adapter.close();

		// now load everything again via JAXB and compare
		DataAnalyzer analyzer = new DataAnalyzer(data);
		Database db = loadFromFile(output);
		Set<String> tablenames = db.getTableNames();

		assertEquals("Wrong number of table entries.", 2, tablenames.size());
		assertTrue("Wrong tables written out.",
				tablenames.contains(emptyTableName) && tablenames.contains(tablename));

		Table table = db.getTable(emptyTableName);
		assertTrue("Table should be empty.", table.getRows().isEmpty());

		table = db.getTable(tablename);
		assertEquals("Wrong number of rows saved.", analyzer.size(), table.getRows().size());

		List< Map<String,String> > scanned = new ArrayList< Map<String,String> >();
		for (Row row : table.getRows()) {
			Map<String,String> item = new HashMap<String,String>();
			for (Entry entry : row.getEntries()) {
				item.put(entry.getKey(), entry.getValue());
			}

			assertTrue("Table contains invalid entry.", analyzer.contains(item));
			assertFalse("Table contains duplicate entry.", scanned.contains(item));
			scanned.add(item);
		}
	}

	@Test
	public void openingAgainClearsCacheOfThingsToWrite() throws Exception {
		String tablename1 = "an empty table";
		String tablename2 = "another empty table";

		adapter.open();
		adapter.writeData(tablename1, new SerializedData());
		adapter.open();
		adapter.writeData(tablename2, new SerializedData());
		adapter.close();

		// now check that only the second table was written out.
		Database db = loadFromFile(output);
		assertEquals("Only one table should have been written out.",
				1, db.getTableNames().size());
		assertTrue("Incorrect table was written out.",
				db.getTableNames().contains(tablename2));
	}

	private Database loadFromFile(File file) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(
				new Class<?>[] {Database.class, Table.class, Row.class, Entry.class});
		Unmarshaller um = jc.createUnmarshaller();

		return (Database) um.unmarshal(file);
	}
}