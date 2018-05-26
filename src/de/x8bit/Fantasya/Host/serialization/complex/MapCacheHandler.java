package de.x8bit.Fantasya.Host.serialization.complex;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Host.serialization.basic.ObjectSerializer;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A handler that loads objects and puts them into a map indexed by their coordinate.
 *
 * In practice, this handler is required for loading regions, because regions
 * are stored in a map, not like everything else in a collection.
 */

public class MapCacheHandler<T extends Atlantis> implements ComplexHandler {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private ObjectSerializer<T> serializer;
	private Map<Coords, T> cache;

	/** Creates a new handler.
	 *
	 * @param serializer  the serializer that does the basic work.
	 * @param cache       the map that we write to or save items from.
	 */
	public MapCacheHandler(ObjectSerializer<T> serializer, Map<Coords, T> cache) {
		if (serializer == null) {
			throw new IllegalArgumentException("Need a valid serializer.");
		}
		if (cache == null) {
			throw new IllegalArgumentException("Need a valid cache to write to.");
		}

		this.serializer = serializer;
		this.cache = cache;
	}

	/** @inheritDoc */
	@Override
	public void loadAll(SerializedData input) {
		if (input == null) {
			throw new IllegalArgumentException("Loading requires input data.");
		}

		if (!input.isEmpty() && !serializer.isValidKeyset(input.keySet())) {
			throw new IllegalArgumentException("Got data with invalid keys.");
		}

		cache.clear();

		for (Map<String,String> item : input) {
			T entry = serializer.load(item);
			if (entry != null) {
				if (cache.containsKey(entry.getCoords())) {
					logger.warn("Map already contains object with coords {}. Replacing object \"{}\" by \"{}\".",
							entry.getCoords(),
							cache.get(entry.getCoords()).getName(),
							entry.getName());
				}
				cache.put(entry.getCoords(), entry);
			}
		}
	}

	/** @inheritDoc */
	@Override
	public SerializedData saveAll() {
		SerializedData output = new SerializedData();

		for (Coords c : cache.keySet()) {
			T item = cache.get(c);
			if (item.getCoords() != c) {
				logger.warn("Coordinates {} of object \"{}\" differ from sorting key {}.",
						item.getCoords(),
						item.getName(),
						c);
			}

			output.add(serializer.save(item));
		}

		return output;
	}
}
