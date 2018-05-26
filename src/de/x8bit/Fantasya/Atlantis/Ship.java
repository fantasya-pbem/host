package de.x8bit.Fantasya.Atlantis;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Buildings.Schiffswerft;
import de.x8bit.Fantasya.Atlantis.Buildings.Seehafen;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionCheats;
import de.x8bit.Fantasya.Atlantis.Helper.ConstructionContainer;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.Atlantis.Units.Aquaner;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.Host.Reports.Writer.XMLWriter;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.FreieNummern;
import de.x8bit.Fantasya.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Basisklasse für die Schiffe
 * @author  mogel
 */

// TODO: "Owner" bedeutet bei Schiffen ploetzlich nicht "besitzender Spieler",
//       sondern "Kapitaen". Schlechter Stil und sehr verwirrend. Ersetze
//       durch eine Referenz "Kapitaen".


public abstract class Ship extends Dingens implements NamedItem {
	
	public Ship() 
	{
		setConstructionCheats(new ConstructionCheats [] { new ConstructionCheats(Schiffswerft.class, new ConstructionContainer [] { new ConstructionContainer(Holz.class, 2)}) });
	}
	
	public static final ArrayList<Ship> PROXY = new ArrayList<Ship>();
	
	public static Ship Create(String type, Coords coords) {
		Ship s = null;

		try	{
			s = (Ship) Class.forName("de.x8bit.Fantasya.Atlantis.Ships." + type).newInstance();
		} catch(Exception ex) { new BigError(ex); }

		// diverse Vorbereitungen
		s.setNummer(FreieNummern.freieNummer(Ship.PROXY));
		s.setCoords(coords);

		// Nicht-EVA-Version: In die DB schreiben:
		s.setName("Schiff " + s.getNummerBase36());

		PROXY.add(s);
		Region.Load(s.getCoords()).getShips().add(s);

		return s;
	}
	
	public static Ship Load(int nummer)
	{
		Ship s = null;
		
		for(int i = 0; i < PROXY.size(); i++) {
			if (PROXY.get(i) != null && PROXY.get(i).getNummer() == nummer) {
				return PROXY.get(i);
			}
		}

		return null;
	}
	
	public static Ship fromResultSet(ResultSet rs) {
		Ship s = null;

		try
		{
            String typ = rs.getString("type");
            s = (Ship) Class.forName("de.x8bit.Fantasya.Atlantis.Ships." + typ).newInstance();

            // die einfachen Dinge setzen
            s.setNummer(rs.getInt("nummer"));
            s.setCoords(new Coords(rs.getInt("koordx"), rs.getInt("koordy"), rs.getInt("welt")));
            s.setName(rs.getString("name"));
            s.setBeschreibung(rs.getString("beschreibung"));
            s.setOwner(rs.getInt("kapitaen"));
            s.setGroesse(rs.getInt("groesse"));
//            s.burn = (rs.getInt("burn") != 0) ? true : false;
            s.fertig = (rs.getInt("fertig") != 0) ? true : false;
            s.setKueste(rs.getString("kueste").equals("") ? null : Richtung.getRichtung(rs.getString("kueste")));
		} catch(Exception e) {
			new BigError("Fehler beim laden des Schiffes '" + Codierung.toBase36(s.getNummer()) + "'");
		}

		return s;
	}

	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle entspricht
	 */
	public Map<String, Object> getDBValues() {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("name", getName());
		fields.put("id", this.getNummerBase36());
		fields.put("koordx", getCoords().getX());
		fields.put("koordy", getCoords().getY());
		fields.put("welt", getCoords().getWelt());
		fields.put("beschreibung", getBeschreibung());
		fields.put("groesse", getGroesse());
		fields.put("type", this.getClass().getSimpleName());
//		fields.put("burn", burn ? 1 : 0);
		fields.put("burn", 0);
		fields.put("fertig", fertig ? 1 : 0);
		fields.put("kueste", getKueste() == null ? "" : getKueste().name());
		fields.put("kapitaen", getOwner());
		fields.put("nummer", getNummer());

		return fields;
	}
	
	/** verbautes Holz des Schiffes */
	private int groesse = 0;
	public int getGroesse() { return groesse; }
	public void setGroesse(int groesse) { if (groesse >= 0) this.groesse = groesse; else new SysMsg("negativer Wert bei Ship::groesse"); }
	
	/** an dieser Küste hat das Schiff angelegt */
	private Richtung kueste = null;
	public Richtung getKueste() { return kueste; }
	public void setKueste(Richtung value) { kueste = value; }
	
	/** ist das Schiff fertig gebaut */
	protected boolean fertig = false;
	public boolean istFertig() { return fertig; }
	public void setFertig(boolean value) { fertig = value; }

	/** soviele Regionen pro Runde können bereist werden */
	protected int geschwindigkeit = 0;
	public int getGeschwindigkeit() {
		Unit owner = Unit.Load(this.getOwner());
		if (owner == null) return geschwindigkeit;
		return (owner.getClass().equals(Aquaner.class) ? geschwindigkeit + 1 : geschwindigkeit); 
	}
	
	/** soviele Punkte müssen alle Matrosen zusammen an Segeltalent haben */
	protected int matrosen = 0;
	public int getMatrosen() { return matrosen; }

	/** soviel Segeln muss der Kapitän mitbringen */ 
	protected int kapitaenTalent = 0;
	public int getKapitaenTalent() { return kapitaenTalent; }
	
	/** soviel kann maximal Transportiert werden */
	protected int kapazitaet = 0;
	public int getKapazitaet() { return kapazitaet; }
	public int getKapazitaetFree() {
		int gewicht = 0;
		Region r = Region.Load(getCoords());
		for(Unit u : r.getUnits()) {
			if (u.getSchiff() == getNummer()) {
				gewicht += u.getGewicht();
			}
		}
		return getKapazitaet() - gewicht;
	}

	/**
	 * Die konkreten Schiffe müssen diese Methode jeweils individuell bereitstellen.
	 * @return Der monatliche Verfall dieses Schiffs, im derzeitigen Zustand.
	 */
	public abstract int getVerfall();

	/**
	 * Diese Methode regelt den Verfall leerstehender Schiffe
	 */
	public void verfallen() {
		int verfall = this.getVerfall();

		this.setGroesse(this.getGroesse() - verfall);
		if (this.getGroesse() <= 1) this.setGroesse(0);
	}

	/**
	 * die Zerstörung eines Schiffs beginnen
	 * @param u - zerstörende Einheit
	 */
	public abstract void Zerstoere(Unit u);

	/**
	 * das Schiff wird zerstört
	 * @param u - zerstörende Einheit
	 * @param items - eine Liste an Items die es pro Größenpunkt gibt
	 */
	protected void Zerstoere(Unit u, Item items[])
	{
		int size = getGroesse();
		String msg = u + " erhält";
		int count = 0;
		for(Item it : items)
		{
			// Anzahl pro Punkt
			int anzahl = it.getAnzahl();

			// auf Punkte vergrößern
			anzahl *= size;

			// 3/4 weg - weil Schiffe Holz-sparend gebaut werden können... :-(
			anzahl = (int) ((float) anzahl / (float) 4 + 0.5);

			// Schiffbauer können mehr retten ... Skill holen
			int skill = u.Talentwert(Schiffbau.class);

			// Prozentwert berechnen -> TW +/- 5%
			double extra = (Random.rnd(skill * 10 - 5, skill * 10 + 5) / 100.0) + 1.0;

			// neuen Wert zur Rettung merken
			anzahl *= extra;

			// Items übergeben
			u.setItem(it.getClass(), u.getItem(it.getClass()).getAnzahl() + anzahl);

			// jetzt noch die Meldung zusammen setzen
			if (count == 0) msg += " "; else msg += ", ";
			msg += anzahl + " " + it.getName();
			count++;
		}
		msg +=" durch die Zerstörung von '" + this + "'.";
		new Info(msg, u, u.getCoords());

		setGroesse(0);

		// alle Einheiten aus dem Schiff rausschmeißen
		Region r = Region.Load(u.getCoords());
		for(Unit hu : r.getUnits()) {
            if (hu.getSchiff() == getNummer()) {
                hu.setSchiff(0);
                new Info("Das Schiff '" + this + "' wurde zerstört.", hu, hu.getCoords());
            }
        }

        r.getShips().remove(this);
        Ship.PROXY.remove(this);
	}

	/**
	 * liefert den nötigen Hafen, damit das Schiff anlegen kann
	 * @return ein Hafen - Seehafen für alle
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends Building>[] neededHarbour() { return new Class[] { Seehafen.class }; }
	
	/**
	 * @return Q&D - die Einheiten sind auf dem Schiff beim Segeln
	 */
	public SortedSet<Unit> getUnits() {
		SortedSet<Unit> units = new TreeSet<Unit>();
		for(Unit unit : Unit.CACHE.getAll(this.getCoords())) {
			if (unit.getSchiff() == this.getNummer()) {
				units.add(unit);
			}
		}

		return units;
	}
	
	/**
	 * überprüft ob die Einheit u ein Gebäude oder Schiff betreten kann ... sie kann wenn
	 * <ul>
	 * <li>das Gebäude keine Besitzer hat</li>
	 * <li>die Einheit Kontakt zum Besitzer hat</li>
	 * </ul> 
	 * @param u - diese Einheit will das Object betreten
	 * @param object - das Object (Gebäude || Schiff)
	 * @return TRUE wenn das Object betreten werden kann
	 */
	public boolean canEnter(Unit u)
	{
		// wenn keiner da, dann einfach rein ^^
		if (getOwner() == 0) return true;
		
		// Owner nachladen und dann testen
		Unit owner = Unit.Load(getOwner());
		if (u.hatKontakt(owner, AllianzOption.Kontaktiere)) return true;

		return false;
	}
	

	@Override
	public void Mache(Unit unit)
	{
		// Größe für max. Produktion berechnen
		int anzahl = getConstructionSize() - getGroesse();
		if (anzahl < 0) anzahl = 0;
		
		// produzieren
		int punkte = super.GenericMake2(unit, anzahl);	// Produktionspunkte holen
		setGroesse(getGroesse() + punkte);				// das Schiff "weiter bauen"
		if (getGroesse() == getConstructionSize()) setFertig(true);		// das Schiff ist fertig
		
		// Meldung
		new Info(unit + " baut für " + punkte + " Punkte an " + this + " weiter", unit, unit.getCoords());
		
		return;
	}
	
	public void SaveXML(XMLWriter xml, Partei partei, boolean kurz)
	{
		xml.ElementStart("ship");
		xml.ElementAttribute("ref", getClass().getSimpleName());
		xml.ElementAttribute("id", getNummerBase36());
		
		xml.ElementShort("name", getName());
		if (getBeschreibung().length() > 0) xml.ElementShort("description", getBeschreibung());
		xml.ElementShort("size", getGroesse());
		xml.ElementShort("capacity", getKapazitaet());
		xml.ElementShort("capacityFree", getKapazitaetFree());
		if (getKueste() != null) xml.ElementShort("coast", getKueste().name());
		
		if (getOwner() != 0)
		{
			Unit u = Unit.Load(getOwner());
			if (u != null) {
				if (partei.cansee(u)) {
					xml.ElementStart("owner");
					xml.ElementAttribute("faction", Codierung.toBase36(u.getTarnPartei()));
					xml.ElementData(u.getNummerBase36());
					xml.ElementEnd();
				} else {
					xml.ElementStart("owner");
					xml.ElementAttribute("message", "vorhanden, aber unbekannt");
					xml.ElementAttribute("faction", 0);
					xml.ElementData(0);
					xml.ElementEnd();
				}
			}
		}
		
		// jetzt noch die Einheiten zu diesem Schiff speichern
		Region region = Region.Load(getCoords());
		boolean header = false;
		for(Unit unit : region.getUnits())
		{
			if (unit.getSchiff() == getNummer())
			{
				if (!partei.cansee(unit)) continue;
				
				if (!header) xml.ElementStart("units");
				unit.SaveXML(xml, partei, kurz);
				header = true;
			}
		}
		if (header) xml.ElementEnd();

		xml.ElementEnd();
	}

    @Override
    public int hashCode() {
        return this.getNummer();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Ship)) return false;

        Ship s = (Ship) other;

        if (s.getNummer() != this.getNummer()) return false;
        return true;
    }

}
