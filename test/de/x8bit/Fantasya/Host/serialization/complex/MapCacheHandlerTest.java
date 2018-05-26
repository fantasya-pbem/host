package de.x8bit.Fantasya.Host.serialization.complex;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Host.serialization.basic.ObjectSerializer;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import de.x8bit.Fantasya.log.FakeAppender;

import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;

import static org.jmock.Expectations.equal;
import static org.jmock.Expectations.returnValue;

import org.jmock.Mockery;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class MapCacheHandlerTest {

	private Mockery context = new Mockery();
	private MapCacheHandler<Atlantis> handler;

	private ObjectSerializer<Atlantis> mockSerializer;
	private Map<Coords, Atlantis> cache = new HashMap<Coords, Atlantis>();

	private Map<String, String> input1 = new HashMap<String, String>();
	private Map<String, String> input2 = new HashMap<String, String>();
	private SerializedData inputData = new SerializedData();

	private Atlantis entry1 = new Atlantis();
	private Atlantis entry2 = new Atlantis();

	// required for mocking
	public static interface TestSerializer extends ObjectSerializer<Atlantis> {}

	@Before
	public void setup() {
		mockSerializer = context.mock(TestSerializer.class);
		handler = new MapCacheHandler<Atlantis>(mockSerializer, cache);

		// create some input
		input1.put("key", "someValue");
		input2.put("key", "anotherValue");
		inputData.add(input1);
		inputData.add(input2);

		// create some data for saving; also this checks that the cache is
		// cleared on loading.
		entry1.setCoords(new Coords(5, 4, 3));
		entry2.setCoords(new Coords(4, 4, 1));
		cache.put(entry1.getCoords(), entry1);
		cache.put(entry2.getCoords(), entry2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresASerializer() {
		new MapCacheHandler<Atlantis>(null, cache);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresACache() {
		new MapCacheHandler<Atlantis>(mockSerializer, null);
	}

	// test loading

	@Test(expected = IllegalArgumentException.class)
	public void loadingRequiresSomeData() {
		handler.loadAll(null);
	}

	@Test
	public void loadingWorksWithEmptyData() {
		handler.loadAll(new SerializedData());
	}

	@Test(expected = IllegalArgumentException.class)
	public void loadingFailsForInvalidKeys() {
		context.checking(new Expectations() {{
			allowing(mockSerializer).isValidKeyset(with(equal(inputData.keySet())));
				will(returnValue(false));
		}});

		handler.loadAll(inputData);
	}

	@Test
	public void loadingWorksProperly() {
		final Atlantis item1 = new Atlantis();
		final Atlantis item2 = new Atlantis();
		item1.setCoords(new Coords(4, 3, 1));
		item2.setCoords(new Coords(7,8,0));

		context.checking(new Expectations() {{
			atLeast(1).of(mockSerializer).isValidKeyset(with(equal(inputData.keySet())));
				will(returnValue(true));
			oneOf(mockSerializer).load(input1);
				will(returnValue(item1));
			oneOf(mockSerializer).load(input2);
				will(returnValue(item2));
		}});

		handler.loadAll(inputData);

		context.assertIsSatisfied();

		assertEquals("Incorrect cache size.", 2, cache.size());
		assertEquals("Incorrect item loaded", item1, cache.get(item1.getCoords()));
		assertEquals("Incorrect item loaded", item2, cache.get(item2.getCoords()));
	}

	@Test
	public void loadingWarnsForTwoRegionsWithIdenticalCoordinates() {
		final Atlantis item = new Atlantis();
		item.setCoords(new Coords(1,2,3));

		context.checking(new Expectations() {{
			allowing(mockSerializer).isValidKeyset(with(equal(inputData.keySet())));
				will(returnValue(true));
			allowing(mockSerializer).load(with(any(Map.class)));
				will(returnValue(item));
		}});

		FakeAppender.reset();
		handler.loadAll(inputData);

		assertTrue("Handler should warn about duplicate regions", FakeAppender.receivedWarningMessage());
	}

	// test saving

	@Test
	public void savingWorksProperly() {
		context.checking(new Expectations() {{
			oneOf(mockSerializer).save(entry1);
				will(returnValue(new SerializedData(input1)));
			oneOf(mockSerializer).save(entry2);
				will(returnValue(new SerializedData(input2)));
		}});

		DataAnalyzer analyzer = new DataAnalyzer(handler.saveAll());

		context.assertIsSatisfied();

		assertEquals("Incorrect size of saved data.", 2, analyzer.size());
		assertTrue("Incorrect saved data.", analyzer.contains(input1));
		assertTrue("Incorrect saved data.", analyzer.contains(input2));
	}

	@Test
	public void savingWarnsOnBadKeys() {
		entry1.setCoords(new Coords(8, 9, 1));
		context.checking(new Expectations() {{
			allowing(mockSerializer).save(with(any(Atlantis.class)));
				will(returnValue(new SerializedData(input1)));
		}});

		FakeAppender.reset();
		handler.saveAll();

		assertTrue("Hanlder should warn on bad keys.", FakeAppender.receivedWarningMessage());
	}
}
