package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Ships.Langboot;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ShipSerializerTest {

	private Map<String, String> serializedMap = new HashMap<String,String>();

	private Map<Coords, Region> regionMap = new HashMap<Coords,Region>();
	private MapCache<Unit> unitCache = new MapCache<Unit>();
	private Region region = new Ebene();

	private ShipSerializer serializer;

	@Before
	public void setup() {
		Coords coords = new Coords(1,2,3);

		Unit unit = new Elf();
		unit.setNummer(638);
		unitCache.add(unit);

		serializedMap.put("type", "Langboot");
		serializedMap.put("nummer", "15");
		serializedMap.put("name", "aGoodOne");
		serializedMap.put("beschreibung", "This is a ship.");
		serializedMap.put("kapitaen", "1");
		serializedMap.put("koordx", String.valueOf(coords.getX()));
		serializedMap.put("koordy", String.valueOf(coords.getY()));
		serializedMap.put("welt", String.valueOf(coords.getWelt()));
		serializedMap.put("groesse", "99");
		serializedMap.put("fertig", "0");
		serializedMap.put("kueste", Richtung.Nordosten.getShortcut());

		// obsolete field, but required by table
		serializedMap.put("id", " ");

		// also add a region that gets the ship attached.
		region.setCoords(coords);
		regionMap.put(coords, region);

		// and of course set up the serializer to test.
		serializer = new ShipSerializer(regionMap, unitCache);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresRegionMap() {
		new ShipSerializer(null, unitCache);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresUnitCache() {
		new ShipSerializer(regionMap, null);
	}

	@Test
	public void keysetsAreRecognizedProperly() {
		assertTrue("Valid keyset is not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("welt");
		assertFalse("Invalid keyset is not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingPrincipallyWorks() {
		Ship s = serializer.load(serializedMap);

		// the ship has to be attached to the correct region.
		assertTrue("Ship was not added to the region.",
				region.getShips().contains(s));

		// and of course, it has to be loaded correctly.
		assertTrue("Wrong ship type was loaded.",
				s.getClass().equals(Langboot.class));
		assertEquals("Wrong id was assigned.",
				(int)Integer.decode(serializedMap.get("nummer")), s.getNummer());
		assertEquals("Wrong name assigned.",
				serializedMap.get("name"), s.getName());
		assertEquals("Wrong description assigned.",
				serializedMap.get("beschreibung"), s.getBeschreibung());
		assertEquals("Wrong captain assigned.",
				(int)Integer.decode(serializedMap.get("kapitaen")), s.getOwner());
		assertEquals("Wrong x coordinate loaded.",
				(int)Integer.decode(serializedMap.get("koordx")), s.getCoords().getX());
		assertEquals("Wrong y coordinate loaded.",
				(int)Integer.decode(serializedMap.get("koordy")), s.getCoords().getY());
		assertEquals("Wrong world coordinate loaded.",
				(int)Integer.decode(serializedMap.get("welt")), s.getCoords().getWelt());
		assertEquals("Wrong size loaded.",
				(int)Integer.decode(serializedMap.get("groesse")), s.getGroesse());
		assertFalse("Finishing incorrectly loaded.", s.istFertig());
		assertEquals("Wrong coast loaded.",
				Richtung.getRichtung(serializedMap.get("kueste")), s.getKueste());
	}

	@Test
	public void incorrectShipTypeWarnsAndReturnsNull() {
		serializedMap.put("type", "aTypeThatCertainlyDoesNotExist");
		FakeAppender.reset();
		
		assertNull(serializer.load(serializedMap));
		assertTrue(FakeAppender.receivedWarningMessage());
	}

	@Test
	public void invalidRegionWarnsAndReturnsNull() {
		regionMap.remove(region.getCoords());
		FakeAppender.reset();

		assertNull(serializer.load(serializedMap));
		assertTrue(FakeAppender.receivedWarningMessage());
	}

	@Test
	public void invalidCoastYieldsWarningAndNullReturnval() {
		serializedMap.put("kueste", "badDirection");
		FakeAppender.reset();

		assertNull(serializer.load(serializedMap));
		assertTrue(FakeAppender.receivedWarningMessage());
	}

	@Test
	public void fertigIsLoadedProperly() {
		// the "fertig" property is a bit tricky, because it involves a conversion
		// int to boolean. Check in depth.
		serializedMap.put("fertig", "2");
		Ship s = serializer.load(serializedMap);
		assertTrue("Fertig was not properly loaded.", s.istFertig());

		serializedMap.put("fertig", "0");
		s = serializer.load(serializedMap);
		assertFalse("Fertig was not properly loaded.", s.istFertig());
	}

	@Test
	public void kuesteIsLoadedProperly() {
		// in detail, we check that a coast may be null.
		serializedMap.put("kueste", "");
		Ship s = serializer.load(serializedMap);

		assertNull("Empty coast was not loaded properly.", s.getKueste());
	}

	@Test
	public void savingWorksAsExpected() {
		Ship ship = serializer.load(serializedMap);
		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(ship));

		assertEquals("Incorrect size of saved data.",
				1, analyzer.size());
		assertTrue("Expected item not contained in saved data.",
				analyzer.contains(serializedMap));
	}

	@Test
	public void onSavingWithMissingRegionLogWarning() {
		Ship s = serializer.load(serializedMap);
		regionMap.clear();
		FakeAppender.reset();

		serializer.save(s);
		assertTrue(FakeAppender.receivedWarningMessage());
	}

	@Test
	public void onSavingWithMissingOwnerLogWarning() {
		Ship s = serializer.load(serializedMap);
		unitCache.clear();
		FakeAppender.reset();

		serializer.save(s);
		assertTrue(FakeAppender.receivedWarningMessage());
	}
}