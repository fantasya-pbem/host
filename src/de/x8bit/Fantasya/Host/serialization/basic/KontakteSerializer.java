package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Saves and loads all contacts that a given unit has. */

public class KontakteSerializer implements ObjectSerializer<Unit> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private MapCache<Unit> unitCache;

	/** Creates a new serializer.
	 *
	 * @param unitCache   the collection of all existing units.
	 * @throws IllegalArgumentException if the unitCache is null.
	 */
	public KontakteSerializer(MapCache<Unit> unitCache) {
		if (unitCache == null) {
			throw new IllegalArgumentException("Require a unit cache for serialization.");
		}

		this.unitCache = unitCache;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("einheit") && keys.contains("partner"));
	}

	@Override
	public Unit load(Map<String, String> mapping) {
		// now find the unit that has contacts and set the contact
		int id = Integer.decode(mapping.get("einheit"));
		Unit unit = unitCache.get(id);
		if (unit == null) {
			logger.warn("Error loading contacts: Unit \"{}\" not found.",
					id);
			return null;
		}

		// check that the partner unit actually exists
		int partnerId = Integer.decode(mapping.get("partner"));
		if (unitCache.get(partnerId) == null) {
			logger.warn("Error loading contacts: Partner \"{}\" of unit \"{}\" not found.",
					partnerId,
					id);
			return null;
		}

		// set the contact
		logger.debug("Loaded contact \"{}\" of unit \"{}\"",
				partnerId,
				id);
		unit.Kontakte.add(partnerId);
		return unit;
	}

	@Override
	public SerializedData save(Unit object) {
		SerializedData data = new SerializedData();

		for (int id : object.Kontakte) {
			if (unitCache.get(id) == null) {
				logger.warn("Error saving contacts: Partner \"{}\" of unit \"{}\" does not exist.",
						id,
						object.getNummer());
				continue;
			}
			Map<String, String> item = new HashMap<String, String>();

			item.put("einheit", String.valueOf(object.getNummer()));
			item.put("partner", String.valueOf(id));

			data.add(item);
		}

		return data;
	}

}