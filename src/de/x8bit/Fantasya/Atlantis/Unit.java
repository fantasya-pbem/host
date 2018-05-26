package de.x8bit.Fantasya.Atlantis;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Buildings.Bergwerk;
import de.x8bit.Fantasya.Atlantis.Buildings.Holzfaellerhuette;
import de.x8bit.Fantasya.Atlantis.Buildings.Mine;
import de.x8bit.Fantasya.Atlantis.Buildings.Saegewerk;
import de.x8bit.Fantasya.Atlantis.Buildings.Sattlerei;
import de.x8bit.Fantasya.Atlantis.Buildings.Schiffswerft;
import de.x8bit.Fantasya.Atlantis.Buildings.Schmiede;
import de.x8bit.Fantasya.Atlantis.Buildings.Steinbruch;
import de.x8bit.Fantasya.Atlantis.Buildings.Steingrube;
import de.x8bit.Fantasya.Atlantis.Buildings.Werkstatt;
import de.x8bit.Fantasya.Atlantis.Effects.EFXBewegungSail;
import de.x8bit.Fantasya.Atlantis.Helper.Kampfzauber;
import de.x8bit.Fantasya.Atlantis.Helper.MapCache;
import de.x8bit.Fantasya.Atlantis.Helper.SortedCache;
import de.x8bit.Fantasya.Atlantis.Items.Alpaka;
import de.x8bit.Fantasya.Atlantis.Items.Armbrust;
import de.x8bit.Fantasya.Atlantis.Items.Bogen;
import de.x8bit.Fantasya.Atlantis.Items.Einhorn;
import de.x8bit.Fantasya.Atlantis.Items.Elefant;
import de.x8bit.Fantasya.Atlantis.Items.Flugdrache;
import de.x8bit.Fantasya.Atlantis.Items.Greif;
import de.x8bit.Fantasya.Atlantis.Items.Kamel;
import de.x8bit.Fantasya.Atlantis.Items.Katapult;
import de.x8bit.Fantasya.Atlantis.Items.Kriegselefant;
import de.x8bit.Fantasya.Atlantis.Items.Kriegshammer;
import de.x8bit.Fantasya.Atlantis.Items.Kriegsmastodon;
import de.x8bit.Fantasya.Atlantis.Items.Mastodon;
import de.x8bit.Fantasya.Atlantis.Items.Pegasus;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Items.Streitaxt;
import de.x8bit.Fantasya.Atlantis.Items.AnimalResource;
import de.x8bit.Fantasya.Atlantis.Items.Wagen;
import de.x8bit.Fantasya.Atlantis.Items.Zotte;
import de.x8bit.Fantasya.Atlantis.Messages.Battle;
import de.x8bit.Fantasya.Atlantis.Messages.Bewegung;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Ships.Boot;
import de.x8bit.Fantasya.Atlantis.Ships.Drachenschiff;
import de.x8bit.Fantasya.Atlantis.Ships.Galeone;
import de.x8bit.Fantasya.Atlantis.Ships.Karavelle;
import de.x8bit.Fantasya.Atlantis.Ships.Langboot;
import de.x8bit.Fantasya.Atlantis.Ships.Tireme;
import de.x8bit.Fantasya.Atlantis.Skills.Armbrustschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Ausdauer;
import de.x8bit.Fantasya.Atlantis.Skills.Bergbau;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Handel;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Holzfaellen;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Skills.Ruestungsbau;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.Atlantis.Skills.Segeln;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Skills.Steinbau;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Skills.Waffenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Wagenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Units.Troll;
import de.x8bit.Fantasya.Host.BefehlsSpeicher;
import de.x8bit.Fantasya.Host.EVA.EVABase;
import de.x8bit.Fantasya.Host.EVA.Lehren.LehrenRecord;
import de.x8bit.Fantasya.Host.EVA.Lernen;
import de.x8bit.Fantasya.Host.EVA.TempEinheiten;
import de.x8bit.Fantasya.Host.EVA.util.BefehlsListe;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.EVA.util.ReiseVerb;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.Host.Reports.Writer.CRWriter;
import de.x8bit.Fantasya.Host.Reports.Writer.XMLWriter;
import de.x8bit.Fantasya.Host.Reports.Writer.ZRWriter;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StringUtils;
import de.x8bit.Fantasya.util.UnitIDPool;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * im Gegensatz zum alten Atlantis ist ja nun OOP angesagt dabei kommt nun auch Vererbung ins Spiel ... alle Einheiten, also Spieler oder Monster erben von Unit
 * @author  mogel
 */

@SuppressWarnings("rawtypes")
public abstract class Unit extends Atlantis implements Comparable {

    public final static String TAG_GEWICHT = "weight";
    public final static String TAG_GESAMTGEWICHT = "Gesamtgewicht";
    public final static String TAG_FREIE_KAPAZITAET = "freieKapazitaet";
    public final static String TAG_FREIE_KAPAZITAET_BERITTEN = "freieKapazitaetBeritten";
    public final static String TAG_KAPAZITAET_GRUND = "freieKapazitaetGrund";

	// wieviel die Einheit minimal und maximal hungert
	protected int minHunger = 0;
	protected int maxHunger = 0;
	
	public Unit() {
		super();
		
		this.sortierGlueck = Random.rnd(-16777216, +16777215);
    }

	public final int sortierGlueck;

	public abstract boolean istSpielerRasse();

	/** Anzahl der maximalen Magier ... default: 3 */
	public int maxMagier() { return 3; }

	/**
	 * Maximale Anzahl Migranten - Personen fremder Rassen bei einer Partei dieser Rasse (selbe wie die Unit)
	 * @param personenDerPartei Anzahl aller Personen in der fraglichen Partei
	 */
	public int maxMigranten(int personenDerPartei) { return 0; }
	
	
	/**
	 * liefert den Rassennamen aus der Datenbank - für die Datenbank
	 * @return Rassenname
	 */
	public String getRasse() { return getClass().getSimpleName(); }

	/**
	 * @return eine menschenlesbare passende Beschreibung Rasse, bspw. (1) "Mensch" oder (27) "Orks"
	 */
	public abstract String getRassenName();
	
	/** Anzahl der Personen in dieser Einheit */
	private int personen;
	public int getPersonen() { return personen; }
	public void setPersonen(int value) { 
        if (value >= 0) {
            personen = value;
        } else {
            throw new IllegalArgumentException("negativer Wert für Personen");
        }
    }

	/** Talente der Einheit */
	private final List<Skill> skills = new ArrayList<Skill>();
	
	public int getLerntage(Class<? extends Skill> skillType) {
		for (Skill skill : skills) {
			if (skill.getClass().equals(skillType)) {
				return skill.getLerntage();
			}
		}

		return 0;
	}
	public Skill getSkill(Class<? extends Skill> skill)	{
		Skill s = null;
		
		// bekannten Skill suchen
		for(int i = 0; i < skills.size(); i++) {
			s = skills.get(i);
			if (s.getClass().equals(skill)) return s; // er hat es ... also zurück damit
		}
		
		// ... ansonsten erstellen
		try	{
			s = skill.newInstance();
			skills.add(s); // merken
		} catch(Exception ex) {
            // wenn es sich um die Basisklasse handelt, verlagern wir den Fehler zurück zum Verursacher:
            if (skill == Skill.class) return null;
            new BigError(ex, "Habe versucht, " + skill.getCanonicalName() + " zu instantiieren.");
        }
		
		return s;
	}
    
	public void setSkill(Class<? extends Skill> skill, int lerntage)
	{
		Skill s = getSkill(skill);
		s.setLerntage(lerntage);
	}
    /**
     * @return eine unveränderliche Ansicht der Talente dieser Einheit
     */
    public List<Skill> getSkills() {
        return Collections.unmodifiableList(skills);
    }
	
	private final Set<Spell> spells = new TreeSet<Spell>();
	public Spell getSpell(Class<? extends Spell> spell)	{
		for(Spell s : spells) {
			if (s.getClass().equals(spell)) {
				return s;
			}
		}
		return null;
	}

	public void setSpell(Spell spell) {
		spells.add(spell);
	}
	public Set<Spell> getSpells() {
		return Collections.unmodifiableSet(spells); }
	
	/** Items der Einheit */
	private final ArrayList<Item> Items = new ArrayList<Item>();
	public void clearItems() {
		Items.clear();
	}
	public List<Item> getItems() {
		return Collections.unmodifiableList(Items);
	}
	public Item getItem(Class<? extends Item> item)
	{
		Item it = null;
		
		// bekanntes Item suchen
		for(int i = 0; i < Items.size(); i++)
		{
			it = Items.get(i);
			if (it.getClass() == item) return it; // er hat es ... also zurück damit
		}
		
		// ... ansonsten erstellen
		try
		{
			it = (Item) item.newInstance();
			Items.add(it); // merken
		} catch(Exception ex) { new BigError(ex); }
		
		return it;
	}
	public void setItem(Class<? extends Item> item, int anzahl)
	{
		Item it = getItem(item);
		it.setAnzahl(anzahl);
	}
	public void addItem(Class<? extends Item> item, int anzahl)
	{
		Item it = getItem(item);
		it.setAnzahl(it.getAnzahl() + anzahl);
	}
    
    /**
     * @return TRUE, wenn die Einheit mindestens 1 Tier bei sich führt (d.h. als Item)
     */
    public boolean hatTiere() {
        for (Item it : getItems()) {
            if ((it instanceof AnimalResource) && (it.getAnzahl() > 0)) return true;
        }
        return false;
    }

    /**
     * @param gesucht Klasse des fraglichen Items
     * @return true, wenn die Einheit wenigstens 1 solches Item hat
     */
    public boolean hat(Class<? extends Item> gesucht) {
        for (Item it : getItems()) {
            if (it.getAnzahl() <= 0) continue;
            if (it.getClass().equals(gesucht)) return true;
        }
        return false;
    }
    
    
	
	
	
	/** Q&D - Anzahl von Irgendwas was gerade benötigt wird */
	public int wants = 0;
	/** Q&D - Anzahl von Irgendwas was gerade benötigt wird */
	public int wants2 = 0;
	
	/**
	 * Ein Map mit "Tags" wie sie z.B. von Magellan und Vorlage verwendet werden.
	 */
	protected final Map<String, String> tags = new HashMap<String, String>();
	
	public void setTag(String name, String value) {
		this.tags.put(name, "\"" + value + "\"");
	}
	public void setTag(String name, int value) {
		this.tags.put(name, Integer.toString(value));
	}
	public String getTag(String name) {
		return tags.get(name);
	}
	public void clearTag(String name) {
		tags.remove(name);
	}


	/** benötigtes Silber zum Rekrutieren */
	protected int RekrutierungsKosten = 100;
	public int getRekrutierungsKosten() { return RekrutierungsKosten; }
	
	/** Tarnrasse für Echsen */
	protected String TarnRasse = "";
	public String getTarnRasse() { return TarnRasse; }
	public void setTarnRasse(String value) {
		if (value.length() != 0) {
			new Fehler("Tarnung der Rasse beherrschen nur Echsen.", this, this.getCoords());
		} else {
			TarnRasse = value;
		}
	}
	
	/** Tarnung für Parteizugehörigkeit */
	private int TarnPartei = 0;
	public int getTarnPartei() { return TarnPartei; }
	public void setTarnPartei(int value) { TarnPartei = value; }
	
	/** Einheit ist in diesem Gebäude */
	private int Gebaeude = 0;
	public int getGebaeude() { return Gebaeude; }
	public void setGebaeude(int value) { Gebaeude = value; }

	/**
	 * liefert TRUE wenn die <b>in dem</b> Gebäude steht ... das Ergebniss
	 * kann von getGebaeude() abweichen ... da getGebaeude() nur aussagt das
	 * die Einheit in das Gebäude will (die Einheit wurde dem Gebäude zugewiesen)
	 * @return TRUE wenn die Einheit komplett in ihrem zugewiesen Gebäude steht
	 */
	public boolean imGebaeude()	{
		// in gar keinem Gebäude
		if (getGebaeude() == 0) return false;

		// das Gebäude holen
		Building building = Building.getBuilding(getGebaeude());
		if (building == null) {
			throw new IllegalStateException(this + " ist angeblich im Gebäude [" + Codierung.toBase36(getGebaeude()) + "], aber das gibt es gar nicht!");
		}

		return building.getBewohner().contains(this);
	}

	
	/** dieses Gebäude wird belagert */
	private int Belagert = 0;
	public int getBelagert() { return Belagert; }
	public void setBelagert(int value) { Belagert = value; }
	
	/** Einheit ist auf diesem Schiff */
	private int Schiff = 0;
	public int getSchiff() { return Schiff; }
	public void setSchiff(int value) { Schiff = value; } 
	
	/** Prefix für die Rasse */
	private String Prefix = "";
	public String getPrefix() { return Prefix; }
	public void setPrefix(String value) { Prefix = value; }
	
	/** benutzt die Einheit Tarnung */
	private int Sichtbarkeit = 0;
	/**
	 * @return 0 = ungetarnt; 1 = einheiten-getarnt
	 */
	public int getSichtbarkeit() { return Sichtbarkeit; }
	/**
	 *
	 * @param value 0 = ungetarnt; 1 = einheiten-getarnt
	 */
	public void setSichtbarkeit(int value) {
		if ((value != 0) && (value != 1)) {
			throw new RuntimeException("Unbekannter Tarn-Zustand (Unit.Sichtbarkeit) " + value);
		}
		Sichtbarkeit = value;
	}
	
	/** Lebenspunkte - erlittener Schaden */
	private int lebenspunkte = 0;
	/** Lebenspunkte - erlittener Schaden */
	public int getLebenspunkte() { return lebenspunkte; }
	/** Lebenspunkte - erlittener Schaden */
	public void setLebenspunkte(int value) { lebenspunkte = value; }
	
	/** max. Lebenspunkte für diese Einheit ... bestehend aus Ausdauer und den Trefferpunkten und Personenanzahl */
	public int maxLebenspunkte() {
		// es wird der Gesundheitszustand nicht von den Werten aus der Tabelle berechnet, sondern zu
		// den Werten mit Ausdauer, ansonsten ist die Einheit immer gleich tot und vorher im super Zustand
		int tw	= Talentwert(Ausdauer.class);
		int tw1	= Math.max((tw-1), 0);			// sonst kommt in der nächsten Formel ein negativer Wert raus
		int ml = ( Trefferpunkte() + (int)((float)Trefferpunkte() * ((float)(tw*5 + tw1*2) / 100.0)) ) * getPersonen();

        // konkret:
        // T1 --> 5% (Halbling: 17) (Troll: 30)
        // T2 --> 12% (19) (33)
        // T3 --> 19% (20) (35)
        // T4 --> 26% (21) (37)
        // T5 --> 33% (22) (39)
        // T6 --> 40% (23) (42)
        // T7 --> 47% (24) (44)
        // T8 --> 54% (26) (46)
        // T9 --> 61% (27) (48)
        // T10 --> 68% (28) (50)
        //
		return ml;
	}
	public String strLebenspunkte()	{
		// es wird der Gesundheitszustand nicht von den Werten aus der Tabelle berechnet, sondern zu
		// den Werten mit Ausdauer, ansonsten ist die Einheit immer gleich tot und vorher im super Zustand
		// { "schwer verwundet", "verwundet", "angeschlagen", "leicht angeschlagen", "gut" };
		
        int maxLP = maxLebenspunkte() / getPersonen();
        int LP = Math.round((float)getLebenspunkte() / (float)getPersonen());
        String genau = " (" + (maxLP - LP) + "/" + maxLP + ")";
        
		int	value = (int)Math.floor(((float) LP / (float) maxLP) * 3.99999 + 0.99999);		// um auf einen Wert zwischen 0 und 4 zu kommen

		switch (value)
		{
			case 0:	return "gut" + genau;					// 0%
			case 1: return "leicht angeschlagen" + genau;	// >0 - 25%
			case 2: return "angeschlagen" + genau;			// 25 - 50%
			case 3: return "verwundet" + genau;				// 50 - 75%
			case 4:	return "schwer verwundet" + genau;		// 75 - 100%
		}

		new BigError("Berechnung in Unit::strLebenspunkte() fehlerhaft ... " + genau + " ... " + this);
		return null;
	}


	
	public int getMaxAura() {
        int tw = Talentwert(Magie.class);
        if (tw == 0) return 0;

        return tw * tw;
    }

	public int getMaxMana() {
        int tw = Talentwert(Magie.class);
        if (tw == 0) return 0;

        return tw * tw;
    }

    /** Aura - positive Magie */
	private int aura = 0;
	public int getAura() { return aura; }
	public void setAura(int value) { aura = value; }
	
	/** Mana - negative Magie */
	private int mana = 0;
	public int getMana() { return mana; }
	public void setMana(int value) { mana = value; }
	
	/** Temp-Nummer */
	private int tempNummer = 0;
	public int getTempNummer() { return tempNummer; }
	public void setTempNummer(int nummer) { tempNummer = nummer; }
	
	/** eine Sammlung aller Effekte */
	private ArrayList<Effect> effects = new ArrayList<Effect>();
	public ArrayList<Effect> getEffects() { return effects; }
	public void addEffect(Effect efx)
	{
		boolean found = false;
		for(Effect e : effects)
		{
			// ist der Effekt schon vorhanden - dann einmal die Propertys kopieren und den Effekt wieder auffrischen
			if (e.getClass().equals(efx.getClass())) {
				for (String key : efx.getProperties()) {
					e.setProperty(key, efx.getStringProperty(key));
				}
				found = true;
			}
		}

		if (!found) effects.add(efx);
	}
	
	/**
	 * liefert die richtige Nummer der Tempeinheit ... gesucht wird nur innerhalb der Region, d.h.
	 * das TEMP 1 in einer anderen Region erneut verwendet werden kann ... als erstes wird aber nur
	 * nach den eigenen Einheiten gesucht innerhalb der Region gesucht ... anschließend wird für
	 * alle Einheiten gesucht ... dadurch können alle Völker eine TEMP 1 Einheit haben ... aber auch
	 * in Absprache untereinander an TEMP Einheiten Items weiter reichen ... anschließend werden alle
	 * eigenen Einheiten welt weit abgeklappert ... zum Schluss etwas ganz Verwegenes - alle Temp-Einheiten
	 * weltweit
	 * @param temp - diese Tempnummer (base36)
	 * @param unit - für diese Partei
	 * @return die richtige Nummer als Base10
	 */
	public static int getRealNummer(String temp, Unit unit)
	{
		temp = temp.toLowerCase();
        if (temp.startsWith("temp ")) temp = temp.substring(5);

		int retval = 0;
		int nummer = 0;
		try { nummer = Codierung.fromBase36(temp); } catch(Exception ex) { new Fehler(unit + " - die Temp-Nummer ist fehlerhaft.", unit, unit.getCoords()); return 0; }

		Region r = Region.Load(unit.getCoords());
		// erst die eigenen Einheiten in der Region
		for (Unit u : Unit.CACHE.getAll(unit.getCoords(), unit.getOwner())) {
			if (u.getTempNummer() == nummer) return u.getNummer();
		}

		// dann alle Einheiten in der Region
		for (Unit u : r.getUnits()) {
			if (u.getTempNummer() == nummer) return u.getNummer();
		}

		// nun die eigenen Einheiten in der gesamten Welt
		for (Unit u : CACHE.getAll(unit.getOwner())) {
			if (u.getTempNummer() == nummer) return u.getNummer();
		}

		// alle Temp-Einheiten in der Welt
		for (Unit u : CACHE) {
			if (u.getTempNummer() == nummer) return u.getNummer();
		}

		return retval;
	}
	
	/** Sortierung für die DB - <font color="red"><b>Mantis #25 beachten</b></font> */
	private int sortierung = 0;
	public int getSortierung() { return sortierung; }
	public void setSortierung(int value) {
		sortierung = value;
	}
	
	/** bewacht diese Einheit die Region */
	private boolean bewacht = false;
	public boolean getBewacht() { return bewacht; }
	public void setBewacht(boolean value) { bewacht = value; }
	
	/** Kampfposition */
	private Kampfposition kampfposition = Kampfposition.Vorne;
	public Kampfposition getKampfposition() { return kampfposition; }
	public void setKampfposition(Kampfposition value) { kampfposition = value; }

	/** zusätzliche Lerntage durch LEHREN */
	private int lehrtage = 0;
	public int getLehrtage() { return lehrtage; }
	public void setLehrtage(int value) { lehrtage = value; }
	
	/** das gesammte Einkommen dieser Einheit (in dieser Runde) */
	private int einkommen = 0;
	public int getEinkommen() { return einkommen; }
	public void setEinkommen(int value) { einkommen = value; }
	
	/** 
	 * liefert die Anzahl der benutzbaren Waffen (alle, außer Katapult) dieser Einheit ...
	 * <b>es werden keine Talente überprüft</b> - es wird nur geprüft ob die Einheit das Talent
	 * überhaupt hat ... also mit 1 LT <u>pro Einheit</u> können 100 Schwerter raus kommen!!
	 * @return Anzahl der benutzbaren Waffen ... nie größer als Personen dieser Einheit
	 */
	public int getWaffen() {
		int waffen = 0;
		
		// verfügbare Waffen ausrechnen
		if (Talentwert(Hiebwaffen.class) > 0) waffen += getItem(Schwert.class).getAnzahl();
		if (Talentwert(Hiebwaffen.class) > 0) waffen += getItem(Streitaxt.class).getAnzahl();
		if (Talentwert(Hiebwaffen.class) > 0) waffen += getItem(Kriegshammer.class).getAnzahl();
		if (Talentwert(Speerkampf.class) > 0) waffen += getItem(Speer.class).getAnzahl();
		if (Talentwert(Armbrustschiessen.class) > 0) waffen += getItem(Armbrust.class).getAnzahl();
		if (Talentwert(Bogenschiessen.class) > 0) waffen += getItem(Bogen.class).getAnzahl();

		return (waffen > getPersonen()) ? getPersonen() : waffen;
	}


	/** Bewegungspunkte für eine Einheit */
	private int bewegungspunkte = 2;
	public void setBewegungspunkte(int value) { /* Wert wird nicht in DB gesichert - kein Changed() */ bewegungspunkte = value; }
	public int getBewegungspunkte() { return bewegungspunkte; }
	
	/** letzte gültige lange Befehl ... "" => dann noch kein langer Befehl */
	private String longorder = "";
	public String getLongOrder() { return longorder; }
	public void setLongOrder(String value) { longorder = value; }
	/** Grundkapazitaet pro Person */
	protected int kapazitaet = 1700;	// Einheit trägt sich selber
	
	/** Grundgewicht pro Person */
	protected int gewicht = 1000;
	
	/** berechnet das Gewicht der Einheit, in hundertstel GE */
	public int getGewicht()	{
		int masse = this.gewicht * getPersonen(); // die Personen selbst
		// new Debug("g-Personen: " + gewicht);
		for(Item item : Items) {
			if (item.getAnzahl() == 0) continue;
			int itemGewicht = item.getGewicht() * item.getAnzahl();
			// new Debug("g-" + item.getAnzahl() + " " + item.getName() + ": " + itemGewicht);
			masse += itemGewicht;
		}

		// new Debug("g-gesamt: " + gewicht);
		return masse;
	}
	/**
	 * das reine Gewicht einer Person
	 * @return
	 */
	public int getGewichtRaw() { return gewicht; }
	
	/**
	 * berechnet die gesammte <b>freie</b> Kapazität der Einheit
	 * @return  die restliche Kapazität
	 * @uml.property  name="kapazitaet"
	 */
	public int gesamteFreieKapazitaet(boolean reiten) {
		int k = 0;
		if (!reiten) {
			k = kapazitaet * getPersonen();
			// new Debug("k-Personen: " + k);
		} else {
			// new Debug("k-Personen: 0 (beritten)");
		}
		
		for(Item item : Items) {
			int itemK = item.getKapazitaet() * item.getAnzahl();
			if (itemK <= 0) continue;

			// new Debug("k-" + item.getAnzahl() + " " + item.getName() + ": " + itemK);
			k += itemK;
		}
        // new Debug("k-Personen und Items: " + k);

        // Pferde ziehen Wagen ODER transportieren etwas ... aber nicht beides !!
		if (getItem(Wagen.class).getAnzahl() > 0) {
            int wagen = getItem(Wagen.class).getAnzahl();
            int zugpferde = getItem(Pferd.class).getAnzahl() + getItem(Zotte.class).getAnzahl();
            int zugtrolle = 0;

            // UUSE int gespanne = wagen; // die Anzahl der tatsächlich gezogenen Wagen

            // zu wenige Pferde?
            if (zugpferde < wagen * 2) {
                // new Debug("Zu wenig Pferde (" + zugpferde + ", " + wagen + " Wagen)");
                if (this instanceof Troll) {
                    int zugbedarf = wagen * 2 - zugpferde;
                    zugtrolle = Math.min(zugbedarf, this.getPersonen());
                    // new Debug(zugtrolle + " Zugtrolle");
                }

                int wagenZuViel = wagen - ((zugpferde + zugtrolle) / 2);

                // UUSE gespanne -= wagenZuViel;

                // die müssen dann getragen/verladen werden!
                k -= wagenZuViel * getItem(Wagen.class).getKapazitaet();
                // new Debug("k-überzählige Wagen werden verladen, haben also selbst keine Kapazität: " + k);
            } else {
				zugpferde = wagen * 2;
			}

			// nur tatsächlich rollende Wagen müssen gezogen werden:

            // Zugtiere (Pferde, Trolle, ...?) in Gespannen können nichts zusätzlich tragen,
            // wohl aber ihr eigenes Gewicht:
            // (wir verlassen uns hier darauf, dass Pferde und Zotten gleiche Gewichte und Kapazitäten haben!)
            k -= zugpferde * (getItem(Pferd.class).getKapazitaet() - getItem(Pferd.class).getGewicht());
            // new Debug("k-Zugpferde: " + k);
            k -= zugtrolle * (kapazitaet - gewicht);
            // new Debug("k-Zugtrolle: " + k);
		}
		// new Debug(this + " - k: " + k + ", g: " + getGewicht());
		return k - getGewicht(); // < 0 dann keine Bewegung mehr möglich
	}
	
	public int getKapazitaet() { return kapazitaet; }
	
	/** Befehle für diese Einheit */
	public List<String> Befehle = new ArrayList<String>();
	
    public BefehlsListe BefehleExperimental = new BefehlsListe();

    /** alle kontaktierten Einheiten (nur Nummern) */
	public List<Integer> Kontakte = new ArrayList<Integer>();
	
	/**
	 * <p>überprüft, ob die Einheit 'this' ihre Partner-Einheit 'other' sehen kann.
	 * Dazu muss sie nicht selbst in der Lage sein, es reicht, wenn ein Wahrnehmer der
	 * gleichen Partei diese Leistung vollbringen kann.</p>
	 * <p>Es gibt eine anonyme Entsprechung dieser Funktion: Partei.cansee(Unit)</p>
	 * @param other - andere Einheit
	 * @return true wenn diese Einheit (this) die Partner-Einheit (other) sehen kann
	 */
	public boolean cansee(Unit other) {
		// Wenn wir die anderen an ihrem tatsächlichen Ort sehen könnten -
		// tja, dann KÖNNEN wir sie sehen!
		return couldSeeInRegion(other, Region.Load(other.getCoords()));
	}

	/**
	 * <p>prüft, ob diese Einheit eine andere (other) sehen KÖNNTE, wenn
	 * other in der Region region wäre.</p>
	 * <p>Wird für den FOLGE-Befehl gebraucht, um die Sichtbarkeit am Ausgangsort
	 * der Reise sicherzustellen.</p>
	 * @see cansee()
	 * @param other die fremde / fragliche Einheit
	 * @param region der hypothetische Ort
	 * @return true wenn diese Einheit (this) die Partner-Einheit (other) sehen kann
	 */
	public boolean couldSeeInRegion(Unit other, Region region) {
		if (other == null) {
			new SysErr("Unit.couldSeeInRegion() mit null als Zieleinheit: " + this + "; " + region);
			return false;
		}

		// eigene Einheit -> somit nicht versteckt
		if (getOwner() == other.getOwner()) return true;

		// Allianzen prüfen
		if (Partei.getPartei(other.getOwner()).hatAllianz(getOwner(), AllianzOption.Kontaktiere)) {
			return true;
		}

		// testen auf Kontakte ... dazu muss other in der Kontaktiere-Liste
		// die Besitzer-Einheit führen ... von der Besitzer-Einheit wird quasi
		// voraus gesetzt, dass sie den partner kontaktieren würde ... ein
		// expliziter Befehl zum Kontaktieren wird somit nicht erwartet
		if(hatKontakt(other, null)) return true;

		// wenn die Einheiten sich nicht kontaktiert haben, dann kann das Tarnungstalent höher sein
		// als das Wahrnehmungstalent des Partners (Übergabe von Tränken!) ... Tarnung == Wahrnehmung -> cansee
		int tarnungOther = Talentwert(other.getSkill(Tarnung.class));
		if (tarnungOther > 0) {
			if (other.getSichtbarkeit() == 0) return true; // Wenn TARNE EINHEIT NICHT befohlen wurde, ist die Einheit ganz normal sichtbar
			if (other.getGebaeude() != 0) return true;
			// Im Schiff kann man sich an Land nicht verstecken, auf See dagegen schon!
			if ((other.getSchiff() != 0) && region.istBetretbar(null)) return true;

			int top = region.topTW(Wahrnehmung.class, this.getOwner());
			
			new Debug("Top-Wahrnehmung in " + this + " für " + Partei.getPartei(this.getOwner()) + ": " + top + " vs. Tarnung " + tarnungOther + " von " + other + ".");
			
			if (top >= tarnungOther) return true;
		} else {
			return true;
		}

		return false;
	}

    /**
     * @return false, wenn Bewacher in der Region die Einheit am Abbau von Ressourcen hindern.
     */
    public boolean canRessourcenAbbauen() {
        if (this.getVerhinderer(AllianzOption.Resourcen).size() > 0) return false;
        return true;
    }

    /**
     * @param ao
     * @return Gibt eine Liste von Einheiten zurück, die diese Einheit an einer mit der Allianz-Option ao verbundenen Handlung hindern. (Eigentlich nur AllianzOption.Resourcen, oder?)
     */
    public SortedSet<Unit> getVerhinderer(AllianzOption ao) {
        SortedSet<Unit> retval = new TreeSet<Unit>();

        Region r = Region.Load(this.getCoords());

        for (Unit other : r.getUnits()) {
            if (!other.getBewacht()) continue;
            if (other.getOwner() == this.getOwner()) continue;

            Partei otherP = Partei.getPartei(other.getOwner());
            if (!otherP.hatAllianz(this.getOwner(), ao)) retval.add(other);
        }

        return retval;
    }

	/**
	 * @param gleichesGebaeudeOderSchiff wenn true, muss der potentielle Erbe im gleichen Schiff bzw. Gebäude sein.
	 * @return eine Liste von Einheiten, die das Erbe (GegenstÃ¤nde, Kommandos über Gebäude und Schiffe) antreten könnten.
	 */
	public SortedSet<Unit> findeErben(boolean gleichesGebaeudeOderSchiff) {
		// TODO Belagerungen berücksichtigen
		SortedSet<Unit> retval = new TreeSet<Unit>();

		SortedSet<Unit> ersteWahl = new TreeSet<Unit>();
		SortedSet<Unit> zweiteWahl = new TreeSet<Unit>();
		SortedSet<Unit> dritteWahl = new TreeSet<Unit>();
		SortedSet<Unit> vierteWahl = new TreeSet<Unit>();

		Partei p = Partei.getPartei(this.getOwner());

		// gibt es einen Empfänger fürs Kapitänsamt?
		Set<Unit> alle = Region.Load(this.getCoords()).getUnits();
		for (Unit erbe : alle) {
			if (erbe.equals(this)) continue;
			if (erbe.getPersonen() <= 0) continue;

			if (gleichesGebaeudeOderSchiff) {
				if (erbe.getSchiff() != this.getSchiff()) continue;
				if (erbe.getGebaeude() != this.getGebaeude()) continue;
			}

			if(this.Kontakte.contains(erbe.getNummer())) {
				// erstmal die (aktiven/eigenen) Kontakte
				ersteWahl.add(erbe);
			} else if (erbe.getOwner() == this.getOwner()) {
				// eigene Partei
				zweiteWahl.add(erbe);
			} else if(p.hatAllianz(erbe.getOwner(), AllianzOption.Kontaktiere)) {
				// Alliierte
				dritteWahl.add(erbe);
			} else {
				// alle anderen
				vierteWahl.add(erbe);
			}
		}

		retval.addAll(ersteWahl);
		retval.addAll(zweiteWahl);
		retval.addAll(dritteWahl);
		retval.addAll(vierteWahl);

		return retval;
	}
	
	/**
	 * testet ob die Einheit reiten kann ... also schnell unterwegs ist
	 * @return TRUE wenn die Einheit reiten kann - ungeachtet möglicher Probleme wegen Zuladung
	 */
	public boolean canRideAnimals()
	{
        boolean ride = false;
		
		int tiere = getItem(Pferd.class).getAnzahl() + 
					getItem(Zotte.class).getAnzahl() + 
					getItem(Pegasus.class).getAnzahl() + 
					getItem(Kamel.class).getAnzahl() + 
					getItem(Alpaka.class).getAnzahl() + 
					getItem(Greif.class).getAnzahl() * 6 + // TW 6 benötigt
					getItem(Flugdrache.class).getAnzahl() * 6 + // TW 6 benötigt
					getItem(Einhorn.class).getAnzahl() * 2;	// TW 2 benötigt

        // ohne Tiere kein Reiten - Reittiere ohne Talent gibt es derzeit nicht
        if (tiere == 0) return false;
		
		// Reiten testen
		if (tiere <= Talentwert(Reiten.class) * getPersonen()) ride = true;
		
		// Elefanten sind langsam ... also bremsen sie
		if (getItem(Elefant.class).getAnzahl() > 0) ride = false;
		if (getItem(Mastodon.class).getAnzahl() > 0) ride = false;

		return ride;
	}

	/**
     * testet die Fähigkeit zum Reisen per pedes, mit Rücksicht auf Gewicht und Tiere / Talente / Wagen,
     * OHNE Rücksicht auf Schiffe/Gebäude/Belagerung oder tatsächlich vorhandene erreichbare Regionen
     * @return TRUE, wenn die Einheit wandern kann
     */
    public boolean canWalk() {
        if (!this.canWalkAnimals()) return false; // Tiere und Reittalent?
        if (!this.hatGenugZugtiere()) return false; // Wagen und Zugtiere?
        
        // geFreieKapazitaet(false): false == zu Fuß
        if (this.gesamteFreieKapazitaet(false) >= 0) return true; // Gepäck?
        
        return false;
    }
    
    /**
	 * testet ob die Einheit fliegen kann
	 * @return TRUE wenn die Einheit fliegen kann
	 */
	public boolean canFly()
	{
		if (!this.canRideAnimals()) return false;
		
		int wagen = getItem(Wagen.class).getAnzahl() + getItem(Katapult.class).getAnzahl();
		int flyelements = getItem(Pegasus.class).getAnzahl() + getItem(Flugdrache.class).getAnzahl() + getItem(Greif.class).getAnzahl();
		return (wagen == 0) && (flyelements >= getPersonen());
	}
	
	/**
	 * tested ob die Einheit schwimmen kann
	 * @return yes, he can
	 */
	public boolean canSwim()
	{
		return false; // das kann pauschal niemand
	}
	
	/**
	 * testet ob die Einheit sich zu Fuß bewegen <b>könnte</b> ... hier wird das
	 * Gewicht und diverses anderes ignoriert ... es wird nur das Talent überprüft
	 * das zum Führen der Tiere nötig ist
	 * @return TRUE - wenn sich die Einheit zu Fuß bewegen <b>könnte</b>
	 */
	public boolean canWalkAnimals()
	{
		// gesamtes verfügbares Talent berechnen
		int talent = getPersonen() * Talentwert(Reiten.class);
		
		// Anzahl Personen, die noch ein frei führbares Tier führen können.
		int freieTiere = getPersonen();
		
		// Kamele und Alpakas können auch ohne Talent geritten werden.
		// 1. Alle Kamele und Alpakas
		int kamele = getItem(Kamel.class).getAnzahl();
		int alpakas = getItem(Alpaka.class).getAnzahl();
		
		// 2. Alle frei führbaren Tiere werden abgezogen. 
		// Hat die Einheit mehr Kamele, als sie führen kann?
		if (kamele > freieTiere)
		{
			// dann bleiben Kamele übrig und alle Alpkas müssen mit Reiten geführt werden.
			kamele = kamele - freieTiere;
		}
		// Ansonsten werden alle Kamele und die noch frei führbaren Alpakas abgezogen.
		else {
			// Die Kamele werden von der freien Führungskapazität abgezogen und die Kamele auf 0 gesetzt.
			freieTiere -= kamele;
			kamele = 0;
			// Der Rest kann die Alpkas frei führen. Dabei dürfen Alpakas nicht negativ werden.
			alpakas -= freieTiere;
			if (alpakas < 0) { alpakas = 0; }
		}
		
		// 3. Alle jetzt noch vorhandenen Tiere reduzieren die Führungsfähigkeit.
		
		// Talentpunkte pro Tier abziehen
		talent -= (getItem(Pferd.class).getAnzahl()+1) / 2; 	// 2 Pferde eine Region führen
		talent -= (getItem(Zotte.class).getAnzahl()+1) / 2; 	// 2 Zotten eine Region führen
		talent -= (getItem(Pegasus.class).getAnzahl()+1) / 2; 	// dito
        talent -= getItem(Elefant.class).getAnzahl() * 2; 		// pro Talent und Elefant
        talent -= getItem(Mastodon.class).getAnzahl() * 2; 		// pro Talent und Mastodon
		talent -= getItem(Einhorn.class).getAnzahl() * 4;		// TW 4 benötigt
		talent -= getItem(Greif.class).getAnzahl() * 6;			// TW 6 benötigt
		talent -= getItem(Flugdrache.class).getAnzahl() * 6;	// TW 6 benötigt
		talent -= kamele;
		talent -= alpakas;
		
		return talent >= 0;
	}
	
	/**
	 * liefert TRUE wenn diese Einheit zu <i>other</i> Kontakt hat ... dies umfasst
	 * folgende Situationen
	 * <br><ul>
	 * <li>beide Einheiten gehören zum gleichen Volk</li>
	 * <li><i>other</i> hat KONTAKTIERE gesetzt</li>
	 * <li>das Volk von <i>other</i> hat entsprechende AllianzOption gesetzt</li>
	 * </ul>
	 * Sichtbarkeit von <i>other</i> entfällt
	 * <br><br>
	 * @param other - Kontakt zu dieser Einheit wird geprüft
	 * @param required - Diese AllianzOption wird für eine positive Auskunft vorausgesetzt. Kann null sein - das entspricht AllianzOption.Kontaktiere
	 * @return TRUE wenn Kontakt vorhanden ist
	 */
	public boolean hatKontakt(Unit other, AllianzOption required)
	{
		if (other == null) throw new IllegalArgumentException(this + ": hatKontakt() mit null als Parameter aufgerufen.");

		// gleiche Partei ... gleiche Einheit fällt automatisch mit drunter
		if (this.getOwner() == other.getOwner()) return true;

		// KONTAKTIERE-Befehl testen
		for(int i = 0; i < other.Kontakte.size(); i++) 
			if (other.Kontakte.get(i) == getNummer()) return true;
		
		// Allianz testen
		Partei p = Partei.getPartei(other.getOwner());
		if (required == null) {
			if (p.hatAllianz(this.getOwner(), AllianzOption.Kontaktiere)) return true;
			return false;
		}

		if (p.hatAllianz(this.getOwner(), required)) return true;

		// Kontaktiere beinhaltet Gib, auch wenn das nicht gesetzt wurde.
		// siehe http://www.fantasya-pbem.de/wiki/index.php/Allianzen
		if (required == AllianzOption.Gib) {
			if (p.hatAllianz(this.getOwner(), AllianzOption.Kontaktiere)) return true;
		}

		return false;
	}
	
	/** betritt ein Gebäude
	 * @return true, falls die Einheit nun drin ist.
	 */
	public boolean Enter(Building building)	{
		if (!building.canEnter(this)) {
			new Fehler(this + " kann das Gebäude " + building + " nicht betreten.", this);
			return false;
		}
		
		Unit belagerer = building.istBelagert();
		if (belagerer != null)
		{
			if (this.Talentwert(Tarnung.class) + 3 <= belagerer.Talentwert(Wahrnehmung.class)) {
				new Fehler(this + " kann das belagerte Gebäude " + building + " nicht betreten.", this);
				new Battle("Eine getarnte Einheit versucht das Gebäude zu betreten.", belagerer);
				return false;
			}
		}
		
		// mögliche Gebäude verlassen
		if (getGebaeude() != 0) Leave(); // erst versuchen zu verlassen
		if (getGebaeude() != 0) return false;	 // wenn nicht - abbrechen ... Fehler gab es ja schon
		
		// mögliche andere Schiffe verlassen
		if (getSchiff() != 0) Leave();
		
		building.Enter(this);			// Gebäude betreten
		return true;
	}
	
	/** betritt ein Schiff */
	public void Enter(Ship ship)
	{
		if (!ship.canEnter(this)) {
			new Fehler(this + " kann das Schiff " + ship + " nicht betreten.", this);
			return;
		}

		// mögliche Gebäude verlassen
		if (getGebaeude() != 0) Leave(); // erst versuchen zu verlassen
		if (getGebaeude() != 0) return;	 // wenn nicht - abbrechen ... Fehler gab es ja schon
		
		// mögliche andere Schiffe verlassen
		if (getSchiff() != 0) Leave();
		
		setSchiff(ship.getNummer());
		if (ship.getOwner() == 0) ship.setOwner(getNummer());
		
		new Info(this + " betritt das Schiff " + ship + ".", this);
	}
	
	/**
	 * verlässt ein Gebäude bzw. Schiff ... 
	 * zuerst auf Schiff testen ... da die Schiffbauer ggf. in der Werft sitzen
 	 * dadurch verlassen sie das Schiff, aber nicht die Werft ... soll beides
 	 * geschehen so muss VERLASSE zweimal gesetzt werden
	 */
	public void Leave()	{
		if (getSchiff() != 0) {
			Ship ship = Ship.Load(getSchiff());
			if (ship == null) {
				setSchiff(0); // BUG
				new SysErr("Einheit in einem Schiff gemeldet - Schiff existiert aber nicht.");
			} else {
				if (ship.getOwner() != getNummer()) {
					// einfach verlassen
					setSchiff(0);
					new Info(this + " verlässt das Schiff " + ship + ".", this);
				} else {
					ship.setOwner(0);
					new Info(this + " verlässt das Schiff " + ship + ".", this);
					
					// Nachfolger suchen ... erst die eigenen Einheiten - auf dem Schiff
					SortedSet<Unit> erben = findeErben(true); // gleiches Schiff
					if (!erben.isEmpty()) {
						Unit erbe = erben.first();
						ship.setOwner(erbe.getNummer());
					}

					// Spezialfall: Werftarbeiter... :-(
					if ((getGebaeude() != 0)) {
						erben = findeErben(false); // egal wo
						if (!erben.isEmpty()) {
							for (Unit erbe:erben) {
								if (erbe.getSchiff() == ship.getNummer()) {
									ship.setOwner(erbe.getNummer());
									break;
								}
							}
						}
					}

					// ... und raus:
					setSchiff(0);
				}
			}
		} else {
			Building building = Building.getBuilding(getGebaeude());
			if (building == null) {
				setGebaeude(0); // BUG
				new SysErr("Einheit in einem Gebäude gemeldet - Gebäude existiert aber nicht.");
			} else {
				Unit belagerer = building.istBelagert();
				if (belagerer != null)	{
					if (Talentwert(Tarnung.class) <= belagerer.Talentwert(Wahrnehmung.class)) {
						new Fehler(this + " kann das belagerte Gebäude " + building + " nicht verlassen.", this, getCoords());
						return;
					}
				}
				if (building.getOwner() != getNummer()) {
					// einfach verlassen ... haben nicht das Kommando
					setGebaeude(0);
					new Info(this + " verlässt das Gebäude " + building + ".", this, getCoords());
				} else {
					Building.PROXY.remove(building);

					building.setOwner(0);
					new Info(this + " verlässt das Gebäude " + building + ".", this, getCoords());
					
					// Nachfolger suchen
					SortedSet<Unit> erben = findeErben(true); // gleiches Gebäude
					if (!erben.isEmpty()) {
						Unit erbe = erben.first();
						building.setOwner(erbe.getNummer());
					}

					// ... und raus:
					setGebaeude(0);

					Building.PROXY.add(building);
				}
			}
		}
	}

	public boolean Lernen(Class<? extends Skill> skill)	{
        int lehrtage = 0;	// Anzahl der bisher erhaltenen Lehrtage

        List<LehrenRecord> meinUnterricht = Lernen.Unterricht.get(this.getNummer());
        if (meinUnterricht != null) {
            for (LehrenRecord lr : meinUnterricht) {
                Unit lehrer = lr.getLehrer();
                if (lehrer.Talentwert(skill) >= Talentwert(skill) + 1) {
                    // COMMAND LERNE <talent>
                    int consum = getPersonen() * 30 - lehrtage; // bereits vorhandene Lehrtage berÃ¼cksichtigen - Mantis #185
                    // auf Ã¼brige Lehrtage des Lehrers kÃ¼rzen
                    if (consum > lehrer.getLehrtage()) consum = lehrer.getLehrtage();
                    // auf maximale Lehrtage der SchÃ¼ler kÃ¼rzen
                    if (lehrtage + consum > getPersonen() * 30) consum = getPersonen() * 30 - lehrtage;

                    lehrer.setLehrtage(lehrer.getLehrtage() - consum);
                    // anrechnen
                    lehrtage += consum;

                    new Info(lehrer + " lehrt " + this + " mit " + consum + " Lehrtagen.", lehrer, lehrer.getCoords());
                } else {
                    new Fehler(lehrer + " ist in '" + skill.getSimpleName() + "' nicht besser als " + this + ".", this, getCoords());
                    new Fehler(lehrer + " ist in '" + skill.getSimpleName() + "' nicht besser als " + this + ".", lehrer, lehrer.getCoords());
                    continue;
                }
            }
        }

        if (lehrtage > getPersonen() * 30) {
            new BigError(this + " hat mehr Lehrtage erhalten als möglich: " + lehrtage + ".");
        }

        // jetzt nur noch Lernen
        this.setLehrtage(lehrtage);
        String msg = this.getSkill(skill).Lernen(this);
        if (msg != null) new Info(this + " " + msg, this);

        return true;
	}

	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle entspricht
	 */
	public Map<String, Object> getDBValues() {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("nummer", getNummer());
		fields.put("id", this.getNummerBase36());
		fields.put("koordx", getCoords().getX());
		fields.put("koordy", getCoords().getY());
		fields.put("welt", getCoords().getWelt());
		fields.put("name", getName());
		fields.put("beschreibung", getBeschreibung());
		fields.put("person", getPersonen());
		fields.put("rasse", this.getClass().getSimpleName());
		fields.put("partei", getOwner());
		fields.put("tarnung_rasse", getTarnRasse());
		fields.put("tarnung_partei", getTarnPartei());
		fields.put("gebaeude", getGebaeude());
		fields.put("schiff", getSchiff());
		fields.put("prefix", getPrefix());
		fields.put("sichtbarkeit", getSichtbarkeit());
		fields.put("lebenspunkte", getLebenspunkte());
		fields.put("lehrtage", getLehrtage());
		fields.put("aura", getAura());
		fields.put("mana", getMana());
		fields.put("tempnummer", getTempNummer());
		fields.put("sortierung", getSortierung());
		fields.put("kampfposition", kampfposition.name());
		fields.put("longorder", getLongOrder());
		fields.put("bewacht", getBewacht() ? 1 : 0);
		fields.put("belagert", getBelagert());
		fields.put("einkommen", getEinkommen());

		return fields;
	}
	
	/**
	 * Berechnug des Talentwertes für einen Skill ... Rassenmodifikationen werden
	 * nicht berücksichtigt (siehe Kommentar)
	 * @param skill dieser Skill soll berechnet werden
	 * @return das Level zu diesem Skill
	 */
	public int Talentwert(Skill skill)
	{
		int l, tw; // l - Lerntage / t - Talentwert
		
		if (getPersonen() == 0) return 0;	// niemand da - keine Werte :)
		
		// Lerntage auf Personen umrechnen
		l = skill.getLerntage() / getPersonen();

		// kein Talent ohne wenigstens einen Lerntag:
		if (l == 0) return 0;

		// Talentwert berechnen
		int sn = l / 15;	//	eigentlich (l / 30) * 2 ... gekürzt
		tw = (int) (-0.5 + Math.sqrt(0.25 + (float) sn));

		// Modifikationen Ã¼ber eine groÃŸe Liste ist nicht! ... das wÃ¼rde
		// nur jedesmal sinnlos in Interationen ausarten und CPU verschwenden
		// es ist effektiver wenn es die Einheit selber macht ... etwa so
		// 
		// public int Talentwert(Skill skill)
		// {
		// 		int s = super.Talentwert(skill);
		//		if (skill == Skill.Alchemie) s += 2;
		//		return s;
		// }
		
		// TODO Modifikationen durch Gebäude .. anders lösen !!
		if (getGebaeude() != 0) {
			Building b = Building.getBuilding(getGebaeude());
			if (b != null) {
				// Resourcen -- klein
				if (b.getClass().equals(Holzfaellerhuette.class) && skill.getClass().equals(Holzfaellen.class))	tw += 1;
				if (b.getClass().equals(Mine.class) && skill.getClass().equals(Bergbau.class))					tw += 1;
				if (b.getClass().equals(Steingrube.class) && skill.getClass().equals(Steinbau.class))			tw += 1;
				// Resourcen -- groß
				if (b.getClass().equals(Saegewerk.class) && skill.getClass().equals(Holzfaellen.class))			tw += 1;
				if (b.getClass().equals(Bergwerk.class) && skill.getClass().equals(Bergbau.class))				tw += 1;
				if (b.getClass().equals(Steinbruch.class) && skill.getClass().equals(Steinbau.class))			tw += 1;
				// Produktion
				if (b.getClass().equals(Schmiede.class) && (skill.getClass().equals(Waffenbau.class) || skill.getClass().equals(Bogenbau.class))) tw += 1;
				if (b.getClass().equals(Werkstatt.class) && skill.getClass().equals(Wagenbau.class))			tw += 1;
				if (b.getClass().equals(Sattlerei.class) && skill.getClass().equals(Ruestungsbau.class))		tw += 1;
				// Schiffe
				if (b.getClass().equals(Schiffswerft.class) && skill.getClass().equals(Schiffbau.class))		tw += 1;
			} else {
				throw new IllegalStateException(this + " sitzt in einem Gebäude [" +  Codierung.toBase36(getGebaeude()) + "] das nicht existiert.");
			}
		}
		
		// und zurück liefern
		return tw;
	}
	
	/**
	 * Berechnung des Talentwertes für diesen Skill
	 * @param skill - der Skill ist wichtig
	 * @return Talentwert
	 */
	public int Talentwert(Class<? extends Skill> skill)	{
		return Talentwert(getSkill(skill));
	}
	
	/** Hungerpunkte für diese Rasse */
	public int Hungerpunkte()
	{
		return Random.rnd(minHunger, maxHunger);
	}
	
	/** Trefferpunkte für diese Rasse - <b><i>nicht</i></b> Lebenspunkte !! */
	public int Trefferpunkte()
	{
		return 10; // sind Bauern
	}

	// --------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * besondere Aktionen beim Rekrutieren ... die Bauern etc. selber werden in der Hauptfunktion
	 * berechnet ... z.B. bekommen Orks beim Rekrutieren entsprechende Talente
	 */
	public boolean Rekrutieren(int anzahl) {
		if (anzahl <= 0) return false;

		Region r = Region.Load(getCoords());

		// checken, ob zu viele Magier existieren:
		if (this.Talentwert(Magie.class) > 0) {
			int vorhanden = 0;
			for (Unit u2 : CACHE.getAll(getOwner())) {
				if (u2.getNummer() == getNummer()) continue;
				if (u2.getSkill(Magie.class).getLerntage() > 0) {
					vorhanden += u2.getPersonen();
				}
			}
			if (vorhanden + getPersonen() + anzahl > this.maxMagier()) {
				new Fehler(this + " - die Rekruten laufen schreiend weg: Zuviele Magier, es wären dann " + (vorhanden + getPersonen() + anzahl) + ".", this, getCoords());
				return false;
			}
		}

		// TODO topTW

		setPersonen(getPersonen() + anzahl);
		getItem(Silber.class).setAnzahl(getItem(Silber.class).getAnzahl() - anzahl * getRekrutierungsKosten());
		r.setBauern(r.getBauern() - anzahl);

		return true;
	}

	/** hier werden Temp-Einheiten erstellt */
	public void MacheTemp()
	{
		List<Einzelbefehl> loeschListe = new ArrayList<Einzelbefehl>();
		Unit temp = null;
		int sortRank = 0;
		for (Einzelbefehl eb : this.BefehleExperimental) {
			// Befehl: MACHE TEMP <nummer>

			// ENDE für die Temp-Einheit
			if ((eb.getProzessor() == TempEinheiten.class) && (eb.getVariante() == 1)) {
				if (eb.isPerformed()) continue; // hier kann es sein, dass Befehle mehrfach kommen: Wenn die Einheit mehrere Temp-Einheiten erstellt.

				// loeschListe.add(eb); // Muss das?
				eb.setPerformed();
				temp = null;
				sortRank = 0;
				// this.Befehle.remove(line);
				// line--;
			}

			// wenn nicht null ... dann Befehle zur TEMP-Einheit verschieben
			if (temp != null) {
				if ((eb.getProzessor() == TempEinheiten.class) && (eb.getVariante() == 0 + EVABase.TEMP)) {
					// MACHE TEMP innerhalb eines anderen MACHE TEMP:

					// implizites ENDE:
					new Fehler("Warnung: '" + eb.getBefehlCanonical() + "' - " + eb.getUnit() + " will entweder verschachtelte TEMP-Einheiten anlegen, oder es fehlt ein ENDE zwischen zwei gewünschten TEMP-Einheiten...", eb.getUnit());
					temp = null;
					sortRank = 0;

				} else {
					// normale Befehle: von der "Mutter-Einheit" zur TEMP-Einheit verschieben:

					// alten Befehl aus dem zentralen Speicher entfernen...
					boolean success = BefehlsSpeicher.getInstance().remove(eb);
					if (!success) new BigError(new RuntimeException("Kann Befehl nicht aus dem BefehlsSpeicher entfernen? " + eb));

					try {
						// neuen Befehl bei der Einheit erstellen...
						Einzelbefehl tempBefehl = new Einzelbefehl(temp, temp.getCoords(), eb.getBefehlCanonical(), sortRank);
						temp.BefehleExperimental.add(tempBefehl);
						BefehlsSpeicher.getInstance().add(tempBefehl);

						sortRank ++;
					} catch (IllegalArgumentException ex) {
						new Fehler(ex.getMessage(), temp, temp.getCoords());
					}

					// alten Befehl aus der Liste der Erzeuger-Einheit lÃ¶schen.
					loeschListe.add(eb);
				}
			}

			// MACHE TEMP xxx - Temp-Einheit erzeugen
			if ((eb.getProzessor() == TempEinheiten.class) && (eb.getVariante() == 0 + EVABase.TEMP)) {
				if (eb.isPerformed()) continue; // hier kann es sein, dass Befehle mehrfach kommen: Wenn die Einheit mehrere Temp-Einheiten erstellt.
				eb.setPerformed();

				try	{
					String tempId = eb.getTargetUnit().toLowerCase();
					if (tempId.startsWith("temp ")) tempId = tempId.substring(5);

					int nummer = Codierung.fromBase36(tempId);
					boolean exists = false;
						for(Unit unit : Unit.CACHE.getAll(getCoords(),getOwner())) {
							if (unit.getTempNummer() == nummer) { exists = true; break; } // Mist - schon vergeben
						}
					if (exists) {
						eb.setError();
						new Fehler("Eine Temp-Einheit mit der Nummer [" + eb.getTargetUnit() + "] existiert bereits.", this);
						temp = null;
					} else {
						temp = CreateUnit(getRasse(), getOwner(), getCoords());
						temp.setTempNummer(nummer);
						temp.setOwner(this.getOwner());
						temp.setTarnPartei(this.getTarnPartei());
						temp.setTarnRasse(this.getTarnRasse());
						temp.setGebaeude(this.getGebaeude());
						temp.setSchiff(this.getSchiff());
						temp.setKampfposition(this.getKampfposition());
						temp.setSortierung(Integer.MAX_VALUE);
					}

				} catch(Exception ex) {
					eb.setError();
					new Fehler("Temp-Nummer '" + eb.getTargetUnit() + "' ist für diese Einheit ungültig.", this, getCoords());
					new BigError(ex);
				}
			}
		}

		this.BefehleExperimental.removeAll(loeschListe);
	}


	/** Unterhalt für diese Einheit
	 * @return TRUE wenn die Einheit gehungert hat - benötigen Echsen */ 
	public boolean actionUnterhalt() {
		boolean hunger = essen(10);
		
		if (hunger) clearTag("ejcOrdersConfirmed"); // wer hungert, macht nicht einfach weiter.
		
		return hunger;
	}
	
	/**
	 * @param bedarfProPerson So viel Silber braucht eine Person, um ausreichend versorgt zu sein.
	 * @return TRUE wenn die Einheit gehungert hat
	 */
	protected boolean essen(int bedarfProPerson) {
		boolean hunger = false;
		boolean log = ZATMode.CurrentMode().isDebug();

		// abschalten:
		log = false;
		
		// Unterhalt berechnen
		Building building = Building.getBuilding(getGebaeude());
		if (building != null) bedarfProPerson += building.UnterhaltEinheit();
		
		// zusätzlicher Unterhalt für Items
		int itemUpkeep = 0;
		for(Item item : getItems()) itemUpkeep += item.Unterhalt();
		
		bedarfProPerson += Math.round((float)itemUpkeep / (float)getPersonen());

		// Wieviel brauchen wir also insgesamt?
		int needed = getPersonen() * bedarfProPerson;

		
		// jetzt wirds Zeit zum futtern
		int silber = getItem(Silber.class).getAnzahl();

		StringBuilder debug = new StringBuilder();
		if (log) debug.append("Einheiten-Unterhalt: ").append(debug).append(this.getNummerBase36()).append(", ").append(this.getPersonen()).append("P., hat ").append(silber).append(" Silber und braucht ").append(needed);
		
		if (needed <= silber) {
			silber -= needed;
			if (log) debug.append(" - alles okay, ").append(needed).append(" ausgegeben.");
		} else {
			Region region = Region.Load(getCoords());
			silber += region.CollectMoney(this, needed - silber, null);
			if (log) debug.append(" - nach dem Sammeln in der Region: ").append(silber);
			// silber += region.CollectMoney(this, needed - silber, " für Lebensmittel");
			if (silber >= needed) {
				// *juhu* genügend Silber vorhanden
				silber -= needed;
				if (log) debug.append(" - Damit reicht es, Silber nach dem Essen ").append(silber);
			} else {
				// *mist*
				hunger = true;
				
				// Hungernde Meute berechnen
				needed -= silber;
				silber = 0;

				if (log) debug.append(". Das reicht nicht, ").append(needed).append(" zu wenig zur Ernährung (" + bedarfProPerson + " p.P.). ");
				int hungrige = needed / bedarfProPerson; // in "needed" sind jetzt soviele Personen die Hungern
				float hungerQuote = ((float)needed / (float)bedarfProPerson) / (float)getPersonen();
				if (log) debug.append(hungrige).append(" Personen hungern (" + NumberFormat.getPercentInstance().format(hungerQuote) + ").");
				
				
				// Die Folgen des Hungers simulieren:
				List<Person> einzelne = new ArrayList<Person>();
				for (int i=0; i<this.getPersonen(); i++) {
					einzelne.add(new Person()); 
					einzelne.get(i).setMaxLebenspunkte(this.maxLebenspunkte() / this.getPersonen());
				}
				
				// die vorhandenen "Lebenspunkte" zufällig aufteilen:
				for (int i=0; i<getLebenspunkte(); i++) {
					einzelne.get(Random.rnd(0, einzelne.size())).addLebenspunkte(1);
				}
				
				// und die Hungerpunkte ebenfalls zufällig verteilen:
				for (int i=0; i<hungrige; i++) {
					int hungerpunkte = Hungerpunkte();
					for (int j=0; j<hungerpunkte; j++) {
						einzelne.get(Random.rnd(0, einzelne.size())).addLebenspunkte(1);
					}
				}
				
				if (log) {
					new Debug("Ende des Monats: Hunger-Status der einzelnen Personen:");
					Collections.sort(einzelne, new PersonenLebenspunktComparator());
					for (Person p : einzelne) {
						int lp = p.getLebenspunkte();
						int mx = p.getMaxLebenspunkte();
						new Debug("\t" + lp + "/" + mx + (lp >= mx?"!":""));
					}
				}
				
				int opfer = 0;
				int gesamtSchaden = 0;
				for (Person p : einzelne) {
					int lp = p.getLebenspunkte();
					int mx = p.getMaxLebenspunkte();
					if (lp >= mx) {
						opfer ++;
					} else {
						gesamtSchaden += lp;
					}
				}
				
				setLebenspunkte(gesamtSchaden);
				
				StringBuilder msg = new StringBuilder();
				if (opfer > 0) {
					if (this.getPersonen() == 1) {
						msg.append(this + " verhungert in " + region + ".");
					} else {
						if (opfer == this.getPersonen()) {
							msg.append("Alle von " + this + " verhungern in " + region + ".");
						} else {
							msg.append(opfer + " von " + this + " verhungern in " + region + ".");
						}
					}
                    // Talente reduzieren (Mantis #338):
                    double talentQuote = (double)(getPersonen() - opfer) / (double)getPersonen();
                    for (Skill sk : getSkills()) {
                        int neueTage = (int)Math.floor((double)sk.getLerntage() * talentQuote);
                        sk.setLerntage(neueTage);
                    }
                    
					setPersonen(getPersonen() - opfer);
                    
				} else {
					msg.append(this + " hungert in " + region + ".");
				}
				
				
				// und noch die Tiere:
				List<String> verluste = new ArrayList<String>();
				int summeTierVerluste = 0;
				for (Item it : getItems()) {
					if (
							(it.getClass() == Kriegselefant.class)
							|| (it.getClass() == Kriegsmastodon.class)
					) {
						int anzahl = it.getAnzahl();
						int verlust = 0;
						int schwellwert = Math.round(hungerQuote * 1000);
						for (int i=0; i < anzahl; i++) {
							if (Random.W(1000) <= schwellwert) verlust++;
						}
						
						if (verlust > 0) {
							it.setAnzahl(verlust);
							verluste.add(it.toString());
							it.setAnzahl(anzahl - verlust);
							summeTierVerluste += verlust;
						}
					}
				}
				
				if (!verluste.isEmpty()) {
					String verb = " gehen"; if (summeTierVerluste == 1) verb = " geht";
					msg.append(" " + StringUtils.aufzaehlung(verluste) + verb + (opfer>0? " mit":"") + " zugrunde.");
				}
				
				if (getPersonen() > 0) {
					new Fehler(msg.toString(), this);
				} else {
					new Fehler(msg.toString(), Partei.getPartei(this.getOwner()));
				}
			}
		}
		getItem(Silber.class).setAnzahl(silber);

		if (log) {
			debug.append(" - Vermögen nach dem Essen: ").append(getItem(Silber.class).getAnzahl());
			new Debug(debug.toString());
		}
		
		return hunger;
	}
	
	// --------------------------------------------------------------------------------------------------------------------------

	/**
	 * Monster-Aktionen
	 */
	public void actionMonster() {	}
	
	// --------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * erzeugt eine neue Einheit
	 * @param rasse - eine Rasse aus de.x8Bit.Fantasya.Units ... "Races.Mensch" oder "Monsters.Zombie"
	 * @param owner - gehÃ¶rt zu diesem Volk
	 * @param c - Koordinaten
	 * @return eine neue Einheit ohne irgendwas
	 */
	public static Unit CreateUnit(String rasse, int owner, Coords c) {
		return CreateUnit(rasse, owner, c.getX(), c.getY(), c.getWelt());
	}

	/**
	 * erzeugt eine neue Einheit
	 * @param rasse - eine Rasse aus de.x8Bit.Fantasya.Units
	 * @param owner - gehÃ¶rt zu diesem Volk
	 * @param x - Koordinaten X
	 * @param y - Koordinaten Y
	 * @param welt - Welt
	 * @return eine neue Einheit ohne irgendwas
	 */
	public static Unit CreateUnit(String rasse, int owner, int x, int y, int welt)
	{
		Unit u = null;
		
		try	{
			u = (Unit) Class.forName("de.x8bit.Fantasya.Atlantis.Units." + rasse).newInstance();
		} catch(Exception ex) { new BigError(ex); }
		
		// diverse Vorbereitungen
		u.setNummer(UnitIDPool.getInstance().getFreieNummer());

		// Voreinstellungen
		u.setName("Einheit " + u.getNummerBase36());
		u.setOwner(owner);
		u.setTarnPartei(owner);
		u.setCoords(new Coords(x, y, welt));
		u.setLebenspunkte(0);

		// zum Proxy
		CACHE.add(u);

		return u;
	}

	/**
	 * lÃ¤d die entsprechende Einheit
	 * @param nummer - die Nummer der Einheit
	 * @return eine Einheit ?!
	 */
	public static Unit Load(int nummer)	{
		return Unit.Get(nummer);
	}
	
	/**
	 * lädt die entsprechende Einheit "flach" - d.h. ohne Items, Skills, Befehle etc.
	 * @return eine Einheit ?!
	 */
	public static Unit fromResultSet(ResultSet rs) {
		Unit u = null;

		try	{
			u = (Unit) Class.forName("de.x8bit.Fantasya.Atlantis.Units." + rs.getString("rasse")).newInstance();
			u.setNummer(rs.getInt("nummer"));
			u.setCoords(new Coords(rs.getInt("koordx"), rs.getInt("koordy"), rs.getInt("welt")));
			u.setName(rs.getString("name"));
			u.setBeschreibung(rs.getString("beschreibung"));
			u.setPersonen(rs.getInt("person"));
			u.setOwner(rs.getInt("partei"));
			u.setTarnRasse(rs.getString("tarnung_rasse"));
			u.setTarnPartei(rs.getInt("tarnung_partei"));
			u.setGebaeude(rs.getInt("gebaeude"));
			u.setSchiff(rs.getInt("schiff"));
			u.setPrefix(rs.getString("prefix"));
			u.setSichtbarkeit(rs.getInt("sichtbarkeit"));
			u.setLebenspunkte(rs.getInt("lebenspunkte"));
			u.setLehrtage(rs.getInt("lehrtage"));
			u.setAura(rs.getInt("aura"));
			u.setMana(rs.getInt("mana"));
			u.setTempNummer(rs.getInt("tempnummer"));
			u.sortierung = rs.getInt("sortierung"); // am setter vorbei mogeln, weil sonst der Cache meckert.
			u.setLongOrder(rs.getString("longorder"));
			u.setBewacht(rs.getInt("bewacht") == 0 ? false : true);
			u.setBelagert(rs.getInt("belagert"));
			u.setEinkommen(rs.getInt("einkommen"));
			u.setKampfposition(Kampfposition.ordinal(rs.getString("kampfposition")));
		} catch(Exception ex) { new BigError(ex); }
		return u;
	}


    /**
	 * "vernichtet" eine Einheit im technischen Sinne - sie ist danach nicht mehr im Objektmodell enthalten.
	 * Ggf. werden für Gebäude, Schiffe und Gegenstände "Erben" gesucht.
	 * @param u die Dahinscheidenden
	 * @param meldungMachen true, wenn Meldungen an den Besitzer und die evtl. Erben gegeben werden sollen.
	 */
	public void purge(boolean meldungMachen) {
		Region r = Region.Load(getCoords());

		for (Einzelbefehl eb : BefehleExperimental) {
			// die Befehle stören und liefern Fehler in ZATBase::Action()
			BefehlsSpeicher.getInstance().remove(eb);
		}

		// gibt es einen Empfänger für Hab und Gut?
		SortedSet<Unit> hinterbliebene = findeErben(false);
		// wenn die Region nicht betretbar ist (z.B. Ozean), kommen nur Erben im gleichen "Container" in Frage
		if (!r.istBetretbar(null)) hinterbliebene = findeErben(true);
		if (getGebaeude() != 0) {
			Building b = Building.getBuilding(getGebaeude());
			Unit belagerer = b.istBelagert();
			if (belagerer != null) {
				if (Talentwert(Tarnung.class) <= belagerer.Talentwert(Wahrnehmung.class)) {
					hinterbliebene = findeErben(true);
				}
			}
		}

		if (!hinterbliebene.isEmpty()) {
			Unit erbe = hinterbliebene.first();
			List<String> sachen = new ArrayList<String>();
			for (Item item : Items) {
				if (item.getAnzahl() > 0) {
					sachen.add(item.getAnzahl() + " " + item.getName());

					int hatSchon = erbe.getItem(item.getClass()).getAnzahl();
					erbe.getItem(item.getClass()).setAnzahl(hatSchon + item.getAnzahl());
					item.setAnzahl(0);
				}
			}
			if (sachen.size() > 0) {
				if (meldungMachen) new Info(
						erbe + " erbt " + StringUtils.aufzaehlung(sachen)
						+ " von der aufgelösten Einheit " + this + ".", erbe, erbe.getCoords()
				);
			}
		} else {
			if (meldungMachen) {
				new Info("Einheit " + this + " in " + r + " ist ohne Erben aufgelöst worden.", Partei.getPartei(getOwner()));
			}
		}

		// das Kommando über Gebäude abgeben
		if (getGebaeude() != 0) {
			Building b = Building.getBuilding(getGebaeude());
			if (b != null) {
				if (b.getOwner() == getNummer()) {
					Building.PROXY.remove(b);

					SortedSet<Unit> erben = findeErben(true); // true = gleiches Gebäude
					if (!erben.isEmpty()) {
						Unit erbe = erben.first();
						b.setOwner(erbe.getNummer());
						if (meldungMachen) new Info(erbe + " erbt das Kommando über " + b + " von der aufgelösten Einheit " + this + ".", erbe, erbe.getCoords());
					} else {
						b.setOwner(0);
					}

					Building.PROXY.add(b);
				}
			}
			setGebaeude(0);
		}

		// das Kommando über Schiffe abgeben
		if (getSchiff() != 0) {
			Ship s = Ship.Load(getSchiff());
			if (s != null) {
				if (s.getOwner() == getNummer()) {
					SortedSet<Unit> erben = findeErben(true); // true = gleiches Schiff
					if (!erben.isEmpty()) {
						Unit erbe = erben.first();
						s.setOwner(erbe.getNummer());
						if (meldungMachen) new Info(erbe + " erbt das Kommando über " + s + " von der aufgelösten Einheit " + this + ".", erbe, erbe.getCoords());
					} else {
						s.setOwner(0);
					}
				}
			}
			setSchiff(0);
		}

		// und jetzt das eigentlich Wichtige:
        Unit.CACHE.remove(this);
    }
	
    /** EVA - Map, die alle Units enthält (verschiedene Indizierungen inklusive) */
    public final static MapCache<Unit> CACHE = new SortedCache<Unit>();

	/**
	 * @return Eine nach Einheiten-Sortierung geordnete Menge aller Einheiten
	 */
	public int compareTo(Unit o) {
		if (this.getNummer() == o.getNummer()) return 0; // dann spielt auch die Sortierung keine Rolle!

		if (this.getSortierung() < o.getSortierung()) return -1;
		if (this.getSortierung() > o.getSortierung()) return +1;

		if (this.getNummer() < o.getNummer()) return +1;
		if (this.getNummer() > o.getNummer()) return -1;

		return 0;
	}

	@Override
	public int compareTo(Object o) {
		return this.compareTo((Unit)o);
	}

	public boolean equals(Unit o) {
		if (this.getNummer() != o.getNummer()) return false;
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
        if (!(obj instanceof Unit)) return false;
		return this.equals((Unit)obj);
	}

	@Override
	public int hashCode() {
		return this.getNummer();
	}



    /**
	 * WARN das funktioniert natürlich nur, wenn der CACHE vorher vollständig gefüllt wurde -
	 * derzeit nur via EVAFastLoader.loadAll()
	 * @param nummer die ID der gesuchten Einheit
	 * @return die Einheit - oder null, wenn es sie nicht gibt
	 */
	public static Unit Get(int nummer) { return CACHE.get(nummer); }
	
	/**
	 * definiert zentral das maximale Handelsvolumen von Einheiten
	 * @return das maximale Handelsvolumen der Einheit
	 */
	public int getMaxHandelsVolumen() {
		return 10 * this.getPersonen() * this.Talentwert(Handel.class);
	}

	/**
	 * speichert die Einheit in den ZR
	 * @param writer
	 */
	public void SaveZR(ZRWriter writer)
	{
		writer.wl("  EINHEIT " + getNummerBase36() + " ; " + this + " (" + getPersonen() + " Personen, " + getItem(Silber.class).getAnzahl() + " Silber)");
		for (Einzelbefehl eb : BefehleExperimental) writer.wl("    " + eb.getBefehlCanonical());
		writer.wl();
	}
	
	// --------------------------------------------------------------------------------------------------------------------------
	
	public void SaveCR_Items(CRWriter writer, Partei partei)
	{
		boolean omniscience = (partei.getNummer() == 0);
		
		// Header-Block immer schreiben - Forlage mag nicht, wenn der nicht da ist
		writer.wl("GEGENSTAENDE");
		// Gegenstände
		// -- 03.10.2011 -- boolean any = false;
		for(Item it : Items) {
			int anzahl = it.getAnzahl();
			if (anzahl == 0) continue;
			if (anzahl < 0) new SysErr("Einheit " + this + " hat " + it + ".");
			
			// -- 03.10.2011 -- if (!any) { writer.wl("GEGENSTAENDE"); any = true; }

			if (omniscience) {
				writer.wl(anzahl, it.getClass().getSimpleName());
			} else if (it.getClass().equals(Silber.class) && partei.getNummer() != this.getOwner()) {
				writer.wl(1, "Silberkiste");
			} else {
				if (this.imGebaeude()) {
                    Building b = Building.getBuilding(this.getGebaeude());
                    if (b != null) {
                        if (!b.getBewohnerParteien().contains(partei)) anzahl = 1;
                    }
                }
				writer.wl(anzahl, it.getClass().getSimpleName());
			}
		}
	}
	/**
	 * speichert die Einheit in den CR
	 * @param writer - passender ReportWriter
	 * @param partei - für diese Partei ist dieser CR-Auszug
	 */
	public void SaveCR(CRWriter writer, Partei partei)
	{
		boolean omniscience = (partei.getNummer() == 0);
		
		// Sichtbarkeit von Einheiten mit Tarnung prüfen
		if (!partei.cansee(this)) {
			if (ZATMode.CurrentMode().isDebug()) new Debug(partei + " kann " + this + " in " + Region.Load(this.getCoords()) + " nicht sehen.");
			return;
		}
        
		// Einheit ausgeben
		writer.wl("EINHEIT " + this.getNummer());
		writer.wl(getName(), "Name");
		//writer.wl(getPersonen(), "Anzahl");
		if (imGebaeude()) {
			if (this.istVon(partei) || omniscience) {
                // eigene Einheit (oder wir sind omniszient):
				writer.wl(getPersonen(), "Anzahl");
			} else {
                // fremde Einheit:
				Building b = Building.getBuilding(getGebaeude());
				Unit hausherr = Unit.Load(b.getOwner());
				if (hausherr == null) {
					throw new IllegalStateException(this  + " ist in Gebäude " + b + ", aber das Gebäude hat keinen Besitzer?");
				} else if (b.getBewohnerParteien().contains(partei)) {
                    // Mantis #312: die Mitbewohner sehen die Details der anderen Bewohner
					writer.wl(getPersonen(), "Anzahl");
				} else {
					// wer nicht drin ist, sieht nur 1 Person pro Einheit
                    writer.wl(1, "Anzahl");
                }
			}
		} else {
            // von Einheiten außerhalb von Gebäuden ist die wahre Zahl sichtbar:
			writer.wl(getPersonen(), "Anzahl");
		}
        
		if (getPrefix().length() > 0) writer.wl(getPrefix(), "typprefix");
		
		if (omniscience) {
			writer.wl(getOwner(), "Partei");
			if (getOwner() != getTarnPartei()) writer.wl(getTarnPartei(), "Anderepartei");
		} else if (this.istVon(partei)) {
			writer.wl(getOwner(), "Partei");
			if (getTarnPartei() != getOwner()) {
				writer.wl(getTarnPartei(), "Anderepartei");
				writer.wl(1, "Parteitarnung");
			}
		} else if (getTarnPartei() == partei.getNummer()) {
			writer.wl(getTarnPartei(), "Partei");	// Tarnpartei ist entwerder 0 (voll getarnt) oder eine Partei (auch die eigene) ... daher kein getOwner() !!
			if (getOwner() != partei.getNummer()) writer.wl(1, "Verraeter");
		} else {
			if (getTarnPartei() != 0) {
				writer.wl(getTarnPartei(), "Partei");
			} else {
				writer.wl(0, "Partei");
				writer.wl(1, "Parteitarnung");
			}
		}
		
		if (getBeschreibung().length() > 0) writer.wl(getBeschreibung(), "Beschr");
		if (getGebaeude() != 0) writer.wl(getGebaeude(), "Burg");
		if (getSchiff() != 0) {
			writer.wl(getSchiff(), "Schiff");
			Ship ship = Ship.Load(getSchiff());
			if (ship != null) {
				if (ship.getOwner() == this.getNummer()) {
                    float fsk = (float)ship.getKapazitaetFree() / 100f;
					writer.wl((int)Math.floor(fsk), "freieSchiffsKapazitaet");
				}
			}
		}
        
		if (getBewacht()) writer.wl(1, "bewacht");
        
		if ((!this.istVon(partei)) && (!omniscience)) { // 0 sieht alles!
			// Rassen für die Echsen
			if (getTarnRasse().length() > 0) {
				writer.wl(getTarnRasse(), "Typ");
			} else {
				writer.wl(this.getClass().getSimpleName(), "Typ");
			}
			
			SaveCR_Items(writer, partei);
		} else {
            // Besitzer oder omniscient:
			if (getTarnPartei() != getOwner()) {
				writer.wl(1,"Parteitarnung");
				if (getOwner() == partei.getNummer()) writer.wl(getTarnPartei(), "Anderepartei");
			}
			if (getTarnRasse().length() > 0) {
				writer.wl(getTarnRasse(), "Typ");
				writer.wl(this.getClass().getSimpleName(), "wahrerTyp");
			} else {
				writer.wl(this.getClass().getSimpleName(), "Typ");
			}
			writer.wl(Kampfposition.PositionCR(getKampfposition()), "Kampfstatus");
			writer.wl(strLebenspunkte(), "hp");	// Lebenspunkte


            // Kapazität zu Fuß / beritten:
            Map<String, String> gewichtsTags = getGewichtTags();
            for (String tag : gewichtsTags.keySet()) {
                try {
                    writer.wl(Integer.parseInt(gewichtsTags.get(tag)), tag);
                } catch (NumberFormatException ex) {
                    writer.wl(gewichtsTags.get(tag), tag);
                }
            }

			
			if (getSkill(Magie.class).getLerntage() > 0) {
				writer.wl(getAura(), "Aura");
				writer.wl(getMaxAura(), "Auramax");
				if (getMana() > 0) {
                    writer.wl(getMana(), "Mana");
                    writer.wl(getMaxMana(), "Manamax");
                }
			}
			
			for (String tagName : tags.keySet()) {
				writer.wl(tags.get(tagName) + ";" + tagName);
			}
			
			// Befehle
			writer.wl("COMMANDS");
            for (Einzelbefehl eb : this.BefehleExperimental) {
                writer.wl("\"" + eb.getBefehlCanonical() + "\"");
            }
	
			// Gegenstände
			SaveCR_Items(writer, partei);
	
			// Skills
			writer.wl("TALENTE");
			for(int i = 0; i < skills.size(); i++) {
				Skill s = skills.get(i);
				if (s.getLerntage() > 0) writer.wl(s.getLerntage() + " " + Talentwert(s), s.getName());
			}
			
			// Sprüche
			if (!spells.isEmpty()) {
				writer.wl("SPRUECHE");
				for(Spell spell : spells) writer.wl("\"" + spell.getName() + "\"");
                if (hasProperty(Kampfzauber.CONFUSIONSPELL)) {
					writer.wl("KAMPFZAUBER 0");
					writer.wl(getStringProperty(Kampfzauber.CONFUSIONSPELL), "name");
					writer.wl(0, "level");
				}
				if (hasProperty(Kampfzauber.ATTACKSPELL)) {
					writer.wl("KAMPFZAUBER 1");
					writer.wl(getStringProperty(Kampfzauber.ATTACKSPELL), "name");
					writer.wl(0, "level");
				}
				if (hasProperty(Kampfzauber.DEFENCESPELL)) {
					writer.wl("KAMPFZAUBER 2");
					writer.wl(getStringProperty(Kampfzauber.DEFENCESPELL), "name");
					writer.wl(0, "level");
				}
			}
		}
	}
    
	public void SaveXML(XMLWriter xml, Partei partei, boolean kurz)	{
		boolean omniscience = (partei.getNummer() == 0);
		
		// Sichtbarkeit von Einheiten mit Tarnung prüfen
		if (!partei.cansee(this)) return;
	
		// Einheit ausgeben
		xml.ElementStart("unit");
		if (getTarnRasse().length() > 0) {
			xml.ElementAttribute("ref", getTarnRasse());
		} else {
			xml.ElementAttribute("ref", getClass().getSimpleName());
		}
		xml.ElementAttribute("id", getNummerBase36());
		if (getPrefix().length() > 0) xml.ElementShort("prefix", getPrefix());
		xml.ElementShort("faction", Codierung.toBase36(getOwner()));
		xml.ElementShort("name", getName());
		// xml.ElementShort("persons", getPersonen());
        
        
		if (imGebaeude()) {
			if (this.istVon(partei) || omniscience) {
                // eigene Einheit (oder wir sind omniszient):
                xml.ElementShort("persons", getPersonen());
			} else {
                // fremde Einheit:
				Building b = Building.getBuilding(getGebaeude());
				Unit hausherr = Unit.Load(b.getOwner());
				if (hausherr == null) {
					throw new IllegalStateException(this  + " ist in Gebäude " + b + ", aber das Gebäude hat keinen Besitzer?");
				} else if (b.getBewohnerParteien().contains(partei)) {
                    // Mantis #312: die Mitbewohner sehen die Details der anderen Bewohner
					xml.ElementShort("persons", getPersonen());
				} else {
					// wer nicht drin ist, sieht nur 1 Person pro Einheit
					xml.ElementShort("persons", 1);
                }
			}
		} else {
            // von Einheiten außerhalb von Gebäuden ist die wahre Zahl sichtbar:
			xml.ElementShort("persons", getPersonen());
		}
        
		if (getBeschreibung().length() > 0) xml.ElementShort("description", getBeschreibung());
		if (getGebaeude() != 0) xml.ElementShort("building", Codierung.toBase36(getGebaeude()));
		if (getSchiff() != 0) xml.ElementShort("ship", Codierung.toBase36(getSchiff()));
		if (getBewacht()) xml.ElementShort("guard", "true");

		if ((!this.istVon(partei)) && (!omniscience)) {
			SaveXML_Items(xml, partei);
		} else {
            // es ist unsere Einheit, oder wir sind allwissend:
            
			// Rassen für die Echsen
			if (getTarnRasse().length() > 0) xml.ElementShort("realrace", getClass().getSimpleName());
			
			if (getTarnPartei() != getOwner()) xml.ElementShort("camouflage", Codierung.toBase36(getTarnPartei()));

			xml.ElementShort("battle", getKampfposition().name());
			xml.ElementShort("health", strLebenspunkte());

			// Kapazität zu Fuß / beritten:
			xml.ElementShort("capacityFree", (this.gesamteFreieKapazitaet(false) / 100));
			if (canRideAnimals()) xml.ElementShort("capacityRidingFree", (this.gesamteFreieKapazitaet(true) / 100));
			xml.ElementShort("grossWeight", (int)Math.ceil(this.getGewicht() / 100));
			
			if (getSkill(Magie.class).getLerntage() > 0) {
				xml.ElementShort("aura", getAura());
				xml.ElementShort("mana", getMana());
			}
			
			// Befehle
			xml.ElementStart("commands");
			for (Einzelbefehl eb : BefehleExperimental) xml.ElementShort("order", eb.getBefehlCanonical());
			xml.ElementEnd();
	
			// Gegenstände
			SaveXML_Items(xml, partei);
	
			// Skills
			if (skills.size() > 0) {
                xml.ElementStart("skills");
                for(Skill skill : skills) {
                    if (skill.getLerntage() > 0) {
                        xml.ElementStart("skill");
                        xml.ElementAttribute("ref", skill.getClass().getSimpleName());
                        xml.ElementAttribute("lerntage", skill.getLerntage());
                        xml.ElementData(Talentwert(skill.getClass()));
                        xml.ElementEnd();
                    }
                }
                xml.ElementEnd();
            }
			
			// Sprüche
			if (spells.size() > 0) {
				xml.ElementStart("spells");
				for(Spell spell : spells) {
					xml.ElementStart("spell");
					xml.ElementAttribute("ref", spell.getClass().getSimpleName());
					xml.ElementData(spell.getName());
					xml.ElementEnd();
				}
				if (hasProperty(Kampfzauber.ATTACKSPELL)) {
					xml.ElementShort("attack", getStringProperty(Kampfzauber.ATTACKSPELL));
				}
				if (hasProperty(Kampfzauber.DEFENCESPELL)){
					xml.ElementShort("defence", getStringProperty(Kampfzauber.DEFENCESPELL));
				}
				if (hasProperty(Kampfzauber.CONFUSIONSPELL)) {
					xml.ElementShort("confusion", getStringProperty(Kampfzauber.CONFUSIONSPELL));
				}
				xml.ElementEnd();
			}
		}

		xml.ElementEnd();
	}
	
	public void SaveXML_Items(XMLWriter xml, Partei partei) {
		boolean omniscience = (partei.getNummer() == 0);
        
		boolean header = false;
		
		for(Item it : getItems()) {
            if (it.getAnzahl() == 0) continue;
            
            if (
                (it.getClass() != Silber.class) 
                || (this.istVon(partei))
                || (omniscience)
            ) {
                if (!header) { xml.ElementStart("items"); header = true; }
                int anzahl = it.getAnzahl();
				if (this.imGebaeude()) {
                    Building b = Building.getBuilding(this.getGebaeude());
                    if (b != null) {
                        if (!b.getBewohnerParteien().contains(partei)) {
                            anzahl = 1; // wir sind nicht im Gebäude dieser Einheit --> nur 1 Stück zu sehen (Mantis #312)
                        }
                    }
                }
                xml.ElementStart("item");
                xml.ElementAttribute("ref", it.getClass().getSimpleName());
                xml.ElementData(anzahl);
                xml.ElementEnd();
            } else {
                // es handelt sich um Silber, gesehen bei einer fremden Einheit:
                if (!header) { xml.ElementStart("items"); header = true; }
                xml.ElementStart("item");
                xml.ElementAttribute("ref", "Silberkiste");
                xml.ElementData(1);
                xml.ElementEnd();
            }
		}
        
		if (header) xml.ElementEnd();
	}


    public Map<String, String> getGewichtTags() {
        Map<String, String> gTags = new HashMap<String, String>();
        
        gTags.put(TAG_GEWICHT, this.getGewicht() + ""); // Standard-CR-Tag


        // menschenlesbar:
        float fk = (float)this.gesamteFreieKapazitaet(false) / 100f;
        float fkb = (float)this.gesamteFreieKapazitaet(true) / 100f;
        float gg = (float)this.getGewicht() / 100f;
        if (canWalkAnimals()) {
            // kann GEHEN (was Tiere und Talent angeht)
            if (canWalk()) {
                // kann auch gehen, was das Gewicht & Wagen angeht:
                gTags.put(TAG_FREIE_KAPAZITAET, ((int)Math.floor(fk)) + "");
                if (hatTiere()) {
                    if (canRideAnimals()) {
                        // kann REITEN (was Tiere und Talent angeht)
                        if (hatGenugZugtiere()) {
                            // kann auch REITEN, was Wagen und Zugtiere angeht,
                            // Gewicht kann sowohl frei als auch überladen sein:
                            gTags.put(TAG_FREIE_KAPAZITAET_BERITTEN, ((int)Math.floor(fkb)) + "");
                        } else {
                            // kann wegen Wagen nicht REITEN:
                            gTags.put(TAG_FREIE_KAPAZITAET_BERITTEN, Integer.MIN_VALUE + "");
                            gTags.put(TAG_KAPAZITAET_GRUND, "zu viele Wagen / Katapulte zum Fahren");
                        }
                    } else {
                        // kann wegen Tieren und/oder Talent nicht REITEN:
                        if (hat(Elefant.class) || hat(Mastodon.class)) {
                            // kann wegen langsamer Tiere nicht REITEN - nicht der Rede wert.
                        } else {
                            gTags.put(TAG_FREIE_KAPAZITAET_BERITTEN, Integer.MIN_VALUE + "");
                            gTags.put(TAG_KAPAZITAET_GRUND, "zu viele Tiere um zu reiten");
                        }
                    }
                } else {
                    // hat keine Tiere und kann deswegen nicht REITEN - nicht der Rede wert.
                }
            } else {
                if (hatGenugZugtiere()) {
                    // kann wegen Gewicht nicht GEHEN:
                    gTags.put(TAG_FREIE_KAPAZITAET, ((int)Math.floor(fk)) + "");
                } else {
                    // kann wegen fehlender Zugtiere nicht GEHEN:
                    gTags.put(TAG_FREIE_KAPAZITAET, Integer.MIN_VALUE+ "");
                    gTags.put(TAG_KAPAZITAET_GRUND, "zu viele Wagen / Katapulte um zu gehen");
                }
            }
        } else {
            // kann wegen Tieren / Talent nicht GEHEN:
            gTags.put(TAG_FREIE_KAPAZITAET, Integer.MIN_VALUE + "");
            gTags.put(TAG_KAPAZITAET_GRUND, "zu viele Tiere um zu gehen");
        }
        
        gTags.put(TAG_GESAMTGEWICHT, ((int)Math.ceil(gg)) + "");

        return gTags;
    }
	
	/**
	 * Bewegung zu Fuß / Pferd etc.
	 * @param richtung - in diese Richtung soll bewegt werden
	 * @return in diese Region wurde sich bewegt ... oder <b>null</b> falls was schief gelaufen ist
	 */
	public Region Movement(Richtung richtung)
	{
		Region r = Region.Load(getCoords());
		
		// Bewachen aufheben
		if (getBewacht()) {
			setBewacht(false);
			new Bewegung(this + " die Bewachung der Region " + r.getName() + " wurde durch die Bewegung aufgehoben.", this);
		}

		return r.Movement(richtung, this);
	}
	
	/**
	 * Bewegung über Ozean
	 * @param ship - das Schiff ... Kapitän wurde schon überprüft
	 * @param richtung - in diese Richtung soll bewegt werden
	 * @return in diese Region wurde sich bewegt ... oder <b>null</b> falls was schief gelaufen ist
	 */
	public Region Movement(Richtung richtung, Ship ship)
	{
		Region r = Region.Load(ship.getCoords());
		
		// Bewachen aufheben
		for(Unit unit : ship.getUnits()) {
			if (unit.getBewacht()) {
				unit.setBewacht(false);
				new Bewegung(unit + " die Bewachung von " + r.getName() + " wurde durch die Abreise aufgehoben.", unit);
			}
		}

		return r.Movement(richtung, ship);
	}
	
	/**
     * Talent wird nicht überprüft, siehe canWalkAnimals() !
     * @return true, wenn die Einheit genügend Tiere hat, um alle Wagen/Katapulte zu ziehen.
     */
    public boolean hatGenugZugtiere() {
		int zugtiere = this.getItem(Pferd.class).getAnzahl() + this.getItem(Pegasus.class).getAnzahl() + this.getItem(Zotte.class).getAnzahl();
		if (this instanceof Troll) zugtiere = getPersonen(); // Trolle können den Wagen ziehen
		if (zugtiere / 2 < this.getItem(Wagen.class).getAnzahl() + this.getItem(Katapult.class).getAnzahl()) {
			return false;
		}
        return true;
    }
    
    /**
	 * überprüfen ob die Bewegung über Land möglich ist
	 * @param - diese Einheit hat den Bewegungsbefehl gesetzt
	 */
	public boolean checkLand(ReiseVerb reiseVerb)
	{
		// alle Bewegungen nutzen nichts wenn Wagen & Katapult nicht bewegt werden können
        if (!hatGenugZugtiere()) {
			if (this instanceof Troll) {
                new Fehler(this + " - wir sind zu wenige um die Wagen bewegen zu können.", this);
            } else {
                new Fehler(this + " hat nicht genügend Pferde um die Wagen bewegen zu können.", this);
            }
            return false;
        }
        

		if (!this.canWalkAnimals())	{
			if (this.getPersonen() == 1) {
				new Fehler(this + " hat seine Tiere nicht unter Kontrolle.", this);
			} else {
				new Fehler(this + " haben ihre Tiere nicht unter Kontrolle.", this);
			}
			return false;
		}
		
		// Gewicht & Kapazität überprüfen - false = zu Fuß
		if (this.gesamteFreieKapazitaet(false) < 0) {
			new Fehler(this + " ist zu schwer um sich zu bewegen.", this);
			return false;
		}
		
		// Bewegungspunkte berechnen
		setBewegungspunkte(2);		// pauschal zu Fuß
		reiseVerb.setVerb("wandert");

		if (!(this instanceof Troll)) {
			int reitenFree = gesamteFreieKapazitaet(true); // true = beritten
			if (reitenFree >= 0) {
				if (getItem(Pegasus.class).getAnzahl() > 0) { setBewegungspunkte(6); reiseVerb.setVerb("fliegt"); }
				if (getItem(Greif.class).getAnzahl() > 0) 		{ setBewegungspunkte(10); reiseVerb.setVerb("fliegt"); }
				if (getItem(Flugdrache.class).getAnzahl() > 0)	{ setBewegungspunkte(10); reiseVerb.setVerb("fliegt"); }
				if (getItem(Pferd.class).getAnzahl() > 0) { setBewegungspunkte(canRideAnimals() ? 4 : 2); reiseVerb.setVerb("reitet"); }
				if (getItem(Einhorn.class).getAnzahl() > 0) { setBewegungspunkte(canRideAnimals() ? 4 : 2); reiseVerb.setVerb("reitet"); }
				if (getItem(Kamel.class).getAnzahl() > 0) { setBewegungspunkte(canRideAnimals() ? 4 : 2); reiseVerb.setVerb("führt die Karawane"); }
			}
		}
		if (getItem(Elefant.class).getAnzahl() > 0) { setBewegungspunkte(2); reiseVerb.setVerb("wandert"); }
		
		// Schiff testen ... falls nicht KapitÃ¤n - aber Schiff soll segeln
		if (this.getSchiff() != 0) {
			Ship ship = Ship.Load(this.getSchiff());
			if (ship == null) {
				this.setSchiff(0);
			} else {
				new Fehler(this + " hat nicht das Kommando über das Schiff " + ship + ".", this, this.getCoords());
				return false;
			}
		}
		
		// Gebäude testen .. einfach verlassen
		if (this.getGebaeude() != 0) this.Leave();
		if (this.getGebaeude() != 0) return false;
		
		return true;
	}
	
	/**
	 * Ã¼berprÃ¼fen ob die Bewegung Ã¼ber Land mÃ¶glich ist
	 * @param - diese Einheit hat den Bewegungsbefehl gesetzt
	 */
	public boolean checkSegeln(ReiseVerb reiseVerb)
	{
		Ship ship = Ship.Load(this.getSchiff());
		if (!ship.istFertig()) {
			new Fehler(this + " - das Schiff " + ship + " muss erst fertig gebaut werden.", this);
			return false;
		}
		
		// Ã¼berhaupt KapitÃ¤n -- einer am Steuer?
		Unit owner = Unit.Load(ship.getOwner());
		if (owner == null) {
			new Fehler(this + " - das Schiff " + ship + " hat keinen Kapitän.", this);
			return false;
		}

		// -- Befehlsgewalt
		if (this.getNummer() != ship.getOwner()) {
			new Fehler(this + " ist nicht Kapitän des Schiffes " + ship + ".", this);
			return false;
		}
		
		// Startregion
		Region region = Region.Load(this.getCoords());
		
		// Gewicht berechnen
		int gewicht = 0;
		for(Unit unit : region.getUnits()) if (unit.getSchiff() == ship.getNummer()) gewicht += unit.getGewicht();
		if (gewicht > ship.getKapazitaet())	{
			new Fehler(this + " - das Schiff " + ship + " ist völlig überladen.", this);
			return false;
		}
		
		// Talent Ã¼berprÃ¼fen ... KapitÃ¤n
		if (this.Talentwert(Segeln.class) < ship.getKapitaenTalent()) {
			new Fehler(this + " hat keine Ahnung wie er das Schiff " + ship + " segeln soll.", this);
			return false;
		}
		
		// Talent Ã¼berprÃ¼fen ... Mannschaft (quasi alle der eigenen Partei)
		int talent = 0;
		for(Unit unit : region.getUnits()) {
			if ( /* unit.getOwner() == owner.getOwner() && */ unit.getSchiff() == ship.getNummer()) {
				talent += unit.Talentwert(Segeln.class) * unit.getPersonen();
			}
		}
		if (talent < ship.getMatrosen()) {
			if (Region.Load(this.getCoords()).istBetretbar(this)) {
				new Fehler("Das Schiff " + ship + " hat nicht genügend Matrosen zum Ablegen.", this, this.getCoords());
			} else {
				new Fehler("Die Mannschaft reicht nicht aus, um " + ship + " zu steuern!", this, this.getCoords());
			}
			return false;
		}
				
		// Schiffbauer Ã¼berprÃ¼fen ... die sind die Einzigen die es schaffen in einem GebÃ¤ude
		// und einem Schiff gleichzeitig zu sein
		for(Unit unit : region.getUnits()) {
			if (unit.getGebaeude() != 0 && unit.getSchiff() == ship.getNummer()) {
				new Fehler("Das Schiff " + ship + " hat noch die Werftarbeiter an Bord.", this, this.getCoords());
				return false;
			}
		}
		
		// Bewegungspunkte setzen
		int punkte = ship.getGeschwindigkeit();
		for(Effect efx : this.getEffects())	{
			EFXBewegungSail bws = (EFXBewegungSail) efx;
			punkte += bws.EFXCalculate();
		}
		this.setBewegungspunkte(punkte);

		if (ship.getClass() == Boot.class) reiseVerb.setVerb("rudert");
		if (ship.getClass() == Langboot.class) reiseVerb.setVerb("fährt");
		if (ship.getClass() == Drachenschiff.class) reiseVerb.setVerb("segelt");
		if (ship.getClass() == Karavelle.class) reiseVerb.setVerb("segelt");
		if (ship.getClass() == Tireme.class) reiseVerb.setVerb("kommandiert die Rudersklaven");
		if (ship.getClass() == Galeone.class) reiseVerb.setVerb("segelt");
		
		return true;
	}
	
	/**
	 * Dokumentation der Reise für den Spieler
	 * @param bewegung - das sind die Regionen, die durchquert wurden
	 * @param reiseVerb dieses Verb wird in den Meldungen verwendet
	 */
	public void ReiseDoku(List<Region> bewegung, String reiseVerb) {
		Region r = null;
		
		Partei p = Partei.getPartei(this.getOwner());
		String msg = "";

		// Ausgangspunkt dokumentieren
		if (bewegung.size() > 0) {
            String rPrint = bewegung.get(0).toString();
            if (bewegung.get(0).getClass() == Ozean.class) {
                Coords my = p.getPrivateCoords(bewegung.get(0).getCoords());
                rPrint += " " + my.xy();
            }
            msg = this + " " + reiseVerb + " von " + rPrint;
			// für die Reporte (Karte) merken:
			// TODO die Startregion mit Details (?)
			p.addKnownRegion(bewegung.get(0), false, Unit.class);
			// TODO die Nachbarn der StartRegion ohne Details (?)
			for (Region nachbar : bewegung.get(0).getNachbarn()) {
				p.addKnownRegion(nachbar, false, Unit.class);
			}
		}

		// Durchreise dokumentieren
		if (bewegung.size() > 1) {
			msg += " durch ";
			for(int i = 1; i < bewegung.size(); i++) {
				r = bewegung.get(i);

				// für die Reporte (Karte) merken:
				// TODO die durchreiste Region mit Details (?)
				p.addKnownRegion(r, false, Unit.class);
				// TODO die Nachbarn der durchreisten Region ohne Details (?)
				for (Region nachbar : r.getNachbarn()) {
					p.addKnownRegion(nachbar, false, Unit.class);
				}

                // Region auflisten,
                String rPrint = r.toString();
                if (r.getClass() == Ozean.class) {
                    Coords my = p.getPrivateCoords(r.getCoords());
                    rPrint += " " + my.xy();
                }
				msg += rPrint;

				// durch Komma trennen, nur die vorletzte mit ... und ...
                if (i < bewegung.size() - 1) {
                    if (i < bewegung.size() -2) {
                        msg += ", ";
                    } else {
                        msg += " und ";
                    }
                }
			}
		}
		if (bewegung.size() > 0) {
            r = Region.Load(this.getCoords());
            String rPrint = r.toString();
            if (r.getClass() == Ozean.class) {
                Coords my = p.getPrivateCoords(r.getCoords());
                rPrint += " " + my.xy();
            }
			msg += " nach " + rPrint + ".";
			new Bewegung(msg, this);
		}
	}

    @Override
    public Object clone() throws CloneNotSupportedException {
        Class<? extends Unit> clazz = this.getClass();
        Unit clone = null;
        try {
            clone = clazz.newInstance();
        } catch (InstantiationException ex) {
            new BigError(ex);
        } catch (IllegalAccessException ex) {
            new BigError(ex);
        }
        
        clone.setNummer(getNummer());
        clone.setPersonen(getPersonen());
        clone.setOwner(getOwner());
        clone.setTarnPartei(getTarnPartei());
        clone.setTarnRasse(getTarnRasse());
        clone.setName(getName());
        clone.setBeschreibung(getBeschreibung());
        clone.setLebenspunkte(getLebenspunkte());
        clone.setBewacht(getBewacht());
        clone.setMana(getMana());
        clone.setEinkommen(getEinkommen());
        clone.sortierung = getSortierung(); // setSortierung() interagiert mit dem UnitCache - das wollen wir hier umgehen.
        clone.setTempNummer(getTempNummer());
        // TODO: sicherheitshalber "deep" clonen...
        clone.Items.addAll(getItems());
        clone.skills.addAll(getSkills());
        clone.spells.addAll(getSpells());
        clone.effects.addAll(getEffects());
        clone.tags.putAll(tags);
        
        
        return clone;
    }
    
    
	
	/**
	 * kapselt eine einzelne Person einer Einheit, z.B. wird wenn Hunger 
	 * herrscht hierüber die individuelle Schwächung modelliert. In gewisser
	 * Weise die Entsprechung zu de.x8bit.Fantasya.Host.ZAT.Battle.Krieger .
	 */
	protected class Person {
		int lebenspunkte;
		int maxLebenspunkte;

		public Person() {
		}

		public int getLebenspunkte() {
			return lebenspunkte;
		}

		public void setLebenspunkte(int lebenspunkte) {
			this.lebenspunkte = lebenspunkte;
		}

		public int getMaxLebenspunkte() {
			return maxLebenspunkte;
		}

		public void setMaxLebenspunkte(int maxLebenspunkte) {
			this.maxLebenspunkte = maxLebenspunkte;
		}

		public void addLebenspunkte(int lp) {
			setLebenspunkte(getLebenspunkte() + lp);
		}
	}
	
	public class PersonenLebenspunktComparator implements Comparator<Person> {
		@Override
		public int compare(Person p1, Person p2) {
			if (p1.getLebenspunkte() > p2.getLebenspunkte()) return -1;
			if (p1.getLebenspunkte() < p2.getLebenspunkte()) return +1;
			return 0;
		}
		
	}

}
