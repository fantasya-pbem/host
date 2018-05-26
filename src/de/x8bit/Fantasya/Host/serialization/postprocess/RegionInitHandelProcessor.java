package de.x8bit.Fantasya.Host.serialization.postprocess;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import java.util.Map;

/** Processor that initializes regions.
 *
 * To be exact, I am not sure if this processor is needed at all. However, it
 * was included in the old loading code, and I was too lazy to figure out if
 * this was obsolete or not. Basically, it just ensures that all regions know
 * about all luxus items, just before we load all luxus item information anyway.
 */

public class RegionInitHandelProcessor implements PostProcessor {

	/** The map of regions; filled during loading. */
	private Map<Coords, Region> regionMap;

	/** Initializes the post-processor.
	 *
	 * @param regionMap  the map of all regions that we iterate over
	 * @throws IllegalArgumentException if the map is null.
	 */
	public RegionInitHandelProcessor(Map<Coords,Region> regionMap) {
		if (regionMap == null) {
			throw new IllegalArgumentException("Require a valid region map.");
		}

		this.regionMap = regionMap;
	}

	/** {@inheritDocs} */
	@Override
	public void process() {
		for (Region r : regionMap.values()) {
			r.Init_Handel();
		}
	}
}