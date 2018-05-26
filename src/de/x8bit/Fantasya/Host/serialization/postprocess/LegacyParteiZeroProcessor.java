package de.x8bit.Fantasya.Host.serialization.postprocess;

import de.x8bit.Fantasya.Atlantis.Partei;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adds a party with id 0 to the party list.
 *
 * Currently, there exists a special party with id "0", which is used for
 * units that hide their affiliation. This party is not guaranteed to be in the
 * table during migration, so this processor makes sure that such a unit is indeed
 * created.
 */

public class LegacyParteiZeroProcessor implements PostProcessor {

	/** The cache of parties; filled during loading. */
	private Collection<Partei> cache;

	/** Creates the processor.
	 *
	 * @param cache  the (global) list of parties
	 * @throws IllegalArgumentException if cache is null.
	 */
	public LegacyParteiZeroProcessor(Collection<Partei> cache) {
		if (cache == null) {
			throw new IllegalArgumentException("Post-processor requires cache of parties.");
		}

		this.cache = cache;
	}

	/** If there is a party with id 0, do nothing, otherwise add it. */
	@Override
	public void process() {
		for (Partei p : cache) {
			if (p.getNummer() == 0) {
				return;
			}
		}

		Logger logger = LoggerFactory.getLogger(this.getClass());
		logger.info("Partei with id 0 not found in DB. Adding manually to the list.");

		// bad style: implicitly adds party to list
		Partei p0 = Partei.Create();
		p0.setNummer(0);
        p0.setEMail("");
		p0.setMonster(1);

		cache.add(p0);
	}

}