package de.x8bit.Fantasya.Host.serialization;

import de.x8bit.Fantasya.Host.serialization.complex.ComplexHandler;
import de.x8bit.Fantasya.Host.serialization.postprocess.PostProcessor;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class SerializerTest {
	
	private Mockery context = new Mockery();
	
	private Adapter fakeAdapter = context.mock(Adapter.class);
	private LinkedHashMap<String,ComplexHandler> handlerMap = new LinkedHashMap<String,ComplexHandler>();
	private Map<String,PostProcessor> postProcessorMap = new HashMap<String,PostProcessor>();
	
	private String table1 = "first table";
	private String table2 = "second table";
	private ComplexHandler handler1 = context.mock(ComplexHandler.class, "First handler");
	private ComplexHandler handler2 = context.mock(ComplexHandler.class, "Second handler");
	private PostProcessor processor = context.mock(PostProcessor.class);
	
	private Sequence order = context.sequence("call order");
	private SerializedData item1 = new SerializedData();
	private SerializedData item2 = new SerializedData();
	
	private Serializer serializer;
	
	@Before
	public void setup() {
		// put the complex handlers in the map in the correct order.
		handlerMap.put(table1, handler1);
		handlerMap.put(table2, handler2);

		postProcessorMap.put(table1, processor);
		
		serializer = new Serializer(fakeAdapter, handlerMap, postProcessorMap);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresValidAdapter() {
		new Serializer(null, handlerMap, postProcessorMap);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresValidHandlers() {
		new Serializer(fakeAdapter, null, postProcessorMap);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorRequiresValidPostprocessingMap() {
		new Serializer(fakeAdapter, handlerMap, null);
	}
	
	@Test
	public void loadingWorksCorrectly() {
		context.checking(new Expectations() {{
			oneOf(fakeAdapter).open(); inSequence(order);
			oneOf(fakeAdapter).readData(table1); inSequence(order);
				will(returnValue(item1));
			oneOf(handler1).loadAll(item1); inSequence(order);
			oneOf(processor).process(); inSequence(order);
			oneOf(fakeAdapter).readData(table2); inSequence(order);
				will(returnValue(item2));
			oneOf(handler2).loadAll(item2); inSequence(order);
			oneOf(fakeAdapter).close(); inSequence(order);
		}});
		
		serializer.loadAll();
		context.assertIsSatisfied();
	}

	@Test
	public void postProcessorsAreCalledCorrectly() {

	}
	
	@Test(expected = RuntimeException.class)
	public void adapterMustBeClosedUnderLoadErrors() throws Exception {
		context.checking(new Expectations() {{
			oneOf(fakeAdapter).open(); inSequence(order);
			oneOf(fakeAdapter).readData(table1); inSequence(order);
				will(throwException(new RuntimeException("error")));
			oneOf(fakeAdapter).close(); inSequence(order);
		}});

		try {
			serializer.loadAll();
		} catch (Exception e) {
			context.assertIsSatisfied();
			throw e;
		}
		
		fail("Should never come here.");
	}
	
	@Test
	public void savingWorksInPrinciple() {
		context.checking(new Expectations() {{
			oneOf(fakeAdapter).open(); inSequence(order);
			oneOf(handler1).saveAll(); inSequence(order);
				will(returnValue(item1));
			oneOf(fakeAdapter).writeData(table1, item1); inSequence(order);
			oneOf(handler2).saveAll(); inSequence(order);
				will(returnValue(item2));
			oneOf(fakeAdapter).writeData(table2, item2); inSequence(order);
			oneOf(fakeAdapter).close(); inSequence(order);
		}});
		
		serializer.saveAll();
		context.assertIsSatisfied();
	}
	
	@Test(expected = RuntimeException.class)
	public void adapterMustBeClosedUnderSaveErrors() throws Exception {
		context.checking(new Expectations() {{
			oneOf(fakeAdapter).open(); inSequence(order);
			oneOf(handler1).saveAll(); inSequence(order);
				will(throwException(new RuntimeException()));
			oneOf(fakeAdapter).close(); inSequence(order);
		}});
		
		try {
			serializer.saveAll();
		} catch (Exception e) {
			context.assertIsSatisfied();
			throw e;
		}
		
		fail("Should never come here.");
	}
}