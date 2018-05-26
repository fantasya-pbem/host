package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.Nachfrage;
import de.x8bit.Fantasya.Atlantis.Items.Juwel;
import de.x8bit.Fantasya.Atlantis.Items.Pelz;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.log.FakeAppender;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class LuxusSerializerTest {

	private Map<String,String> serializedMap = new HashMap<String,String>();

	private Map<Coords,Region> regionMap = new HashMap<Coords,Region>();
	private Region region = new Ebene();

	private LuxusSerializer serializer;
	
	@Before
	public void setup() {
		Coords coords= new Coords(15,13,1);
		serializedMap.put("koordx", String.valueOf(coords.getX()));
		serializedMap.put("koordy", String.valueOf(coords.getY()));
		serializedMap.put("welt", String.valueOf(coords.getWelt()));
		serializedMap.put("luxus", "Juwel");
		serializedMap.put("nachfrage", "5");

		region.setCoords(coords);
		regionMap.put(coords, region);

		serializer = new LuxusSerializer(regionMap);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresValidMap() {
		new LuxusSerializer(null);
	}

	@Test
	public void keysetsAreProperlyRecognized() {
		assertTrue("Valid keyset was not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));

		serializedMap.remove("koordx");
		assertFalse("Invalid keyset is not recognized.",
				serializer.isValidKeyset(serializedMap.keySet()));
	}

	@Test
	public void loadingBasicallyWorks() {
		Region r = serializer.load(serializedMap);

		assertEquals("Wrong region returned.", region, r);
		assertEquals("Wrong number of luxus goods.", 1, r.getLuxus().size());

		Nachfrage nf = r.getLuxus().get(0);
		assertEquals("Wrong luxus good loaded.",
				serializedMap.get("luxus"), nf.getItem().getSimpleName());
		assertEquals("Wrong luxus demand loaded.",
				(double)Double.valueOf(serializedMap.get("nachfrage"))/1000, nf.getNachfrage(), 1e-5);
	}

	@Test
	public void loadingFailsAndLogsIfRegionIsNotFound() {
		regionMap.remove(region.getCoords());
		FakeAppender.reset();

		assertNull("On error, null should be returned.", serializer.load(serializedMap));
		assertTrue("On error, a warning should be issued.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void loadingFailsAndLogsOnInvalidLuxusItem() {
		serializedMap.put("luxus", "aCertainlyInvalidClass");
		FakeAppender.reset();

		assertNull("On error, null should be returned.", serializer.load(serializedMap));
		assertTrue("On error, a warning should be issued.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void loadingFailsAndLogsOnNonLuxusItem() {
		serializedMap.put("luxus", "Eisen");
		FakeAppender.reset();

		assertNull("On error, null should be returned.", serializer.load(serializedMap));
		assertTrue("On error, a warning should be issued.", FakeAppender.receivedWarningMessage());
	}

	@Test
	public void savingBasicallyWorks() {
		Map<String,String> secondMap = new HashMap<String,String>(serializedMap);
		secondMap.put("luxus", "Pelz");

		serializer.load(serializedMap);
		serializer.load(secondMap);

		DataAnalyzer analyzer = new DataAnalyzer(serializer.save(region));
		
		assertEquals("Incorrect size of saved data.",
				2, analyzer.size());
		assertTrue("Item not contained in serialized data.",
				analyzer.contains(serializedMap));
		assertTrue("Item not contained in serialized data.",
				analyzer.contains(secondMap));
	}

	@Test
	public void complainIfMultipleLuxusGoodsHaveNegativePrices() {
		Map<String,String> secondMap = new HashMap<String,String>(serializedMap);
		secondMap.put("luxus", "Pelz");

		serializer.load(serializedMap);
		serializer.load(secondMap);

		region.setNachfrage(Juwel.class, -5);
		region.setNachfrage(Pelz.class, -1);

		FakeAppender.reset();
		serializer.save(region);
		assertTrue("Serializer should warn on inconsistent data.",
				FakeAppender.receivedWarningMessage());
	}
}
