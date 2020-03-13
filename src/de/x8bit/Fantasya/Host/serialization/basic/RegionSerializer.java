package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This serializer loads/saves a basic region.
 * 
 * Advanced stuff like roads etc. are done by separate handlers.
 */

// TODO: Regions could have their type as an enum instead of loading (usually
// pretty dull) classes.

// FIXME: The column "beschreibung" is capitalized in the data tables. I
// capitalized it here right now as well, but this would require a more serious fix.

public class RegionSerializer implements ObjectSerializer<Region> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("typ")
				&& keys.contains("koordx")
				&& keys.contains("koordy")
				&& keys.contains("welt")
				&& keys.contains("name")
				&& keys.contains("Beschreibung")
				&& keys.contains("bauern")
				&& keys.contains("ralter")
				&& keys.contains("entstandenin")
				&& keys.contains("insel")
				&& keys.contains("silber"));
	}

	@Override
	public Region load(Map<String, String> mapping) {
		Region region;
		try {
			region = (Region) Class.forName(
					"de.x8bit.Fantasya.Atlantis.Regions." + mapping.get("typ")).newInstance();
		} catch (Exception ex) {
			logger.warn("Error loading region. Bad region type \"{]\" for region \"{}\"",
					mapping.get("typ"), mapping.get("name"));
			return null;
		}

		region.setName(mapping.get("name"));
		region.setBeschreibung(mapping.get("Beschreibung"));
		region.setCoords(new Coords(
				Integer.decode(mapping.get("koordx")),
				Integer.decode(mapping.get("koordy")),
				Integer.decode(mapping.get("welt"))));
		region.setBauern(Integer.decode(mapping.get("bauern")));
		region.setAlter(Integer.decode(mapping.get("ralter")));
		region.setEnstandenIn(Integer.decode(mapping.get("entstandenin")));
		region.setInselKennung(Integer.decode(mapping.get("insel")));
		region.setSilber(Integer.decode(mapping.get("silber")));

		logger.debug("Loaded region \"{}\" with type \"{}\" at coordinate {}.",
				region.getName(),
				mapping.get("typ"),
				region.getCoords());

		return region;
	}

	@Override
	public SerializedData save(Region region) {
		// Compatibility notes: Several deprecated (and unused) values from the
		// old saving code are not saved here at all:
		// 1. "baeume" (constant 0, unused)
		// 2. "luxus" (unused, stored with the other luxus goods)
		// 3. "lohn" (unused, calculated on the fly)
		Map<String, String> output = new HashMap<String, String>();

		output.put("typ", region.getClass().getSimpleName());
		output.put("name", region.getName());
		output.put("Beschreibung", region.getBeschreibung());
		output.put("koordx", String.valueOf(region.getCoords().getX()));
		output.put("koordy", String.valueOf(region.getCoords().getY()));
		output.put("welt", String.valueOf(region.getCoords().getWelt()));
		output.put("bauern", String.valueOf(region.getBauern()));
		output.put("ralter", String.valueOf(region.getAlter()));
		output.put("entstandenin", String.valueOf(region.getEnstandenIn()));
		output.put("insel", String.valueOf(region.getInselKennung()));
		output.put("silber", String.valueOf(region.getSilber()));

		// FIXME: Column "luxus" is required in the table, but never used.
		output.put("luxus", "none");

		return new SerializedData(output);
	}
}
