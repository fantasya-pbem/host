package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Partei;

import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class AllianzSerializerTest {

	Map<String,String> serializedMap = new HashMap<String,String>();
	List<Partei> parteiList = new ArrayList<Partei>();

	Partei owner = new Partei();
	Partei faction = new Partei();

	AllianzSerializer serializer;

	@Before
	public void setup() {
		owner.setNummer(1);
		faction.setNummer(2);

		parteiList.add(owner);
		parteiList.add(faction);
		serializer = new AllianzSerializer(parteiList);

		serializedMap.put("partei", String.valueOf(owner.getNummer()));
		serializedMap.put("partner", String.valueOf(faction.getNummer()));
		serializedMap.put("optionen", "Steuern");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresValidCollection() {
		new AllianzSerializer(null);
	}

	@Test
	public void keysetsAreProperlyRecognized() {
		assertTrue("Correct keyset was not recoginized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("partei");
		assertFalse("Invalid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingWorksInPrinciple() {
		Partei partei = serializer.load(serializedMap);

		assertEquals("Incorrect party returned.", owner, partei);
		assertEquals("Alliance not properly added.", 1, partei.getAllianzen().size());
		assertTrue("Alliance was not added with correct partner.",
				partei.hatAllianz(faction.getNummer()));

		for (AllianzOption ao : AllianzOption.values()) {
			if (ao.equals(AllianzOption.Steuern)) {
				assertTrue("Alliance option was not added.",
						partei.hatAllianz(faction.getNummer(), ao));
			} else {
				assertFalse("Wrong option was added.",
						partei.hatAllianz(faction.getNummer(), ao));
			}
		}
	}

	@Test
	public void onInvalidParteiReturnNullAndWarn() {
		parteiList.remove(owner);
		FakeAppender.reset();

		assertNull("On error, null must be returned.", serializer.load(serializedMap));
		assertTrue("On error, warning must be logged.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void onInvalidPartnerReturnNullAndWarn() {
		parteiList.remove(faction);
		FakeAppender.reset();

		assertNull("On error, null must be returned.", serializer.load(serializedMap));
		assertTrue("On error, warning must be logged.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void onInvalidAllianceOptionWarn() {
		serializedMap.put("optionen", "Alles");
		FakeAppender.reset();

		serializer.load(serializedMap);
		assertTrue("On error, warning must be logged.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void savingWorksProperly() {
		Map<String,String> secondMap = new HashMap<String,String>();
		secondMap.put("partei", String.valueOf(owner.getNummer()));
		secondMap.put("partner", String.valueOf(faction.getNummer()));
		secondMap.put("optionen", "Treiben");

		serializer.load(serializedMap);
		serializer.load(secondMap);

		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(owner));
		
		assertEquals("Incorrect number of alliances saved.",
				2, analyzer.size());
		assertTrue("First element not found in serialized data.",
				analyzer.contains(serializedMap));
		assertTrue("Second element not found in serialized data.",
				analyzer.contains(secondMap));
	}
}