package de.x8bit.Fantasya.Host.serialization.integration;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Seide;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Skills.Segeln;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.serialization.Adapter;
import de.x8bit.Fantasya.Host.serialization.Serializer;
import de.x8bit.Fantasya.Host.serialization.SerializerFactory;
import de.x8bit.Fantasya.Host.serialization.xml.XmlReadAdapter;
import de.x8bit.Fantasya.Host.serialization.xml.XmlWriteAdapter;
import de.x8bit.Fantasya.Host.EVA.util.NeuerSpieler;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Test;

public class SerializationIntegrationTest {

	private final String inputFile = this.getClass().getResource("files/gamedata.xml").getFile();
	private final String outputFile = "output.xml";


	@After
	public void tearDown() {
		new File(outputFile).delete();
	}

	@Test
	public void loadData() throws Exception {
		// We load a fixed set of game data from an xml file and check that it
		// is correct. This requires in-depth knowledge of the xml dataset, which
		// is therefore deeply intertwined with the xml file to the test.
		//
		// Note: The tests are not exhaustive, and are not meant to be. We
		// merely test that each item is loaded.

		// 1. Load the data from the xml file.
		Adapter inputAdapter = new XmlReadAdapter(inputFile);
		Serializer serializer = SerializerFactory.buildSerializer(inputAdapter);
		serializer.loadAll();


		// 2. Now check at some points that the data was indeed loaded correctly.
		checkGameIsLoadedCorrectly();
	}

	@Test
	public void saveData() throws Exception {
		// 1. Load the data from the xml file
		Adapter inputAdapter = new XmlReadAdapter(inputFile);
		Serializer serializer = SerializerFactory.buildSerializer(inputAdapter);
		serializer.loadAll();

		// 2. Save it
		Adapter outputAdapter = new XmlWriteAdapter(outputFile);
		serializer = SerializerFactory.buildSerializer(outputAdapter);
		serializer.saveAll();

		// 3. Clean a few lists
		Partei.PROXY.clear();
		Unit.CACHE.clear();

		// 4. Load the saved data again
		inputAdapter = new XmlReadAdapter(inputFile);
		serializer = SerializerFactory.buildSerializer(inputAdapter);
		serializer.loadAll();

		// 5. check that the freshly loaded data is ok.
		checkGameIsLoadedCorrectly();
	}


	/** Does a few checks if the set up game data is indeed the one in the xml file. */
	private void checkGameIsLoadedCorrectly() {
		// There should be two parties in the game with some specific data,
		// plus one with id 0
		assertEquals("Wrong number of players loaded.", 3, Partei.PROXY.size());

		// the following tests are conceptually simpler if we remove the party with
		// id 0, so we do this here.
		for (Partei p : Partei.PROXY) {
			if (p.getNummer() == 0) {
				Partei.PROXY.remove(p);
				break;
			}
		}

		// now the other tests.
		for (Partei p : Partei.PROXY) {
			if (p.getNummer() == 1) {
				assertEquals("Wrong player name", "Partei1", p.getName());
			} else if (p.getNummer() == 2) {
				assertEquals("Wrong player name", "Partei2", p.getName());
			} else {
				fail("Wrong player id.");
			}
		}

		Map<Integer, Integer> partner = new HashMap<Integer,Integer>();
		partner.put(1, 2);
		partner.put(2, 1);

		// They have enabled Gib as Alliance option
		for (Partei p : Partei.PROXY) {
			assertTrue("Allianzoption not set.",
					p.hatAllianz(partner.get(p.getNummer()), AllianzOption.Gib));
		}

		// They have 50% tax rate on each other
		for (Partei p : Partei.PROXY) {
			assertEquals("Wrong tax rate set.",
					50, p.getSteuern(partner.get(p.getNummer())).getRate());
		}

		// They have some property "someProperty" set to "set"
		for (Partei p : Partei.PROXY) {
			assertEquals("Wrong property set.",
					"set", p.getStringProperty("someProperty"));
		}


		// Three regions were loaded: Ebene, Ocean, and mountains
		Coords coordEbene = new Coords(0,0,0);
		Coords coordOzean = new Coords(0,1,0);
		Coords coordBerge = new Coords(1,1,0);

		assertEquals("Wrong number of regions loaded.", 3, Region.CACHE.size());
		assertEquals("Ebene not loaded.",
				"Ebene", Region.CACHE.get(coordEbene).getClass().getSimpleName());
		assertEquals("Ebene not loaded.",
				"Ozean", Region.CACHE.get(coordOzean).getClass().getSimpleName());
		assertEquals("Ebene not loaded.",
				"Berge", Region.CACHE.get(coordBerge).getClass().getSimpleName());

		// Iron can be found in the mountains
		assertEquals("Wrong amount of iron ore found in mountains.",
				100, Region.CACHE.get(coordBerge).getResource(Eisen.class).getAnzahl());

		// there is a street leading to nowhere in particular
		assertEquals("Wrong street construction loaded.",
				50, Region.CACHE.get(coordEbene).getStrassensteine(Richtung.Westen));

		// the flatlanders buy expensive silk.
		assertEquals("Wrong luxus good price.",
				50.0, Region.CACHE.get(coordEbene).getNachfrage(Seide.class).getNachfrage(), 0.01);

		// and region properties are set.
		assertEquals("Wrong property set.",
				50, Region.CACHE.get(coordEbene).getIntegerProperty("elevation"));


		// One fortress with properties
		assertEquals("Wrong number of buildings was loaded.",
				1, Building.PROXY.size());
		assertEquals("Wrong building type loaded.",
				"Burg", Building.PROXY.get(1).getClass().getSimpleName());
		assertEquals("Wrong building property loaded.",
				"ganz hoch", Building.PROXY.get(1).getStringProperty("Mauern"));


		// One ship on the ocean
		assertEquals("Wrong number of ships loaded.",
				1, Ship.PROXY.size());
		assertEquals("Wrong ship loaded.",
				"Ultimate II", Ship.PROXY.get(0).getName());


		// There are two units: party 1 has a sailor, party 2 a miner
		assertEquals("Wrong number of units loaded.",
				2, Unit.CACHE.size());
		assertEquals("Wrong unit data.",
				1, Unit.CACHE.getAll(coordOzean).iterator().next().getOwner());
		assertEquals("Wrong unit data.",
				2, Unit.CACHE.getAll(coordBerge).iterator().next().getOwner());

		Unit sailor = Unit.CACHE.getAll(coordOzean).iterator().next();
		Unit miner = Unit.CACHE.getAll(coordBerge).iterator().next();

		// The miner has an iron bar.
		assertEquals("Wrong number of items set.",
				10, miner.getItem(Eisen.class).getAnzahl());

		// The sailor can sail
		assertEquals("Wrong number of sailing days.",
				450, sailor.getSkill(Segeln.class).getLerntage());

		// and the sailor knows a spell
		assertEquals("Wrong spell loaded.",
				"GuterWind", sailor.getSpells().iterator().next().getClass().getSimpleName());

		// it has a property set
		assertEquals("Wrong property set.",
				"Sunny", sailor.getStringProperty("Forecast"));

		// the sailor has contacted the miner.
		assertTrue("Wrong contacts set.",
				sailor.Kontakte.contains(miner.getNummer()));

		// and is sailing pretty fast
//		assertEquals("Wrong effect set.",
//				"EFXBewegungSail", sailor.getEffects().get(0).getTyp());
//		assertEquals("Wrong effect property.",
//				"fast", sailor.getEffects().get(0).getStringProperty("speed"));


		// The sailor moves to the south.
		// fails because setting a command requires a functioning environment
		//assertEquals("Wrong command set.",
		//		"NACH SW", sailor.Befehle.get(0));

		// I won't even try messages either.

		// A new Echse player is going to be let free
		assertEquals("Wrong new player race.",
				"Echse", NeuerSpieler.PROXY.get(0).getRasse().getSimpleName());
	}
}