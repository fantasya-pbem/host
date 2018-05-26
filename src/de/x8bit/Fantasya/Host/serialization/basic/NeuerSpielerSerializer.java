package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import de.x8bit.Fantasya.Host.EVA.util.NeuerSpieler;
import de.x8bit.Fantasya.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Loads/saves the data relevant for a new player. */
public class NeuerSpielerSerializer implements ObjectSerializer<NeuerSpieler> {

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("email")
				&& keys.contains("rasse")
				&& keys.contains("tarnung")
				&& keys.contains("holz")
				&& keys.contains("eisen")
				&& keys.contains("steine")
				&& keys.contains("insel")
				&& keys.contains("name")
				&& keys.contains("description")
				&& keys.contains("password")
				&& keys.contains("user_id"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public NeuerSpieler load(Map<String, String> mapping) {
		NeuerSpieler ns = new NeuerSpieler();
		
		try {
			ns.setRasse((Class<? extends Unit>)Class.forName(
					"de.x8bit.Fantasya.Atlantis.Units." + StringUtils.normalize(mapping.get("rasse"))));
		} catch (Exception e) {
			throw new IllegalArgumentException("Incorrect race supplied.", e);
		}
		
		ns.setTarnung(null);
		if (!mapping.get("tarnung").isEmpty()) {
			try {
				ns.setTarnung((Class<? extends Unit>)Class.forName(
						"de.x8bit.Fantasya.Atlantis.Units." + mapping.get("tarnung")));
			} catch (Exception e) {
				throw new IllegalArgumentException("Incorrect tarn race supplied.", e);
			}
		}
		ns.setHolz(Integer.decode(mapping.get("holz")));
		ns.setEisen(Integer.decode(mapping.get("eisen")));
		ns.setSteine(Integer.decode(mapping.get("steine")));
		ns.setInsel(Integer.decode(mapping.get("insel")));
		ns.setEmail(mapping.get("email"));
		ns.setName(mapping.get("name"));
		ns.setDescription(mapping.get("description"));
		ns.setPassword(mapping.get("password"));
		ns.setUserId(Integer.decode(mapping.get("user_id")));
		
		return ns;
	}

	@Override
	public SerializedData save(NeuerSpieler object) {
		Map<String, String> data = new HashMap<String, String>();
		
		data.put("rasse", object.getRasse().getClass().getSimpleName());
		data.put("tarnung", "");
		if (object.getTarnung() != null) {
			data.put("tarnung", object.getTarnung().getSimpleName());
		}
		data.put("holz", String.valueOf(object.getHolz()));
		data.put("eisen", String.valueOf(object.getEisen()));
		data.put("steine", String.valueOf(object.getSteine()));
		data.put("insel", String.valueOf(object.getInsel()));
		data.put("email", String.valueOf(object.getEmail()));
		data.put("name", String.valueOf(object.getName()));
		data.put("description", String.valueOf(object.getDescription()));
		data.put("password", String.valueOf(object.getPassword()));
		data.put("user_id", String.valueOf(object.getUserId()));
		
		return new SerializedData(data);
	}
}