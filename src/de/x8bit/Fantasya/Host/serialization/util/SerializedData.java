package de.x8bit.Fantasya.Host.serialization.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/** A block of equal serialized data items.
 *
 * This class encapsulates a simple list of key/value maps. However, it adds
 * some checks to make sure that all maps have the same keys.
 */

public final class SerializedData implements Iterable<Map<String, String>> {

	private List<Map<String, String>> internalData = new ArrayList<Map<String, String>>();

	/** Creates an empty SerializedData object. */
	public SerializedData() {
	}
	
	/** Creates an instance and fills it immediately with some data. */
	public SerializedData(Map<String, String> data) {
		add(data);
	}

	/** Adds another element to the data object.
	 *
	 * Note: The first time you call this function, you define of which keys
	 * the data set is made up. Subsequent calls must have these and only these
	 * keys as parameters.
	 *
	 * @param data  a map with a key/value relation that specifies an object
	 * in the serialized state;
	 * @throws IllegalArgumentException if the mapping has incorrect keys or
	 * is null.
	 */
	public void add(Map<String, String> data) {
		if (data == null || data.isEmpty()) {
			throw new IllegalArgumentException("Added a null object to the dataset.");
		}

		if (!isEmpty() && !data.keySet().equals(internalData.get(0).keySet())) {
			throw new IllegalArgumentException("Key set was not consistent with data stored so far.");
		}

		internalData.add(data);
	}

	/** Adds another compatible SerializedData object.
	 *
	 * If this object has not had any keyset yet, this will be taken from the added
	 * data.
	 *
	 * @param data the other SerializedData instance
	 * @throws InvalidArgumentException if data is null, or has an
	 * incompatible keyset.
	 */
	public void add(SerializedData data) {
		if (data == null) {
			throw new IllegalArgumentException("Added data must not be null.");
		}

		if (data.isEmpty()) {
			return;
		}

		for (Map<String, String> item : data) {
			add(item);
		}
	}
	
	/** Returns true if no data is stored in this container. */
	public boolean isEmpty() {
		return internalData.isEmpty();
	}

	/** Returns the keys of the data. */
	public Set<String> keySet() {
		if (isEmpty()) {
			throw new IllegalStateException("An empty data set has no keys.");
		}
		
		return Collections.unmodifiableSet(internalData.get(0).keySet());
	}

	@Override
	public Iterator<Map<String, String>> iterator() {
		return internalData.iterator();
	}
}
