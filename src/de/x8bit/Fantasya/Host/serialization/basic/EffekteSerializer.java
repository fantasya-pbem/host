package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Effect;
import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads and saves Effects. */

public class EffekteSerializer implements ObjectSerializer<Effect> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private MapCache<Unit> unitCache;

	/** Constructs a new serializer.
	 *
	 * @param unitCache  a collection of all loaded units.
	 */
	public EffekteSerializer(MapCache<Unit> unitCache) {
		if (unitCache == null) {
			throw new IllegalArgumentException("Serializer needs a valid unit cache.");
		}

		this.unitCache = unitCache;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("id")
				&& keys.contains("name")
				&& keys.contains("einheit"));
	}

	@Override
	public Effect load(Map<String, String> mapping) {
		// load and setup the effect
		Effect effect;
		try {
			effect = (Effect) Class.forName(
					"de.x8bit.Fantasya.Atlantis.Effects."+mapping.get("name")).newInstance();
		} catch (Exception ex) {
			logger.warn("Error loading effect: Bad type \"{}\".",
					mapping.get("name"));
			return null;
		}

		effect.setNummer(Integer.decode(mapping.get("id")));

		// add the effect to the correct unit.
		int unitId = Integer.decode(mapping.get("einheit"));
		Unit unit = unitCache.get(unitId);
		if (unit == null) {
			logger.warn("Error loading effect: Unit \"{}\" not found.",
					unitId);
			return null;
		}

		logger.debug("Loaded effect \"{}\" of type \"{}\" affecting unit \"{}\".",
				mapping.get("id"),
				mapping.get("name"),
				unitId);
		effect.setUnit(unitId);
		unit.addEffect(effect);
		return effect;
	}

	@Override
	public SerializedData save(Effect effect) {
		SerializedData data = new SerializedData();

		// destroyed effects are not saved.
		if (effect.toDestroy()) {
			return data;
		}

		// if the affected unit does not exist, this constitutes an error
		Unit unit = unitCache.get(effect.getUnit());
		if (unit == null) {
			logger.warn("Error saving effect \"{}\" of type \"{}\": Unit \"{}\" does not exist. Not saving.",
					effect.getNummer(),
					effect.getClass().getSimpleName(),
					effect.getUnit());
			return data;
		}

		if (!unit.getEffects().contains(effect)) {
			logger.warn("Warning: Effect \"{}\" of type \"{}\" unknown to unit \"{}\".",
					effect.getNummer(),
					effect.getClass().getSimpleName(),
					effect.getUnit());
		}

		// now save
		Map<String, String> item = new HashMap<String, String>();

		item.put("id", String.valueOf(effect.getNummer()));
		item.put("name", effect.getClass().getSimpleName());
		item.put("einheit", String.valueOf(effect.getUnit()));

		data.add(item);

		return data;
	}

}