package de.x8bit.Fantasya.Host.serialization.complex;

import java.util.Collection;
import java.util.Map;

import de.x8bit.Fantasya.Host.serialization.basic.ObjectSerializer;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

/** Loads a bunch of objects and populates a list (the cache) with
 * the resulting objects.
 *
 * This is similar to a CacheLooperHandler. However, when loading, it not only
 * loads all sorts of items, but also populates the cache with it. A typical
 * use-case for this class would be a handler that loads all units and puts
 * them in some cache.
 */

public class CacheFillerHandler<T> extends CacheLooperHandler<T> {

	/** @{inheritDocs} */
	public CacheFillerHandler(ObjectSerializer<T> serializer, Collection<T> cache) {
		super(serializer, cache);
	}

	/** Clears the cache, and fills it with one object per entry. */
	@Override
	public void loadAll(SerializedData input) {
		if (input == null) {
			throw new IllegalArgumentException("Need a non-null mapping!");
		}

		if (!input.isEmpty() && !serializer.isValidKeyset(input.keySet())) {
			throw new IllegalArgumentException(
					"Error during loading: Serializer refused keys."
					+ "\nFailing keyset was:\n" + input.keySet());
		}

		// clear the cache before filling it with loaded data.
		cache.clear();

		// load all the single entries
		for (Map<String,String> entry : input) {
			T item = serializer.load(entry);
			if (item != null) {
				cache.add(item);
			}
		}
	}
}
