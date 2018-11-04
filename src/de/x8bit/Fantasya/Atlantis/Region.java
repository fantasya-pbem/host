package de.x8bit.Fantasya.Atlantis;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Buildings.Hafen;
import de.x8bit.Fantasya.Atlantis.Buildings.Leuchtturm;
import de.x8bit.Fantasya.Atlantis.Buildings.Ruine;
import de.x8bit.Fantasya.Atlantis.Buildings.Seehafen;
import de.x8bit.Fantasya.Atlantis.Buildings.Steg;
import de.x8bit.Fantasya.Atlantis.Helper.Nachfrage;
import de.x8bit.Fantasya.Atlantis.Helper.RegionsSicht;
import de.x8bit.Fantasya.Atlantis.Items.Alpaka;
import de.x8bit.Fantasya.Atlantis.Items.Elefant;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Kamel;
import de.x8bit.Fantasya.Atlantis.Items.Katapult;
import de.x8bit.Fantasya.Atlantis.Items.LuxusGood;
import de.x8bit.Fantasya.Atlantis.Items.Mastodon;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Zotte;
import de.x8bit.Fantasya.Atlantis.Messages.Bewegung;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Fehler;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Chaos;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Sandstrom;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Atlantis.Ships.Boot;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.EVA.Environment;
import de.x8bit.Fantasya.Host.EVA.Reporte;
import de.x8bit.Fantasya.Host.EVA.util.InselVerwaltung;
import de.x8bit.Fantasya.Host.EVA.util.ParteiInselAnker;
import de.x8bit.Fantasya.Host.EVA.util.ZATMode;
import de.x8bit.Fantasya.Host.Reports.Writer.CRWriter;
import de.x8bit.Fantasya.Host.Reports.Writer.XMLWriter;
import de.x8bit.Fantasya.Host.Terraforming.RegionsNamen;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StringUtils;

/**
 * hmmm ... eine Region?!
 * @author  mogel
 */
@SuppressWarnings("rawtypes")
public abstract class Region extends Atlantis {

	static {
		CACHE = new TreeMap<Coords, Region>();
	}

	/**
	 * erzeugt eine neue Region an den entsprechenden Koordinaten
	 * @param typ - Typ der Region, also Ebene etc.
	 * @param coords - Koordinaten der neuen Region
	 * @return eine neue Region
	 */
	public static Region Create(String typ, Coords coords) {
		return Create(typ, coords.getX(), coords.getY(), coords.getWelt());
	}

	/**
	 * erzeugt eine neue Region an den entsprechenden Koordinaten
	 * @param typ - Typ der Region, also Ebene etc.
	 * @param x	- Koordinate X
	 * @param y - Koordinate Y
	 * @param welt - Welt
	 * @return eine neue Region
	 */
	public static Region Create(String typ, int x, int y, int welt) {
		// new SysMsg("neue Region [" + x + "/" + y + "/" + welt + "/" + typ + "]");

		Region r = null;
		try {
			// pauschal anlegen
			r = (Region) Class.forName("de.x8bit.Fantasya.Atlantis.Regions." + typ).newInstance();
			r.setCoords(new Coords(x, y, welt));
			r.setEnstandenIn(GameRules.getRunde() - 5); // vgl. Partei::cansee() wieso hier minus 5
			r.RenameRegion();

			// Region komplett initialisieren ... Resourcen und so
			r.Init();

			if (Main.getBFlag("EVA")) {
				Region.CACHE.put(r.getCoords(), r);
			}

			// jetzt aus DB laden und zurück liefern ... wenn die
			// Region bereits existierte (und somit beim einfügen ein Fehler auftrat),
			// dann wird jetzt hier die eigentliche Region geliefert
			r = Load(x, y, welt);
		} catch (Exception e) {
			new SysMsg("konnte Region [" + x + "/" + y + "/" + welt + "/" + typ + "] nicht erstellen - " + e.toString());
		}

		return r;
	}

	/**
	 * lädt eine bekannte Region aus der Datenbank
	 * @param coords - die Koordinaten
	 * @return eine bekannte Region, ggf. Chaos wenn die Region noch nicht existiert
	 */
	public static Region Load(Coords coords) {
		return Load(coords.getX(), coords.getY(), coords.getWelt());
	}

	/**
	 * lädt eine bekannte Region aus der Datenbank
	 * @param x - Koordinate X
	 * @param y - Koordinate Y
	 * @param welt - die Ebene
	 * @return eine bekannte Region, ggf. Chaos wenn die Region (noch) nicht existiert
	 */
	public static Region Load(int x, int y, int welt) {
		Coords c = new Coords(x, y, welt);

		if (Region.CACHE.containsKey(c)) {
			return Region.CACHE.get(c);
		}

		// Dann müssen wir davon ausgehen, dass es die Region (noch) nicht gibt:
		return new Chaos(x, y, welt);
	}

	/**
	 * lädt eine Region "flach" aus der DB - also ohne Einheiten, Resourcen, Luxus etc.
	 * Es liegt in der Verantwortung der aufrufenden Instanz, diese Verknüpfungen herzustellen,
	 * das "lazy init" von Region wird hier gerade umgangen!
	 * @param rs ResultSet aus der Tabelle 'regionen'
	 * @return die Region - oder null
	 */
	public static Region fromResultSet(ResultSet rs) {
		Region r = null;

		try {
			String typ = rs.getString("typ");
			r = (Region) Class.forName("de.x8bit.Fantasya.Atlantis.Regions." + typ).newInstance();

			Coords c = new Coords(rs.getInt("koordx"), rs.getInt("koordy"), rs.getInt("welt"));
			r.setCoords(c);

			r.setBauern(rs.getInt("bauern"));
			r.setName(rs.getString("name"));
			r.setBeschreibung(rs.getString("beschreibung"));
			r.setAlter(rs.getInt("ralter"));
			r.setEnstandenIn(rs.getInt("entstandenin"));
			r.setInselKennung(rs.getInt("insel"));
			r.setSilber(rs.getInt("silber"));

			// r.__units = new ArraySortedSet<Unit>();
		} catch (Exception e) {
			new BigError("Fehler beim Laden der Region " + rs.toString() + ": " + e.getMessage());
		}

		return r;
	}
	/** (EVA) Map ALLER Regionen, Schlüssel sind ihre Koordinaten */
	public final static Map<Coords, Region> CACHE;
	public static boolean USE_TOPTW_CACHE = false;

	@Override
	public int getNummer() {
		return this.getCoords().asRegionID(true);
	}

	/** alle Einheiten in dieser Region */
//	private SortedSet<Unit> __units = null;
	/**
	 * @return alle Einheiten in dieser Region
	 */
	public Set<Unit> getUnits() {
		return Unit.CACHE.getAll(getCoords());
	}

	/**
	 * @param p die gewünschte Partei
	 * @return alle Einheiten der Partei p in dieser Region
	 */
	public SortedSet<Unit> getUnits(Partei p) {
		return getUnits(p.getNummer());
	}

	/**
	 * @param parteiNummer die Nummer der gewünschten Partei
	 * @return alle Einheiten der Partei #parteiNummer in dieser Region
	 */
	public SortedSet<Unit> getUnits(int parteiNummer) {
		SortedSet<Unit> retval = new TreeSet<Unit>();
		for (Unit u : getUnits()) {
			if (u.getOwner() == parteiNummer) {
				retval.add(u);
			}
		}
		return retval;
	}

	public Set<Partei> anwesendeParteien() {
		Set<Partei> parteien = new HashSet<Partei>();
		for (Unit unit : getUnits()) {
			Partei p = Partei.getPartei(unit.getOwner());
			if (p == null) {
				throw new IllegalStateException("Eine anwesende Partei in Region " + this + " ist null: Partei [" + p + "].");
			}

			parteien.add(p);
		}

		return parteien;
	}

	/** alle Gebäude in dieser Region
	 * @return Alle Gebäude in dieser Region, unabhängig von Art, Insassen und Zustand.
	 */
	public Set<Building> getBuildings() {
		return Building.PROXY.getAll(getCoords());
	}

	public Nachfrage getNachfrage(Class<? extends Item> luxus) {
		for (Nachfrage n : getLuxus()) {
			if (n.getItem() == luxus) {
				return n;
			}
		}
		return null;
	}
	
	public Nachfrage getKaufNachfrage()
	{
		for(Nachfrage nachfrage : this.getLuxus())
		{
			if (nachfrage.getNachfrage() < 0) { return nachfrage; }
		}
		return null;
	}
	
	public boolean hasKaufNachfrage()
	{
		for (Nachfrage nachfrage : this.getLuxus())
		{
			if (nachfrage.getNachfrage() < 0) { return true; }
		}
		return false;
	}
	
	public boolean canNachfrage() { return true; }
	
	/** alle Handelsgüter für diese Region */
	private ArrayList<Nachfrage> __luxus = new ArrayList<Nachfrage>();

	public ArrayList<Nachfrage> getLuxus() {
		if (__luxus.isEmpty()) {
			// offensichtlich eine Region, die noch nicht durch Init() gegangen ist! (?)
			// dieser Fall tritt derzeit (Okt 2010) regelmäßig in Region.cloneAs() auf
// crasht mit dem neuen Layout-System und sollte nicht mehr aktuell sein.
//			this.Init_Handel();
		}
		return __luxus;
	}

	/**
	 * Sollte nur beim Terraforming o.ä. verwendet werden!
	 * @param luxusListe
	 */
	public void setLuxus(List<Nachfrage> luxusListe) {
		__luxus = new ArrayList<Nachfrage>();
		__luxus.addAll(luxusListe);
	}
	/** alle Schiffe in dieser Region */
	private List<Ship> __ships = new ArrayList<Ship>();

	public List<Ship> getShips() {
		return __ships;
	}

	/**
	 * liefert den höchsten TW für die gesammte Region
	 * @param skill - dieser Skill ist gesucht
	 * @param partei - die Einheit muss zu dieser Partei gehören (oder 0 für alle Parteien)
	 * @return TW oder 0, falls es gar keine passende Einheit gibt
	 */
	public int topTW(Class<? extends Skill> skill, int partei) {
		Unit u = TopSkill(skill, partei);
		if (u == null) {
			// new Debug("topTW(" + skill.getSimpleName() + ")-Einheit in " + this + " für " + Partei.Load(partei) + " ist null.");
			return 0;
		}
		return u.Talentwert(skill);
	}

	/**
	 * liefert die Einheit mit dem höchsten Skill für die gesammte Region
	 * @param skill - dieser Skill ist gesucht
	 * @param partei - die Einheit muss zu dieser Partei gehören (oder 0 für alle Parteien)
	 * @return Einheit mit höchsten Skill oder null, wenn es gar keine passende Einheit gibt
	 */
	public Unit TopSkill(Class<? extends Skill> skill, int partei) {
		if (Region.USE_TOPTW_CACHE) {
			// gecachte Abkürzung, wird in Reporte() verwendet,
			// wg. Partei.cansee(Unit) / Unit.cansee(Unit)
			return Reporte.TopSkillCache.topUnit(this.getCoords(), partei, skill);
		}

		Unit tu = null;

		// jetzt die Region durchsuchen nach einer Einheit die besser (als nichts) ist
		Set<Unit> kandidaten;
		if (partei == 0) {
			kandidaten = getUnits();
		} else {
			kandidaten = getUnits(partei);
		}
		for (Unit u : kandidaten) {
			if (tu == null) {
				tu = u;
			}
			// höchstes Talent in der gesamten Region?
			if (u.Talentwert(skill) > tu.Talentwert(skill)) {
				tu = u;
			}
		}

		return tu;
	}

	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle entspricht
	 */
	public Map<String, Object> getStrassenDBValues(Richtung richtung) {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("koordx", this.getCoords().getX());
		fields.put("koordy", this.getCoords().getY());
		fields.put("welt", this.getCoords().getWelt());
		fields.put("richtung", richtung.name());
		fields.put("anzahl", getStrassensteine(richtung));

		return fields;
	}

	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle entspricht
	 */
	public Map<String, Object> getDBValues() {
		Map<String, Object> fields = new HashMap<String, Object>();

		String luxus = "";
		for (Nachfrage n : this.getLuxus()) {
			if (n.getNachfrage() < 0) {
				luxus = n.getItem().getSimpleName();
			}
		}

		fields.put("baeume", 0); // deprecated Baum
		fields.put("bauern", this.getBauern());
		fields.put("name", this.getName());
		fields.put("beschreibung", this.getBeschreibung());
		fields.put("ralter", this.getAlter());
		fields.put("entstandenin", getEnstandenIn()); // neu 2011-05-25
		fields.put("insel", this.getInselKennung());
		fields.put("typ", this.getClass().getSimpleName());				// Typ ist über zauberspruch änderbar !
		fields.put("silber", this.getSilber());
		fields.put("koordx", this.getCoords().getX());
		fields.put("koordy", this.getCoords().getY());
		fields.put("welt", this.getCoords().getWelt());
		fields.put("luxus", luxus);
		fields.put("lohn", this.getLohn());

		return fields;
	}

	/**
	 * gibt der Regionen einen Namen
	 */
	public void RenameRegion() {
		if (Random.W(6) < 3) {
			setName(RegionsNamen.Zufaellig());
			return;
		}

		String vokale = "aeiou";
		String kons = "bcdfghjklmnpqrstvwxyz";

		boolean Apostroph = false;		// true wenn ein ' gesetzt werden soll

		StringBuilder name = new StringBuilder("");

		int maxsize;
		int i;
		int lastChar;		// ob das letzte ein Vokal(0) oder Konsonant war

		maxsize = Random.rnd(5, 13);
		lastChar = Random.rnd(0, 2);	// Anfang mit Vokal(0) oder Konsonant (1)

		for (i = 0; i < maxsize; i++) {
			// kleine Chance für '
			if ((Random.rnd(0, 255) % 128) <= 4) {
				Apostroph = true;
			}

			// ... aber nicht am Anfang und Ende ...
			if ((i == 0) || (i == maxsize - 1)) {
				Apostroph = false;
			}

			// ... und nicht wenn gerade erst eines war
			if (i > 0) {
				if ((name.charAt(i - 1) == '\'') || (name.charAt(i - 1) == '-')) {
					Apostroph = false;
				}
			}

			if (!Apostroph) {
				// dann setze einen Buchstaben ein
				char buchstabe;
				if (lastChar != 0) {
					// neuer Vokal
					buchstabe = vokale.charAt(Random.rnd(0, vokale.length()));
				} else {
					// neuer Konsonant
					buchstabe = kons.charAt(Random.rnd(0, kons.length()));
				}

				// erste Buchstabe immer groß!
				if (i == 0) {
					String s = new StringBuilder().append(buchstabe).toString();
					s = s.toUpperCase();
					buchstabe = s.charAt(0);
				} else {
					if (name.charAt(i - 1) == '-') // bei Bindestrichen (Unterwelt) auch Groß
					{
						String s = new StringBuilder().append(buchstabe).toString();
						s = s.toUpperCase();
						buchstabe = s.charAt(0);
					}
				}

//				// Buchstabe nach Bindestrich auch groß
//				if (name.length() > 2) if (name.charAt(name.length() - 1) == '-')
//				{
//					String s = new StringBuilder().append(buchstabe).toString();
//					s = s.toUpperCase();
//					buchstabe = s.charAt(0);
//				}

				// hinzufügen
				name.append(buchstabe);

				// Vokale und Konsonanten immer abwechselnd
				lastChar++;
				lastChar %= 2;
			} else {
				// sonst ein Apostroph
				if (getCoords().getWelt() >= 0) {
					name.append('\'');
				} else {
					name.append('-');	// Unterwelt
				}
			}
		}

		// Name der Region setzen
		setName(name.toString());
	}

	/**
	 * initialisiert die Region mit weiteren Dingen die in allen Regionen gleich sind
	 */
	public void Init() {
		Init_Handel();
	}

	/** hier werden jetzt alle Handelsgüter pauschal initialisiert */
	@SuppressWarnings("unchecked")
	public void Init_Handel() {
		__luxus.clear();

		// alle bekannten Luxusgüter sammeln und Nachfrage erzeugen
		if (Paket.getPaket("Items") == null) {
			return;
		}
		
		for (Paket p : Paket.getPaket("Items")) {
			if (p.Klasse instanceof LuxusGood) {
				__luxus.add(new Nachfrage((Class<? extends Item>) p.Klasse.getClass(), 11.0f));
			}
		}
	}

	@Override
	public String toString() {
		String typ = this.getClass().getSimpleName() + " ";

		if (typ.equalsIgnoreCase("Chaos ")) {
			typ = "";
		}
		if (typ.equalsIgnoreCase("Ozean ")) {
			typ = "";
		}
		if (typ.equalsIgnoreCase("Lavastrom ")) {
			typ = "";
		}
		if (typ.equalsIgnoreCase("Sandstrom ")) {
			typ = "";
		}
		if (typ.equalsIgnoreCase("aktiverVulkan ")) {
			typ = "aktiver Vulkan ";
		}

		return typ + getName();
	}

	public Region() {
		strassensteine = new int[Richtung.values().length];
	}

	/**
	 * @return
	 * @uml.property  name="baum"
	 */
	public int getBaum() {
		return getResource(Holz.class).getAnzahl();
	}
	/**
	 * Bauern in der Region
	 * @uml.property  name="bauern"
	 */
	private int Bauern;

	/**
	 * @return
	 * @uml.property  name="bauern"
	 */
	public int getBauern() {
		return Bauern;
	}

	/**
	 * @param bauern
	 * @uml.property  name="bauern"
	 */
	public void setBauern(int bauern) {
		if (bauern >= 0) {
			Bauern = bauern;
		} else {
			new SysMsg("negativer Wert für bauern");
		}
	}

	/** Rekruten der Region */
	public int Rekruten() {
		return getBauern() / 20;
	}
	/** Resourcen in der Region */
	private List<Item> resourcen = new ArrayList<Item>();

	public List<Item> getResourcen() {
		return resourcen;
	}

	public Item getResource(Class<? extends Item> item) {
		Item resource = null;

		// suchen
		for (int i = 0; i < resourcen.size(); i++) {
			resource = resourcen.get(i);
			if (resource.getClass().equals(item)) {
				return resource;
			}
		}

		// bei Bedarf erstellen
		try {
			resource = (Item) item.newInstance();
			resourcen.add(resource);
		} catch (Exception ex) {
			new BigError(ex);
		}

		return resource;
	}

	public void setResource(Class<? extends Item> item, int anzahl) {
		Item t = getResource(item);
		t.setAnzahl(anzahl);
	}
	/** verbaute Strassensteine in dieser Region */
	private int[] strassensteine;

	/** verbaute Strassensteine in dieser Region */
	public int getStrassensteine(Richtung richtung) {
		return strassensteine[richtung.ordinal()];
	}

	/** verbaute Strassensteine in dieser Region */
	public void setStrassensteine(Richtung richtung, int anzahl) {
		if (anzahl >= 0) {
			strassensteine[richtung.ordinal()] = anzahl;
		} else {
			new SysMsg("negativer Wert für strassensteine");
		}
	}
	/**
	 * benötigte Steine pro Strasse
	 * @uml.property  name="steineFuerStrasse"
	 * @uml.associationEnd
	 */
	private int steineFuerStrasse = 0;

	public int getSteineFuerStrasse() {
		return steineFuerStrasse;
	}

	public void setSteineFuerStrasse(int value) {
		steineFuerStrasse = value;
	}

	/**
	 * Sollte in abgeleiteten Klassen entsprechend überladen werden!
	 * @return Der passende deutsche Artikel z.B. für die Auflistung von Nachbarregionen im NR.
	 */
	public String getArtikel() {
		return "die";
	}
	/**
	 * Angabe zur Besiedlung der Region: 0, wenn lange keine Einheit mehr hier war. Maximalwert (derzeit 10), wenn Einheiten anwesend sind.
	 * Wird jede Runde um 1 verringert bis zum Minimum 0.
	 * @uml.property  name="alter"
	 */
	private int Alter;

	/**
	 *
	 * @return Angabe zur Besiedlung der Region: 0, wenn lange keine Einheit mehr hier war. Maximalwert (derzeit 10), wenn Einheiten anwesend sind.
	 * @uml.property  name="alter"
	 */
	public int getAlter() {
		return Alter;
	}

	/**
	 * @param value
	 * @uml.property  name="alter"
	 */
	public void setAlter(int value) {
		Alter = value;
	}
	/**
	 * die Runde, in der die Region &quot;erschaffen&quot; wurde. Falls nicht bekannt, -1 verwenden!
	 */
	protected int enstandenIn;

	/**
	 * @return die Runde, in der die Region &quot;erschaffen&quot; wurde. Falls nicht bekannt, -1
	 */
	public int getEnstandenIn() {
		return enstandenIn;
	}

	/**
	 * @param entstandenIn die Runde, in der die Region &quot;erschaffen&quot; wurde. Falls nicht bekannt, -1 verwenden!
	 */
	public void setEnstandenIn(int enstandenIn) {
		this.enstandenIn = enstandenIn;
	}
	/**
	 * Silber der Bauern
	 * @uml.property  name="silber"
	 */
	private int silber = 0;

	/**
	 * @return
	 * @uml.property  name="silber"
	 */
	public int getSilber() {
		return silber;
	}

	/**
	 * @param value
	 * @uml.property  name="silber"
	 */
	public void setSilber(int value) {
		if (silber < 0) {
			new SysErr("Silber für '" + this + "' < 0 => Reset");
		}
		silber = value;
	}
	/**
	 * Insel-Kennung der Region
	 * @uml.property  name="inselKennung"
	 */
	private int InselKennung;

	/**
	 * @return
	 * @uml.property  name="inselKennung"
	 */
	public int getInselKennung() {
		return InselKennung;
	}

	/**
	 * @param value
	 * @uml.property  name="inselKennung"
	 */
	public void setInselKennung(int value) {
		InselKennung = value;
	}

	/** Handelsgut für diese Region - das produzieren die Bauern */
	public Class<? extends Item> getProduce() {
		for (Nachfrage n : getLuxus()) {
			if (n.getNachfrage() < 0) {
				return n.getItem();
			}
		}
		return null; // Ozeane etc.
	}

	/** Nachfrage für ein Handelsgut festlegen */
	public void setNachfrage(Class<? extends Item> item, float nachfrage) {
		for (Nachfrage n : getLuxus()) {
			if (n.getItem().equals(item)) {
				n.setNachfrage(nachfrage);
				return;
			}
		}

		getLuxus().add(new Nachfrage(item, nachfrage));
	}

	/**
	 * sammelt das Geld von anderen Einheiten bzw. Alliierten
	 * @param unit - diese Einheit
	 * @param bedarf - soviel benötigt
	 * @param grund - wenn ungleich null, wird dem Spender eine Meldung über diesen Grund geschickt
	 * @return soviel bekommen
	 */
	public int CollectMoney(Unit unit, int bedarf, String grund) {
		// TODO Alliierte
		//Partei partei = Partei.Load(unit.getOwner());

		int money = 0;

		for (Unit other : getUnits()) {
			// nichts bei sich selbst einsammeln:
			if (other.getNummer() == unit.getNummer()) {
				continue;
			}
			// und auch nicht bei fremden Parteien
			if (other.getOwner() != unit.getOwner()) {
				continue;
			}

			Item geld = other.getItem(Silber.class);
			int spende = 0;

			if (geld.getAnzahl() < bedarf) {
				// alles "abbuchen":
				spende = geld.getAnzahl();
			} else {
				// nur den (Rest-)Bedarf einsammeln:
				spende = bedarf;
			}

			money += spende;
			bedarf -= spende;
			geld.setAnzahl(geld.getAnzahl() - spende);

			if ((grund != null) && (spende > 0)) {
				new Info(other + " borgt " + unit + " " + spende + " Silber " + grund + ".", other);
			}

			if (bedarf <= 0) {
				break;
			}
		}

		return money;
	}

	/**
	 * der Lohn in dieser Region
	 */
	public int getLohn() {
		Burg biggestCastle = null;

		for (Building b : getBuildings()) {
			if (!b.getClass().equals(Burg.class)) {
				continue;
			}
			if (biggestCastle == null || biggestCastle.getSize() < b.getSize()) {
				biggestCastle = (Burg)b;
			}
		}

		if (biggestCastle == null) {
			return 11;
		}
		return biggestCastle.getLohn();
	}

	/**
	 * existiert eine Strasse in die angegebene Richtung ... pauschal erstmal nein :)
	 * @param richtung
	 * @return ja / nein / vieleicht
	 */
	public boolean hatStrasse(Richtung richtung) {
		// Nachbarregion holen
		Region hr = Region.Load(getCoords().shift(richtung));
		// Richtung umdrehen
		Richtung back = richtung.invert();
		// Strasse nur wenn in beiden Regionen in die passende Richtung fertig
		// new SysMsg("Straßensteine in " + this + " nach " + richtung + ": " + this.getStrassensteine(richtung) + " von mind. " + this.getSteineFuerStrasse());
		// new SysMsg("Straßensteine in " + hr + " nach " + back + ": " + hr.getStrassensteine(back) + " von mind. " + hr.getSteineFuerStrasse());
		return ((this.getStrassensteine(richtung) >= this.getSteineFuerStrasse()) && (hr.getStrassensteine(back) >= hr.getSteineFuerStrasse()));
	}

	/**
	 * testet ob die Region bewacht wird ... dabei liefert die Funktion ein TRUE
	 * wenn die Region von <b>Nicht</b>-Alliierten Einheiten bewacht wird
	 * @param unit - Status für diese Einheit abfragen
	 * @param ao - diese Option wird benötigt um das Bewache zu umgehen
	 * @return TRUE wenn die Region von Nicht-Allierten bewacht wird
	 */
	public boolean istBewacht(Unit unit, AllianzOption ao) {
		boolean bewacht = false;

		Partei owner = Partei.getPartei(unit.getOwner());
		if (owner == null) {
			new BigError("Einheit ohne Parteizugehörigkeit!");
		}

		for (Unit other : getUnits()) {
			// eigene Einheiten sind egal ... nicht wachende auch
			if (other.getOwner() != unit.getOwner() && other.getBewacht()) {
				Partei o = Partei.getPartei(other.getOwner());
				// wenn die Partei jetzt nicht HELFE <Option> gesetzt hat -> schlecht ... es wird bewacht
				if (!o.hatAllianz(unit.getOwner(), ao)) {
					bewacht = true;
				}
			}
		}

		return bewacht;
	}

	/**
	 * @param u Einheit, die von eventueller Bewachung betroffen wäre
	 * @return Alle Parteien, die effektiv bewachende Einheiten gegenüber Einheit u haben - Parteien, die nicht mit Einheit u alliert sind und die auch keinen Kontakt aufgenommen haben.
	 */
	public Set<Partei> getBewacherParteien(Unit u) {
		Set<Partei> bewacher = new HashSet<Partei>();
		for (Unit other : getUnits()) {
			// eigene Einheiten sind egal ... nicht wachende auch
			if ((other.getOwner() != u.getOwner()) && other.getBewacht()) {
				Partei o = Partei.getPartei(other.getOwner());
				// wenn die Partei jetzt nicht HELFE <Option> gesetzt hat -> schlecht ... es wird bewacht
				if (!o.hatAllianz(u.getOwner(), AllianzOption.Kontaktiere)) {
					bewacher.add(o);
				}
			}
		}

		if (bewacher.size() > 0) {
			// erstmal schlecht, haben die alle KONTAKTIERE gesetzt?
			for (Unit other : getUnits()) {
				if (other.getOwner() == u.getOwner()) {
					continue;
				}

				if (u.hatKontakt(other, AllianzOption.Kontaktiere)) {
					Partei o = Partei.getPartei(other.getOwner());
					bewacher.remove(o);
				}
			}
		}

		return bewacher;
	}

	/**
	 * @param parteien Diejenigen Partei, deren Wachen beschrieben werden sollen. Vorzugsweise der Rückgabewert von Region.getBewacherParteien(Unit) .
	 * @return menschenlesbare Beschreibung der Bewacher
	 */
	public String getBewacherPhrase(Set<Partei> parteien) {
		if (parteien.isEmpty()) {
			return "niemand";
		}

		StringBuilder sb = new StringBuilder();
		List<Unit> bewacher = new ArrayList<Unit>();
		for (Partei p : parteien) {
			for (Unit u : getUnits()) {
				if (u.getOwner() != p.getNummer()) {
					continue;
				}
				if (!u.getBewacht()) {
					continue;
				}

				bewacher.add(u);
			}
		}

		sb.append(StringUtils.aufzaehlung(bewacher));

		return sb.toString();
	}

	/**
	 * testet, ob diese Region eine Burg in der entsprechenden Größe besitzt
	 * @param building - das Gebäude wird getestet
	 * @param mindestgroesse - so groß muss es sein - oder 0 wenn egal (also nur testen auf vorhanden)
	 * @param owner - Besitzer-<b>Partei</b> oder null, wenn egal
	 * @return
	 */
	public boolean hatGebaeude(Class<? extends Building> building, int mindestgroesse, Unit owner) {
		boolean ok = false;

		for (Building b : getBuildings()) {
			if (b.getClass().equals(building)) {
				if (owner != null) {
					Unit u = Unit.Load(b.getOwner());
					if (u != null) {
						if (u.hatKontakt(owner, AllianzOption.Kontaktiere) && b.getSize() >= mindestgroesse && b.hatFunktion()) {
							ok = true;
						}
					}
				} else {
					if (b.getSize() >= mindestgroesse && b.hatFunktion()) {
						ok = true;
					}
				}
			}
		}
		return ok;
	}

	/**
	 * <font color="red"><b>die Überschreibung in den einzelnen Regionen liefert die <u>freie</u> Arbeitsplätze</b></font><br/>
	 * liefert die verwendeten Arbeitsplätze ... dabei liegt der Verbrauch wie folgt
	 * <ul>
	 * <li>1 Baum benötigt 10 Arbeitsplätze</li>
	 * <li>1 Bauern benötigt 1 Arbeitsplatz</li>
	 * <li>1 Pferd benötigt 1 Arbeitsplatz</li>
	 * <li>1 Kamel benötigt 1 Arbeitsplatz</li>
	 * <li>1 Elefant benötigt 5 Arbeitsplätze</li>
	 * </ul>
	 * <font color="red"><b>die Überschreibung in den einzelnen Regionen liefert die <u>freie</u> Arbeitsplätze</b></font>
	 * @return verwendete Arbeitsplätze
	 */
	public int benutzteArbeitsplaetze() {
		int used = getBauern();

		used += getResource(Holz.class).getAnzahl() * 10;
		used += getResource(Pferd.class).getAnzahl();
		used += getResource(Kamel.class).getAnzahl();
		used += getResource(Elefant.class).getAnzahl() * 5;
		
		used += getResource(Zotte.class).getAnzahl();
		used += getResource(Alpaka.class).getAnzahl();
		used += getResource(Mastodon.class).getAnzahl() * 5;

		return used;
	}

	public int freieArbeitsplaetze() {
		return 0;
	}

	/**
	 * Wachstum von allem Möglichen und Unterhalt der Bauern
	 */
	public void Wachstum() {
		Wachstum_Bauern(5, 1);
		BauernUnterhalt();

		getResource(Holz.class).actionWachstum(this);
		
		getResource(Pferd.class).actionWachstum(this);
		getResource(Elefant.class).actionWachstum(this);
		getResource(Kamel.class).actionWachstum(this);
		
		getResource(Zotte.class).actionWachstum(this);
		getResource(Mastodon.class).actionWachstum(this);
		getResource(Alpaka.class).actionWachstum(this);

		Wachstum_Kraeuter();
	}

	/**
	 * berechnet das Wachstum der Bauern
	 * @param pauschal - soviel promille pro Groessenpunkt der Burg
	 * @param gebaeude - soviel promille pro Gebaeudetyp
	 */
	protected void Wachstum_Bauern(int pauschal, int gebaeude) {
		int nachwuchs = 0;

		// die Groesse der groessten Burg wir anhand des Lohnes berechnet.
		float rate = (float) ((this.getLohn() - 11) * pauschal);

		// dazu kommen jetzt noch die Anzahl der Gebaeudetypen
		Set<Class<? extends Building>> vielfalt = new HashSet<Class<? extends Building>>();
		for (Building b : this.getBuildings()) {
			if (b.getClass() == Burg.class) {
				continue;
			}
			if (b.getClass() == Ruine.class) {
				continue;
			}
			vielfalt.add(b.getClass());
		}

		rate += vielfalt.size() * gebaeude;

		// Bauern wachsen nur bei freien Arbeitsplaetzen "zufaellig" mit der
		// berechneten Rate.
		if (freieArbeitsplaetze() >= 1) {
			nachwuchs = (int) ((float)getBauern() * rate/200.0f);
//			Statistik - mogel - 13.01.2013
//			if (getBauern() > 1) {
//				for (int i = 0; i < getBauern(); i++) {
//					if (Random.W(1000) < rate) {
//						nachwuchs++;
//					}
//				}
//			}

			if (nachwuchs > 0) {
				Environment.NeugeboreneBauern.put(this, nachwuchs);
				setBauern(getBauern() + nachwuchs);
			}
		}

		// new Debug(this + " - " + getBauern() + " Bauern - neu: " + nachwuchs + " - rate: " + NumberFormat.getPercentInstance().format(rate), getCoords());
	}

	protected void Wachstum_Kraeuter() {
	}

	/** die Bauern wollen auch etwas essen */
	private void BauernUnterhalt() {
		// new Info("In " + this + " " + getCoords() + " wollen " + getBauern() + " Bauern essen, es gibt " + getSilber() + " Silber.", 0);
		int bedarf = getBauern() * 10; // soviel nötig
		if (bedarf < getSilber()) {
			setSilber(getSilber() - bedarf);
		} else {
			int hungern = (bedarf - getSilber()) / 10;
			int tot = 0;
			for (int i = hungern; i > 0; i--) {
				tot += (Random.rnd(0, 100) > 66) ? 1 : 0;
			}
			if (tot > 0) {
				Environment.VerhungerteBauern().put(this, tot);
//				new Debug("In " + this + " " + getCoords() + " hungern " + hungern + " Bauern, davon sind " + tot + " verhungert.", 0);
				new Info("In " + this + " hungern " + hungern + " Bauern, davon sind " + tot + " verhungert.", this);
				setBauern(getBauern() - tot);
			}
			setSilber(0);
		}
	}

	/**
	 * Bewegung einer Einheit durch bzw. aus der Region hinaus
	 * @param richtung - in die Richtung will die Einheit
	 * @param unit - und zwar diese Einheit
	 * @return die neue Region, wo die Einheit nun steht
	 */
	public Region Movement(Richtung richtung, Unit unit) {
		Region r = Region.Load(this.getCoords().shift(richtung));

		// Für uns nicht!!
		if (!Partei.getPartei(unit.getOwner()).canAccess(r)) {
			new Fehler(unit + " hat Angst vor " + GameRules.TERRAIN_UNSICHTBARER_REGIONEN + "-Regionen.", unit);
			return null;
		}

		// Chaos-Region ahead
		if (r instanceof Chaos) {
			new Fehler(unit + " hat Angst vor " + GameRules.TERRAIN_UNSICHTBARER_REGIONEN + "-Regionen.", unit);
			return null;
		}

		// nicht Betretbar
		if (!r.istBetretbar(unit)) {
			if (!unit.canFly()) {
				new Fehler(unit + " - " + r + " ist für uns nicht zugänglich.", unit);
				return null;
			}
		}

		// Bewegungspunkte überprüfen
		boolean hatStrasse = this.hatStrasse(richtung);
		int bewegungspunkte = (hatStrasse) ? 1 : 2;
		if (unit.getBewegungspunkte() < bewegungspunkte) {
			new Fehler(unit + " schafft keine weitere Reise in die Nachbarregion.", unit);
			return null;
		}

		// new SysMsg(unit + "@" + this + " -- BP vorher: " + unit.getBewegungspunkte() + " - verbraucht " + bewegungspunkte + "BP");
		// Ggf. Katapulteinheiten nur ein Feld bewegen lassen, wenn die Richtung keine Strasse hat.
		if (unit.getItem(Katapult.class).getAnzahl() > 0 && !(hatStrasse))
		{
			unit.setBewegungspunkte(0);
		}
		// Ansonsten nur normal die Bewegungspunkte abziehen.
		else
		{
			unit.setBewegungspunkte(unit.getBewegungspunkte() - bewegungspunkte);
		}

		Unit.CACHE.remove(unit);
		unit.setCoords(r.getCoords());
		Unit.CACHE.add(unit);


		return r;
	}

	/**
	 * Bewegung einer Einheit durch bzw. aus der Region hinaus
	 * @param richtung - in die Richtung will die Einheit
	 * @param ship - und zwar als Kapitän auf diesem Schiff
	 * @return die neue Region, wo die Einheit nun steht
	 */
	public Region Movement(Richtung richtung, Ship ship) {
		Unit kapitaen = Unit.Load(ship.getOwner());

		// in diese Region soll gesegelt werden
		Region next = Region.Load(this.getCoords().shift(richtung));

		// Für uns nicht!!
		if (!Partei.getPartei(kapitaen.getOwner()).canAccess(next)) {
			new Fehler(kapitaen + " hat Angst vor " + GameRules.TERRAIN_UNSICHTBARER_REGIONEN + "-Regionen.", kapitaen);
			return null;
		}

		// nicht Betretbar
		if (next.istBetretbar(null)) {
			// von Land zu Land geht nicht (außer Kanal, das aber später)
			if (this.istBetretbar(null)) {
				new Fehler(kapitaen + " - " + next + " ist kein Ozean.", kapitaen);
				return null;
			}

			if (!ship.getClass().equals(Boot.class)) {
				if (!next.getClass().equals(Ebene.class) && !next.getClass().equals(Wald.class)) {
					if (ship.neededHarbour() != null) {
						boolean ok = false;
						int minSize = 5;
						List<String> portReqs = new ArrayList<String>();
						for (Class<? extends Building> building : ship.neededHarbour()) {
							if (building.equals(Steg.class)) {
								minSize = 5;
							}
							if (building.equals(Hafen.class)) {
								minSize = 10;
							}
							if (building.equals(Seehafen.class)) {
								minSize = 20;
							}

							portReqs.add("einen " + building.getSimpleName() + " (Größe min. " + minSize + ")");

							if (!ok) {
								ok = next.hatGebaeude(building, minSize, kapitaen);
							}
						}
						if (!ok) {
							new Fehler("Das Schiff " + ship + " kann nur in Wäldern oder Ebenen anlegen; hier würde es " + StringUtils.aufzaehlungOder(portReqs) + " benötigen.", kapitaen);
							return null;
						}
						Building building = next.getGebaeude(Seehafen.class);
						if (building != null) {
							if (building.hatFunktion()) {
								Unit buildingowner = Unit.Load(building.getOwner());
								if (buildingowner != null) {
									Partei besitzerVolk = Partei.getPartei(buildingowner.getOwner());
									Allianz allianz = besitzerVolk.getAllianz(kapitaen.getOwner());
									if (kapitaen.getOwner() != besitzerVolk.getNummer()) {
										if (!allianz.getOption(AllianzOption.Kontaktiere)) {
											new Fehler("Der Seehafen " + building + " unter dem Kommando von " + buildingowner + " hindert " + ship + " am Einlaufen.", kapitaen);
											return null;
										} else {
											new Bewegung("Der Seehafen " + building + " unter dem Kommando von " + buildingowner + " erlaubt uns die Einfahrt.", kapitaen);
										}
									} else {
										// der Seehafen gehört uns selbst.
									}
								}
							} else {
								// Gebäude hat keine Funktion
								Unit buildingowner = Unit.Load(building.getOwner());
								if (buildingowner != null) {
									new Bewegung("Das Schiff " + ship + " konnte anlegen, weil das Gebäude " + building + " keinen Unterhalt hat.", buildingowner);
								}
							}
						} else {
							// kein Seehafen vorhanden
						}
					} else {
						// Schiff benötigt keinen Steg, Hafen oder Seehafen
					}
				}
			}
		}

		// Chaos-Region ahead
		if (next instanceof Chaos) {
			new Fehler(kapitaen + " hat Angst vor Chaos-Regionen.", kapitaen);
			return null;
		}

		// Bewegungspunkte überprüfen
		if (kapitaen.getBewegungspunkte() <= 0) {
			new Fehler(kapitaen + " schafft keine weitere Reise in die Nachbarregion.", kapitaen);
			return null;
		}

		// Einheit bewegen
		kapitaen.setBewegungspunkte(kapitaen.getBewegungspunkte() - 1);


		Set<Unit> mannschaft = new HashSet<Unit>(ship.getUnits()); // wg. ConcurrentMod.Ex.
		for (Unit unit : mannschaft) {

			Unit.CACHE.remove(unit);
			unit.setCoords(next.getCoords());
			Unit.CACHE.add(unit);

		}

		// Koordinaten des Schiffs anpassen
		ship.setCoords(next.getCoords());
		this.getShips().remove(ship);
		next.getShips().add(ship);

		return next;
	}

	private Building getGebaeude(Class<? extends Building> clazz) {
		for (Building b : this.getBuildings()) {
			if (b.getClass() == clazz) {
				return b;
			}
		}
		return null;
	}

	/**
	 * kann diese Region zu Fuß/Pferd durchreist werden
	 * @param unit das Betreten speziell für diese Einheit prüfen (oder NULL)
	 * @return TRUE wenn die Region betreten werden kann
	 */
	public boolean istBetretbar(Unit unit) {
		if (unit == null) {
			return true;
		}

		// Die Region ist zu neu bzw. die Partei sieht diese Region
		// aus irgendwelchen anderen Gründen nicht.
		if (!Partei.getPartei(unit.getOwner()).canAccess(this)) {
			return false;
		}

		return true;
	}

	/** eine Liste aller Nachbar-Regionen. Gibt ggf. auch Chaos mit zurück, wo keine Regionen existieren. */
	public List<Region> getNachbarn() {
		List<Region> retval = new ArrayList<Region>();
		for (Coords c : getCoords().getNachbarn()) {
			retval.add(Region.Load(c));
		}
		return retval;
	}

	/*
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 *
	 *   alles zum Speichern der Reporte
	 *
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 */
	/**
	 * speichert die Region in den CR
	 * @param writer - passender ReportWriter
	 * @param partei - die Partei für diese Region
	 * @param kurz - TRUE wenn keine eigenen Einheiten hier
	 */
	public void SaveCR(CRWriter writer, Partei partei, RegionsSicht rs) {
		// if (!this.getUnits(partei).isEmpty()) new Debug(Reporte.TopSkillCache.toString(Wahrnehmung.class));

		int grenzid = 0;
		boolean omniszienz = (partei.getNummer() == 0);

		boolean kurz = !rs.hasDetails();
		boolean zuJung = !partei.canAccess(this); // wenn die Region zu jung ist, soll sie "versteckt" werden.
        if (zuJung) kurz = true;
		if (omniszienz) kurz = false; // 0 sieht alles.

		Coords my = partei.getPrivateCoords(getCoords());
        if (!zuJung) {
            writer.wl("REGION " + my.getX() + " " + my.getY() + " " + my.getWelt() + " ");
            writer.wl(getName(), "Name");
			String terrain = this.getClass().getSimpleName();
			writer.wl(terrain, "Terrain");
			writer.wl(this.getCoords().asRegionID(false), "id");
		} else {
			writer.wl("REGION " + my.getX() + " " + my.getY() + " " + my.getWelt() + " ");
			writer.wl(GameRules.TERRAIN_UNSICHTBARER_REGIONEN, "Terrain");
			writer.wl(this.getCoords().asRegionID(false), "id");
		}
//		if (this instanceof Ozean) writer.wl( ((Ozean)this).getSturmValue(), "Sturmvalue");
		if (!kurz) {
			{
				InselVerwaltung iv = InselVerwaltung.getInstance();
				InselVerwaltung.ParteiReportDaten prd = iv.getParteiReportDaten(partei);
				if ((!prd.getRegionenOhneInsel().contains(getCoords()))
						&& (!prd.getNachbarnOhneInsel().contains(getCoords()))) {
					int inselId = iv.getPrivateInselNummer(partei, getCoords());
					if (inselId != -1) {
						writer.wl(inselId, "Insel");
					}
				}
			}

			writer.wl(getBauern(), "Bauern");
			// Tiere fehlen => Resourcen-Infos ?!
			writer.wl(getResource(Holz.class).getAnzahl(), "Baeume");
			if (getBeschreibung().length() > 0) {
				writer.wl(getBeschreibung(), "Beschr");
			}
			writer.wl(getSilber(), "Silber");
			writer.wl(getSilber() / 20, "Unterh"); // <- ?? WOZU ??
			writer.wl(getBauern() / 20, "Rekruten");
			writer.wl(getLohn(), "Lohn");
			if (omniszienz) {
				writer.wl(getEnstandenIn(), "entstandenIn");
				writer.wl(getAlter(), "Regionsalter");
				writer.wl(this.InselKennung, "inselkennung");
			}
			if (rs.getQuelle() == Leuchtturm.class) {
				writer.wl("lighthouse", "visibility");
			}

			if (ZATMode.CurrentMode().isDebug()) {
				Set<Partei> parteien = new HashSet<Partei>();
				parteien.add(partei);
				if (omniszienz) {
					parteien.addAll(Partei.PROXY);
				}

				for (Partei p : parteien) {
					if (p.getInselAnker().keySet().contains(getCoords())) {
						// hier liegt ein PIA:
						ParteiInselAnker pia = ParteiInselAnker.FindOrCreateFor(p, getCoords());
						writer.wl(pia.toString(), "inselanker_" + p.getNummerBase36());
					}
				}
			}

			// ** Gebäude
			for (Building building : getBuildings()) {
				building.SaveCR(writer, partei, kurz);
			}

			// ** Schiffe
			for (Ship ship : getShips()) {
				writer.wl("SCHIFF " + ship.getNummer());
				writer.wl(ship.getTyp(), "Typ");
				writer.wl(ship.getName(), "Name");
				if (ship.getBeschreibung().length() > 0) {
					writer.wl(ship.getBeschreibung(), "Beschr");
				}
				writer.wl(ship.getGroesse(), "Groesse");
				if (ship.getKueste() != null) {
					writer.wl(ship.getKueste().ordinal(), "Kueste");
				}
				writer.wl(ship.getKapazitaet(), "capacity");
				writer.wl(ship.getKapazitaetFree(), "freieKapazitaet");
				long prozent = Math.round(((double) ship.getGroesse() / (double) ship.getConstructionSize()) * 100.0);
				if (prozent < 100) {
					writer.wl(100 - prozent, "Schaden");
				}
				if (ship.getOwner() != 0) {
					Unit u = Unit.Load(ship.getOwner());
					if (u == null) {
						new SysErr("Kapitän von Schiff " + ship + " existiert nicht: [" + Codierung.toBase36(ship.getOwner()) + "].");
					} else if (!partei.cansee(u)) {
						writer.wl(0, "Kapitaen");
					} else {
						writer.wl(ship.getOwner(), "Kapitaen");
						writer.wl(u.getTarnPartei(), "Partei");
					}
				}
			}

			// ** Preise für Luxus
			if (this.istBetretbar(null) && (this.getClass() != Sandstrom.class)) {
				// nur in betretbaren Region kann gehandelt werden
				if (getLuxus().size() > 0) {
					writer.wl("PREISE");
				}
				for (Nachfrage n : getLuxus()) {
					Paket p = Paket.FindItem(n.getItem().getSimpleName());
					writer.wl((int) (n.getNachfrage() * ((Item) p.Klasse).getPrice()), n.getItem().getSimpleName());
				}
			}

			// ** Resourcen
			for (int i = 0; i < resourcen.size(); i++) {
				Item resource = resourcen.get(i);
				if (resource.getAnzahl() == 0) {
					continue;
				}

				writer.wl("RESOURCE " + ResourcenHash(resource.getTyp()));
				writer.wl(resource.getClass().getSimpleName(), "type");
				writer.wl(1, "skill");	// TODO: Tiere erfordern tlw. mehr Skill!
				writer.wl(resource.getAnzahl(), "number");
			}

			// ** Grenzen (Straßen / Feuerwand / etc.)
			for (Richtung richtung : Richtung.values()) {
				if (getStrassensteine(richtung) > 0) {
					writer.wl("GRENZE " + grenzid++);
					writer.wl("Straße", "typ");
					writer.wl(richtung.ordinal(), "richtung");
					long prozent = (long) Math.floor(((double) getStrassensteine(richtung) / (double) getSteineFuerStrasse()) * 100.0);
					writer.wl(prozent, "prozent");
				}
			}

			// ** Effekte (Nebel / Pest / etc.)
			// ** Durchschiffungen
			// ** Durchreisen

			// ** Einheiten
			for (Unit u : getUnits()) {
				u.SaveCR(writer, partei);
			}
		} else {
			if (ZATMode.CurrentMode().isDebug()) {
				if (partei.getInselAnker().keySet().contains(getCoords())) {
					// hier liegt ein PIA:
					ParteiInselAnker pia = ParteiInselAnker.FindOrCreateFor(partei, getCoords());
					writer.wl(pia.toString(), "inselanker");
				}
			}

			// kurz:
			if (rs.getQuelle() == Leuchtturm.class) {
				writer.wl("lighthouse", "visibility");
			} else {
				// bekannt machen, dass in der Nachbarregion eine Einheit existiert:
				writer.wl("neighbour", "visibility");
			}
		}
	}

	private int ResourcenHash(String resource) {
		int hash = 0;
		for (int i = 0; i < resource.length(); i++) {
			hash += resource.charAt(i);
		}
		return hash;
	}

	public void SaveXML(XMLWriter xml, Partei partei, boolean kurz) {
		boolean hidden = !partei.canAccess(this); // wenn die Region zu jung ist, soll sie "versteckt" werden.
		if (hidden) {
			kurz = true;
		}

		boolean header = false;

		if (!hidden) {
			xml.ElementStart("region");
			xml.ElementAttribute("ref", this.getClass().getSimpleName());
			xml.ElementAttribute("x", (getCoords().getX() - partei.getUrsprung().getX()));
			xml.ElementAttribute("y", (getCoords().getY() - partei.getUrsprung().getY()));
			xml.ElementAttribute("welt", getCoords().getWelt());
			xml.ElementShort("name", getName());
		} else {
			xml.ElementStart("region");
			xml.ElementAttribute("ref", GameRules.TERRAIN_UNSICHTBARER_REGIONEN);
			xml.ElementAttribute("x", (getCoords().getX() - partei.getUrsprung().getX()));
			xml.ElementAttribute("y", (getCoords().getY() - partei.getUrsprung().getY()));
			xml.ElementAttribute("welt", getCoords().getWelt());
		}

		if (kurz) {
			xml.ElementEnd();
			return;
		}

		if (getBeschreibung().length() > 0) {
			xml.ElementShort("description", getBeschreibung());
		}

		xml.ElementShort("pesants", getBauern());
		xml.ElementShort("silver", getSilber());
		xml.ElementShort("entertain", getSilber() / 20);
		xml.ElementShort("recruit", getBauern() / 20);
		xml.ElementShort("wage", getLohn());

		// Gebäude
		if (getBuildings().size() > 0) {
			xml.ElementStart("buildings");
		}
		for (Building building : getBuildings()) {
			building.SaveXML(xml, partei, kurz);
		}
		if (getBuildings().size() > 0) {
			xml.ElementEnd();
		}

		// Schiffe
		if (getShips().size() > 0) {
			xml.ElementStart("ships");
		}
		for (Ship ship : getShips()) {
			ship.SaveXML(xml, partei, kurz);
		}
		if (getShips().size() > 0) {
			xml.ElementEnd();
		}

		// Handelsgüter
		if (getLuxus().size() > 0) {
			xml.ElementStart("merchandise");
		}
		for (Nachfrage nachfrage : getLuxus()) {
			Paket p = Paket.FindItem(nachfrage.getItem().getSimpleName());
			xml.ElementStart("price");
			xml.ElementAttribute("ref", ((Item) p.Klasse).getClass().getSimpleName());
			xml.ElementData((int) (nachfrage.getNachfrage() * ((Item) p.Klasse).getPrice()));
			xml.ElementEnd();
		}
		if (getLuxus().size() > 0) {
			xml.ElementEnd();
		}

		// Resourcen
		xml.ElementStart("resources");
		for (Item resource : resourcen) {
			if (resource.getAnzahl() == 0) {
				continue;
			}
			xml.ElementStart("resource");
			xml.ElementAttribute("ref", resource.getClass().getSimpleName());
			xml.ElementData(resource.getAnzahl());
			xml.ElementEnd();
		}
		xml.ElementEnd();

		// Strassen
		for (Richtung richtung : Richtung.values()) {
			if (getStrassensteine(richtung) > 0) {
				if (!header) {
					xml.ElementStart("streets");
					header = true;
				}
				xml.ElementStart("street");
				xml.ElementAttribute("direction", richtung.name());
				xml.ElementData(getStrassensteine(richtung));
				xml.ElementEnd();
			}
		}
		if (header) {
			xml.ElementEnd();
		}

		// Einheiten
		if (getUnits().size() > 0) {
			xml.ElementStart("units");
		}
		for (Unit unit : getUnits()) {
			if (unit.getSchiff() == 0 && unit.getGebaeude() == 0) {
				unit.SaveXML(xml, partei, kurz);
			}
		}
		if (getUnits().size() > 0) {
			xml.ElementEnd();
		}

		xml.ElementEnd();
	}

	/**
	 * liefert alle Regionen die zu dieser Insel (bzw. Block) gehören
	 * @param insel
	 * @param withOutOcean - TRUE wenn Ozean ignoriert werden sollen
	 * @return
	 */
	public static List<Region> getInselRegionen(int insel, boolean withOutOcean) {
		List<Region> regionen = new ArrayList<Region>();
		for (Region r : CACHE.values()) {
			if (r.getInselKennung() == insel) {
				if (withOutOcean) {
					if (r.getClass() != Ozean.class) {
						regionen.add(r);
					}
				} else {
					regionen.add(r);
				}
			}
		}
		if (regionen.isEmpty()) {
			new SysMsg("keine Regionen zu Insel #" + insel + " gefunden");
		}
		return regionen;
	}

	/**
	 * liefert einen speziellen Typ an Regionen einer Insel
	 * @param insel - diese Insel-KENNUNG (!)
	 * @param clazz - dieser Regionstyp
	 * @return alle gefunden Regionen der Insel
	 */
	public static List<Region> getRegions4Insel(int insel, Class<? extends Region> clazz) {
		List<Region> regionen = new ArrayList<Region>();
		for (Region r : CACHE.values()) {
			if (r.getInselKennung() == insel) {
				if (r.getClass().equals(clazz)) {
					regionen.add(r);
				}
			}
		}
		return regionen;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Regionen sollen nicht geklont werden.");
	}

	/**
	 * Erzeugt eine Kopie dieser Region mit einen anderen Terrain-Typ. Das ist
	 * überall dort nötig, wo zur Laufzeit der Typ und damit die Klasse einer
	 * Region verändert werden soll.
	 * @param typ Terrain-Typ der gewünschten Kopie
	 * @return Eine Kopie dieser Region mit dem Terrain-Typ typ
	 */
	public Region cloneAs(Class<? extends Region> typ) {
		Region kopie = null;

		try {
			kopie = typ.newInstance();
		} catch (InstantiationException ex) {
			new BigError(ex);
		} catch (IllegalAccessException ex) {
			new BigError(ex);
		}

		// NICHT MEHR? (mit dem späten Setzen der Coords tricksen wir den oben genannten Mechanismus aus.)
		kopie.setCoords(this.getCoords());

		kopie.setName(this.getName());
		kopie.setBeschreibung(this.getBeschreibung());
		kopie.setAlter(this.getAlter());
		kopie.setEnstandenIn(this.getEnstandenIn());
		kopie.setInselKennung(this.getInselKennung());
		kopie.setBauern(this.getBauern());
		kopie.setSilber(this.getSilber());
		for (Item it : this.getResourcen()) {
			kopie.setResource(it.getClass(), it.getAnzahl());
		}

		// die "Inhalte":
		for (Nachfrage n : this.getLuxus()) {
			kopie.setNachfrage(n.getItem(), n.getNachfrage());
		}
		for (String key : getProperties()) {
			// we exploit here the fact that every property can be represented by a string
			kopie.setProperty(key, this.getStringProperty(key));
		}
		// brauchen wir mit AtlantisCache nicht mehr.
//		for (Building b : this.getBuildings()) {
//			kopie.getBuildings().add(b);
//		}
		for (Ship s : this.getShips()) {
			kopie.getShips().add(s);
		}
//		for (Unit u : this.getUnits()) {
//			kopie.getUnits().add(u);
//		}
		for (Richtung r : Richtung.values()) {
			kopie.setStrassensteine(r, this.getStrassensteine(r));
		}


		return kopie;
	}

	@Override
	public int hashCode() {
		return this.getCoords().hashCode() - 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Region other = (Region) obj;

		return this.getCoords().equals(other.getCoords());
	}

	public boolean containsRace(Class<? extends Unit> clazz) {
		for (Unit u : getUnits()) {
			if (u.getClass().equals(clazz)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * liefert den Eigentümer dieser Region, das ist natürlich das Volk was
	 * die größe Burg in der Region besitzt
	 * @return Nummer des Volkes oder 0 wenn kein Volk hier das Sagen hat
	 */
	@Override
	public int getOwner() {
		Building biggest = null;
		int owner = 0;
		for(Building b : getBuildings()) {
			if (b instanceof Burg) {
				if (biggest == null) {
					biggest = b;
				} else {
					if (b.getSize() > biggest.getSize()) biggest = b;
				}
			}
		}

		if (biggest == null) {
			return 0;
		}

		Unit u = Unit.Get(biggest.getOwner());
		if (u == null) {
			return 0;	// Gebäude ist ohne Befehlshaber
		}
		
		return u.getOwner();
	}
}