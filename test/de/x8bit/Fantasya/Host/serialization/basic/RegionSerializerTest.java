package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class RegionSerializerTest {

	private Map<String, String> serializedMap = new HashMap<String,String>();
	private RegionSerializer serializer = new RegionSerializer();

	@Before
	public void setup() {
		serializedMap.put("typ", "Ebene");
		serializedMap.put("name", "someRegion");
		serializedMap.put("Beschreibung", "descriptorNeeded");
		serializedMap.put("koordx", "10");
		serializedMap.put("koordy", "5");
		serializedMap.put("welt", "-3");
		serializedMap.put("bauern", "42");
		serializedMap.put("ralter", "77");
		serializedMap.put("entstandenin", "0");
		serializedMap.put("insel", "87");
		serializedMap.put("silber", "1000000");
		serializedMap.put("luxus", "none");
	}

	@Test
	public void validityOfKeysetsIsProperlyRecognized() {
		assertTrue("Valid keyset is not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("typ");
		assertFalse("Invalid keyset is not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingWorksAsExpected() {
		Region r = serializer.load(serializedMap);

		assertTrue("Wrong region type loaded.",
				r.getClass().equals(Ebene.class));
		assertEquals("Wrong name loaded.",
				serializedMap.get("name"), r.getName());
		assertEquals("Wrong description loaded.",
				serializedMap.get("Beschreibung"), r.getBeschreibung());

		Coords coordinate = new Coords(
				Integer.decode(serializedMap.get("koordx")),
				Integer.decode(serializedMap.get("koordy")),
				Integer.decode(serializedMap.get("welt")));
		assertEquals("Wrong coordinates loaded.",
				coordinate, r.getCoords());

		assertEquals("Wrong number of peasants.",
				Integer.decode(serializedMap.get("bauern")), (Integer)r.getBauern());
		assertEquals("Wrong age loaded.",
				Integer.decode(serializedMap.get("ralter")), (Integer)r.getAlter());
		assertEquals("Wrong creation data loaded.",
				Integer.decode(serializedMap.get("entstandenin")), (Integer)r.getEnstandenIn());
		assertEquals("Wrong age loaded.",
				Integer.decode(serializedMap.get("insel")), (Integer)r.getInselKennung());
		assertEquals("Wrong age loaded.",
				Integer.decode(serializedMap.get("silber")), (Integer)r.getSilber());
	}

	@Test
	public void onInvalidRegionTypeReturnNullAndLog() {
		serializedMap.put("typ", "guaranteedNotToExist");
		FakeAppender.reset();

		assertNull("On bad input, nothing should be returned.", serializer.load(serializedMap));
		assertTrue("On bad input, warning should be logged.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void savingWorksAsExpected() {
		Region region = serializer.load(serializedMap);
		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(region));

		assertEquals("Incorrect size of saved data.",
				1, analyzer.size());
		assertTrue("Expected item not contained in saved data.",
				analyzer.contains(serializedMap));
	}
}
