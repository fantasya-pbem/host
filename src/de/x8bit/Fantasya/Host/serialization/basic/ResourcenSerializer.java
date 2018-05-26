package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Loads resources connected to certain regions. */

public class ResourcenSerializer implements ObjectSerializer<Region> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Coords, Region> map;

	/** Constructs a new serializer.
	 *
	 * @param map a mapping from coordinates to the regions at this coordinate.
	 * @throws IllegalArgumentException if the argument is null.
	 */
	public ResourcenSerializer(Map<Coords,Region> map) {
		if (map == null) {
			throw new IllegalArgumentException("Need a non-null map.");
		}

		this.map = map;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("koordx")
				&& keys.contains("koordy")
				&& keys.contains("welt")
				&& keys.contains("resource")
				&& keys.contains("anzahl"));
	}

	/** Loads a resource and attaches it to the corresponding region.
	 *
	 * @param mapping a mapping that holds the resource in serialized state.
	 * @return the region that got the new resource.
	 * @throws IllegalArgumentException if the region cannot be found or if
	 * the resource does not correspond to a valid object.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Region load(Map<String, String> mapping) {
		Coords coords = new Coords(
				Integer.decode(mapping.get("koordx")),
				Integer.decode(mapping.get("koordy")),
				Integer.decode(mapping.get("welt")));
		if (!map.containsKey(coords)) {
			logger.warn("Could not find region at coordinate {}. Ignoring resource {}",
					coords,
					mapping.get("resource"));
			return null;
		}

		Region r = map.get(coords);
		Class<? extends Item> resourceClass = null;
		try {
			resourceClass = (Class<? extends Item>) Class.forName(
					"de.x8bit.Fantasya.Atlantis.Items." + mapping.get("resource"));
		} catch (ClassNotFoundException ex) {
			logger.warn("Cannot load resource at coordinate {}. Resource \"{}\" does not exist.",
					coords,
					mapping.get("resource"));
			return null;
		}

		r.setResource(resourceClass , Integer.decode(mapping.get("anzahl")));
		logger.debug("Loading resources at {}. {} items of {} loaded.",
				coords,
				mapping.get("anzahl"),
				mapping.get("resource"));
		return r;
	}

	/** Saves all resources of the argument region and returns them in
	 *  serialized form.
	 */
	@Override
	public SerializedData save(Region object) {
		SerializedData output = new SerializedData();
		Coords coords = object.getCoords();

		for (Item item : object.getResourcen()) {
			Map<String, String> values = new HashMap<String, String>();
			
			values.put("koordx", String.valueOf(coords.getX()));
			values.put("koordy", String.valueOf(coords.getY()));
			values.put("welt", String.valueOf(coords.getWelt()));
			values.put("resource", item.getClass().getSimpleName());
			values.put("anzahl", String.valueOf(item.getAnzahl()));

			output.add(values);
		}
		
		return output;
	}
}
