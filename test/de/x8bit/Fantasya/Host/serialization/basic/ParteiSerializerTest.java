package de.x8bit.Fantasya.Host.serialization.basic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class ParteiSerializerTest {

	Map<String, String> serializedMap = new HashMap<String, String>();
	ParteiSerializer serializer = new ParteiSerializer();

	@Before
	public void setup() {
		serializedMap.put("age", "5");
		serializedMap.put("name", "someName");
		serializedMap.put("beschreibung", "someDescription");
		serializedMap.put("id", "a");
		serializedMap.put("email", "testmail");
		serializedMap.put("rasse", "Elf");
		serializedMap.put("password", "topsecret");
		serializedMap.put("website", "home");
		serializedMap.put("nmr", "18");
		serializedMap.put("originx", "7");
		serializedMap.put("originy", "9");
		serializedMap.put("cheats", "1");
		serializedMap.put("monster", "3");
		serializedMap.put("steuern", "15");
		serializedMap.put("user_id", "10");
		serializedMap.put("owner_id", "100005");
	}

	@Test
	public void legalSetOfEntriesIsAccepted() {
		assertTrue("A legal set of input values must be accepted as valid.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void setOfKeysIsRejectedIfAnyEntryIsMissing() {
		// leave out any key and check that the keyset becomes invalid.
		for (String keyToRemove : serializedMap.keySet()) {
			Set<String> keysWithHole = new HashSet<String>(serializedMap.keySet());
			keysWithHole.remove(keyToRemove);

			assertFalse("A keyset without " + keyToRemove + " must be refused.",
					serializer.isValidKeyset(keysWithHole));
		}
	}

	@Test
	public void loadingWorksWithLegalInput() {
		Partei partei = serializer.load(serializedMap);

		assertNotNull("Returned partei was null.", partei);
		assertEquals("Age was not set correctly",
				Integer.decode(serializedMap.get("age")).intValue(), partei.getAlter());
		assertEquals("Name was not set.",
				serializedMap.get("name"), partei.getName());
		assertEquals("Description was not set.",
				serializedMap.get("beschreibung"), partei.getBeschreibung());
		assertEquals("Nummer was not properly set.",
				Integer.parseInt(serializedMap.get("id"), 36), partei.getNummer());
		assertEquals("Email was not properly set.",
				serializedMap.get("email"), partei.getEMail());
		assertEquals("Race was not properly set.",
				serializedMap.get("rasse"), partei.getRasse());
		assertEquals("Password was not properly set.",
				serializedMap.get("password"), partei.getPassword());
		assertEquals("Website was not properly set.",
				serializedMap.get("website"), partei.getWebsite());
		assertEquals("NoMoveRound was not properly set.",
				Integer.decode(serializedMap.get("nmr")).intValue(), partei.getNMR());
		assertEquals("X coordinate was not properly set.",
				Integer.decode(serializedMap.get("originx")).intValue(),partei.getUrsprung().getX());
		assertEquals("Y coordinate was not properly set.",
				Integer.decode(serializedMap.get("originy")).intValue(),partei.getUrsprung().getY());
		assertEquals("cheats was not properly set.",
				Integer.decode(serializedMap.get("cheats")).intValue(), partei.getCheats());
		assertEquals("Monster was not properly set.",
				Integer.decode(serializedMap.get("monster")).intValue(), partei.getMonster());
		assertEquals("Steuern was not properly set.",
				Integer.decode(serializedMap.get("steuern")).intValue(), partei.getDefaultsteuer());
		assertEquals("user_id was not set.",
				Integer.decode(serializedMap.get("user_id")).intValue(), partei.getUserId());
		assertEquals("owner_id was not set.",
				Integer.decode(serializedMap.get("owner_id")).intValue(), partei.getOwnerId());
	}

	@Test
	public void onInvalidDataNullIsReturned() {
		serializedMap.put("age", "noNumber");
		assertNull("Invalid object returned.", serializer.load(serializedMap));
	}

	@Test
	public void onInvalidDataWarningIsLogged() {
		FakeAppender.reset();

		serializedMap.put("age", "noNumber");
		serializer.load(serializedMap);

		assertTrue("No warning was logged.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void savingReturnsTheOriginalMapping() {
		Partei p = serializer.load(serializedMap);
		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(p));

		assertEquals("Incorrect size of saved data.",
				1, analyzer.size());
		assertTrue("Expected item not found in serialized data.",
				analyzer.contains(serializedMap));
	}
}
