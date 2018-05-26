package de.x8bit.Fantasya.Host.serialization.complex;

import de.x8bit.Fantasya.Host.serialization.basic.ObjectSerializer;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import java.util.Collection;
import java.util.Map;

/** Operates over some cache of objects.
 *
 * For loading, this class does not do anything special. However, it does
 * know about some cache, whose content is saved one by one.
 *
 * One of the standard use-cases for this handler is a set of objects that have
 * special properties (tax rates for parties, items for units), which have to
 * be loaded all in a row, and where all tax rates for each party or the whole
 * equipment for each unit have to be saved.
 */


public class CacheLooperHandler<T> implements ComplexHandler {

	protected ObjectSerializer<T> serializer;
	protected Collection<T> cache;

	/** Constructs a new handler.
	 *
	 * @param serializer
	 *         an ObjectSerializer that is used to load/save a single entry.
	 * @param cache
	 *         Holds the list of objects that we save.
	 * @throws IllegalArgumentException if any of the arguments is null.
	 */
	public CacheLooperHandler(ObjectSerializer<T> serializer, Collection<T> cache) {
		if (cache == null) {
			throw new IllegalArgumentException("Need a valid cache.");
		}
		if (serializer == null) {
			throw new IllegalArgumentException("Need a valid serializer");
		}

		this.serializer = serializer;
		this.cache = cache;
	}

	/** Loads each entry in the input, and passes it on to the ObjectSerializer.
	 *
	 * @param input
	 *         the data that we want to cast into gameplay objects.
	 * @throws IllegalArgumentException if the input is null or lacks some data.
	 */
	@Override
	public void loadAll(SerializedData input) {
		if (input == null) {
			throw new IllegalArgumentException("Need data to load from.");
		}

		if (!input.isEmpty() && !serializer.isValidKeyset(input.keySet())) {
			throw new IllegalArgumentException("Data lacks relevant keys.");
		}

		for (Map<String,String> item : input) {
			serializer.load(item);
		}
	}

	/** Saves all elements in the cache, and returns them in serialized form. */
	@Override
	public SerializedData saveAll() {
		SerializedData data = new SerializedData();
		for (T entry : cache) {
			data.add(serializer.save(entry));
		}

		return data;
	}
}