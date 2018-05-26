package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Partei;

import de.x8bit.Fantasya.Atlantis.Steuer;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class SteuerSerializerTest {
	Map<String,String> serializedMap = new HashMap<String,String>();
	List<Partei> parteiList = new ArrayList<Partei>();

	Partei owner = new Partei();
	Partei faction = new Partei();

	SteuerSerializer serializer;

	@Before
	public void setup() {
		owner.setNummer(1);
		faction.setNummer(2);

		parteiList.add(owner);
		parteiList.add(faction);
		serializer = new SteuerSerializer(parteiList);

		serializedMap.put("owner", String.valueOf(owner.getNummer()));
		serializedMap.put("rate", "50");
		serializedMap.put("partei", String.valueOf(faction.getNummer()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullCollectionNotAcceptedInConstructor() {
		new SteuerSerializer(null);
	}

	@Test
	public void keysetsAreProperlyRecognizedAsValid() {
		assertTrue("Valid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("owner");
		assertFalse("Invalid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingWorksInPrinciple() {
		Partei partei = serializer.load(serializedMap);

		assertEquals("Incorrect party returned.", owner, partei);
		assertEquals("Steuern not properly added", 1, partei.getSteuern().size());

		Steuer tax = partei.getSteuern().get(0);
		assertEquals("Invalid base party", partei.getNummer(), tax.getOwner());
		assertEquals("Invalid tax rate.",
				serializedMap.get("rate"), String.valueOf(tax.getRate()));
		assertEquals("Invalid taxed party.", faction.getNummer(), tax.getFaction());
	}

	@Test
	public void onInvalidParteiReturnNullAndLog() {
		parteiList.remove(owner);
		FakeAppender.reset();

		assertNull("On error, null must be returned.", serializer.load(serializedMap));
		assertTrue("On error, warning must be logged.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void onInvalidClientReturnNullAndLog() {
		parteiList.remove(faction);
		FakeAppender.reset();

		assertNull("On error, null must be returned.", serializer.load(serializedMap));
		assertTrue("On error, warning must be logged.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void savingWorksProperly() {
		Partei additionalFaction = new Partei();
		additionalFaction.setNummer(3);
		parteiList.add(additionalFaction);

		Map<String,String> secondMap = new HashMap<String,String>();
		secondMap.put("owner", "1");
		secondMap.put("rate", "50");
		secondMap.put("partei", "3");

		serializer.load(serializedMap);
		serializer.load(secondMap);
		
		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(owner));
		
		assertEquals("Incorrect size of saved data.",
				2, analyzer.size());
		assertTrue("Expected item not found in saved data.",
				analyzer.contains(serializedMap));
		assertTrue("Expected item not found in saved data.",
				analyzer.contains(secondMap));
	}
}