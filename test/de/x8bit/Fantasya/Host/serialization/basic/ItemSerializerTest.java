package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ItemSerializerTest {

	private Map<String,String> serializedMap = new HashMap<String,String>();

	private Unit unit = new Elf();
	private MapCache<Unit> unitCache = new MapCache<Unit>();

	private ItemSerializer serializer = new ItemSerializer(unitCache);

	@Before
	public void setup() {
		unit.setNummer(1);
		unitCache.add(unit);

		serializedMap.put("item", "Schwert");
		serializedMap.put("nummer", String.valueOf(unit.getNummer()));
		serializedMap.put("anzahl", "25");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresNonNullArgument() {
		new ItemSerializer(null);
	}

	@Test
	public void keysetCanBeTestedForValidity() {
		assertTrue("Valid keyset is not correctly detected.",
				serializer.isValidKeyset(serializedMap.keySet()));
		serializedMap.remove("item");
		assertFalse("Invalid keyset is not correctly detected.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingWorksInPrinciple() {
		Unit loaded = serializer.load(serializedMap);

		assertEquals("Incorrect unit returned.", unit, loaded);

		assertEquals("Wrong number of item types loaded.", 1, loaded.getItems().size());
		assertNotNull("Wrong item type loaded.", loaded.getItem(Schwert.class));
		assertEquals("Wrong number of items loaded.",
				(int)Integer.decode(serializedMap.get("anzahl")),
				loaded.getItem(Schwert.class).getAnzahl());
	}

	@Test
	public void warnAndDoNothingIfUnitIsNotFound() {
		serializedMap.put("nummer", "1111111111");
		FakeAppender.reset();
		
		assertNull( serializer.load(serializedMap) );
		assertTrue( FakeAppender.receivedWarningMessage() );
		assertTrue( unit.getItems().isEmpty() );
	}

	@Test
	public void warnAndDoNothingOnInvalidItemtype() {
		serializedMap.put("item", "nonExistentItem");
		FakeAppender.reset();

		assertNull( serializer.load(serializedMap) );
		assertTrue( FakeAppender.receivedWarningMessage() );
	}

	@Test
	public void warnAndDoNothingForItemsizeZeroOrLess() {
		for (int count = -2; count <= 0; count++) {
			serializedMap.put("anzahl", String.valueOf(count));
			FakeAppender.reset();

			assertNull( serializer.load(serializedMap) );
			assertTrue( FakeAppender.receivedWarningMessage() );
		}
	}

	@Test
	public void unitCanBeSerialized() {
		Map<String,String> secondItem = new HashMap<String,String>(serializedMap);
		secondItem.put("item", "Holz");
		secondItem.put("anzahl", "1");

		serializer.load(serializedMap);
		serializer.load(secondItem);

		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(unit));
		
		assertEquals("Incorrect size of serialized data.",
				2, analyzer.size());
		assertTrue("Item is not contained in saved data.",
				analyzer.contains(serializedMap));
		assertTrue("Item is not contained in saved data.",
				analyzer.contains(secondItem));
	}

	@Test
	public void warnAndSaveNothingOnItemCountZeroOrLess() {
		for (int count = -2; count <= 0; count++) {
			unit.addItem(Schwert.class, count);
			// see the code for an explanation why this is commented out.
//			FakeAppender.reset();
//
			assertTrue( serializer.save(unit).isEmpty() );
//			assertTrue( FakeAppender.receivedWarningMessage() );
		}
	}
}