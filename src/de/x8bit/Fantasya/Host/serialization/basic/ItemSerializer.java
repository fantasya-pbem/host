package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads an item of a unit and adds it to the corresponding unit.
 *
 * It also serializes all items belonging to a certain unit in the reverse mode.
 */

public class ItemSerializer implements ObjectSerializer<Unit> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private MapCache<Unit> units;

	/** Constructs a new serializer.
	 *
	 * @param units a collection of loaded units to which items can be added.
	 * @throws IllegalArgumentException if the argument is null.
	 */
	public ItemSerializer(MapCache<Unit> units) {
		if (units == null) {
			throw new IllegalArgumentException("Serializer requires non-null unit list.");
		}

		this.units = units;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("item")
				&& keys.contains("nummer")
				&& keys.contains("anzahl"));
	}

	/** Loads a single item from a serialized entry.
	 *
	 * @param mapping the entry describing the item
	 * @throws IllegalArgumentException if the item class or the owning unit
	 * cannot be found.
	 * @return the unit that got the item added.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Unit load(Map<String, String> mapping) {
		
		// Compatibility note: The old serialization code used setItem() to 
		// set the number of items, while here, we use the more logical addItem().
		// This should be irrelevant in the absence of a broken database, though.
		Class<? extends Item> clazz;
		try {
			clazz = (Class<? extends Item>) Class.forName(
					"de.x8bit.Fantasya.Atlantis.Items." + mapping.get("item"));
		} catch (ClassNotFoundException ex) {
			logger.warn("Error loading item: item type \"{}\" is invalid.",
					mapping.get("item"));
			return null;
		}

		int id = Integer.decode(mapping.get("nummer"));
		Unit unit = units.get(id);
		if (unit == null) {
			logger.warn("Error loading item: Unit {} not found", id);
			return null;
		}

		int count = Integer.decode(mapping.get("anzahl"));
		if (count <= 0) {
			logger.warn("Error loading item {} for unit {}: zero count.",
					mapping.get("item"),
					id);
			return null;
		}
		
		unit.addItem(clazz, count);
		logger.debug("Loaded {} items \"{}\" for unit {}.",
				count,
				mapping.get("item"),
				id);

		return unit;
	}

	/** Serializes all items owned by a unit.
	 *
	 * @param object the unit whose items we serialize.
	 * @return the serialized data
	 */
	@Override
	public SerializedData save(Unit object) {
		SerializedData output = new SerializedData();

		for (Item item : object.getItems()) {
			if (item.getAnzahl() <= 0) {
				// does not work: in the code, basically all items are attached
				// to each unit, most of which have amount zero. so this log statement
				// produces too much output.
//				logger.warn("Unit {} contained item {} with zero count; not saving entry.",
//						object.getNummer(),
//						item.getClass().getSimpleName());
				continue;
			}

			Map<String, String> entry = new HashMap<String, String>();

			entry.put("item", item.getClass().getSimpleName());
			entry.put("nummer", String.valueOf(object.getNummer()));
			entry.put("anzahl", String.valueOf(item.getAnzahl()));

			output.add(entry);
		}

		return output;
	}
}
