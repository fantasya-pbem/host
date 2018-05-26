package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import de.x8bit.Fantasya.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This serializer loads and saves a single ship.
 *
 * Note that ships are put in two different data structures (Ship.PROXY and
 * Region.getShip()), which is why this serializer requires the region map
 * to attach the ships properly.
 */

// TODO: Ships are pretty broken in that they use the Owner property of the
// Atlantis base class for their captain (instead of the owning player like most
// other Atlantis objects do).

// TODO: There are several pointless columns some of which have to be populated.
// Notable examples: id (we fill with garbage), "burn" (unused)

public class ShipSerializer implements ObjectSerializer<Ship> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Coords, Region> regionMap;
	private MapCache<Unit> unitCache;

	/** Constructs a new serializer.
	 *
	 * @param regionMap a map of all regions, so that ships can be attached there.
	 */
	public ShipSerializer(Map<Coords, Region> regionMap, MapCache<Unit> unitCache) {
		if (regionMap == null) {
			throw new IllegalArgumentException("Need a valid region map.");
		}
		if (unitCache == null) {
			throw new IllegalArgumentException("Need a valid unit cache.");
		}

		this.regionMap = regionMap;
		this.unitCache = unitCache;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("type")
				&& keys.contains("nummer")
				&& keys.contains("koordx")
				&& keys.contains("koordy")
				&& keys.contains("welt")
				&& keys.contains("name")
				&& keys.contains("beschreibung")
				&& keys.contains("kapitaen")
				&& keys.contains("groesse")
				&& keys.contains("fertig")
				&& keys.contains("kueste"));
	}

	@Override
	public Ship load(Map<String, String> mapping) {
		Ship ship;
		try {
			ship = (Ship) Class.forName(
					"de.x8bit.Fantasya.Atlantis.Ships." + mapping.get("type")).newInstance();
		} catch (Exception ex) {
			logger.warn("Cannot load ship with id \"{}\": Type \"{}\" does not exist.",
					mapping.get("nummer"),
					mapping.get("type"));
			return null;
		}

		Coords coords = new Coords(
				Integer.decode(mapping.get("koordx")),
				Integer.decode(mapping.get("koordy")),
				Integer.decode(mapping.get("welt")));

		// attach the ship to the corresponding region
		if (!regionMap.containsKey(coords)) {
			logger.warn("Ship \"{}\" could not be loaded: Region at \"{}\" does not exist.",
					mapping.get("nummer"),
					coords);
			return null;
		}
		regionMap.get(coords).getShips().add(ship);

		// and set all the data items.
		ship.setNummer(Integer.decode(mapping.get("nummer")));
		ship.setName(mapping.get("name"));
		ship.setBeschreibung(mapping.get("beschreibung"));
		ship.setCoords(coords);
		ship.setOwner(Integer.decode(mapping.get("kapitaen")));
		ship.setGroesse(Integer.decode(mapping.get("groesse")));

		// fertig and kueste need special treatment.
		ship.setFertig(false);
		if (Integer.decode(mapping.get("fertig")) != 0) {
			ship.setFertig(true);
		}

		if (!mapping.get("kueste").isEmpty()) {
			try {
				ship.setKueste(Richtung.getRichtung(mapping.get("kueste")));
			} catch (IllegalArgumentException e) {
				logger.warn("Error loading ship \"{}\": Coast side \"{}\" invalid.",
						mapping.get("nummer"),
						mapping.get("kueste"));
				return null;
			}
		}

		// general logging
		logger.debug("Loaded ship \"{}\" with id {}.",
				ship.getName(),
				ship.getNummer());

		return ship;
	}

	@Override
	public SerializedData save(Ship object) {

		// Compatibility notes: The never-loaded and thus obsolete property
		// "burn" (constant 0, nowhere used) is not loaded from the table.
		// Furthermore, "kueste" now contains the direction shortcut instead of
		// the name; works just as well.
		Map<String, String> data = new HashMap<String, String>();

		data.put("type", object.getClass().getSimpleName());
		data.put("nummer", String.valueOf(object.getNummer()));
		data.put("name", object.getName());
		data.put("beschreibung", object.getBeschreibung());
		data.put("koordx", String.valueOf(object.getCoords().getX()));
		data.put("koordy", String.valueOf(object.getCoords().getY()));
		data.put("welt", String.valueOf(object.getCoords().getWelt()));
		data.put("kapitaen", String.valueOf(object.getOwner()));
		data.put("groesse", String.valueOf(object.getGroesse()));

		data.put("fertig", "0");
		if (object.istFertig()) {
			data.put("fertig", "1");
		}

		data.put("kueste", "");
		if (object.getKueste() != null) {
			data.put("kueste", object.getKueste().getShortcut());
		}

		// error logging
		if (!regionMap.containsKey(object.getCoords())) {
			logger.warn("Error saving ship \"{}\": Region at \"{}\" does not exist.",
					object.getNummer(),
					object.getCoords());
		}
		if (unitCache.get(object.getOwner()) == null) {
			logger.warn("Error saving ship \"{}\": Owner does not exist.",
					object.getNummer(),
					object.getOwner());
		}

		// save pointless columns
		data.put("id", " ");

		return new SerializedData(data);
	}
}