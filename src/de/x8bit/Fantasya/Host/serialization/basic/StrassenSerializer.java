package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Serializer to load and save streets in a region. */

public class StrassenSerializer implements ObjectSerializer<Region> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Map<Coords, Region> map;

	/** Constructs a new serializer.
	 *
	 * @param map a mapping from coordinates to regions to look up regions.
	 */
	public StrassenSerializer(Map<Coords,Region> map) {
		if (map == null) {
			throw new IllegalArgumentException("Need a valid region map.");
		}

		this.map = map;
	}
	
	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("koordx")
				&& keys.contains("koordy")
				&& keys.contains("welt")
				&& keys.contains("richtung")
				&& keys.contains("anzahl"));
	}

	@Override
	public Region load(Map<String, String> mapping) {
		// check that the region exists
		Coords coords = new Coords(
				Integer.decode(mapping.get("koordx")),
				Integer.decode(mapping.get("koordy")),
				Integer.decode(mapping.get("welt")));
		if (!map.containsKey(coords)) {
			logger.warn("Error loading street: Region not found at {}", coords);
			return null;
		}
		
		Region region = map.get(coords);

		// check/get the direction of the street
		Richtung richtung;
		try {
			richtung = Richtung.getRichtung(mapping.get("richtung"));
		} catch (IllegalArgumentException ex) {
			logger.warn("Error loading street at {}; Invalid direction \"{}\"",
					coords,
					mapping.get("richtung"));
			return null;
		}

		// check/get the amount of stones
		int amount = Integer.decode(mapping.get("anzahl"));
		if (amount < 0) {
			logger.warn("Error loading street at {}: Negative number of stones ({}); setting to zero.",
					coords,
					amount);
			amount = 0;
		}
		if (amount > region.getSteineFuerStrasse()) {
			logger.warn("Error loading street at {}: Too many stones ({}); setting to maximum ({}).",
					coords,
					amount,
					region.getSteineFuerStrasse());
			amount = region.getSteineFuerStrasse();
		}

		logger.debug("Loaded street at {} in direction {} with {} stones.",
				coords,
				richtung,
				amount);
		region.setStrassensteine(richtung, amount);
		return region;
	}

	@Override
	public SerializedData save(Region object) {
		SerializedData data = new SerializedData();
		Coords coords = object.getCoords();

		for (Richtung dir : Richtung.values()) {
			if (object.getStrassensteine(dir) > 0) {
				if (object.getStrassensteine(dir) > object.getSteineFuerStrasse()) {
					logger.warn("Error saving street at {} in direction {}: too many stones ({}). Saving {} instead.",
							object.getCoords(),
							dir,
							object.getStrassensteine(dir),
							object.getSteineFuerStrasse());
					object.setStrassensteine(dir, object.getSteineFuerStrasse());
				}
				
				Map<String, String> entry = new HashMap<String, String>();

				entry.put("koordx", String.valueOf(coords.getX()));
				entry.put("koordy", String.valueOf(coords.getY()));
				entry.put("welt", String.valueOf(coords.getWelt()));
				entry.put("richtung", dir.getShortcut());
				entry.put("anzahl", String.valueOf(object.getStrassensteine(dir)));
				data.add(entry);
			}
		}

		return data;
	}
}
