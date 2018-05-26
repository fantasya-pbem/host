package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class KontakteSerializerTest {

	private KontakteSerializer serializer;
	private Map<String,String> serializedMap = new HashMap<String,String>();

	private MapCache<Unit> unitCache = new MapCache<Unit>();
	private Unit unit = new Elf();
	private Unit partner = new Elf();

	@Before
	public void setup() {
		unit.setNummer(1);
		partner.setNummer(2);

		unitCache.add(unit);
		unitCache.add(partner);

		serializedMap.put("einheit", String.valueOf(unit.getNummer()));
		serializedMap.put("partner", String.valueOf(partner.getNummer()));

		serializer = new KontakteSerializer(unitCache);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresUnitCache() {
		new KontakteSerializer(null);
	}

	@Test
	public void keysetAreCheckedForValidity() {
		assertTrue("Valid keyset is not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("einheit");
		assertFalse("Invalid keyset is not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingWorks() {
		Unit u = serializer.load(serializedMap);

		assertEquals("Incorrect unit returned.", unit, u);
		assertEquals("Incorrect number of contacts.",
				1, unit.Kontakte.size());
		assertTrue("Incorrect contact set.",
				unit.Kontakte.contains(partner.getNummer()));
	}

	@Test
	public void warnAndReturnIfUnitNotFound() {
		serializedMap.put("einheit", "11111");
		FakeAppender.reset();

		assertNull( serializer.load(serializedMap) );
		assertTrue( FakeAppender.receivedWarningMessage() );
	}

	@Test
	public void warnAndReturnIfPartnerNotFound() {
		serializedMap.put("partner", "11111");
		FakeAppender.reset();
		
		assertNull( serializer.load(serializedMap) );
		assertTrue( unit.Kontakte.isEmpty() );
		assertTrue( FakeAppender.receivedWarningMessage() );
	}

	@Test
	public void savingWorks() {
		// add a second partner to ensure that looping works.
		Unit secondPartner = new Elf();
		secondPartner.setNummer(5);
		unitCache.add(secondPartner);

		Map<String,String> secondMap = new HashMap<String,String>(serializedMap);
		secondMap.put("partner", String.valueOf(secondPartner.getNummer()));

		// load, save again and check that the original data reappears.
		serializer.load(serializedMap);
		serializer.load(secondMap);
		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(unit));

		assertEquals("Incorrect number of elements saved.",
				2, analyzer.size());
		assertTrue("Incorrect contacts saved.",
				analyzer.contains(serializedMap) && analyzer.contains(secondMap));
	}

	@Test
	public void warnAndSaveNothingIfPartnerDoesNotExist() {
		unit.Kontakte.add(11111);
		FakeAppender.reset();

		assertTrue( serializer.save(unit).isEmpty() );
		assertTrue( FakeAppender.receivedWarningMessage() );
	}
}