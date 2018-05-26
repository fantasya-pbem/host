package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import java.util.Map;
import java.util.Set;

/**Maps a list of key/value pairs into an Atlantis object and vice versa.
 *
 * Implementations of this class take a map of key/value pairs, and try to
 * construct a specific Atlantis object from them. They can also perform the
 * inverse operation, turning an Atlantis object into a map of key/value pairs.
 *
 * Note: not all serializers create or serialize an object. Some also save only
 * certain fields, such as Properties of an Atlantis object. In such a case,
 * a single object may yield multiple data points, which is why we return a
 * SerializedData object when saving.
 */

public interface ObjectSerializer<T> {

	/** Returns true if the keyset contains all entries required for
	 * constructing the object.
	 *
	 * Note that object construction can still fail because of invalid data,
	 * even if this function returns true.
	 *
	 * @param keys the set of keys that is checked for correctness.
	 * @return true if the keyset contains all required entries, false otherwise.
	 */
	public boolean isValidKeyset(Set<String> keys);

	/** Creates the new object from a key/value map.
	  *
	  * @param mapping the map of key/value pairs that holds the state of the object.
	  * @return the constructed object
	  * @throws IllegalArgumentException if the mapping does not translate into
	  * a valid object.
	  */
	public T load(Map<String, String> mapping);

	/** Serializes the object into a mapping of key/value pairs.
	  *
	  * @param object the object to serialize
	  * @return a SerializedData instance that represents the object in a serialized state.
	  */
	public SerializedData save(T object);
}
