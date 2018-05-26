package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.Nachfrage;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.LuxusGood;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Serializer that loads/saves luxus good demands attached to a certain region. */

// TODO: To avoid compatibility issues, I just copied a very odd code flow that
// would divide Nachfrage by 1000 after loading from the database, and multiply
// again by 1000 when writing to it. That is ... odd, to say the least, so 
// this should be fixed by a database upgrade in the long run.
public class LuxusSerializer implements ObjectSerializer<Region> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Coords, Region> map;

	/** Creates a new serializer.
	 *
	 * @param map a mapping from coordinates to regions for lookup.
	 * @throws IllegalArgumentException if the mapping is null.
	 */
	public LuxusSerializer(Map<Coords, Region> map) {
		if (map == null) {
			throw new IllegalArgumentException("Need a valid map.");
		}

		this.map = map;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("koordx")
				&& keys.contains("koordy")
				&& keys.contains("welt")
				&& keys.contains("luxus")
				&& keys.contains("nachfrage"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Region load(Map<String, String> mapping) {
		Coords coords = new Coords(
				Integer.decode(mapping.get("koordx")),
				Integer.decode(mapping.get("koordy")),
				Integer.decode(mapping.get("welt")));
		if (!map.containsKey(coords)) {
			logger.warn("Error loading luxus goods: No region for {} at coords {}",
					mapping.get("luxus"),
					coords);
			return null;
		}

		Region region = map.get(coords);
		Class<? extends Item> clazz = null;
		try {
			clazz = (Class<? extends Item>) Class.forName(
					"de.x8bit.Fantasya.Atlantis.Items." + mapping.get("luxus"));
		} catch (ClassNotFoundException ex) {
			logger.warn("Error loading luxus good \"{}\": no valid luxus item found.",
					mapping.get("luxus"));
			return null;
		}

		// Fixme: In principle, clazz should only allow instances of LuxusGood,
		//        then this could be caught in the code above, but currently
		//        the Region interface does not work like that.
		boolean isLuxus = false;
		for (Class<?> interfaceClazz : clazz.getInterfaces()) {
			if (interfaceClazz.equals(LuxusGood.class)) {
				isLuxus = true;
				break;
			}
		}

		if (!isLuxus) {
			logger.warn("Error loading luxus good \"{}\": not a valid luxus good.",
					mapping.get("luxus"));
			return null;
		}

		region.setNachfrage(clazz, Float.valueOf(mapping.get("nachfrage")) / 1000f);
		logger.debug("Loaded luxus good at {} of type {} with value {}.",
				coords,
				mapping.get("luxus"),
				mapping.get("nachfrage") + "/1000");
		return region;
	}

	@Override
	public SerializedData save(Region object) {
		SerializedData output = new SerializedData();
		Coords coords = object.getCoords();

		boolean produceFound = false;
		for (Nachfrage nf : object.getLuxus()) {
			Map<String, String> item = new HashMap<String, String>();

			item.put("koordx", String.valueOf(coords.getX()));
			item.put("koordy", String.valueOf(coords.getY()));
			item.put("welt", String.valueOf(coords.getWelt()));
			item.put("luxus", nf.getItem().getSimpleName());
			item.put("nachfrage", String.valueOf(Math.round(nf.getNachfrage() * 1000)));

			output.add(item);

			if (nf.getNachfrage() < 0) {
				if (produceFound) {
					logger.warn("Error while saving: Region at {} produces multiple luxus goods.",
							coords);
				}
				produceFound = true;
			}
		}

		return output;
	}
}
