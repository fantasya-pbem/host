package de.x8bit.Fantasya.Host.serialization.xml;

import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;


public class XmlIntegrationTest {

	private final String filename = "test.xml";
	private final String tablename = "some table name";
	private final int numEntries = 5;

	@After
	public void tearDown() {
		new File(filename).delete();
	}

	@Test
	public void writeReadTesting() {
		// This test checks that the XmlWriteAdapter and the XmlReadAdapter are
		// compatible with each other. We do this by producing some test data,
		// writing it out to a file, reading it in again and comparing.

		// First, produce the test data.
		SerializedData data = new SerializedData();
		for (int i = 0; i < numEntries; i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("someColumn", String.valueOf(i));
			item.put("anotherColumn", String.valueOf(i+1));
			data.add(item);
		}

		// write it to some file and read it in again
		XmlWriteAdapter writeAdapter = new XmlWriteAdapter(filename);
		writeAdapter.open();
		writeAdapter.writeData(tablename, data);
		writeAdapter.close();

		XmlReadAdapter readAdapter = new XmlReadAdapter(filename);
		readAdapter.open();
		SerializedData newData = readAdapter.readData(tablename);
		readAdapter.close();

		// Now compare. We check the size and that each element is found in the
		// new data.
		DataAnalyzer analyzer = new DataAnalyzer(newData);

		assertEquals("Wrong number of elements in read-in data.",
				numEntries, analyzer.size());
		for (Map<String,String> item : data) {
			assertTrue("Element was not contained in read-in data.",
					analyzer.contains(item));
		}
	}
}