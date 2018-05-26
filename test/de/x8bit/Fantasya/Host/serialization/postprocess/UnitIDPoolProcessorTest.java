package de.x8bit.Fantasya.Host.serialization.postprocess;

import de.x8bit.Fantasya.util.UnitIDPool;
import org.junit.Test;
import static org.junit.Assert.*;

public class UnitIDPoolProcessorTest {

	@Test
	public void processorCleardIdPool() {
		UnitIDPool.getInstance().add(1);
		assertFalse(UnitIDPool.getInstance().isEmpty());

		UnitIDPoolProcessor processor = new UnitIDPoolProcessor();
		processor.process();
		assertTrue(UnitIDPool.getInstance().isEmpty());
	}
}
