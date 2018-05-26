package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Spells.Erdbeben;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class SpellSerializerTest {

	private SpellSerializer serializer;
	private Map<String,String> serializedMap = new HashMap<String,String>();

	private MapCache<Unit> unitCache = new MapCache<Unit>();
	private Unit unit;

	@Before
	public void setup() {
		unit = new Elf();
		unit.setNummer(1);
		unitCache.add(unit);

		serializedMap.put("einheit", String.valueOf(unit.getNummer()));
		serializedMap.put("Spruch", "Erdbeben");

		serializer = new SpellSerializer(unitCache);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresUnitList() {
		new SpellSerializer(null);
	}

	@Test
	public void keysetsAreRecognizedProperly() {
		assertTrue("Valid keyset is not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("einheit");
		assertFalse("Invalid keyset is not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingWorksInPrinciple() {
		Unit u = serializer.load(serializedMap);

		assertEquals("Incorrect unit returned.", unit, u);
		assertEquals("Incorrect number of spells.", 1, unit.getSpells().size());
		assertTrue("Incorrect spell set.", unit.getSpells().contains(new Erdbeben()));
	}

	@Test
	public void warnAndReturnOnInvalidSpell() {
		serializedMap.put("Spruch", "ASpellGuaranteedNotToExist");
		FakeAppender.reset();

		assertNull( serializer.load(serializedMap) );
		assertTrue( FakeAppender.receivedWarningMessage() );
	}

	@Test
	public void exceptionOnInvalidUnit() {
		serializedMap.put("einheit", "832");
		FakeAppender.reset();

		assertNull( serializer.load(serializedMap) );
		assertTrue( FakeAppender.receivedWarningMessage() );
	}

	@Test
	public void savingWorksProperly() {
		// load two spells for additional testing.
		Map<String,String> secondMap = new HashMap<String,String>(serializedMap);
		secondMap.put("Spruch", "Feuerball");

		serializer.load(serializedMap);
		serializer.load(secondMap);

		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(unit));

		assertEquals("Incorrect number of spells saved.",
				2, analyzer.size());
		assertTrue("Incorrect spells saved.",
				analyzer.contains(serializedMap) && analyzer.contains(secondMap));
	}
}