package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import de.x8bit.Fantasya.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** This serializer loads and saves the commands belonging to a certain unit.
 *
 * There are a couple of highly ugly features here. First, the commands inside
 * a unit are stored in two different ways, which are both needed in the code,
 * or so it seems. Second, commands are more or less so statically set up that
 * it is practically impossible to test that the loading works as expected.
 */
public class BefehleSerializer implements ObjectSerializer<Unit> {

	private Collection<Unit> unitCache;

	/** Creates a new serializer.
	 *
	 * @param unitCache   a collection of all existing units.
	 * @throws IllegalArgumentException if the cache is null.
	 */
	public BefehleSerializer(Collection<Unit> unitCache) {
		if (unitCache == null) {
			throw new IllegalArgumentException("Serializer requires a unit list.");
		}

		this.unitCache = unitCache;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("nummer") && keys.contains("befehl"));
	}

	@Override
	public Unit load(Map<String, String> mapping) {
		int id = Integer.decode(mapping.get("nummer"));
		for (Unit unit : unitCache) {
			if (unit.getNummer() == id) {
				unit.Befehle.add(mapping.get("befehl"));
				unit.BefehleExperimental.add(unit, mapping.get("befehl"));
				return unit;
			}
		}

		throw new IllegalArgumentException("Befehl found for non-existent unit.");
	}

	@Override
	public SerializedData save(Unit object) {
		SerializedData data = new SerializedData();

		// TODO: not sure what this is for; can be removed ???
		if (object.getCoords().getWelt() == 0) {
			return data;  // virtuelle Einheiten haben keine Befehle! Schtonk!
		}

		// Compatibility note: the old serialization code also wrote out
		// "sortierung", but never read it in again. Thus, it is just discarded here.
		for (String befehl : object.Befehle) {
			Map<String, String> item = new HashMap<String, String>();

			item.put("nummer", String.valueOf(object.getNummer()));
			item.put("befehl", befehl);

			data.add(item);
		}

		return data;
	}
}