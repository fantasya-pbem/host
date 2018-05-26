package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParteienPropertySerializerTest {

	private Map<String, String> serializedMap = new HashMap<String, String>();
	private Collection<Partei> parteiList = new ArrayList<Partei>();
	private Partei object = new Partei();
	private ParteienPropertySerializer serializer = new ParteienPropertySerializer(parteiList);

	@Before
	public void setup() {
		object.setNummer(42);
		parteiList.add(object);

		Partei otherEntry = new Partei();
		otherEntry.setNummer(80);
		parteiList.add(otherEntry);

		serializedMap.put("id", String.valueOf(1));
		serializedMap.put("partei", String.valueOf(object.getNummer()));
		serializedMap.put("name", "myproperty");
		serializedMap.put("value", "myvalue");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresNonNullCollection() {
		new ParteienPropertySerializer(null);
	}

	@Test
	public void validKeysetsAreRecognized() {
		assertTrue("Valid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		for (String key : serializedMap.keySet()) {
			Map<String,String> tmp = new HashMap<String,String>(serializedMap);
			tmp.remove(key);
			assertFalse("invalid keyset was not recognized.",
					serializer.isValidKeyset(tmp.keySet()));
		}
	}

	@Test
	public void propertiesAreCorrectlyLoaded() {
		Partei result = serializer.load(serializedMap);

		assertEquals("Incorrect object returned on loading.",
				object, result);
		assertEquals("Wrong number of properties set.",
				1, result.getProperties().size());
		assertEquals("Wrong property value set.",
				serializedMap.get("value"), result.getStringProperty(serializedMap.get("name")));
	}

	@Test
	public void onInvalidIdReturnNullAndWarn() {
		parteiList.clear();
		FakeAppender.reset();

		assertNull("On error, null must be returned.", serializer.load(serializedMap));
		assertTrue("On error, warning must be logged.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void propertiesAreCorrectlySaved() {
		Map<String, String> anotherMap = new HashMap<String, String>(serializedMap);
		anotherMap.put("name", "anotherProperty");
		anotherMap.put("value", "anotherValue");

		serializer.load(serializedMap);
		serializer.load(anotherMap);
		SerializedData data = serializer.save(object);
		DataAnalyzer analyzer = new DataAnalyzer(data);

		assertEquals("Incorrect number of properties saved.", 2, analyzer.size());
		assertTrue("Incorrect properties saved.",
				dataContainsMap(data, serializedMap) && dataContainsMap(data, anotherMap));
	}

	@Test
	public void savedIdsAreUnique() {
		serializer.load(serializedMap);
		Set<Integer> spentIds = new HashSet<Integer>();

		for (int count = 0; count < 100; count++) {
			SerializedData data = serializer.save(object);

			int id = Integer.decode(data.iterator().next().get("id"));
			assertFalse( spentIds.contains(id) );

			spentIds.add(id);
		}
	}

	/** We need a custom comparator, because the id field is more or less random,
	 * and should not be compared.
	 */
	private boolean dataContainsMap(SerializedData data, Map<String,String> input) {
		for (Map<String,String> entry : data) {
			boolean found = true;

			for (String key : input.keySet()) {
				if (!entry.containsKey(key)) {
					found = false;
					break;
				}

				if (!key.equals("id") && !entry.get(key).equals(input.get(key))) {
					found = false;
					break;
				}
			}

			if (found) {
				return true;
			}
		}
		
		return false;
	}
}