package de.x8bit.Fantasya.Atlantis;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Host.Reports.Writer.CRWriter;
import de.x8bit.Fantasya.Host.Reports.Writer.XMLWriter;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.FreieNummern;
import de.x8bit.Fantasya.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author  mogel  Basisklasse für die Gebäude
 */
public abstract class Building extends Dingens implements NamedItem
{
	protected Building() { }
	
	static {
		PROXY = new MapCache<Building>();
	}

	/** PROXY für alle geladen Gebäude */
	public static final MapCache<Building> PROXY;


	/**
	 * dient zum schnelleren Nachschlagen der tatsächlichen 
	 * Bewohner von Gebäuden, d.h. Unterscheidung zwischen
	 * Einheiten, die wirklich rein passen und solchen, die 
	 * im Vorgarten campieren müssen.
	 */
	private static Set<Unit> BewohnerCache;


	public static void ErneuereBewohnerCache() {
		BewohnerCache = new HashSet<Unit>();
		for (Building b : Building.PROXY) {
			for (Unit u : b.getBewohner()) {
				BewohnerCache.add(u);
			}
		}
	}

	/**
	 * dient zum schnelleren Nachschlagen der tatsächlichen
	 * Bewohner von Gebäuden, d.h. Unterscheidung zwischen
	 * Einheiten, die wirklich rein passen und solchen, die
	 * im Vorgarten campieren müssen.
	 *
	 * @return alle Einheiten, die in einem Gebäude unterkommen.
	 */
	public static Set<Unit> BewohnerCache() {
		return BewohnerCache;
	}
	
	/**
	 * Wird u.a. jedesmal aufgerufen, wenn das Talent einer Einheit in einem 
	 * Gebäude ermittelt werden soll - sollte also schnell sein.
	 * @param nummer
	 * @return das Gebäude mit der angegebenen Nummer, oder null, falls es das (derzeit) nicht gibt.
	 */
	public static Building getBuilding(int nummer)	{
		return PROXY.get(nummer);
	}
	
    public static Building fromResultSet(ResultSet rs) {
		Building b = null;

        try {
            String typ = rs.getString("type");
            b = (Building) Class.forName("de.x8bit.Fantasya.Atlantis.Buildings." + typ).newInstance();

            b.setNummer(rs.getInt("nummer"));
			b.setCoords( new Coords(rs.getInt("koordx"), rs.getInt("koordy"), rs.getInt("welt")) );
            b.setName(rs.getString("name"));
            b.setBeschreibung(rs.getString("beschreibung"));
            b.setSize(rs.getInt("size"));
            b.setFunktion(rs.getInt("funktion") == 0 ? false : true);
            b.setOwner(rs.getInt("owner"));
		} catch(Exception e) {
			throw new RuntimeException("Fehler beim Laden des Gebäudes [" + Codierung.toBase36(b.getNummer()) + "].", e);
		}
		
		return b;
    }

    public static Building Create(String type, Coords coords) {
		Building b = null;
		
		try	{
			b = (Building) Class.forName("de.x8bit.Fantasya.Atlantis.Buildings." + type).newInstance();
		} catch(Exception ex) { new BigError(ex); }
		
		// diverse Vorbereitungen
		b.setNummer(FreieNummern.freieNummer(PROXY));
		b.setCoords(coords);
		b.setName(b.getTyp() + " " + b.getNummerBase36());

		// Proxy nicht vergessen
		PROXY.add(b);
		
		return b;
	}
	
	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle entspricht
	 */
	public Map<String, Object> getDBValues() {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("nummer", getNummer());
		fields.put("id", this.getNummerBase36());
		fields.put("name", getName());
		fields.put("koordx", getCoords().getX());
		fields.put("koordy", getCoords().getY());
		fields.put("welt", getCoords().getWelt());
		fields.put("beschreibung", getBeschreibung());
		fields.put("monument", "");
		fields.put("size", getSize());
		fields.put("type", getClass().getSimpleName());
		fields.put("funktion", hatFunktion() ? 1 : 0);
		fields.put("schiff", 0);
		fields.put("wegweiser", 0);
		fields.put("owner", getOwner());

		return fields;
	}

	/** Groesse des Gebäudes */
	private int Groesse = 0;
	public int getSize() { return Groesse; }
	public void setSize(int groesse) { if (groesse >= 0) Groesse = groesse; else new SysMsg("negativer Wert bei Building::groesse"); }

	/** Typ des Gebäudes
	 * @return den spezifischen Namen dieses Gebäudes
	 */
	@Override
	public String getTyp() { return this.getClass().getSimpleName(); }
	
	/** Unterhalt gezahlt ? */
	private boolean funktion = false;
	public boolean hatFunktion() { return funktion; }
	public void setFunktion(boolean value) { funktion = value; }
	
	/**
	 * wird das Gebaeude belagert?
	 * @return eine belagernde Einheit mit dem besten Wahrnehmungstalent (durchaus mit TW 0 wenn kein Wahrnehmer vorhanden) ... NULL wenn nicht belagert
	 */
	public Unit istBelagert()
	{
		List<Unit> belagerer = new ArrayList<Unit>();
		Region region = Region.Load(this.getCoords());
		for(Unit unit : region.getUnits()) if (unit.getBelagert() == this.getNummer()) belagerer.add(unit);
		
		if (belagerer.isEmpty()) return null;
		
		Unit wahrnehmer = null;
		for(Unit unit : belagerer)
		{
			if (wahrnehmer == null)
			{
				wahrnehmer = unit;
			} else
			{
				if (wahrnehmer.Talentwert(Wahrnehmung.class) < unit.Talentwert(Wahrnehmung.class)) wahrnehmer = unit;
			}
		}
		return wahrnehmer;
	}
	
	/** Benötigter Unterhalt für das Gebäude */
	public int GebaeudeUnterhalt() { return 0; }
	
	/** benötigter Unterhalt für Einheiten die in dem Gebäude sind (es zählt getGebaeude()!) ... der Unterhalt ist <b>zusätzlich</b> */
	public int UnterhaltEinheit() { return 0; }
	
	/**
	 * überprüft ob die Einheit u ein Gebäude betreten kann ... sie kann wenn
	 * <ul>
	 * <li>das Gebäude keine Besitzer hat</li>
	 * <li>die Einheit Kontakt zum Besitzer hat</li>
	 * </ul> 
	 * @param u - diese Einheit will herein
	 * @return TRUE wenn das Gebäude betreten werden kann
	 */
	public boolean canEnter(Unit u)
	{
		// Belagerte Gebäude können nicht betreten werden
		if (istBelagert() != null) return false;
		
		// wenn keiner da, dann einfach rein ^^
		if (getOwner() == 0) return true;
		
		// Owner nachladen und dann testen
		Unit owner = Unit.Load(getOwner());
		if (owner == null) {
			new SysErr(this + " hat einen unbekannte Einheit '" + getNummerBase36() + "'");
			setOwner(0);
			return true;
		}
		if (u.hatKontakt(owner, AllianzOption.Kontaktiere)) return true;

		return false;
	}

	
	/**
	 * das Gebäude wird zerstört
	 * @param u - zerstörende Einheit
	 * @param items - eine Liste an Items die es pro Größenpunkt gibt
	 */
	protected void Zerstoere(Unit u, Item items[])
	{
		int size = getSize();
		String msg = u + " erhält";
		int count = 0;
		for(Item it : items)
		{
			// Anzahl pro Punkt
			int anzahl = it.getAnzahl();
			
			// auf Punkte vergrößern
			anzahl *= size;
			
			// die Hälfte fällt wech
			anzahl /= 2;
			
			// Baumeister können mehr retten ... Skill holen
			int skill = u.Talentwert(Burgenbau.class);
			
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
		msg +=" durch die Zerstörung von " + this + ".";
		new Info(msg, u);
		
		setSize(0); // #140
		
        Building.PROXY.remove(this);
		
		// alle Einheiten aus dem Gebäude rausschmeißen
		Region r = Region.Load(u.getCoords());
		for(Unit hu : r.getUnits()) {
            if (hu.getGebaeude() == getNummer()) {
                hu.setGebaeude(0);
                new Info("Das Gebäude " + this + " wurde zerstört.", hu);
            }
        }
        
	}
	
	/**
	 * die Zerstörung eines Gebäudes beginnen
	 * @param u - zerstörende Einheit
	 */
	public abstract void Zerstoere(Unit u);

	/**
	 * baut an einem Gebäude weiter
	 * @param u - diese Einheit will bauen
	 * @param anzahl - das ist die maximale Größe die dieses Gebäude gebaut werden darf/kann => 0 für unendlich
	 * @param skill - das benötigte Talent (bisher nur Burgenbau)
	 * @param talentwert - das mind. Talent zum Bauen
	 * @param needed - die items die benötigt werden
	 */
	protected void GenericMake(Unit u, int anzahl, Class<? extends Skill> skill, int talentwert, Item [] needed)
	{
		int tw = u.Talentwert(skill);
		
		// mögliche Anzahl berechnen
		if (anzahl != 0)
		{
			// an die maximale Größe anpassen
			int max = anzahl - getSize(); // soviel kann noch gebaut werden
			if ((tw * u.getPersonen() / talentwert) < max)
			{
				anzahl = (tw * u.getPersonen()) / talentwert;
			} else
			{
				anzahl = max;
			}
		} else
		{
			anzahl = (tw * u.getPersonen()) / talentwert;
		}
		
		// mit den vorhanden Items abgleichen
		if (needed != null)
		{
			for(Item item : needed)
			{
				Item useritem = u.getItem(item.getClass());
				if (useritem.getAnzahl() < anzahl * item.getAnzahl())
				{
					anzahl = useritem.getAnzahl() / item.getAnzahl();
					new Fehler(u + " - nur " + useritem + " vorhanden, kürze Produktion auf " + anzahl + " Größenpunkte.", u, u.getCoords());
				}
			}
			// -- jetzt existiert die max. Anzahl von dem was hergestellt werden kann, also machen wir es mal ^^
			for(Item item : needed)
			{
				Item useritem = u.getItem(item.getClass());
				useritem.setAnzahl(useritem.getAnzahl() - anzahl * item.getAnzahl());
			}
			setSize(getSize() + anzahl);

			if (!getClass().equals(Burg.class)) {
				if (anzahl > 0) new Info(u + " baut um " + anzahl + " Größenpunkte an " + this + ".", u);
			}
		} else {
			new BigError(new RuntimeException("Jedes Gebäude benötigt Baumaterialien !!"));
		}
	}
	
	/**
	 * speichert das Gebäude in den CR
	 * @param writer - passender ReportWriter
	 * @param partei - die Partei für diese Region
	 * @param kurz - TRUE wenn keine eigenen Einheiten hier
	 */
	public void SaveCR(CRWriter writer, Partei partei, boolean kurz)
	{
		writer.wl("BURG " + getNummer());
		writer.wl(getTyp(), "Typ");
		writer.wl(getName(), "Name");
		if (getBeschreibung().length() > 0) writer.wl(getBeschreibung(), "Beschr");
		writer.wl(getSize(), "Groesse");
		if (getOwner() != 0)
		{
			Unit u = Unit.Load(getOwner());
			if (u != null)
			{
				writer.wl(getOwner(), "Besitzer");
				writer.wl(u.getTarnPartei(), "Partei");
			} else
			{
				new SysErr("Owner [" + Codierung.toBase36(getOwner()) + "] existiert nicht mehr für " + this.getTyp() + " " + this + ".");
				setOwner(0);
			}
		}
	}
	
	public void SaveXML(XMLWriter xml, Partei partei, boolean kurz)
	{
		xml.ElementStart("building");
		xml.ElementAttribute("ref", getClass().getSimpleName());
		xml.ElementAttribute("id", getNummerBase36());
		xml.ElementShort("name", getName());
		if (getBeschreibung().length() > 0) xml.ElementShort("description", getBeschreibung());
		xml.ElementShort("size", getSize());
		if (getOwner() != 0)
		{
			Unit u = Unit.Load(getOwner());
			if (u != null)
			{
				xml.ElementStart("owner");
				xml.ElementAttribute("faction", Codierung.toBase36(u.getTarnPartei()));
				xml.ElementData(Codierung.toBase36(getOwner()));
				xml.ElementEnd();
			} else
			{
				new SysErr("Owner [" + Codierung.toBase36(getOwner()) + "] existiert nicht mehr für " + this.getTyp() + " " + this + ".");
				setOwner(0);
			}
		}
		
		// jetzt noch die Einheiten zu diesem Gebäude speichern
		Region region = Region.Load(getCoords());
		boolean header = false;
		for(Unit unit : region.getUnits())
		{
			if (unit.getGebaeude() == getNummer())
			{
				if (!header) xml.ElementStart("units");
				unit.SaveXML(xml, partei, kurz);
				header = true;
			}
		}
		if (header) xml.ElementEnd();
		
		xml.ElementEnd();
	}

	/**
	 * eine Einheit versucht das Gebäude zu betreten
	 * @param unit - diese Einheit will rein
	 */
	public void Enter(Unit unit)
	{
		unit.setGebaeude(getNummer());
		if (getOwner() == 0) {
			Building.PROXY.remove(this);
			setOwner(unit.getNummer());
			Building.PROXY.add(this);
		}
			
		new Info(unit + " betritt das Gebäude " + this + ".", unit);
	}

	/**
	 * @return Q&D - die Einheiten sind im Gebäude (bzw. sollen/wollen ins Gebäude, wenn es zu klein sein sollte)
	 */
	public SortedSet<Unit> getUnits() {
		SortedSet<Unit> units = new TreeSet<Unit>();
		for(Unit unit : Unit.CACHE.getAll(this.getCoords())) {
			if (unit.getGebaeude() == this.getNummer()) {
				units.add(unit);
			}
		}
		return units;
	}

	/**
     * @return Menge aller Parteien, die tatsächlich innerhalb der Mauern vertreten sind
     */
    public Set<Partei> getBewohnerParteien() {
        Set<Partei> retval = new HashSet<Partei>();
        for (Unit u : getBewohner()) {
            retval.add(Partei.getPartei(u.getOwner()));
        }
        return retval;
    }
    
    /**
	 * @return Liste aller Einheiten, die tatsächlich in das Gebäude passen.
	 */
	public SortedSet<Unit> getBewohner()	{
		SortedSet<Unit> bewohner = new TreeSet<Unit>();

		// die verfügbare Größe
		int groesse = getSize();

		// alle Einheiten abklappern, der vorgegebenen Reihenfolge nach.
		for(Unit unit : getUnits()) {
			// passt die aktuelle Einheit noch rein?
			if (groesse - unit.getPersonen() < 0) break;
			
			groesse -= unit.getPersonen();
			bewohner.add(unit);
		}

		return bewohner;
	}

    @Override
    public int hashCode() {
        return this.getNummer();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Building)) return false;
        final Building b = (Building) other;
        if (b.getNummer() != this.getNummer()) return false;
		
        return true;
    }
}
