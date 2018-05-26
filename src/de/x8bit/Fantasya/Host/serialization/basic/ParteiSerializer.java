package de.x8bit.Fantasya.Host.serialization.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import de.x8bit.Fantasya.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Serializes/Deserializes a Partei to/from a key/value mapping. */
public class ParteiSerializer implements ObjectSerializer<Partei> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/** Returns true if all entries for a Partei are in the keyset. */
	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return keys.contains("age")
				&& keys.contains("name")
				&& keys.contains("beschreibung")
				&& keys.contains("id")
				&& keys.contains("email")
				&& keys.contains("rasse")
				&& keys.contains("password")
				&& keys.contains("website")
				&& keys.contains("nmr")
				&& keys.contains("originx")
				&& keys.contains("originy")
				&& keys.contains("cheats")
				&& keys.contains("monster")
				&& keys.contains("steuern")
				&& keys.contains("user_id")
				&& keys.contains("owner_id");
	}

	/** Creates the new partei from the mapping. */
	@Override
	public Partei load(Map<String, String> mapping) {
		Partei object = null;
		
		try {
			object = new Partei(Integer.decode(mapping.get("age")));
			object.setName(mapping.get("name"));
			object.setBeschreibung(mapping.get("beschreibung"));
			object.setNummer(Integer.parseInt(mapping.get("id"), 36));
			object.setEMail(mapping.get("email"));
			object.setRasse(mapping.get("rasse"));
			object.setPassword(mapping.get("password"));
			object.setWebsite(mapping.get("website"));
			object.setNMR(Integer.decode(mapping.get("nmr")));
			object.setUrsprung(new Coords(Integer.decode(mapping.get("originx")),
					Integer.decode(mapping.get("originy")), 1));
			object.setCheats(Integer.decode(mapping.get("cheats")));
			object.setMonster(Integer.decode(mapping.get("monster")));
			object.setDefaultsteuer(Integer.decode(mapping.get("steuern")));
			object.setUserId(Integer.decode(mapping.get("user_id")));
			object.setOwnerId(Integer.decode(mapping.get("owner_id")));

			logger.debug("Loaded Partei \"{}\" with id \"{}\"",
					object.getName(), object.getNummer());
		} catch (RuntimeException ex) {
			logger.warn("Error while loading Partei \"{}\"; exception was:\n{}",
					mapping.get("name"), ex);
		}

		return object;
	}

	/** Serializes the partei into a mapping of key/value pairs. */
	@Override
	public SerializedData save(Partei object) {
		// Compatibility notes: The unused property "id" (nummer in base36) is
		// discarded in the new serialization code.
		Map<String, String> results = new HashMap<String, String>();

		results.put("age", String.valueOf(object.getAlter()));
		results.put("name", object.getName());
		results.put("beschreibung", object.getBeschreibung());
		results.put("id", String.valueOf(object.getNummerBase36()));
		results.put("email", object.getEMail());
		results.put("rasse", object.getRasse());
		results.put("password", object.getPassword());
		results.put("website", object.getWebsite());
		results.put("nmr", String.valueOf(object.getNMR()));
		results.put("originx", String.valueOf(object.getUrsprung().getX()));
		results.put("originy", String.valueOf(object.getUrsprung().getY()));
		results.put("cheats", String.valueOf(object.getCheats()));
		results.put("monster", String.valueOf(object.getMonster()));
		results.put("steuern", String.valueOf(object.getDefaultsteuer()));
		results.put("user_id", String.valueOf(object.getUserId()));
		results.put("owner_id", String.valueOf(object.getOwnerId()));

		return new SerializedData(results);
	}
}
