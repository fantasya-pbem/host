package de.x8bit.Fantasya.Host.serialization.basic;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.serialization.util.SerializedData;
import de.x8bit.Fantasya.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Serializes/Deserializes a basic unit without skills, items etc. */

// TODO: Eine Einheit serializiert etwa zweimal so viele Daten wie jede andere
// Klasse, das stinkt nach bloat. Teilweise kann man die Daten als properties
// speichern, weil nur wenige Einheiten sie jemals benoetigen (mana, aura),
// teilweise muessen sie wahrscheinlich ueberhaupt nicht gespeichert werden
// (sortierung, tempnummer, lehrtage). Die Id wird im Feld "nummer" und "id"
// gespeichert, und wird auch in beiden Feldern verlangt (NOT NULL), obwohl es zweimal
// derselbe Wert ist.
public class EinheitenSerializer implements ObjectSerializer<Unit> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Collection<Partei> partyList;
	private Set<Coords> regionList;

	public EinheitenSerializer(Collection<Partei> partyList, Set<Coords> regionList) {
		if (partyList == null) {
			throw new IllegalArgumentException("Require a valid list of parties for unit serialization.");
		}
		if (regionList == null) {
			throw new IllegalArgumentException("Require a valid regionList for unit serialization.");
		}

		this.partyList = partyList;
		this.regionList = regionList;
	}

	@Override
	public boolean isValidKeyset(Set<String> keys) {
		return (keys.contains("rasse")
				&& keys.contains("nummer")
				&& keys.contains("koordx")
				&& keys.contains("koordy")
				&& keys.contains("welt")
				&& keys.contains("name")
				&& keys.contains("beschreibung")
				&& keys.contains("person")
				&& keys.contains("partei")
				&& keys.contains("tarnung_rasse")
				&& keys.contains("tarnung_partei")
				&& keys.contains("gebaeude")
				&& keys.contains("schiff")
				&& keys.contains("prefix")
				&& keys.contains("sichtbarkeit")
				&& keys.contains("lebenspunkte")
				&& keys.contains("lehrtage")
				&& keys.contains("aura")
				&& keys.contains("mana")
				&& keys.contains("tempnummer")
				&& keys.contains("sortierung")
				&& keys.contains("longorder")
				&& keys.contains("bewacht")
				&& keys.contains("belagert")
				&& keys.contains("einkommen")
				&& keys.contains("kampfposition"));
	}

	@Override
	public Unit load(Map<String, String> mapping) {
		Unit unit;
		try {
			unit = (Unit) Class.forName("de.x8bit.Fantasya.Atlantis.Units." + mapping.get("rasse")).newInstance();
		} catch (Exception ex) {
			logger.warn("Error loading unit \"{}\": Race {} not found.",
					mapping.get("nummer"),
					mapping.get("rasse"));
			return null;
		}

		unit.setNummer(Integer.decode(mapping.get("nummer")));
		unit.setCoords(new Coords(
				Integer.decode(mapping.get("koordx")),
				Integer.decode(mapping.get("koordy")),
				Integer.decode(mapping.get("welt"))));
		unit.setName(mapping.get("name"));
		unit.setBeschreibung(mapping.get("beschreibung"));
		unit.setPersonen(Integer.decode(mapping.get("person")));
		unit.setOwner(Integer.decode(mapping.get("partei")));
		unit.setTarnRasse(mapping.get("tarnung_rasse"));
		unit.setTarnPartei(Integer.decode(mapping.get("tarnung_partei")));
		unit.setGebaeude(Integer.decode(mapping.get("gebaeude")));
		unit.setSchiff(Integer.decode(mapping.get("schiff")));
		unit.setPrefix(mapping.get("prefix"));
		unit.setSichtbarkeit(Integer.decode(mapping.get("sichtbarkeit")));
		unit.setLebenspunkte(Integer.decode(mapping.get("lebenspunkte")));
		unit.setLehrtage(Integer.decode(mapping.get("lehrtage")));
		unit.setAura(Integer.decode(mapping.get("aura")));
		unit.setMana(Integer.decode(mapping.get("mana")));
		unit.setTempNummer(Integer.decode(mapping.get("tempnummer")));
		unit.setSortierung(Integer.decode(mapping.get("sortierung")));
		unit.setLongOrder(mapping.get("longorder"));
		unit.setBelagert(Integer.decode(mapping.get("belagert")));
		unit.setEinkommen(Integer.decode(mapping.get("einkommen")));
		try {
			unit.setKampfposition(Kampfposition.valueOf(Kampfposition.class, mapping.get("kampfposition")));
		} catch (IllegalArgumentException ex) {
			logger.warn("Error loading unit \"{}\": Kampfposition \"{]\" could not be set;"
					+ "using default (Kampfposition.Vorne)",
					unit.getNummer(),
					mapping.get("kampfposition"));
			unit.setKampfposition(Kampfposition.Vorne);
		}

		unit.setBewacht(false);
		if (Integer.decode(mapping.get("bewacht")) != 0) {
			unit.setBewacht(true);
		}

		// now check if the unit is valid at all, otherwise return null.
		if (!regionList.contains(unit.getCoords())) {
			logger.warn("Error loading unit \"{}\": No region at location {}.",
					unit.getNummer(),
					unit.getCoords());
			return null;
		}

		Partei owner = null;
		for (Partei p : partyList) {
			if (p.getNummer() == unit.getOwner()) {
				owner = p;
			}
		}
		if (owner == null) {
			logger.warn("Error loading unit \"{}\": Owner \"{}\" does not exist.",
					unit.getNummer(),
					unit.getOwner());
			return null;
		}


		logger.debug("Loaded unit \"{}\" with id \"{}\"",
				unit.getName(),
				unit.getNummer());

		return unit;
	}

	@Override
	public SerializedData save(Unit object) {
		Map<String, String> data = new HashMap<String, String>();

		// Compatibility notes: The old serialization code saved several 
		// obsolete columns. These are: "id" (nummer in base36 format, never loaded)
		data.put("rasse", object.getClass().getSimpleName());
		data.put("koordx", String.valueOf(object.getCoords().getX()));
		data.put("koordy", String.valueOf(object.getCoords().getY()));
		data.put("welt", String.valueOf(object.getCoords().getWelt()));
		data.put("nummer", String.valueOf(object.getNummer()));
		data.put("name", object.getName());
		data.put("beschreibung", object.getBeschreibung());
		data.put("person", String.valueOf(object.getPersonen()));
		data.put("partei", String.valueOf(object.getOwner()));
		data.put("tarnung_rasse", object.getTarnRasse());
		data.put("tarnung_partei", String.valueOf(object.getTarnPartei()));
		data.put("gebaeude", String.valueOf(object.getGebaeude()));
		data.put("schiff", String.valueOf(object.getSchiff()));
		data.put("prefix", object.getPrefix());
		data.put("sichtbarkeit", String.valueOf(object.getSichtbarkeit()));
		data.put("lebenspunkte", String.valueOf(object.getLebenspunkte()));
		data.put("lehrtage", String.valueOf(object.getLehrtage()));
		data.put("mana", String.valueOf(object.getMana()));
		data.put("aura", String.valueOf(object.getAura()));
		data.put("tempnummer", String.valueOf(object.getTempNummer()));
		data.put("sortierung", String.valueOf(object.getSortierung()));
		data.put("longorder", object.getLongOrder());
		data.put("belagert", String.valueOf(object.getBelagert()));
		data.put("einkommen", String.valueOf(object.getEinkommen()));
		data.put("kampfposition", object.getKampfposition().toString());
		data.put("id", object.getNummerBase36());

		data.put("bewacht", "0");
		if (object.getBewacht()) {
			data.put("bewacht", "1");
		}

		return new SerializedData(data);
	}

}