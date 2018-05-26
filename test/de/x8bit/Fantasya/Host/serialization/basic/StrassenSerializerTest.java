package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Richtung;

import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class StrassenSerializerTest {

	private Map<String,String> serializedMap = new HashMap<String,String>();

	private Map<Coords,Region> regionMap = new HashMap<Coords,Region>();
	private Region region;

	private StrassenSerializer serializer;

	@Before
	public void setup() {
		Coords coords = new Coords(15, 22, 1);
		serializedMap.put("koordx", String.valueOf(coords.getX()));
		serializedMap.put("koordy", String.valueOf(coords.getY()));
		serializedMap.put("welt", String.valueOf(coords.getWelt()));
		serializedMap.put("richtung", "NO");
		serializedMap.put("anzahl", "42");

		region = new Ebene();
		region.setCoords(coords);
		regionMap.put(coords, region);

		serializer = new StrassenSerializer(regionMap);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresValidMap() {
		new StrassenSerializer(null);
	}

	@Test
	public void isValidKaysetWorksProperly() {
		assertTrue("Valid keyset not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("anzahl");
		assertFalse("Invalid keyset not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingBasicallyWorks() {
		Region r = serializer.load(serializedMap);
		Richtung streetDir = Richtung.getRichtung(serializedMap.get("richtung"));

		assertEquals("Wrong region returned.", region, r);
		for (Richtung dir : Richtung.values()) {
			if (dir == streetDir) {
				assertEquals("Wrong number of stones set.",
						(int)Integer.decode(serializedMap.get("anzahl")), r.getStrassensteine(dir));
			} else {
				assertEquals("Wrong direction for street set.",
						0, r.getStrassensteine(dir));
			}
		}
	}

	@Test
	public void returnNullAndWarnIfRegionNotFound() {
		regionMap.clear();
		FakeAppender.reset();

		assertNull("When region is not found, nothing should be returned.", serializer.load(serializedMap));
		assertTrue("Warning should have been issued.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void returnNullAndWarnOnInvalidDirection() {
		serializedMap.put("richtung", "badbadbad");
		FakeAppender.reset();

		assertNull("On bad direction data, nothing should be returned.", serializer.load(serializedMap));
		assertTrue("Warning should have been issued.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void fixAndWarnOnNegativeNumberOfStones() {
		serializedMap.put("anzahl", "-1");
		FakeAppender.reset();

		Region r = serializer.load(serializedMap);
		assertEquals("Stones should be fixed to zero.", 0, r.getStrassensteine(Richtung.Nordosten));
		assertTrue("Warning should have been issued.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void returnNullAndWarnOnTooManyStones() {
		serializedMap.put("anzahl", String.valueOf(region.getSteineFuerStrasse()+1));
		FakeAppender.reset();

		Region r = serializer.load(serializedMap);
		assertEquals("Stones should be fixed to maximum.",
				r.getSteineFuerStrasse(),
				r.getStrassensteine(Richtung.Nordosten));
		assertTrue("Warning should have been issued.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void savingWorksProperly() {
		Map<String,String> secondMap = new HashMap<String,String>(serializedMap);
		secondMap.put("richtung", "SW");

		serializer.load(serializedMap);
		serializer.load(secondMap);

		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(region));

		assertEquals("Incorrect size of saved data.",
				2, analyzer.size());
		assertTrue("Expected item not found in saved data.",
				analyzer.contains(serializedMap));
		assertTrue("Expected item not found in saved data.",
				analyzer.contains(secondMap));
	}

	// this test could be easily made invalid by proper checking in Region, but
	// we do not do this for now...
	@Test
	public void fixAndWarnIfTooManyStonesAreUsed() {
		region.setStrassensteine(Richtung.Nordosten, region.getSteineFuerStrasse()+1);
		serializedMap.put("anzahl", String.valueOf(region.getSteineFuerStrasse()));
		FakeAppender.reset();

		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(region));
		assertTrue("On bad number of stones, do an auto-fix.", analyzer.contains(serializedMap));
		assertTrue("Warning should be issued.", FakeAppender.receivedWarningMessage());
	}
}
