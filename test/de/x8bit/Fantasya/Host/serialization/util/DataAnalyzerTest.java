package de.x8bit.Fantasya.Host.serialization.util;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DataAnalyzerTest {

	private SerializedData data = new SerializedData();

	private DataAnalyzer analyzer;

	@Before
	public void setup() {
		analyzer = new DataAnalyzer(data);

		// fill the data object with stuff
		Map<String, String> entry = new HashMap<String, String>();
		entry.put("keyA", "some content");
		entry.put("keyB", "more content");
		data.add(entry);

		entry = new HashMap<String, String>();
		entry.put("keyA", "stuff");
		entry.put("keyB", "other stuff");
		data.add(entry);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorParameterIsRequired() {
		new DataAnalyzer(null);
	}

	@Test
	public void sizeOfDataObjectCanBeQueried() {
		assertEquals("Incorrect size was returned.", 2, analyzer.size());
	}

	@Test
	public void queryForContainedDataItems() {
		Map<String,String> containedItem = null;
		for (Map<String,String> item : data) {
			containedItem = new HashMap<String,String>(item);
		}

		Map<String,String> notContainedItem = new HashMap<String,String>();
		notContainedItem.put("keyA", "random things");
		notContainedItem.put("keyB", "stuff");

		assertTrue("Contained item was not recognized.",
				analyzer.contains(containedItem));
		assertFalse("Not contained item was not recognized.",
				analyzer.contains(notContainedItem));
	}
}