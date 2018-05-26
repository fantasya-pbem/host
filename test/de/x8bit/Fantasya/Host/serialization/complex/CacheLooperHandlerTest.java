package de.x8bit.Fantasya.Host.serialization.complex;

import de.x8bit.Fantasya.Host.serialization.basic.ObjectSerializer;
import de.x8bit.Fantasya.Host.serialization.util.DataAnalyzer;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CacheLooperHandlerTest {

	private Mockery context = new Mockery();

	private ObjectSerializer<String> mockSerializer;
	private List<String> cache = new ArrayList<String>();

	private ComplexHandler handler;

	private Map<String, String> input1 = new HashMap<String, String>();
	private Map<String, String> input2 = new HashMap<String, String>();
	private SerializedData inputData = new SerializedData();

	// makes JMock work with the generic types
	public static interface TestSerializer extends ObjectSerializer<String> {}

	@Before
	public void setup() {
		mockSerializer = context.mock(TestSerializer.class);
		handler = new CacheLooperHandler<String>(mockSerializer, cache);

		// construct some input data
		input1.put("someKey", "value1");
		input2.put("someKey", "value2");
		inputData.add(input1);
		inputData.add(input2);

		// fill the cache with some data for saving
		cache.add("valueA");
		cache.add("valueB");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorFailsForNullCollection() {
		new CacheLooperHandler<String>(mockSerializer, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorFailsForNullSerializer() {
		new CacheLooperHandler<String>(null, cache);
	}

	@Test(expected = IllegalArgumentException.class)
	public void loadingFailsOnNullData() {
		handler.loadAll(null);
	}
	
	@Test
	public void loadingDoesNothingOnEmptyData() {
		handler.loadAll(new SerializedData());
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwExceptionOnMissingKeys() {
		// make isValidKeyset() return false
		context.checking( new Expectations() {{
			oneOf(mockSerializer).isValidKeyset(with(equal(inputData.keySet())));
					will(returnValue(false));
			}});

		handler.loadAll(inputData);
	}

	@Test
	public void serializerIsCalledProperlyForLoading() {
		final String firstItem = "justSomeItemName";
		final String secondItem = "yetAnotherItemName";

		// isValidKeyset() returns true, serialization returns the raw string
		context.checking( new Expectations() {{
			oneOf(mockSerializer).isValidKeyset(with(equal(inputData.keySet())));
					will(returnValue(true));
			oneOf(mockSerializer).load(with(equal(input1)));
					will(returnValue(firstItem));
			oneOf(mockSerializer).load(with(equal(input2)));
					will(returnValue(secondItem));
		}});

		handler.loadAll(inputData);

		context.assertIsSatisfied();
	}

	@Test
	public void afterSavingCacheContentIsSerialized() {
		context.checking( new Expectations() {{
			oneOf(mockSerializer).save(with(equal(cache.get(0))));
				will(returnValue(new SerializedData(input1)));
			oneOf(mockSerializer).save(with(equal(cache.get(1))));
				will(returnValue(new SerializedData(input2)));
		}});

		DataAnalyzer analyzer = new DataAnalyzer(handler.saveAll());

		assertEquals("Incorrect size of output data.",
				2, analyzer.size());
		assertTrue("First element not contained in output data.",
				analyzer.contains(input1));
		assertTrue("Second element not contained in output data.",
				analyzer.contains(input2));
	}
}