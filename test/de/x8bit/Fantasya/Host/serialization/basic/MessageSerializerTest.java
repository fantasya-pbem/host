package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Partei;
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

public class MessageSerializerTest {
	
	Map<String,String> serializedMap = new HashMap<String,String>();
	Map<String,String> serializedMapWoRefs;
	
	private Collection<Partei> parteiList = new ArrayList<Partei>();
	private Collection<Coords> coordsList = new ArrayList<Coords>();
	private MapCache<Unit> unitList = new MapCache<Unit>();
	
	Partei partei = new Partei();
	Coords coords = new Coords(1,2,3);
	Unit unit = new Elf();
	
	private MessageSerializer serializer = new MessageSerializer(
			parteiList, coordsList, unitList);
	
	@Before
	public void setup() {
		partei.setNummer(42);
		unit.setNummer(43);
		
		parteiList.add(partei);
		coordsList.add(coords);
		unitList.add(unit);
		
		serializedMap.put("kategorie", "BigError");
		serializedMap.put("text", "message text");
		serializedMap.put("partei", String.valueOf(partei.getNummer()));
		serializedMap.put("koordx", String.valueOf(coords.getX()));
		serializedMap.put("koordy", String.valueOf(coords.getY()));
		serializedMap.put("welt", String.valueOf(coords.getWelt()));
		serializedMap.put("einheit", String.valueOf(unit.getNummer()));
		
		// make another map without references to units etc.
		serializedMapWoRefs = new HashMap<String,String>(serializedMap);
		serializedMapWoRefs.put("partei", "0");
		serializedMapWoRefs.put("koordx", "0");
		serializedMapWoRefs.put("koordy", "0");
		serializedMapWoRefs.put("welt", "0");
		serializedMapWoRefs.put("einheit", "0");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresValidParteiList() {
		new MessageSerializer(null, coordsList, unitList);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresValidRegionList() {
		new MessageSerializer(parteiList, null, unitList);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresValidUnitList() {
		new MessageSerializer(parteiList, coordsList, null);
	}
	
	@Test
	public void keysetValidityIsRecognized() {
		assertTrue("Valid keysets are recognized as valid.",
				serializer.isValidKeyset(serializedMap.keySet()));
		
		serializedMap.remove("text");
		assertFalse("Invalid keysets are not recognized as invalid.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}
	
	@Test
	public void loadingWorksProperly() {
		Message msg = serializer.load(serializedMap);
		
		assertTrue("Incorrect class loaded.", msg instanceof BigError);
		assertEquals("Incorrect text set.", serializedMap.get("text"), msg.getText());
		assertEquals("Incorrect party set.", partei, msg.getPartei());
		assertEquals("Incorrect coordinates set.", coords, msg.getCoords());
		assertEquals("Incorrect unit set.", unit, msg.getUnit());
	}
	
	@Test
	public void loadingWorksWithoutParteiCoordsAndUnit() {
		Message msg = serializer.load(serializedMapWoRefs);
		
		assertNull("Partei set although not requested.", msg.getPartei());
		assertNull("Coordinates set although not requested.", msg.getCoords());
		assertNull("Unit set although not requested.", msg.getUnit());
	}
	
	@Test
	public void idIsAlwaysSetAndIncreasing() {
		int maxId = 0;
		
		for (int i = 0; i < 1000; i++) {
			Message msg = serializer.load(serializedMap);
			
			assertTrue("Ids are not strictly monotonically increasing.", msg.getEvaId() > maxId);
			maxId = msg.getEvaId();
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionOnInvalidMessageType() {
		serializedMap.put("kategorie", "NotExistantMessageType");
		serializer.load(serializedMap);
	}
	
/*	@Test(expected = IllegalArgumentException.class)
	public void exceptionOnNonExistantPartei() {
		serializedMap.put("partei", "100000");
		serializer.load(serializedMap);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionOnInvalidCoordinate() {
		serializedMap.put("koordx", "78");
		serializer.load(serializedMap);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionOnInvalidUnit() {
		serializedMap.put("einheit", "78");
		serializer.load(serializedMap);
	}
*/
	@Test
	public void savingWithReferencesWorks() {
		Message msg = serializer.load(serializedMap);
		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(msg));
		
		assertEquals("Wrong number of saved items.", 1, analyzer.size());
		assertTrue("Wrong saved item.", analyzer.contains(serializedMap));
	}
	
	@Test
	public void savingWithoutReferencesWorks() {
		Message msg = serializer.load(serializedMapWoRefs);
		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(msg));
		
		assertEquals("Wrong number of saved items.", 1, analyzer.size());
		assertTrue("Wrong saved item.", analyzer.contains(serializedMapWoRefs));
	}
}