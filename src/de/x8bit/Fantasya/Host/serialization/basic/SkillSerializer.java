package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class loads and saves the skills belonging to some unit.
 */
public class SkillSerializer implements ObjectSerializer<Unit> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private MapCache<Unit> unitCache;

	/** Creates a new serializer.
	 *
	 * @param unitCache   a collection of loaded units.
	 * @throws IllegalArgumentException if the cache is not supplied.
	 */
	public SkillSerializer(MapCache<Unit> unitCache) {
		if (unitCache == null) {
			throw new IllegalArgumentException("Require a valid unit list.");
		}

		this.unitCache = unitCache;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("talent")
				&& keys.contains("nummer")
				&& keys.contains("lerntage"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Unit load(Map<String, String> mapping) {
		// figure out the correct skill class
		Class<? extends Skill> clazz;
		try {
			clazz = (Class<? extends Skill>) Class.forName(
					"de.x8bit.Fantasya.Atlantis.Skills." + mapping.get("talent"));
		} catch (ClassNotFoundException ex) {
			logger.warn("Error loading skill: Invalid type \"{}\"",
					mapping.get("talent"));
			return null;
		}

		// figure out the correct unit
		int id = Integer.decode(mapping.get("nummer"));
		Unit unit = unitCache.get(id);
		if (unit == null) {
			logger.warn("Error loading skill: Unit with id \"{}\" not found.",
					id);
			return null;
		}

		int lerntage = Integer.decode(mapping.get("lerntage"));
		if (lerntage <= 0) {
			logger.warn("Error loading skill {} of unit {}: non-positive values.",
					mapping.get("talent"),
					id);
			return null;
		}

		logger.debug("Loading skill \"{}\" of unit \"{}\" with {} lerntage.",
				mapping.get("talent"),
				id,
				lerntage);
		unit.setSkill(clazz, lerntage);
		return unit;
	}

	@Override
	public SerializedData save(Unit object) {
		SerializedData data = new SerializedData();

		for (Skill skill : object.getSkills()) {
			if (skill.getLerntage() <= 0) {
				// the current code effectively produces a skill object for each
				// skill and attaches it to each unit. Most of them are zero,
				// because the unit simply does not have the skill, so this
				// logging produces too much output currently.
//				logger.warn("Error saving skill \"{}\" of unit \"{}\": Non-positive lerntage.",
//						skill.getClass().getSimpleName(),
//						object.getNummer());
				continue;
			}

			Map<String, String> item = new HashMap<String, String>();

			item.put("nummer", String.valueOf(object.getNummer()));
			item.put("talent", skill.getClass().getSimpleName());
			item.put("lerntage", String.valueOf(skill.getLerntage()));

			data.add(item);
		}

		return data;
	}
}
