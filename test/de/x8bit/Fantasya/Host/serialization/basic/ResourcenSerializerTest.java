package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ResourcenSerializerTest {

	private Map<String,String> serializedMap = new HashMap<String,String>();
	private ResourcenSerializer serializer;

	private Region region = new Ebene();
	private Map<Coords, Region> regionMap = new HashMap<Coords, Region>();

	@Before
	public void setup() {
		Coords coords = new Coords(15,13,1);
		region.setCoords(coords);
		regionMap.put(coords, region);

		serializedMap.put("koordx", String.valueOf(coords.getX()));
		serializedMap.put("koordy", String.valueOf(coords.getY()));
		serializedMap.put("welt", String.valueOf(coords.getWelt()));
		serializedMap.put("resource", "Eisen");
		serializedMap.put("anzahl", "28");

		serializer = new ResourcenSerializer(regionMap);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresValidMap() {
		new ResourcenSerializer(null);
	}

	@Test
	public void keysetsAreProperlyRecognized() {
		assertTrue("Valid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("anzahl");
		assertFalse("Invalid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingWorksInPrinciple() {
		Region r = serializer.load(serializedMap);

		assertEquals("Wrong region was returned.", region, r);

		List<Item> resources = region.getResourcen();
		assertEquals("Wrong number of resources set.", 1, resources.size());
		assertEquals("Wrong resource type set.",
				serializedMap.get("resource"), resources.get(0).getClass().getSimpleName());
		assertEquals("Wrong number of items set",
				Integer.decode(serializedMap.get("anzahl")), (Integer)resources.get(0).getAnzahl());
	}

	@Test
	public void warnAndReturnNullIfRegionNotFound() {
		serializedMap.put("welt", "-3");
		FakeAppender.reset();
		
		assertNull("On error, null should be returned.", serializer.load(serializedMap));
		assertTrue("A warning should be logged on error.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void warnAndReturnNullIfResourceNotFound() {
		serializedMap.put("resource", "certainlyInvalidResource");
		FakeAppender.reset();

		assertNull("On error, null should be returned.", serializer.load(serializedMap));
		assertTrue("A warning should be logged on error.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void savingWorksProperly() {
		Map<String,String> secondMap = new HashMap<String,String>(serializedMap);
		secondMap.put("resource", "Stein");
		secondMap.put("anzahl", "7");

		serializer.load(serializedMap);
		serializer.load(secondMap);

		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(region));

		assertEquals("Incorrect size of saved data.",
				2, analyzer.size());
		assertTrue("Expected item not contained in serialized data.",
				analyzer.contains(serializedMap));
		assertTrue("Expected item not contained in serialized data.",
				analyzer.contains(secondMap));
	}
}
