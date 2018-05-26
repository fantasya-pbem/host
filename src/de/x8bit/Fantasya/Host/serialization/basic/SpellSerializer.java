package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This serializes/deserializes the spells that the units know. */

// Fixme: For some odd reason, the columns in the tables are capitalized.
// Sometimes.

public class SpellSerializer implements ObjectSerializer<Unit> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private MapCache<Unit> unitCache;

	/** Constructs a new serializer.
	 *
	 * @param unitCache   a collection of all existing units.
	 */
	public SpellSerializer(MapCache<Unit> unitCache) {
		if (unitCache == null) {
			throw new IllegalArgumentException("No unit cache supplied.");
		}

		this.unitCache = unitCache;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("Spruch") && keys.contains("einheit"));
	}

	@Override
	public Unit load(Map<String, String> mapping) {
		// find the correct spell class
		Spell spell = null;
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Spell> clazz = (Class<? extends Spell>) Class.forName(
					"de.x8bit.Fantasya.Atlantis.Spells." + mapping.get("Spruch"));
			spell = clazz.newInstance();
		} catch (Exception ex) {
			logger.warn("Error loading spell: Invalid spell \"{}\"",
					mapping.get("Spruch"));
			return null;
		}

		// load the correct spell
		int id = Integer.decode(mapping.get("einheit"));
		Unit unit = unitCache.get(id);
		if (unit == null) {
			logger.warn("Error loading spell: Unit \"{}\" does not exist.",
					id);
			return null;
		}

		logger.debug("Loading spell \"{}\" for unit \"{}\"",
				mapping.get("Spruch"),
				id);
		unit.setSpell(spell);
		return unit;
	}

	@Override
	public SerializedData save(Unit object) {
		SerializedData data = new SerializedData();
		
		for (Spell spell : object.getSpells()) {
			Map<String, String> item = new HashMap<String, String>();
			
			item.put("einheit", String.valueOf(object.getNummer()));
			item.put("Spruch", spell.getClass().getSimpleName());
			
			data.add(item);
		}

		return data;
	}

}