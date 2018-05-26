package de.x8bit.Fantasya.Host.serialization.util;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class SerializedDataTest {

	private SerializedData data;
	private SerializedData emptyData = new SerializedData();

	private Map<String, String> validItem = new HashMap<String, String>();
	private Map<String, String> invalidItem = new HashMap<String, String>(validItem);


	@Before
	public void setup() {
		validItem.put("key1", "someValue");
		validItem.put("key2", "someOtherValue");
		data = new SerializedData(validItem);

		invalidItem.put("aCompletelyUnknownKey", "irrelevantValue");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructingWithNullArgumentFails() {
		new SerializedData(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addingANullMapRaisesAnError() {
		data.add((Map<String, String>)null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addingANullDataRaisesAnError() {
		data.add((SerializedData)null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addingAnEmptyElementRaisesAnError() {
		data.add(new HashMap<String, String>());
	}

	@Test
	public void addingAnEmptySerializedDataIsFine() {
		// this would catch the case where an object simply does not give anything
		// serialized, such as Properties.
		data.add(new SerializedData());
	}

	@Test(expected = IllegalArgumentException.class)
	public void itemsMayNotHaveDifferentKeysFromFirstEntry() {
		data.add(invalidItem);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addedDataMustBeCompatibleWithFirstEntry() {
		data.add( new SerializedData(invalidItem) );
	}

	@Test
	public void addedDataIsAddedToInternalDataset() {
		data.add( new SerializedData(validItem) );

		int count = 0;
		for (Map<String,String> item : data) {
			assertEquals("Incorrect data item in dataset.", validItem, item);
			count++;
		}

		assertEquals("Data was not added properly.", 2, count);
	}

	@Test
	public void keysetIsTakenFromArgumentIfEmpty() {
		emptyData.add(data);

		assertEquals("Keyset was not copied.", data.keySet(), emptyData.keySet());
	}

	@Test
	public void isEmptyReturnsWhetherDataIsEmptyOrNot() {
		assertTrue("Empty data set should say so.", emptyData.isEmpty());
		assertFalse("Not-empty data set should not pretend to be empty.", data.isEmpty());
	}

	@Test(expected = IllegalStateException.class)
	public void keySetCannotBeObtainedWithoutData() {
		emptyData.keySet();
	}

	@Test
	public void keySetReturnsViewOfStoredKeys() {
		assertEquals("Key set does not match input.", validItem.keySet(), data.keySet());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void keySetIsNotModifiable() {
		data.keySet().clear();
	}

	@Test
	public void returnedIteratorWorksWithEmptyDataset() {
		assertFalse("Iterator must not loop for empty dataset.",
				emptyData.iterator().hasNext());
	}

	@Test
	public void iteratorAllowsLoopingOverElements() {
		Map<String, String> anotherItem = new HashMap<String, String>(validItem);
		anotherItem.put("key1", "aCompletelyDifferentValue");
		data.add(anotherItem);

		boolean itemFound = false;
		boolean anotherItemFound = false;

		for (Map<String,String> entry : data) {
			if (entry.equals(validItem) && !itemFound) {
				itemFound = true;
			}
			else if (entry.equals(anotherItem) && !anotherItemFound) {
				anotherItemFound = true;
			}
			else {
				throw new IllegalStateException("Invalid entry looped over.");
			}
		}

		assertTrue("Did not loop over all entries.", itemFound && anotherItemFound);
	}

}
