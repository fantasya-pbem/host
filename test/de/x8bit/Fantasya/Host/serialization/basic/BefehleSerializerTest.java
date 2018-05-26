package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

// TODO: Not fuly functional due to deep dependencies and assumptions in the
// command setup code.

public class BefehleSerializerTest {

	private Map<String,String> serializedMap = new HashMap<String,String>();

	private Unit unit = new Elf();
	private Collection<Unit> unitCache = new ArrayList<Unit>();

	private BefehleSerializer serializer = new BefehleSerializer(unitCache);

	@Before
	public void setup() {
		unit.setNummer(15);
		unit.setCoords(new Coords(1,1,1));
		unitCache.add(unit);

		serializedMap.put("nummer", String.valueOf(unit.getNummer()));
		serializedMap.put("befehl", "NACH O");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresUnitCache() {
		new BefehleSerializer(null);
	}

	@Test
	public void validKeysetsAreRecognized() {
		assertTrue("Valid keysets are not recognized properly.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("befehl");
		assertFalse("Invalid keyset is not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

// FIXME: Impossible to test, because Commands are rejected without dozens
// of lines of setup or severe hacking.
/*	@Test
	public void loadingWorks() {
		Unit u = serializer.load(serializedMap);

		assertEquals("Incorrect unit returned.",
				unit, u);
		assertTrue("Command was not added to list.",
				unit.Befehle.contains(serializedMap.get("befehl")));
		assertEquals("Command was not added to list.",
				serializedMap.get("befehl"), unit.BefehleExperimental.get(0).getBefehl());
	}
*/

	@Test(expected = IllegalArgumentException.class)
	public void errorOnNonExistingUnit() {
		serializedMap.put("nummer", "100000");
		serializer.load(serializedMap);
	}

	@Test
	public void savingWorks() {
		Map<String,String> secondMap = new HashMap<String,String>(serializedMap);
		secondMap.put("befehl", "NACH S");

		// does not work; do a workaround
		// serializer.load(serializedMap);
		// serializer.load(secondMap);
		unit.Befehle.add(serializedMap.get("befehl"));
		unit.Befehle.add(secondMap.get("befehl"));

		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(unit));

		assertEquals("Incorrect number of commands saved.",
				2, analyzer.size());
		assertTrue("Incorrect command saved.",
				analyzer.contains(serializedMap) && analyzer.contains(secondMap));
	}
}