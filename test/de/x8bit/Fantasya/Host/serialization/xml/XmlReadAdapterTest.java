package de.x8bit.Fantasya.Host.serialization.xml;

import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class XmlReadAdapterTest {

	private final String xmlFile = this.getClass().getResource("files/database.xml").getFile();
	private final String brokenFile = this.getClass().getResource("files/broken.xml").getFile();
	private final String inconsistentFile = this.getClass().getResource("files/badDatabase.xml").getFile();

	private Table table;
	private XmlReadAdapter adapter = new XmlReadAdapter(xmlFile);
	private XmlReadAdapter badAdapter = new XmlReadAdapter(inconsistentFile);

	@Before
	public void setup() throws Exception {
		// construct a database and load the first table for comparison.
		JAXBContext jc = JAXBContext.newInstance(
				new Class<?>[] {Database.class, Table.class, Row.class, Entry.class});
		Database database = (Database) jc.createUnmarshaller().unmarshal(new File(xmlFile));
		table = database.getTable(database.getTableNames().iterator().next());
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresFilename() {
		new XmlReadAdapter(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresNonEmptyFilename() {
		new XmlReadAdapter("");
	}

	@Test(expected = IllegalStateException.class)
	public void errorIfFileDoesNotExist() {
		adapter = new XmlReadAdapter("fileShouldNotExist.xml");
		adapter.open();
	}

	@Test(expected = IllegalStateException.class)
	public void errorOnXmlLoading() {
		adapter = new XmlReadAdapter(brokenFile);
		adapter.open();
	}

	@Test(expected = IllegalStateException.class)
	public void adapterMustBeOpenedBeforeTablesCanBeRead() {
		adapter.readData(table.getName());
	}

	@Test(expected = IllegalStateException.class)
	public void adapterMustNotBeClosedWhenTablesAreRead() {
		adapter.open();
		adapter.close();
		adapter.readData(table.getName());
	}

	@Test
	public void openingMultipleTimesAndSoOnWorks() {
		adapter.open();
		adapter.readData(table.getName());
		adapter.close();
		adapter.open();
		adapter.open();
		adapter.readData(table.getName());
	}

	@Test
	public void readingFailsSilentlyForIncorrectTable() {
		adapter.open();
		DataAnalyzer analyzer = new DataAnalyzer(adapter.readData("aTableThatDoesNotExist"));
		assertEquals("Reading of a non-existent table does not give empty data.",
				0, analyzer.size());
	}

	@Test
	public void inconsistentDatabaseIsOpenedWithoutProblems() {
		badAdapter.open();
	}

	@Test(expected = IllegalStateException.class)
	public void errorIfTableIsNotConsistent() {
		badAdapter.open();
		badAdapter.readData(table.getName());
	}

	@Test
	public void tablesAreConvertedIntoSerializedDataObjects() {
		// first, create our serialized data objects, or rather, wrap then in an
		// analyzer
		adapter.open();
		DataAnalyzer analyzer = new DataAnalyzer(adapter.readData(table.getName()));

		// then construct our comparison objects.
		List< Map<String, String> > rows = new ArrayList< Map<String,String> >();
		for (Row row : table.getRows()) {
			Map<String,String> mapping = new HashMap<String,String>();
			for (Entry entry : row.getEntries()) {
				mapping.put(entry.getKey(), entry.getValue());
			}
			rows.add(mapping);
		}

		// finally, check that everything is fine.
		assertEquals("Incorrect number of elements converted.", rows.size(), analyzer.size());

		for (Map<String,String> item : rows) {
			assertTrue("Row not found in converted elements.", analyzer.contains(item));
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void writingIsNotAllowed() {
		adapter.open();
		adapter.writeData("someTable", adapter.readData(table.getName()));
	}
}