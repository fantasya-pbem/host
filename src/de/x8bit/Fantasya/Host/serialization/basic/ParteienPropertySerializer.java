package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Specific serializer for Parteien properties.
 *
 * They are treated specially from _all_ other properties, which is kind of
 * annoying. As soon as the table layout is fixed (the required key "id" has to
 * be removed without replacement, and "partei" should be renamed to "id"), this
 * class can be removed without harm.
 *
 * Further annoying issue: For the beta, the property_parteien actually has a
 * NULL value for the partei field. Whatever that is supposed to mean, but it
 * needs to be caught as well...
 */
public class ParteienPropertySerializer implements ObjectSerializer<Partei> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Collection<Partei> parteiList;

	private static int uniqueId = 1;

	
	public ParteienPropertySerializer(Collection<Partei> parteiList) {
		if (parteiList == null) {
			throw new IllegalArgumentException("List of parties must not be null");
		}

		this.parteiList = parteiList;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("id")
				&& keys.contains("partei")
				&& keys.contains("name")
				&& keys.contains("value"));
	}

	@Override
	public Partei load(Map<String, String> mapping) {
		if (mapping.get("partei") == null) {
			return null;
		}

		int id = Integer.decode(mapping.get("partei"));

		for (Partei entry : parteiList) {
			if (entry.getNummer() == id) {
				entry.setProperty(mapping.get("name"), mapping.get("value"));

				// some logging coding
				String shortProperty = mapping.get("value");
				if (shortProperty.length() > 100) {
					shortProperty = shortProperty.substring(0, 100) + "...";
				}
				logger.debug("Loading property for partei {}:  {} = {}",
						id, mapping.get("name"), shortProperty);
				return entry;
			}
		}

		logger.warn("Error with property \"{}\" of partei {}: Partei does not exist.",
				mapping.get("name"), id);
		return null;
	}

	@Override
	public SerializedData save(Partei object) {
		// Compatibility notes: With the old load/save code, the property tables
		// varied to some respect.
		// The "id" property was called "partei" for Partei properties.
		// While the loading code is agnostic (it accepts all variants), the
		// saving code only saves the "id" property.
		SerializedData data = new SerializedData();

		for (String property : object.getProperties()) {
			Map<String, String> item = new HashMap<String, String>();

			item.put("id", String.valueOf(uniqueId));
			item.put("partei", String.valueOf(object.getNummer()));
			item.put("name", property);
			item.put("value", object.getStringProperty(property));

			data.add(item);
			uniqueId++;
		}

		return data;
	}
}