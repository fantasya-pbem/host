package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Atlantis;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import de.x8bit.Fantasya.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Generic serializer to load or save the properties of some Atlantis object.
 * 
 * The exact type has to be specified as a generic parameter, since many
 * different Atlantis objects can have properties.
 */
// TODO: Partei properties work completely different from all other properties.
// So they need a custom loader. This is, put nicely, highly unusual and annoying.
// Unify this.
public class PropertySerializer<T extends Atlantis> implements ObjectSerializer<T> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Collection<T> objectList;

	/** Constructs a new serializer.
	 * 
	 * @param objectList the list of objects that can have properties.
	 * @throws IllegalArgumentException if the objectList is null.
	 */
	public PropertySerializer(Collection<T> objectList) {
		if (objectList == null) {
			throw new IllegalArgumentException("List of objects with properties must not be null.");
		}

		this.objectList = objectList;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("id")
				&& keys.contains("name")
				&& keys.contains("value"));
	}

	/** Loads a new property.
	 * 
	 * @return the object whose property was set.
	 * @throws IllegalArgumentException if the mapping does not refer to a valid
	 * object in the list.
	 */
	@Override
	public T load(Map<String, String> mapping) {
		int id = Integer.decode(mapping.get("id"));

		for (T entry : objectList) {
			if (entry.getNummer() == id) {
				entry.setProperty(mapping.get("name"), mapping.get("value"));
				
				// some logging coding
				String shortProperty = mapping.get("value");
				if (shortProperty.length() > 100) {
					shortProperty = shortProperty.substring(0, 100) + "...";
				}
				logger.debug("Loading property for entity {}:  {} = {}",
						id, mapping.get("name"), shortProperty);
				return entry;
			}
		}

		logger.warn("Error with property \"{}\" of entity {}: Entity does not exist.",
				mapping.get("name"), id);
		return null;
	}

	@Override
	public SerializedData save(T object) {
		// Compatibility notes: With the old load/save code, the property tables
		// varied to some respect.
		// The "id" property was called "partei" for Partei properties.
		// While the loading code is agnostic (it accepts all variants), the
		// saving code only saves the "id" property.
		SerializedData data = new SerializedData();

		for (String property : object.getProperties()) {
			Map<String, String> item = new HashMap<String, String>();

			item.put("id", String.valueOf(object.getNummer()));
			item.put("name", property);
			item.put("value", object.getStringProperty(property));

			data.add(item);
		}

		return data;
	}
}