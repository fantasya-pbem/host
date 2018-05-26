package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Allianz;
import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Steuer;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.IDHint;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.UnitIDPool;

/**
 * @author  mogel
 */
public class Nummer extends EVABase
{
	/**
	 * @uml.property  name="db"
	 * @uml.associationEnd  
	 */
//	private Datenbank db = null;

	public Nummer()
	{
		super("nummer", "Neue Nummern vergeben");
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
        BefehlsMuster bm = null;
		List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

        bm = new BefehlsMuster(Nummer.class, 0, "^@?(nummer) ((einheit)|(gebaeude)|(gebäude)|(burg)|(schiff)|(volk)|(partei)) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "n", Art.KURZ);
        Set<String> keywords = new HashSet<String>();
        keywords.add("nummer");
        keywords.add("einheit");
        keywords.add("gebaeude");
        keywords.add("gebäude");
        keywords.add("burg");
        keywords.add("schiff");
        keywords.add("volk");
        keywords.add("partei");
        bm.setKeywords(keywords);
		bm.addHint(new IDHint(2));
		retval.add(bm);

        return retval;
    }
	
	@Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean NummerVolk(Unit u, int nummer) {
		Partei p = Partei.getPartei(u.getOwner());
		int alt = p.getNummer();

		String infoMsg = "";
		if (Partei.getPartei(nummer) != null) {
			int distance = 1;
			int newNummer = -1;
			for (boolean found = false; !found; ) {
				if (Partei.getPartei(nummer + distance) == null) {
					newNummer = nummer + distance;
					break;
				}

				if (nummer - distance > 0) {
					if (Partei.getPartei(nummer - distance) == null) {
						newNummer = nummer - distance;
						break;
					}
				}
				distance++;
			}
			infoMsg = "NUMMER VOLK - [" + Codierung.toBase36(nummer) + "] ist nicht frei, verwende [" + Codierung.toBase36(newNummer) + "].";
			nummer = newNummer;
		} else {
			infoMsg = "NUMMER VOLK - ändere von [" + Codierung.toBase36(alt) + "] auf [" + Codierung.toBase36(nummer) + "].";
		}

		Set<Unit> geaenderte = new HashSet<Unit>();
		Set<Message> betroffeneMessages = new HashSet<Message>();
		Collection<Unit> alle = new HashSet<Unit>(Unit.CACHE); // wg. ConcurrentModEx.
		for(Unit unit : alle) {
			if (unit.getOwner() == alt) {
				if (!geaenderte.contains(unit)) {
					Unit.CACHE.remove(unit);

					List<Message> my = Message.Retrieve(null, (Coords)null, u);
					for (Message msg : my) {
						Message.Cache().remove(msg);
						betroffeneMessages.add(msg);
					}
				}
				unit.setOwner(nummer);
				geaenderte.add(unit);
			}
			if (unit.getTarnPartei() == alt) {
				if (!geaenderte.contains(unit)) Unit.CACHE.remove(unit);
				unit.setTarnPartei(nummer);
				geaenderte.add(unit);
			}
		}

		p.setNummer(nummer);

		for (Unit unit : geaenderte) Unit.CACHE.add(unit);
		// Meldungen für diese Einheit auch "umnummerieren" - s.o.
		for (Message msg : betroffeneMessages) {
			Message.Cache().add(msg);
		}

//		for(Partei partei : Partei.PROXY) {
//			if (partei.getNummer() == alt) partei.setNummer(nummer);
//			for(int partnerNr : partei.getAllianzen().keySet()) {
//				if (partnerNr != alt) continue;
		
		for(Partei partei : Partei.PROXY) {
			if (partei.getNummer() == alt) partei.setNummer(nummer);
			Object[] nummern = (new HashSet(partei.getAllianzen().keySet())).toArray();
			for(int i = 0; i < nummern.length; i++) {
				int partnerNr = (Integer) nummern[i];
				if (partnerNr != alt) continue;

				// partnerNr == alt ; und es existiert eine entsprechende Allianz
				Allianz a = partei.getAllianz(alt);

				// Allianz mit der alten Nummer entfernen:
				partei.getAllianzen().remove(alt);

				// Partner-Nummer auf die neue Nummer setzen und die Allianz "registrieren"
				a.setPartner(nummer);
				partei.getAllianzen().put(a.getPartner(), a);
			}
			for(Steuer st : partei.getSteuern()) if (st.getFaction() == alt) st.setFaction(nummer);
		}
        
        // Inselnamen und -beschreibungen:
        String idAlt = Codierung.toBase36(alt);
        String idNeu = Codierung.toBase36(nummer);
        for (Region r : Region.CACHE.values()) {
            if (r.hasProperty(GameRules.INSELNAME_FUER_PARTEI + idAlt)) {
                String name = r.getStringProperty(GameRules.INSELNAME_FUER_PARTEI + idAlt);
                r.removeProperty(GameRules.INSELNAME_FUER_PARTEI + idAlt);
                r.setProperty(GameRules.INSELNAME_FUER_PARTEI + idNeu, name);
            }
            if (r.hasProperty(GameRules.INSELBESCHREIBUNG_FUER_PARTEI + idAlt)) {
                String beschreibung = r.getStringProperty(GameRules.INSELBESCHREIBUNG_FUER_PARTEI + idAlt);
                r.removeProperty(GameRules.INSELBESCHREIBUNG_FUER_PARTEI + idAlt);
                r.setProperty(GameRules.INSELBESCHREIBUNG_FUER_PARTEI + idNeu, beschreibung);
            }
        }

		new Info(infoMsg, u); // so kommt der MessageCache nicht durcheinander
		
		return true;
	}
	
	private boolean NummerEinheit(Unit u, int nummer) {
		int alt = u.getNummer();

		String infoMsg = "";
		if (Unit.Load(nummer) != null) {
			int distance = 1;
			int newNummer = -1;
			for (boolean found = false; !found; ) {
				if (Unit.Load(nummer + distance) == null) {
					newNummer = nummer + distance;
					break;
				}

				if (nummer - distance > 0) {
					if (Unit.Load(nummer - distance) == null) {
						newNummer = nummer - distance;
						break;
					}
				}
				distance++;
			}
			infoMsg = "NUMMER EINHEIT - [" + Codierung.toBase36(nummer) + "] ist nicht frei, verwende [" + Codierung.toBase36(newNummer) + "].";
			nummer = newNummer;
		} else {
			infoMsg = "NUMMER EINHEIT - ändere von [" + Codierung.toBase36(alt) + "] auf [" + Codierung.toBase36(nummer) + "].";
		}

		List<Message> my = Message.Retrieve(null, (Coords)null, u);
		Set<Message> betroffeneMessages = new HashSet<Message>();
		for (Message msg : my) {
			Message.Cache().remove(msg);
			betroffeneMessages.add(msg);
		}

		Unit.CACHE.remove(u);
		u.setNummer(nummer);
		Unit.CACHE.add(u);
		
		// Meldungen für diese Einheit auch "umnummerieren" - s.o.
		for (Message msg : betroffeneMessages) {
			Message.Cache().add(msg);
		}
		
		// wichtig: ID aus dem Vorrat freier IDs entfernen
		UnitIDPool.getInstance().remove(nummer);

		// eigentlich müssen wir wirklich nur .setOwner() machen - weder Coords noch Partei ändern sich... ?
		Building tempbuilding = null;
		for(Building b : Building.PROXY) if (b.getOwner() == alt) tempbuilding = b;
		if (tempbuilding != null) {
			tempbuilding.setOwner(nummer);
			Building.PROXY.remove(tempbuilding);
			Building.PROXY.add(tempbuilding);
		}
		for(Ship s : Ship.PROXY) if (s.getOwner() == alt) s.setOwner(nummer);

		new Info(infoMsg, u); // so kommt die Einheit in den Genuss der Meldung, und der MessageCache kommt nicht durcheinander

		return true;
	}
	
	private boolean NummerGebaeude(Unit u, int nummer) {
		Building b = Building.getBuilding(u.getGebaeude());
		if (b == null) {
			new Fehler("NUMMER GEBÄUDE - " + u + " ist in keinem Gebäude.", u);
			return true;
		}
		if (b.getOwner() != u.getNummer()) {
			new Fehler("NUMMER GEBÄUDE - " + u + " ist nicht der Besitzer.", u);
			return true;
		}

		int alt = b.getNummer();
		
		if (Building.getBuilding(nummer) != null) {
			int distance = 1;
			int newNummer = -1;
			for (boolean found = false; !found; ) {
				if (Building.getBuilding(nummer + distance) == null) {
					newNummer = nummer + distance;
					break;
				}

				if (nummer - distance > 0) {
					if (Building.getBuilding(nummer - distance) == null) {
						newNummer = nummer - distance;
						break;
					}
				}
				distance++;
			}
			new Info("NUMMER GEBÄUDE - " + Codierung.toBase36(nummer) + " ist nicht frei, verwende " + Codierung.toBase36(newNummer) + ".", u);
			nummer = newNummer;
		} else {
			new Info("NUMMER GEBÄUDE - ändere von " + Codierung.toBase36(alt) + " auf " + Codierung.toBase36(nummer) + ".", u);
		}

		Building.PROXY.remove(b);
		b.setNummer(nummer);
		Building.PROXY.add(b);
		
		for(Unit unit : Unit.CACHE.getAll(b.getCoords())) {
			if (unit.getGebaeude() == alt) {
				unit.setGebaeude(nummer);
			}
		}

		return true;
	}
	
	private boolean NummerSchiff(Unit u, int nummer)
	{
		Ship s = Ship.Load(u.getSchiff());
		if (s == null) {
			new Fehler("NUMMER - " + u + " ist auf keinem Schiff.", u);
			return true;
		}
		if (s.getOwner() != u.getNummer()) {
			new Fehler("NUMMER - " + u + " ist nicht der Kapitän.", u);
			return true;
		}

		int alt = s.getNummer();
		
		if (Ship.Load(nummer) != null) {
			int distance = 1;
			int newNummer = -1;
			for (boolean found = false; !found; ) {
				if (Ship.Load(nummer + distance) == null) {
					newNummer = nummer + distance;
					break;
				}

				if (nummer - distance > 0) {
					if (Ship.Load(nummer - distance) == null) {
						newNummer = nummer - distance;
						break;
					}
				}
				distance++;
			}
			new Info("NUMMER SCHIFF - [" + Codierung.toBase36(nummer) + "] ist nicht frei, verwende [" + Codierung.toBase36(newNummer) + "].", u);
			nummer = newNummer;
		} else {
			new Info("NUMMER SCHIFF - ändere von [" + Codierung.toBase36(alt) + "] auf [" + Codierung.toBase36(nummer) + "].", u);
		}

		s.setNummer(nummer);
		for(Unit unit : Unit.CACHE.getAll(s.getCoords())) {
			if (unit.getSchiff() == alt) unit.setSchiff(nummer);
		}
		
		return true;
	}
	
	
	@Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();
//			Partei p = Partei.getPartei(u.getOwner());

			int newId = Codierung.fromBase36(eb.getTargetId());

			String param = eb.getTokens()[1].toLowerCase();
			boolean ok = false;

			if (param.equals("einheit")) ok = NummerEinheit(u, newId);		// COMMAND NUMMER EINHEIT <nummer>
			if (param.equals("gebaeude") || param.equals("burg")) ok = NummerGebaeude(u, newId);	// COMMAND NUMMER GEBÄUDE <nummer>
			if (param.equals("schiff")) ok = NummerSchiff(u, newId);		// COMMAND NUMMER SCHIFF <nummer>
			if (param.equals("volk") || param.equals("partei")) ok = NummerVolk(u, newId);			// COMMAND NUMMER VOLK <nummer>

			if (!ok) {
				eb.setError();
				new Fehler("Parameter '" + param + "' ist unbekannt.", u);
				continue;
			}

			eb.setPerformed();

		}

	}

	@Override
    public void DoAction(Einzelbefehl eb) { }
	@Override
	public void PostAction() { }
	@Override
	public void PreAction() { }
	
}
