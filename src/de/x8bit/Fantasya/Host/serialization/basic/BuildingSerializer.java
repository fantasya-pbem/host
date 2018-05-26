package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Serializer that loads or saves a building. */
public class BuildingSerializer implements ObjectSerializer<Building> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Set<Coords> regionCoords;
	private MapCache<Unit> unitList;
	
	public BuildingSerializer(Set<Coords> regionCoords, MapCache<Unit> unitList) {
		if (regionCoords == null) {
			throw new IllegalArgumentException("List of region coordinates must not be null.");
		}
		if (unitList == null) {
			throw new IllegalArgumentException("List of units must not be null.");
		}

		this.regionCoords = regionCoords;
		this.unitList = unitList;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("koordx")
				&& keys.contains("koordy")
				&& keys.contains("welt")
				&& keys.contains("nummer")
				&& keys.contains("name")
				&& keys.contains("beschreibung")
				&& keys.contains("type")
				&& keys.contains("size")
				&& keys.contains("funktion")
				&& keys.contains("owner"));
	}

	@Override
	public Building load(Map<String, String> mapping) {
		// fail on a bad building type
		Building building;
		try {
			building = (Building) Class.forName(
					"de.x8bit.Fantasya.Atlantis.Buildings." + mapping.get("type")).newInstance();
		} catch (Exception ex) {
			logger.warn("Error loading Building \"{}\": Type \"{}\" does not exist.",
					mapping.get("nummer"),
					mapping.get("type"));
			return null;
		}

		// fail on bad coordinates
		Coords coord = new Coords(
				Integer.decode(mapping.get("koordx")),
				Integer.decode(mapping.get("koordy")),
				Integer.decode(mapping.get("welt")));
		if (!regionCoords.contains(coord)) {
			logger.warn("Error loading building \"{}\": Region at coordinates {} does not exist.",
					mapping.get("nummer"),
					coord);
			return null;
		}

		building.setCoords(coord);
		building.setNummer(Integer.decode(mapping.get("nummer")));
		building.setName(mapping.get("name"));
		building.setBeschreibung(mapping.get("beschreibung"));
		building.setSize(Integer.decode(mapping.get("size")));
		building.setFunktion(Integer.decode(mapping.get("funktion")) > 0);
		building.setOwner(Integer.decode(mapping.get("owner")));

		logger.debug("Loaded building \"{}\" at {} of type {}",
				String.valueOf(building.getNummer()),
				coord,
				mapping.get("type"));

		return building;
	}

	@Override
	public SerializedData save(Building object) {
		Map<String, String> mapping = new HashMap<String, String>();

		mapping.put("type", object.getClass().getSimpleName());
		mapping.put("koordx", String.valueOf(object.getCoords().getX()));
		mapping.put("koordy", String.valueOf(object.getCoords().getY()));
		mapping.put("welt", String.valueOf(object.getCoords().getWelt()));
		mapping.put("nummer", String.valueOf(object.getNummer()));
		mapping.put("name", object.getName());
		mapping.put("beschreibung", object.getBeschreibung());
		mapping.put("size", String.valueOf(object.getSize()));
		if (object.hatFunktion()) {
			mapping.put("funktion", "1");
		} else {
			mapping.put("funktion", "0");
		}
		mapping.put("owner", String.valueOf(object.getOwner()));

		// log errors
		// note that it is perfectly natural for a building to have owner 0,
		// meaning noone owns this building.
		if (object.getOwner() != 0 && unitList.get(object.getOwner()) == null) {
			logger.warn("Owner \"{}\" of building \"{}\" does not exist.",
					object.getOwner(),
					object.getNummer());
		}

		if (!regionCoords.contains(object.getCoords())) {
			logger.warn("Location \"{}\" of building \"{}\" does not exist.",
					object.getCoords(),
					object.getNummer());
		}

		// TODO: These columns are completely, utterly useless (also in the old
		// code), but still required as long as the table layout is not changed.
		mapping.put("monument", " ");
		mapping.put("id", " ");

		return new SerializedData(mapping);
	}
}