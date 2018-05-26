package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.Cache;
import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Units.Elf;

import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class BuildingSerializerTest {

	private Map<String,String> serializedMap = new HashMap<String,String>();

	private HashSet<Coords> coordSet = new HashSet<Coords>();
	private MapCache<Unit> unitCache = new MapCache<Unit>();

	private BuildingSerializer serializer = new BuildingSerializer(coordSet, unitCache);

	@Before
	public void setup() {
		Coords coords = new Coords(15,14,-2);
		coordSet.add(coords);

		// a unit with the id of the owning unit.
		Unit unit = new Elf();
		unit.setNummer(816);
		unitCache.add(unit);

		serializedMap.put("koordx", String.valueOf(coords.getX()));
		serializedMap.put("koordy", String.valueOf(coords.getY()));
		serializedMap.put("welt", String.valueOf(coords.getWelt()));
		serializedMap.put("nummer", "543");
		serializedMap.put("name", "someBuilding");
		serializedMap.put("beschreibung", "descriptionToDo");
		serializedMap.put("type", "Schmiede");
		serializedMap.put("size", "83");
		serializedMap.put("funktion", "1");
		serializedMap.put("owner", String.valueOf(unit.getNummer()));

		// and also populate the dumb, useless columns
		serializedMap.put("monument", " ");
		serializedMap.put("id", " ");
	}

	@Test(expected = IllegalArgumentException.class) 
	public void rejectNullCoordinateSet() {
		new BuildingSerializer(null, unitCache);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectNullUnitList() {
		new BuildingSerializer(coordSet, null);
	}

	@Test
	public void keysetsAreProperlyRecognized() {
		assertTrue("Valid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("owner");
		assertFalse("Invalid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingBasicallyWorks() {
		Coords coords = new Coords(
				Integer.decode(serializedMap.get("koordx")),
				Integer.decode(serializedMap.get("koordy")),
				Integer.decode(serializedMap.get("welt")));
		Building b = serializer.load(serializedMap);

		assertEquals("Wrong coordinates set.",
				coords, b.getCoords());
		assertEquals("Wrong id set",
				(int)Integer.decode(serializedMap.get("nummer")), b.getNummer());
		assertEquals("Wrong building type",
				serializedMap.get("type"), b.getTyp());
		assertEquals("Wrong name set.",
				serializedMap.get("name"), b.getName());
		assertEquals("Wrong description set.",
				serializedMap.get("beschreibung"), b.getBeschreibung());
		assertEquals("Wrong buildings size set.",
				(int)Integer.decode(serializedMap.get("size")), b.getSize());
		assertEquals("Wrong functionality set.",
				true, b.hatFunktion());
		assertEquals("Wrong owner set.",
				(int)Integer.decode(serializedMap.get("owner")), b.getOwner());
	}

	@Test
	public void invalidTypeRaisesWarningAndReturnsNull() {
		serializedMap.put("type", "certainlyNotExisting");
		FakeAppender.reset();

		assertNull("Serializer must return null on error.", serializer.load(serializedMap));
		assertTrue("Serializer must warn on error.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void badCoordinateRaisesWarningAndReturnsNull() {
		serializedMap.put("koordx", "42");
		FakeAppender.reset();

		assertNull("Serializer must return null on error.", serializer.load(serializedMap));
		assertTrue("Serializer must warn on error.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void funktionIsLoadedProperly() {
		// funktion is a boolean, but it is saved as an int, which is sort of
		// difficult...

		serializedMap.put("funktion", "1");
		Building building = serializer.load(serializedMap);
		assertTrue("Funktion not converted properly.", building.hatFunktion());

		serializedMap.put("funktion", "0");
		building = serializer.load(serializedMap);
		assertFalse("Funktion is not converted properly.", building.hatFunktion());
	}

	@Test
	public void savingWorksProperly() {
		Building b = serializer.load(serializedMap);
		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(b));
		FakeAppender.reset();

		assertEquals("Incorrect size of saved data.",
				1, analyzer.size());
		assertTrue("Element not contained in saved data.",
				analyzer.contains(serializedMap));
		assertFalse("When saving, no warning should be raised.",
				FakeAppender.receivedWarningMessage());
	}

	@Test
	public void warnIfOwnerDoesNotExistWhileSaving() {
		FakeAppender.reset();
		Building b = serializer.load(serializedMap);
		unitCache.clear();

		serializer.save(b);
		assertTrue(FakeAppender.receivedWarningMessage());
	}

	@Test
	public void warnIfRegionDoesNotExistWhileSaving() {
		FakeAppender.reset();
		Building b = serializer.load(serializedMap);
		coordSet.clear();

		serializer.save(b);
		assertTrue(FakeAppender.receivedWarningMessage());
	}

	@Test
	public void funktionIsSavedProperly() {
		Building building = serializer.load(serializedMap);

		building.setFunktion(true);
		serializedMap.put("funktion", "1");
		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(building));
		assertTrue("funktion is not saved properly.",
				analyzer.contains(serializedMap));

		building.setFunktion(false);
		serializedMap.put("funktion", "0");
		analyzer = new DataAnalyzer(serializer.save(building));
		assertTrue("funktion is not saved properly.",
				analyzer.contains(serializedMap));
	}
}