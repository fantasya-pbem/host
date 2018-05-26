package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class EinheitenSerializerTest {

	private Map<String,String> serializedMap = new HashMap<String,String>();
	private Set<Coords> regionList = new HashSet<Coords>();
	private Collection<Partei> partyList = new HashSet<Partei>();

	private EinheitenSerializer serializer = new EinheitenSerializer(partyList, regionList);

	@Before
	public void setup() {
		regionList.add(new Coords(1, 2, 3));

		Partei p = new Partei();
		p.setNummer(42);
		partyList.add(p);
		
		// fill the serialized map with valid data
		serializedMap.put("rasse", "Echse");
		serializedMap.put("nummer", "15");
		serializedMap.put("koordx", "1");
		serializedMap.put("koordy", "2");
		serializedMap.put("welt", "3");
		serializedMap.put("name", "unit name");
		serializedMap.put("beschreibung", "unit description");
		serializedMap.put("prefix", "high");
		serializedMap.put("person", "10");
		serializedMap.put("partei", String.valueOf(p.getNummer()));
		serializedMap.put("tarnung_rasse", "Mensch");
		serializedMap.put("tarnung_partei", "37");
		serializedMap.put("gebaeude", "9");
		serializedMap.put("schiff", "28");
		serializedMap.put("sichtbarkeit", "1");
		serializedMap.put("lebenspunkte", "5");
		serializedMap.put("lehrtage", "30");
		serializedMap.put("aura", "15");
		serializedMap.put("mana", "14");
		serializedMap.put("tempnummer", "8");
		serializedMap.put("sortierung", "18");
		serializedMap.put("longorder", "do something");
		serializedMap.put("bewacht", "1");
		serializedMap.put("belagert", "1");
		serializedMap.put("einkommen", "55");
		serializedMap.put("kampfposition", "Nicht");
		serializedMap.put("id", "f");
	}

	@Test(expected = IllegalArgumentException.class)
	public void requireRegionListInConstructor() {
		serializer = new EinheitenSerializer(partyList, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void requireParteiListInConstructor() {
		serializer = new EinheitenSerializer(null, regionList);
	}

	@Test
	public void isValidTestsWhetherKeysetIsValid() {
		assertTrue("Valid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("lebenspunkte");
		assertFalse("Invalid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingWorksProperly() {
		Unit unit = serializer.load(serializedMap);

		assertEquals("Wrong race loaded.",
				serializedMap.get("rasse"), unit.getClass().getSimpleName());
		assertEquals("Wrong number set.",
				(int)Integer.decode(serializedMap.get("nummer")), unit.getNummer());
		assertEquals("Wrong x coordinate set.",
				(int)Integer.decode(serializedMap.get("koordx")), unit.getCoords().getX());
		assertEquals("Wrong y coordinate set.",
				(int)Integer.decode(serializedMap.get("koordy")), unit.getCoords().getY());
		assertEquals("Wrong world coordinate set.",
				(int)Integer.decode(serializedMap.get("welt")), unit.getCoords().getWelt());
		assertEquals("Wrong name set.",
				serializedMap.get("name"), unit.getName());
		assertEquals("Wrong description set.",
				serializedMap.get("beschreibung"), unit.getBeschreibung());
		assertEquals("Wrong prefix set.",
				serializedMap.get("prefix"), unit.getPrefix());
		assertEquals("Wrong number of persons.",
				(int)Integer.decode(serializedMap.get("person")), unit.getPersonen());
		assertEquals("Wrong party set.",
				(int)Integer.decode(serializedMap.get("partei")), unit.getOwner());
		assertEquals("Wrong tarn race set.",
				serializedMap.get("tarnung_rasse"), unit.getTarnRasse());
		assertEquals("Wrong tarn party set.",
				(int)Integer.decode(serializedMap.get("tarnung_partei")), unit.getTarnPartei());
		assertEquals("Wrong building set.",
				(int)Integer.decode(serializedMap.get("gebaeude")), unit.getGebaeude());
		assertEquals("Wrong ship set.",
				(int)Integer.decode(serializedMap.get("schiff")), unit.getSchiff());
		assertEquals("Wrong visibility set.",
				(int)Integer.decode(serializedMap.get("sichtbarkeit")), unit.getSichtbarkeit());
		assertEquals("Wrong number of hit points.",
				(int)Integer.decode(serializedMap.get("lebenspunkte")), unit.getLebenspunkte());
		assertEquals("Wrong number of study days.",
				(int)Integer.decode(serializedMap.get("lehrtage")), unit.getLehrtage());
		assertEquals("Wrong aura set.",
				(int)Integer.decode(serializedMap.get("aura")), unit.getAura());
		assertEquals("Wrong mana set.",
				(int)Integer.decode(serializedMap.get("mana")), unit.getMana());
		assertEquals("Wrong temp number set.",
				(int)Integer.decode(serializedMap.get("tempnummer")), unit.getTempNummer());
		assertEquals("Wrong order set.",
				serializedMap.get("longorder"), unit.getLongOrder());
		assertTrue("Wrong guard status set.", unit.getBewacht());
		assertEquals("Wrong siege status set.",
				(int)Integer.decode(serializedMap.get("belagert")), unit.getBelagert());
		assertEquals("Wrong income set.",
				(int)Integer.decode(serializedMap.get("einkommen")), unit.getEinkommen());
		assertEquals("Wrong fighting position set.",
				serializedMap.get("kampfposition"), unit.getKampfposition().toString());
	}

	@Test
	public void guardingIsLoadedProperly() {
		serializedMap.put("bewacht", "1");
		Unit unit = serializer.load(serializedMap);
		assertTrue("Unit should have guarded flag set.", unit.getBewacht());

		serializedMap.put("bewacht", "0");
		unit = serializer.load(serializedMap);
		assertFalse("Unit should have guarded flag unset.", unit.getBewacht());
	}

	@Test
	public void loadingWarnsAndReturnsNullOnWrongUnitType() {
		serializedMap.put("rasse", "raceCertainlyDoesNotExist");
		FakeAppender.reset();

		assertNull(serializer.load(serializedMap));
		assertTrue(FakeAppender.receivedWarningMessage());
	}

	@Test
	public void loadingWarnsAndReturnsNullOnInvalidRegion() {
		regionList.clear();
		FakeAppender.reset();

		assertNull(serializer.load(serializedMap));
		assertTrue(FakeAppender.receivedWarningMessage());
	}

	@Test
	public void loadingWarnsAndReturnsNullOnInvalidPartei() {
		partyList.clear();
		FakeAppender.reset();

		assertNull(serializer.load(serializedMap));
		assertTrue(FakeAppender.receivedWarningMessage());
	}

	@Test
	public void loadingWarnsAndSetsDefaultOnBadKampfposition() {
		serializedMap.put("kampfposition", "DoesNotExist");
		FakeAppender.reset();

		Unit unit = serializer.load(serializedMap);

		assertEquals(Kampfposition.Vorne, unit.getKampfposition());
		assertTrue(FakeAppender.receivedWarningMessage());
	}

	@Test
	public void savingWorksProperly() {
		Unit unit = serializer.load(serializedMap);
		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(unit));

		assertEquals("Incorrect size of serialized data.", 1, analyzer.size());
		assertTrue("Data not saved properly.", analyzer.contains(serializedMap));
	}
}