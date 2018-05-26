package de.x8bit.Fantasya.Host.EVA;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Schiffswerft;
import de.x8bit.Fantasya.Atlantis.Items.PersistentResource;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.Atlantis.Skills.Strassenbau;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.util.AnzahlHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.ItemHint;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsMuster.Art;
import de.x8bit.Fantasya.Host.EVA.util.DoppelteAusfuehrungException;
import de.x8bit.Fantasya.Host.EVA.util.IDHint;
import de.x8bit.Fantasya.util.Codierung;

public class Produktion extends EVABase {
	/** 
	 * <p>speichert (sehr heimlich - ist von außen nicht zu sehen / zugreifbar und soll das auch nicht sein!)
	 * persistente Ressourcen zwecks Wiederherstellung der Regionen nach der Produktion.</p>
	 * <p>Evtl. mit eleganterer Implementierung ersetzen.</p>
	 */
	private static List<ResourceRecord> persistentResources;

	public Produktion()	{
		super("mache", "Produktion von allem Möglichen");	// Regionen weise!
		
		addTemplate("");
		for (BefehlsMuster pattern : getMuster()) addTemplate(pattern.getRegex());
	}

    public static List<BefehlsMuster> getMuster() {
		BefehlsMuster bm = null;
        List<BefehlsMuster> retval = new ArrayList<BefehlsMuster>();

		// Items - alle Namen auflisten:
        List<String> itemNames = new ArrayList<String>();
        for (Paket p : Paket.getPaket("Items")) {
            for (String name : getNames(p)) {
                itemNames.add(name);
            }
        }
        // ... und als RegEx formulieren:
        StringBuilder regEx = new StringBuilder();
        regEx.append("(");
        for (String name : itemNames) {
            if (regEx.length() > 1) regEx.append("|");
            regEx.append("(" + name + ")");
        }
        regEx.append(")");

        // Variante 1 - Items ohne Anzahl
        bm = new BefehlsMuster(Produktion.class, 1,
				"^(mache)[n]? " + regEx + "([ ]+(\\/\\/).*)?",
				"m", Art.LANG);
		bm.addHint(new ItemHint(1));
        bm.setKeywords("mache", "machen");
		retval.add(bm);
        // Variante 2 - Items mit Anzahl
        bm = new BefehlsMuster(Produktion.class, 2,
				"^(mache)[n]? ([0-9]+) " + regEx + "([ ]+(\\/\\/).*)?",
				"m", Art.LANG);
		bm.addHint(new AnzahlHint(1));
		bm.addHint(new ItemHint(2));
        bm.setKeywords("mache", "machen");
		retval.add(bm);





        // Variante 11 - Strassen
		StringBuilder richtungen = new StringBuilder();
		for (Richtung r : Richtung.values()) {
			if (richtungen.length() > 0) richtungen.append("|");
			richtungen.append(r.getShortcut().toLowerCase());
		}
		for (Richtung r : Richtung.values()) {
			if (richtungen.length() > 0) richtungen.append("|");
			richtungen.append(r.toString().toLowerCase());
		}
        bm = new BefehlsMuster(Produktion.class, 11, "^(mache)[n]? (strasse) (" + richtungen + ")([ ]+(\\/\\/).*)?", "m", Art.LANG);
        bm.setKeywords("mache", "machen", "strasse", "straße");
        retval.add(bm);





        // Schiffe - alle Namen auflisten:
        List<String> shipNames = new ArrayList<String>();
        for (Paket p : Paket.getPaket("Ships")) {
            shipNames.add(p.Klasse.getName().toLowerCase());
        }
        // hier beschummeln wir die Spieler ein bisschen...
        shipNames.remove("plmyvfastap");
		shipNames.add("plm[a-z]*");

        // ... und als RegEx formulieren:
        regEx = new StringBuilder();
        regEx.append("(");
        for (String name : shipNames) {
            if (regEx.length() > 1) regEx.append("|");
            regEx.append("(" + name + ")");
        }
        regEx.append(")");

        // Variante 21 - Schiff anfangen
        bm = new BefehlsMuster(Produktion.class, 21, "^(mache)[n]? " + regEx + "([ ]+(\\/\\/).*)?", "m", Art.LANG);
        bm.setKeywords("mache", "machen");
        retval.add(bm);
        // Variante 22 - Schiff weiterbauen
		bm = new BefehlsMuster(Produktion.class, 22, "^(mache)[n]? (schiff) [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "m", Art.LANG);
		bm.addHint(new IDHint(2));
        bm.setKeywords("mache", "machen", "schiff");
        retval.add(bm);




        // Gebaeude - alle Namen auflisten:
        List<String> buildingNames = new ArrayList<String>();
        for (Paket p : Paket.getPaket("Buildings")) {
            buildingNames.addAll(EVABase.getNames(p)); // damit werden auch "ComplexNames" berücksichtigt, bspw. Varianten mit / ohne Umlaut
        }
        // ... und als RegEx formulieren:
        StringBuilder regExGrundstein = new StringBuilder();
        regExGrundstein.append("(");
		regEx = new StringBuilder(); // fürs Weiterbauen -
		regEx.append("((gebäude)|(gebaeude)|(burg)"); // mit den neutralen Begriffen
        for (String name : buildingNames) {
            if (regExGrundstein.length() > 1) regExGrundstein.append("|");
            regExGrundstein.append("(" + name + ")");

			regEx.append("|(").append(name).append(")");
        }
        regExGrundstein.append(")");
		regEx.append(")");

        // Variante 31 - Gebaeude anfangen
        bm = new BefehlsMuster(Produktion.class, 31, "^(mache)[n]? " + regEx + "([ ]+(\\/\\/).*)?", "m", Art.LANG);
        bm.setKeywords("mache", "machen");
        retval.add(bm);
        // Variante 32 - Gebaude weiterbauen
        bm = new BefehlsMuster(Produktion.class, 32, "^(mache)[n]? " + regEx + " [a-z0-9]{1,4}([ ]+(\\/\\/).*)?", "m", Art.LANG);
		bm.addHint(new IDHint(2));
        bm.setKeywords("mache", "machen", "gebäude", "gebaeude", "burg");
        retval.add(bm);


        return retval;
    }

	@Override
	public boolean DoAction(Unit u, String[] befehl) { return false; }

	@Override
	public void DoAction(Region r, String befehl) {
		List<Einzelbefehl> befehle = BefehlsSpeicher.getInstance().get(this.getClass(), r.getCoords());

		for (Einzelbefehl eb : befehle) {
			if (eb.isPerformed()) throw new DoppelteAusfuehrungException(eb.toString());

			// Angaben über die "Akteure" selbst:
			Unit u = eb.getUnit();
			int variante = eb.getVariante();

			
			// MACHE <item>				-> MACHE <anzahl> <item> var. 1
			// MACHE <anzahl> <item>	=> neue Items var. 2
			if ((variante == 1) || (variante == 2)) {
				// Item herstellen
				int anzahl = 0;
				if (variante == 2) anzahl = eb.getAnzahl();

				Class<? extends Item> clazz = eb.getItem();

				Item item = u.getItem(clazz);
				if (anzahl == 0) {
					item.Mache(u);
				} else {
					item.Mache(u, anzahl);
				}
				
				eb.setPerformed();
			}

			// MACHE STRASSE <richtung>	-> Fortsetzen bzw. Anfangen
			if (variante == 11) {
				Strasse_Continue(u, eb);
				eb.setPerformed();
			}


			// Schiffe:
			if ((variante == 21) || (variante == 22)) {
				if (u.Talentwert(Schiffbau.class) == 0) {
					new Fehler(u + " hat keine Ahnung vom Schiffe bauen.", u);
				}
			}
			// MACHE <schifftyp>		-> neues Schiff
			if (variante == 21) {
				@SuppressWarnings("unchecked")
				Class<? extends Ship> clazz = (Class<? extends Ship>) Paket.FindShip(eb.getTokens()[1]).Klasse.getClass();
				if (clazz == null) {
					eb.setError();
					new Fehler(u + " hat keinen Bauplan für '" + eb.getTokens()[1] + "'.", u);
					continue;
				}
				Schiff_Start(u, eb, clazz);

				eb.setPerformed();
			}
			// MACHE SCHIFF <nummer>	-> Continue Schiff
			if (variante == 22) {
				Schiff_Continue(u, eb);
				eb.setPerformed();
			}

			
			// GEBÄUDE:
			if ((variante == 31) || (variante == 32)) {
				if (u.Talentwert(Burgenbau.class) == 0) {
					eb.setError();
					new Fehler(u + " hat keine Ahnung vom Burgenbau.", u);
					continue;
				}
			}
			// MACHE <gebäudetyp>		-> neues Gebäude
			// MACHE BURG				=> MACHE <gebaeudetyp> | <gebaeudetyp> => "Burg"
			if (variante == 31) {
				// Gebäude anfangen
                Paket paket = Paket.FindBuilding(eb.getTokens()[1]);
                if (paket == null) paket = Paket.FindBuilding("Burg"); // unbekanntes Gebäude?
				@SuppressWarnings("unchecked")
				Class<? extends Building> clazz = (Class<? extends Building>) paket.Klasse.getClass();
				if (clazz == null) {
					eb.setError();
					new Fehler(u + " hat keinen Bauplan für '" + eb.getTokens()[1] + "'.", u);
					continue;
				}
				Gebaeude_Start(u, eb, clazz);

				eb.setPerformed();
			}
			// MACHE GEBAEUDE <nummer>	-> Continue Gebäude
			// MACHE BURG <nummer>		=> MACHE GEBAEUDE <nummer>
			if (variante == 32) {
				Gebaeude_Continue(u, eb);
				eb.setPerformed();
			}

			// MACHE <whatever>			=> welcome to the future
		}
	}
	
    @Override
	public void PostAction() {
		// zerstörte Gebäude und Referenzen bei den Einheiten zu diesen Gebäuden löschen
		List<Building> loeschliste = new ArrayList<Building>();
		for (Building b : Building.PROXY) {
			if (b.getSize() > 0) continue;

			int id = b.getNummer();

			for (Unit u : Unit.CACHE.getAll(b.getCoords())) {
				if (u.getBelagert() == id) {
					new Info(b + " ist zerstört - die Belagerung wurde erfolgreich beendet.", u);
					u.setBelagert(0);
				}
				if (u.getGebaeude() == id) {
					new Info(b + " ist zerstört worden - wir stehen vor den Trümmern.", u);
					u.setGebaeude(0);
				}
			}

			loeschliste.add(b);
		}
		for (Building gone : loeschliste) {
			Building.PROXY.remove(gone);
		}
		new SysMsg(loeschliste.size() + " Gebäude gelöscht, deren Größe 0 war.");

		
        
		ZerstoereSchiffe();
        
		// zerstörte Schiffe und Referenzen bei den Einheiten zu diesen Schiffen löschen
		List<Ship> loeschliste2 = new ArrayList<Ship>();
		for (Ship s : Ship.PROXY) {
			if (s.getGroesse() > 0) continue;

			int id = s.getNummer();

			for (Unit u : Unit.CACHE.getAll(s.getCoords())) {
				if (u.getSchiff() == id) {
					u.setSchiff(0);
					Region r = Region.Load(s.getCoords());
					if (r.istBetretbar(u)) {
						new Info(s + " ist zerstört worden - wir stehen vor den traurigen Resten.", u);
					} else if (r instanceof Ozean) {
						new Info(s + " ist versenkt worden! Rette sich wer kann!", u);
					} else {
						// weder betret- noch segelbar? Chaos?
					}
				}
			}

			loeschliste2.add(s);
		}
		for (Ship gone : loeschliste2) {
            Region.Load(gone.getCoords()).getShips().remove(gone);
			Ship.PROXY.remove(gone);
		}
		new SysMsg(loeschliste2.size() + " Schiffe gelöscht -> size == 0");



		// Mantis #181 - böser Hack, siehe PreAction()
		// persistente Resourcen in den Regionen wiederherstellen
		for (ResourceRecord memory : Produktion.persistentResources) { // static list
			Region r = Region.Load(memory.getCoords());
			r.setResource(memory.getResource(), memory.getAnzahl());
		}
		// Speicher freigeben:
		Produktion.persistentResources.clear();
	}

	@Override
	public void PreAction() {
		// Mantis #181 - eher ein böser Hack, vielleicht ein zusätzliches Feld "permanentAnzahl" 
		// in der Tabelle resourcen ODER eine Nutzung von property_regionen
		// siehe auch PostAction()
		//
		// persistente Resourcen der Regionen vor dem Abbau merken:
		List<Paket> allItems = Paket.getPaket("Items");
		List<Item> persistentResourceTypes = new ArrayList<Item>();
		for (Paket p:allItems) {
			Item i = (Item)p.Klasse;
			if (i instanceof PersistentResource) {
				persistentResourceTypes.add(i);
			}
		}

		Produktion.persistentResources = new ArrayList<ResourceRecord>(); // static list
		for (Region r : Region.CACHE.values()) {
			for (Item i:persistentResourceTypes) {
				int anzahl = r.getResource(i.getClass()).getAnzahl();
				if (anzahl > 0) {
					ResourceRecord memory = new ResourceRecord(r.getCoords(), anzahl, i.getClass());
					Produktion.persistentResources.add(memory); // static list
				}
			}
		}
	}
	
	/** zerstört langsam Schiffe die keinen Besitzer haben */
	private void ZerstoereSchiffe()
	{
		// Schiffe beschädigen, die keinen Besitzer haben
		// wenn sie nicht fertig sind, dann zerfallen sie wesentlich schneller
		for (Ship s : Ship.PROXY) {
			if (s.getOwner() == 0) s.verfallen();
		}
	}

	

	// COMMAND MACHE STRASSE <richtung>
	public void Strasse_Continue(Unit u, Einzelbefehl befehl){
		String richtungName = befehl.getTokens()[2];
		Richtung richtung = Richtung.getRichtung(richtungName);
		if (richtung == null) {
			befehl.setError();
			new Fehler(u + " - die Richtung '" + richtungName + "' ist unbekannt.", u);
			return;
		}

		// Talentwert überprüfen
		int tw = u.Talentwert(Strassenbau.class);
		if (tw == 0) {
			befehl.setError();
			new Fehler(u + " hat keine Ahnung vom Straßenbau.", u);
			return;
		}
		
		// Baumöglichkeiten überprüfen
		Region r = Region.Load(u.getCoords());
		if (r.getSteineFuerStrasse() == 0) {
			befehl.setError();
			new Fehler("In " + r + " kann keine Strasse gebaut werden.", u);
			return;
		}
		
		// benötigten Steine ausrechnen
		int bedarf = r.getSteineFuerStrasse() - r.getStrassensteine(richtung);
		
		// möglichen Steine berechnen 
		int steine = tw * u.getPersonen();
		if (steine > bedarf) steine = bedarf; // ohne Meldung
		
		if (steine > u.getItem(Stein.class).getAnzahl()) {
			steine = u.getItem(Stein.class).getAnzahl();
			new Info(u + " hat zu wenig Steine um voll bauen zu können.", u);
		}

		
		// Strasse bauen
		r.setStrassensteine(richtung, r.getStrassensteine(richtung) + steine);
		u.getItem(Stein.class).setAnzahl(u.getItem(Stein.class).getAnzahl() - steine);
		if (r.getStrassensteine(richtung) == r.getSteineFuerStrasse()) {
			new Info(u + " stellt mit " + steine + " Steinen die Strasse nach " + richtung + " fertig.", u);
		} else {
			new Info(u + " verbaut " + steine + " Steine an der Strasse nach " + richtung + ".", u);
		}
	}

	// COMMAND MACHE GEBÄUDE <nummer>
	public void Gebaeude_Continue(Unit u, Einzelbefehl befehl) {
		String id = befehl.getTargetId();

		int nummer = 0;
		try { nummer = Codierung.fromBase36(id); } catch(Exception ex) { /* nüschts */ }
		if (nummer == 0) {
			befehl.setError();
			new Fehler(u + " - das Gebäude '" + id + "' wurde nicht gefunden.", u);
			return;
		}
		Building building = Building.getBuilding(nummer);
		if (building == null) {
			befehl.setError();
			new Fehler(u + " - das Gebäude [" + id + "] wurde nicht gefunden.", u);
			return;
		}
			
		if (!u.getCoords().equals(building.getCoords())) {
			befehl.setError();
			new Fehler(u + " - das Gebäude [" + id + "] wurde nicht gefunden.", u);
			return;
		}

		// endlich!
		building.Mache(u);
	}
	
	// COMMAND MACHE <gebäude>
	public void Gebaeude_Start(Unit u, Einzelbefehl befehl, Class<? extends Building> clazz) {
		Building building = Building.Create(clazz.getSimpleName(), u.getCoords());
		building.Mache(u);

		@SuppressWarnings("unused")
		Region r = Region.Load(u.getCoords());
		// r.getBuildings().add(building); // wird jetzt schon in Building.Create erledigt.
		
		if (building.getSize() > 0)	{
			// das Gebäude wurde angefangen ... die Wahrscheinlichkeit das dieses Gebäude
			// einen Besitzer hat könnte, aber wirklich nur *könnte*, gegen 0 tendieren
			// als setzen wir den Erbauer mal pauschal in das Gebäude
			u.Enter(building);
            building.setFunktion(true);

			// Befehl anpassen:
			befehl.setTokens(new String[]{"MACHE", "GEBÄUDE", building.getNummerBase36()});
		} else {
			befehl.setError();
		}
	}
	
	public void Schiff_Continue(Unit u, Einzelbefehl befehl) {
		String id = befehl.getTargetId();

		int nummer = 0;
		try { nummer = Codierung.fromBase36(id); } catch(Exception ex) { /* nüschts */ }
		if (nummer == 0) {
			befehl.setError();
			new Fehler(u + " - das Schiff '" + id + "' wurde nicht gefunden.", u);
			return;
		}
		Ship ship = Ship.Load(nummer);
		if (ship == null) {
			befehl.setError();
			new Fehler(u + " - das Schiff '" + id + "' wurde nicht gefunden.", u);
			return;
		}

		if (!u.getCoords().equals(ship.getCoords())) {
			befehl.setError();
			new Fehler(u + " steht nicht in der gleichen Region wie " + ship + ".", u);
			return;
		}

		// endlich!
		ship.Mache(u);
		
	}
	
	public void Schiff_Start(Unit u, Einzelbefehl befehl, Class<? extends Ship> clazz) {
		Ship ship = Ship.Create(clazz.getSimpleName(), u.getCoords());
		ship.Mache(u);

		@SuppressWarnings("unused")
		Region r = Region.Load(u.getCoords());
		// r.getShips().add(ship); // wird jetzt schon in Ship.Create erledigt.

		if (ship.getGroesse() > 0)	{
			// Befehl anpassen:
			befehl.setTokens(new String[]{"MACHE", "SCHIFF", ship.getNummerBase36()});

			Building building = Building.getBuilding(u.getGebaeude());
			if (building == null) {
				// das Schiff wurde angefangen ... die Wahrscheinlichkeit das dieses Gebäude
				// einen Besitzer hat könnte, aber wirklich nur *könnte*, gegen 0 tendieren
				// also setzen wir den Erbauer mal pauschal in das Schiff
				// jetzt rein da ... ganz sauber ohne Haxx
				u.Enter(ship);
			} else {
				// jetzt testen ob die Einheit in der Werft steht ... da darf sie nämlich nicht raus
				if (building.getClass().equals(Schiffswerft.class)) {
					// gut es sind offiziell Schiffsbauer ... also kein Enter
					// sondern mittels Haxx setzen
					// ggf. muss die Einheit aus dem letzen Schiff raus
					if (u.getSchiff() > 0) u.Leave();
					// jetzt der Haxx
					u.setSchiff(ship.getNummer());
					ship.setOwner(u.getNummer());
				} else {
					// ein anderes Gebäude ... also raus und rauf ... without Haxx again
					u.Enter(ship);
				}
			}
			// entgegen dem Gebäude wird hier direkt gesetzt ... dadurch ist es möglich
			// das die Einheit in einem Gebäude bleibt

		} else {
			befehl.setError();
			return;
		}
	}

    @Override
    public void DoAction(Einzelbefehl eb) { }


	private class ResourceRecord {
		Coords coords;
		int anzahl;
		Class<? extends Item> resource;

		public int getAnzahl() {
			return anzahl;
		}

		public Coords getCoords() {
			return coords;
		}

		public Class<? extends Item> getResource() {
			return resource;
		}
		
		public ResourceRecord(Coords coords, int anzahl, Class<? extends Item> resource) {
			this.coords = coords;
			this.anzahl = anzahl;
			this.resource = resource;
		}

	}
}
