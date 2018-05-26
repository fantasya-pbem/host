package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class PropertySerializerTest {

	private Map<String, String> serializedMap = new HashMap<String, String>();
	private Collection<Atlantis> objectList = new ArrayList<Atlantis>();
	private Atlantis object = new Atlantis();
	private PropertySerializer<Atlantis> serializer = new PropertySerializer<Atlantis>(objectList);

	@Before
	public void setup() {
		object.setNummer(42);
		objectList.add(object);

		Atlantis otherEntry = new Atlantis();
		otherEntry.setNummer(80);
		objectList.add(otherEntry);

		serializedMap.put("id", String.valueOf(object.getNummer()));
		serializedMap.put("name", "myproperty");
		serializedMap.put("value", "myvalue");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresNonNullCollection() {
		new PropertySerializer<Atlantis>(null);
	}

	@Test
	public void validKeysetsAreRecognized() {
		assertTrue("Valid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("id");
		assertFalse("invalid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void propertiesAreCorrectlyLoaded() {
		Atlantis result = serializer.load(serializedMap);

		assertEquals("Incorrect object returned on loading.",
				object, result);
		assertEquals("Wrong number of properties set.",
				1, result.getProperties().size());
		assertEquals("Wrong property value set.",
				serializedMap.get("value"), result.getStringProperty(serializedMap.get("name")));
	}

	@Test
	public void onInvalidIdReturnNullAndWarn() {
		objectList.clear();
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
		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(object));

		assertEquals("Incorrect number of properties saved.", 2, analyzer.size());
		assertTrue("Incorrect properties saved.",
				analyzer.contains(serializedMap) && analyzer.contains(anotherMap));
	}
}