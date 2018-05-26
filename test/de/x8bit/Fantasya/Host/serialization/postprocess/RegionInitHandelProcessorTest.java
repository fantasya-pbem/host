package de.x8bit.Fantasya.Host.serialization.postprocess;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import java.util.HashMap;
import java.util.Map;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

public class RegionInitHandelProcessorTest {

	@Test(expected = IllegalArgumentException.class)
	public void rejectEmptyRegionMap() {
		new RegionInitHandelProcessor(null);
	}

	@Test
	public void initializeAllRegions() {
		Mockery context = new Mockery() {{
			setImposteriser(ClassImposteriser.INSTANCE);
		}};

		final Region r1 = context.mock(Region.class, "region 1");
		final Region r2 = context.mock(Region.class, "region 2");

		Map<Coords, Region> regionMap = new HashMap<Coords, Region>();
		regionMap.put(new Coords(2,3,1), r1);
		regionMap.put(new Coords(2,2,1), r2);

		context.checking(new Expectations() {{
			oneOf(r1).Init_Handel();
			oneOf(r2).Init_Handel();
		}});

		RegionInitHandelProcessor processor = new RegionInitHandelProcessor(regionMap);
		processor.process();

		context.assertIsSatisfied();
	}
}