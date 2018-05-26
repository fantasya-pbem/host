package de.x8bit.Fantasya.Host.serialization.postprocess;

import de.x8bit.Fantasya.Atlantis.Partei;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;

public class LegacyParteiZeroProcessorTest {

	Collection<Partei> parties = new ArrayList<Partei>();
	LegacyParteiZeroProcessor processor = new LegacyParteiZeroProcessor(parties);

	@Test(expected = IllegalArgumentException.class)
	public void requireListOfPartiesInConstructor() {
		new LegacyParteiZeroProcessor(null);
	}

	@Test
	public void addZeroParteiToCollectionIfNotExistant() {
		processor.process();

		assertEquals("No new party added to list.", 1, parties.size());

		Partei element = parties.iterator().next();

		assertEquals("New party has wrong id.", 0, element.getNummer());
		assertTrue("New party has an email address.", element.getEMail().trim().isEmpty());
		assertEquals("Party does not belong to monsters.", 1, element.getMonster());
	}

	@Test
	public void notAddingZeroPartyIfAlreadyInList() {
		Partei zero = Partei.Create();
		zero.setNummer(0);
		parties.add(zero);

		processor.process();

		assertEquals("New party was added although zero id already exists.", 1, parties.size());
	}
}