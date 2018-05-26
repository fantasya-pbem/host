package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Effect;
import de.x8bit.Fantasya.Atlantis.Effects.EFXBewegungSail;
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

public class EffekteSerializerTest {

	private Map<String,String> serializedMap = new HashMap<String,String>();
	private EffekteSerializer serializer;

	private MapCache<Unit> unitCache = new MapCache<Unit>();
	private Unit unit = new Elf();


	@Before
	public void setup() {
		unit.setNummer(15);
		unitCache.add(unit);

		serializedMap.put("id", "1");
		serializedMap.put("name", "EFXBewegungSail");
		serializedMap.put("einheit", String.valueOf(unit.getNummer()));

		serializer = new EffekteSerializer(unitCache);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresUnitCache() {
		new EffekteSerializer(null);
	}

	@Test
	public void keysetsAreProperlyValidated() {
		assertTrue("Valid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("name");
		assertFalse("Invalid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingPrincipallyWorks() {
		Effect effect = serializer.load(serializedMap);

		assertEquals("Wrong number of effects added to unit.",
				1, unit.getEffects().size());
		assertEquals("Wrong effect added to unit.",
				effect, unit.getEffects().get(0));
		assertTrue("Invalid effect type loaded.",
				effect instanceof EFXBewegungSail);
		assertEquals("Invalid effekt id.",
				(int)Integer.decode(serializedMap.get("id")), effect.getNummer());
	}

	@Test
	public void warnAndReturnOnIllegalEffectType() {
		serializedMap.put("name", "nonExistingEffect");
		FakeAppender.reset();

		assertNull( serializer.load(serializedMap) );
		assertTrue( FakeAppender.receivedWarningMessage() );
	}

	@Test
	public void exceptionOnIllegalUnitIds() {
		serializedMap.put("einheit", "1");
		FakeAppender.reset();

		assertNull( serializer.load(serializedMap) );
		assertTrue( FakeAppender.receivedWarningMessage() );
	}

	@Test
	public void effectsAreSavedProperly() {
		Effect effect = serializer.load(serializedMap);

		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(effect));

		assertEquals("Incorrect number of saved elements.", 1, analyzer.size());
		assertTrue("Incorrect saving of elements.",
				analyzer.contains(serializedMap));
	}

	@Test
	public void destroyedEffectsAreNotSaved() {
		Effect effect = serializer.load(serializedMap);

		effect.destroyIt();
		assertTrue("Destroyed effect was saved.", serializer.save(effect).isEmpty());
	}

	@Test
	public void warnAndSaveNothingOnInvalidUnit() {
		Effect effect = serializer.load(serializedMap);
		unitCache.clear();
		FakeAppender.reset();

		assertTrue( serializer.save(effect).isEmpty() );
		assertTrue( FakeAppender.receivedWarningMessage() );
	}

	@Test
	public void saveButWarnIfUnitDoesNotKnowAboutEffect() {
		Effect effect = serializer.load(serializedMap);
		unit.getEffects().clear();
		FakeAppender.reset();

		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(effect));

		assertEquals("Incorrect number of saved elements.", 1, analyzer.size());
		assertTrue("Incorrect saving of elements.",
				analyzer.contains(serializedMap));

		assertTrue( FakeAppender.receivedWarningMessage() );
	}
}