package de.x8bit.Fantasya.Host.serialization.postprocess;

import de.x8bit.Fantasya.util.UnitIDPool;

/** Cleans the pool of unit id's.
 *
 * Another of those simple processors that should in principle not be required.
 * Here we implement some very simple logic that was found in EVAFastLoader.
 */

public class UnitIDPoolProcessor implements PostProcessor {

	@Override
	public void process() {
		UnitIDPool.getInstance().clear();
	}
}
