package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Serializer to load/save messages. 
 *
 * Note: In contrast to the old serialization algorithm, I have removed the use
 * of time stamps. They were basically never used anyway.
 */

// TODO: I have not modified the serialization routines to keep the table layout,
// even though they seem buggy. Most importantly, the coordinate (0,0,0) is interpreted
// as the message having no coordinate, although the coordinate is not
// documented anywhere as being in whatever way special.
public class MessageSerializer implements ObjectSerializer<Message> {

	private Collection<Partei> parteiList;
	private Collection<Coords> coordsList;
	private MapCache<Unit> unitList;

	/** Constructs a new serializer.
	 * 
	 * @param parteiList a list of all active parties
	 * @param coordsList a list of all available coordinates
	 * @param unitList a list of all available units
	 */
	public MessageSerializer(Collection<Partei> parteiList,
			Collection<Coords> coordsList, MapCache<Unit> unitList) {
		if (parteiList == null || coordsList == null || unitList == null) {
			throw new IllegalArgumentException("Serializer needs valid lists.");
		}

		this.parteiList = parteiList;
		this.coordsList = coordsList;
		this.unitList = unitList;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("kategorie")
				&& keys.contains("text")
				&& keys.contains("partei")
				&& keys.contains("koordx")
				&& keys.contains("koordy")
				&& keys.contains("welt")
				&& keys.contains("einheit"));
	}

	@Override
	public Message load(Map<String, String> mapping) {
		// load the message type
		Message msg = null;
		try {
			msg = (Message) Class.forName(
					"de.x8bit.Fantasya.Atlantis.Messages." + mapping.get("kategorie")).newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not load message type.", e);
		}

		msg.setText(mapping.get("text"));

		// load the player that the message corresponds to
		int pid = Integer.decode(mapping.get("partei"));
		for (Partei p : parteiList) {
			if (p.getNummer() == pid) {
				msg.setPartei(p);
			}
		}

		if (pid != 0 && msg.getPartei() == null) {
			// TODO: proper logging
			return msg;
			//throw new IllegalArgumentException("Partei not found in list.");
		}

		// load the coordinates that the message refers to
		Coords coords = new Coords(Integer.decode(mapping.get("koordx")),
				Integer.decode(mapping.get("koordy")),
				Integer.decode(mapping.get("welt")));
		if (coords.getX() != 0 || coords.getY() != 0 || coords.getWelt() != 0) {
			if (!coordsList.contains(coords)) {
				// TODO: proper logging
				return msg;
				//throw new IllegalArgumentException("Coordinate not found on map.");
			}
			msg.setCoords(coords);
		}

		// load the unit that the message refers to
		int uid = Integer.decode(mapping.get("einheit"));
		Unit u = unitList.get(uid);
		if (u != null) {
			msg.setUnit(u);
		}

		if (uid != 0 && msg.getUnit() == null) {
			// TODO: proper logging
			return msg;
			//throw new IllegalArgumentException("Unit not found in list.");
		}

		return msg;
	}

	@Override
	public SerializedData save(Message object) {
		// Compatibility notes: I do not save a couple of seemingly pointless
		// properties:
		// 1. "zeit" was a property that saw only use that I could identify as
		//    pointless.
		// 2. Debuglevel was a constant zero and never loaded.
		Map<String, String> data = new HashMap<String, String>();

		data.put("kategorie", object.getClass().getSimpleName());
		data.put("text", object.getText());

		data.put("partei", "0");
		if (object.getPartei() != null) {
			data.put("partei", String.valueOf(object.getPartei().getNummer()));
		}

		data.put("koordx", "0");
		data.put("koordy", "0");
		data.put("welt", "0");
		if (object.getCoords() != null) {
			Coords coords = object.getCoords();
			data.put("koordx", String.valueOf(coords.getX()));
			data.put("koordy", String.valueOf(coords.getY()));
			data.put("welt", String.valueOf(coords.getWelt()));
		}

		data.put("einheit", "0");
		if (object.getUnit() != null) {
			data.put("einheit", String.valueOf(object.getUnit().getNummer()));
		}

		return new SerializedData(data);
	}
}