package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class SkillSerializerTest {

	private SkillSerializer serializer;

	private MapCache<Unit> unitCache = new MapCache<Unit>();
	private Unit unit = new Elf();

	private Map<String,String> serializedMap = new HashMap<String,String>();
	
	@Before
	public void setup() {
		// create the serializer
		serializer = new SkillSerializer(unitCache);
		
		// fill the cache with some data
		unit.setNummer(42);
		unitCache.add(unit);
		
		// fill the serializedMap with some data
		serializedMap.put("talent", "Wahrnehmung");
		serializedMap.put("nummer", String.valueOf(unit.getNummer()));
		serializedMap.put("lerntage", "17");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresUnitCache() {
		new SkillSerializer(null);
	}

	@Test
	public void validityOfKeysetsIsRecognized() {
		assertTrue("Valid keyset is not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("talent");
		assertFalse("Invalid keyset is not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingPrincipallyWorks() {
		Unit u = serializer.load(serializedMap);

		assertEquals("Wrong unit returned.", unit, u);
		assertEquals("Wrong number of skills loaded.",
				1, unit.getSkills().size());
		assertEquals("Wrong knowledge level learned",
				(int)Integer.decode(serializedMap.get("lerntage")),
				unit.getLerntage(Wahrnehmung.class));
	}

	@Test
	public void warnAndReturnOnInvalidSkill() {
		serializedMap.put("talent", "anInvalidSkill");
		FakeAppender.reset();

		assertNull( serializer.load(serializedMap) );
		assertTrue( FakeAppender.receivedWarningMessage() );
	}

	@Test
	public void warnAndReturnOnInvalidUnit() {
		serializedMap.put("nummer", "11111111");
		FakeAppender.reset();
		
		assertNull( serializer.load(serializedMap) );
		assertTrue( FakeAppender.receivedWarningMessage() );
	}

	@Test
	public void warnAndReturnOnBadLerntage() {
		for (int lerntage = -2; lerntage <= 0; lerntage++) {
			serializedMap.put("lerntage", String.valueOf(lerntage));
			FakeAppender.reset();

			assertNull( serializer.load(serializedMap) );
			assertTrue( unit.getSkills().isEmpty() );
			assertTrue( FakeAppender.receivedWarningMessage() );
		}
	}

	@Test
	public void savingWorksCorrectly() {
		// add two skills to the unit, to check that looping works properly.
		Map<String,String> secondMap = new HashMap<String,String>(serializedMap);
		secondMap.put("talent", "Burgenbau");

		serializer.load(serializedMap);
		serializer.load(secondMap);

		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(unit));

		assertEquals("Incorrect number of skills saved.", 2, analyzer.size());
		assertTrue("Not all skills correctly saved.",
				analyzer.contains(serializedMap) && analyzer.contains(secondMap));
	}

	@Test
	public void warnAndReturnOnSavingBadLerntage() {
		for (int lerntage = -2; lerntage <= 0; lerntage++) {
			unit.setSkill(Wahrnehmung.class, lerntage);
			// see the code for an explanation why this is commented out.
//			FakeAppender.reset();

			assertTrue( serializer.save(unit).isEmpty() );
//			assertTrue( FakeAppender.receivedWarningMessage() );
		}
	}
}
