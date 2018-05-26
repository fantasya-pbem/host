package de.x8bit.Fantasya.Atlantis;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Buildings.Leuchtturm;
import de.x8bit.Fantasya.Atlantis.Helper.RegionsSicht;
import de.x8bit.Fantasya.Atlantis.Messages.BigError;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Host.EVA.util.Atlas;
import de.x8bit.Fantasya.Host.EVA.util.ParteiInselAnker;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Reports.Writer.XMLWriter;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.FreieNummern;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.UnitList;

/**
 * @author  mogel
 */
public class Partei extends Atlantis {

	static {
		PROXY = new ArrayList<Partei>();
	}
	public static final ArrayList<Partei> PROXY;
	private String Rasse = "Mensch";
	private String EMail = "";
	private String Website = "";
	private String Password = "empty";
	private int NMR = 0;
	private Coords Ursprung = new Coords(0, 0, 0);
	private int Alter = 0;
	private int Defaultsteuer = 0;
	private int Monster = 0;
	private Map<Integer, Allianz> allianzen = new HashMap<Integer, Allianz>();
	private ArrayList<Steuer> steuern = new ArrayList<Steuer>();
	private int cheats = 0;
	
	private int userId = 0;
	private int ownerId = 0;

	/** Muss noch mit Leben gefuellt werden... */
	public Partei() {
	}

	/** Erzeugt eine Partei mit verschiedenen Setupwerten. */
	public Partei(int Alter) {
		this.Alter = Alter;
	}

	public void setCheats(int value) {
		cheats = value;
	}

	public int getCheats() {
		return cheats;
	}
	/**
	 * speichert(Koordinaten der) Regionen, die in dieser Runde 
	 * für die Partei sichtbar sind.
	 * Verwendung für die Reporte (CR, XML ?)
	 */
	final protected Map<Coords, RegionsSicht> knownRegions = new HashMap<Coords, RegionsSicht>();
	/**
	 * speichert(Koordinaten der) Regionen, die DER VERGANGENHEIT 
	 * für die Partei sichtbar waren.
	 * Verwendung für die Reporte (CR, XML ?)
	 */
	final public Atlas atlas = new Atlas();
	/**
	 * in einem Partei-Property dieses Namens wird der Atlas gespeichert
	 */
	public final static String PROPERTY_ATLAS = "atlas";
	/**
	 * speichert das Wissen / die Namen und IDs der Inseln, die dieser Partei bekannt sind.
	 */
	final protected Map<Coords, ParteiInselAnker> inselAnker = new HashMap<Coords, ParteiInselAnker>();

	public Map<Coords, ParteiInselAnker> getInselAnker() {
		return inselAnker;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Partei)) {
			return false;
		}

		final Partei other = (Partei) obj;
		if (this.getNummer() != other.getNummer()) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + this.getNummer();
		return hash;
	}

	public String getEMail() {
		return EMail;
	}

	public void setEMail(String mail) {
		EMail = mail;
	}

	public String getRasse() {
		return Rasse;
	}

	public void setRasse(String rasse) {
		Rasse = rasse;
	}

	public String getWebsite() {
		return Website;
	}

	public void setWebsite(String website) {
		Website = website;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

	public int getNMR() {
		return NMR;
	}

	public void setNMR(int nmr) {
		NMR = nmr;
	}

	public void setUrsprung(Coords value) {
		Ursprung = new Coords(value);
	}

	public Coords getUrsprung() {
		return Ursprung;
	}

	public int getAlter() {
		return Alter;
	}

	public int getDefaultsteuer() {
		return Defaultsteuer;
	}

	public void setDefaultsteuer(int rate) {
		Defaultsteuer = rate;
	}

	public boolean isMonster() {
		return Monster != 0;
	}

	/**
	 * liefert den Status der Partei f�r das Monsterdasein
	 * 1 -> kein weitere World XML-Report
	 * 2 -> nur der GEO XML-Report
	 * 3 -> der komplette World XML-Report
	 * @return
	 * @see isMonster()
	 */
	public int getMonster() {
		return Monster;
	}

	/**
	 * liefert den Status der Partei f�r das Monsterdasein
	 * 1 -> kein weitere World XML-Report
	 * 2 -> nur der GEO XML-Report
	 * 3 -> der komplette World XML-Report
	 * @return
	 * @see isMonster()
	 */
	public void setMonster(int value) {
		this.Monster = value;
	}

	/**
	 * liefert die komplette Allianz-Stati
	 * @param partner
	 * @return das entsprechende Allianz-Objekt - garantiert nicht null!
	 */
	public Allianz getAllianz(int partner) {
		Allianz a = allianzen.get(partner);

		// noch nicht vorhanden, dann neu
		if (a == null) {
			a = new Allianz();
			a.setPartei(this.getNummer());
			a.setPartner(partner);
			allianzen.put(partner, a);
		}

		return a;
	}

	/**
	 * testet ob diese Partei eine Allianz für eine Option festgelegt hat
	 * @param partner - Parteinummer des Partner
	 * @param ao - Alloanzoption (Allianz.AllianzOption)
	 * @return true wenn die Allianz für die Option steht
	 */
	public boolean hatAllianz(int partner, AllianzOption ao) {
		Allianz a = getAllianz(partner);
		return a.getOption(ao);
	}

	/**
	 * teste ob überhaupt eine Allianz besteht
	 * @param partner - Allianzpartner
	 * @return TRUE wenn irgend eine Allianz besteht (welche auch immer)
	 */
	public boolean hatAllianz(int partner) {
		boolean yes = false;
		for (AllianzOption ao : AllianzOption.values()) {
			if (!yes) {
				yes = hatAllianz(partner, ao);
			}
		}
		return yes;
	}

	/**
	 * setzt eine AllianzOption
	 * @param partner - Gegenseite
	 * @param ao - AllianzOption
	 * @param yes - TRUE wenn gesetzt (oder FALSE löscht)
	 */
	public void setAllianz(int partner, AllianzOption ao, boolean yes) {
		Allianz a = getAllianz(partner);
		a.setOption(ao, yes);
	}

	public Map<Integer, Allianz> getAllianzen() {
		return allianzen;
	}

	/**
	 * Die 'anonyme' Entsprechung zu Unit.cansee(Unit)
	 * @param other
	 * @return true, wenn irgendwer von dieser Partei die Einheit other sehen kann.
	 */
	public boolean cansee(Unit other) {
		if (other == null) {
			throw new RuntimeException("Partei.cansee() mit null als Einheit aufgerufen.");
		}

		// Shortcut:
		if (other.getOwner() == this.getNummer()) {
			return true; // äh ... ja.
		}
		if (this.getNummer() == 0) {
			return true; // omniszient!
		}

		Region r = Region.Load(other.getCoords());
		// ist überhaupt jemand von uns da?
		if (r.getUnits(this.getNummer()).isEmpty()) {
			RegionsSicht rs = this.getRegionsSicht(other.getCoords());
			if ((rs != null) && (rs.hasDetails())) {
				if (rs.getQuelle() != Leuchtturm.class) {
					new Debug(
							"cansee(" + other.getNummerBase36() + "): Es ist niemand von " + this + " in " + r
							+ ", aber es liegt eine detaillierte RegionsSicht vor (" + rs.getQuelle().getSimpleName() + ").");
				}
			} else {
				new Debug("cansee(" + other.getNummerBase36() + "): Es ist niemand von " + this + " in " + r + ".");
				return false;
			}
		}

		int tarnung = other.Talentwert(Tarnung.class);
		if (tarnung > 0) {
			if (other.getSichtbarkeit() == 0) {
				return true; // Wenn TARNE EINHEIT NICHT befohlen wurde, ist die Einheit ganz normal sichtbar
			}
			if (other.getGebaeude() != 0) {
				return true;
			}

			// Im Schiff kann man sich an Land nicht verstecken, auf See dagegen schon!
			if ((other.getSchiff() != 0) && r.istBetretbar(null)) {
				return true;
			}

			// Allianzen prüfen
			if (Partei.getPartei(other.getOwner()).hatAllianz(this.getNummer(), AllianzOption.Kontaktiere)) {
				return true;
			}

			// okay, jetzt gegen deren Willen:
			int top = r.topTW(Wahrnehmung.class, this.getNummer());

			new Debug("Top-Wahrnehmung in " + r + " für " + this + ": " + top + " vs. Tarnung " + tarnung + " von " + other + ".");


			if (top >= tarnung) {
				return true;
			}

			// nö, der hat sich gut versteckt.
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @param r
	 * @return true, wenn diese Partei die Region (unabhängig von eventuell anwesenden Einheiten!) theoretisch sehen und betreten kann.
	 */
	public boolean canAccess(Region r) {
		if (this.getNummer() == 0) {
			return true; // 0 kann alles sehen.
		}
		
		// alte Regionen sollten das "Herstellungsdatum" -1 haben. Daher werden diese
		// immer erkannt. Zusätzlich wird am Anfang von Spielen die Welt für alle "sichtbar"
		// gemacht, dadurch haben Spieler die in einer neuen Welt ausgesetzt werden die Chance
		// noch mit den etwas älteren (paar Runden) Spielern zu kooperieren. Anschließend
		// wird die Grenze langsam angehoben
		if (r.getEnstandenIn() < 0) return true; 

		// das ist von der Benennung her etwas unglücklich? De facto ist Alter
		// derzeit (2011-06-05) aber die Entstehungsrunde.
		int parteiGeburtsRunde = /* GameRules.getRunde() - */ this.getAlter();
		if (parteiGeburtsRunde < r.getEnstandenIn()) {
			// wir sind vor der Region entstanden!
			int regionExistiertSeit = GameRules.getRunde() - r.getEnstandenIn();
			
			// Newbie-Schutz am Anfang von Spielen etwas reduzieren - macht sonst keinen Sinn
			int sichtbar = GameRules.GetRegionenSichtbarNachRunden();
			if (sichtbar > GameRules.getRunde()) sichtbar = GameRules.getRunde();

			if (regionExistiertSeit < sichtbar) {
				return false; // zu neu für uns...
			}
		}
		return true;
	}

	public Steuer getSteuern(int faction) {
		Steuer steuer = null;
		for (Steuer s : steuern) {
			if (s.getFaction() == faction) {
				steuer = s;
				break;
			}
		}
		if (steuer == null) {
			steuer = new Steuer(this.getNummer(), this.getDefaultsteuer(), faction);
			steuern.add(steuer);
		}
		return steuer;
	}

	public void setSteuern(int faction, int rate) {
		Steuer steuer = getSteuern(faction);
		steuer.setRate(rate);
	}

	/**
	 * @return
	 * @uml.property  name="steuern"
	 */
	public List<Steuer> getSteuern() {
		return steuern;
	}

	/**
	 * @return Eine Liste mit ALLEN Einheiten dieser Partei - das ist nicht sehr performant und sollte sparsam verwendet werden.
	 */
	public Set<Unit> getEinheiten() {
		return Unit.CACHE.getAll(getNummer());
	}

	/**
	 * @return Die Anzahl aller Personen dieser Partei.
	 */
	public int getPersonen() {
		int retval = 0;
		for (Unit u : getEinheiten()) {
			if (u.getCoords().getWelt() != 0) {
				retval += u.getPersonen();
			}
		}
		return retval;
	}

	/**
	 * @return Alle Magier in dieser Partei (Einheiten mit mehr als 0 Lerntagen in Magie).
	 */
	public UnitList getMagier() {
		UnitList retval = new UnitList();
		for (Unit u : getEinheiten()) {
			if (u.getSkill(Magie.class).getLerntage() > 0) {
				retval.add(u);
			}
		}
		return retval;
	}

	/**
	 * @return Die Anzahl maximal möglicher Magier (Personen - unabhängig von den derzeit vorhandenen).
	 */
	public int getMaxMagier() {
		Unit einheimischer = null;
		for (Unit maybe : getEinheiten()) {
			if (maybe.getRasse().equalsIgnoreCase(getRasse())) {
				einheimischer = maybe;
				break;
			}
		}

		int moeglich = 0;
		if (einheimischer == null) {
			if (getPersonen() > 0) {
				new SysErr("Partei " + this + " hat keinen einzigen Einheimischen?");
			}
		} else {
			moeglich = einheimischer.maxMagier();
		}
		if (moeglich < 0) {
			moeglich = 0;
		}

		return moeglich;
	}

	/**
	 * @return Alle Migranten (Einheiten mit einer anderen Rasse) in dieser Partei.
	 */
	public UnitList getMigranten() {
		UnitList retval = new UnitList();
		for (Unit u : getEinheiten()) {
			if (!u.getRasse().equals(getRasse())) {
				retval.add(u);
			}
		}
		return retval;
	}

	/**
	 * @return Die Anzahl maximal möglicher Migranten (unabhängig von den derzeit vorhandenen).
	 */
	public int getMaxMigranten() {
		Unit einheimischer = null;
		for (Unit maybe : getEinheiten()) {
			if (maybe.getRasse().equalsIgnoreCase(getRasse())) {
				einheimischer = maybe;
				break;
			}
		}

		int moeglich = 0;
		if (einheimischer == null) {
			if (getPersonen() > 0) {
				new SysErr("Partei " + this + " hat keinen einzigen Einheimischen?");
			}
		} else {
			// Ein Objekt der jeweiligen Rasse kann entscheiden, wie viele Migranten bei einer
			// bestimmten Anzahl Volks-Angehöriger möglich sind:
			moeglich = einheimischer.maxMigranten(getPersonen());
		}
		if (moeglich < 0) {
			moeglich = 0;
		}

		return moeglich;
	}

	/**
	 * erzeugt einen neuen Spieler
	 */
	public static Partei Create() {
		Partei p = new Partei();

		// das Alter der Volkes setzen
		p.Alter = GameRules.getRunde();

		// ein sicheres Passwort erzeugen ... wird am
		// Ende aber wieder vom Management überschrieben
		p.GeneratePasswort();

		// Nummer vergeben
		p.setNummer(FreieNummern.freieNummer(PROXY));
		p.setEMail("fantasya@x8bit.de");
		p.setRasse("Mensch");
		p.setPassword(p.Password);

		return p;
	}

	/**
	 * erzeugt ein neues Passwort
	 */
	private void GeneratePasswort() {
		Password = "";
		String chars = "abcdefghijkmnopqrstuvwxyz23456789"; //ABCDEFGHIJKLMNPQRSTUVWXYZ";
		for (int i = 0; i < 8; i++) {
			Password += chars.charAt(Random.rnd(0, chars.length() - 1));
		}
	}

	/**
	 * läd einen Spieler aus der Datenbank
	 * @param nummer - Nummer des Spielers
	 */
	public static Partei getPartei(int nummer) {
		for (int i = 0; i < PROXY.size(); i++) {
			if (PROXY.get(i) != null && PROXY.get(i).getNummer() == nummer) {
				return PROXY.get(i);
			}
		}

		return null;
	}

	@Deprecated
	public static Partei fromResultSet(ResultSet rs) {
		Partei p = null;

		try {
			p = new Partei();
			p.Alter = rs.getInt("age");
			p.setName(rs.getString("name"));
			p.setBeschreibung(rs.getString("beschreibung"));
			p.setNummer(rs.getInt("nummer"));
			p.EMail = rs.getString("email");
			p.Rasse = rs.getString("rasse");
			// p.Sprache = rs.getString("sprache");
			p.Password = rs.getString("password");
			p.Website = rs.getString("website");
			p.NMR = rs.getInt("nmr");
			// TODO Eigentlich müsste der Parteien-Ursprung auch die Welt bezeichnen - oder?
			p.setUrsprung(new Coords(rs.getInt("originx"), rs.getInt("originy"), 1));
			p.setCheats(rs.getInt("cheats"));
			p.Monster = rs.getInt("monster");
			p.setRasse(rs.getString("rasse"));
			p.setDefaultsteuer(rs.getInt("steuern"));
		} catch (Exception ex) {
			new BigError(ex);
		}

		return p;
	}

	/**
	 * @return Eine Map Feldname =&gt; Wert, die den Feldern der entsprechenden Datenbank-Tabelle entspricht
	 */
	public Map<String, Object> getDBValues() {
		Map<String, Object> fields = new HashMap<String, Object>();

		fields.put("nummer", this.getNummer());
		fields.put("id", this.getNummerBase36());
		fields.put("name", getName());
		fields.put("beschreibung", getBeschreibung());
		fields.put("email", getEMail());
		fields.put("password", getPassword());
		fields.put("website", getWebsite());
		fields.put("nmr", getNMR());
		fields.put("originx", Ursprung.getX());
		fields.put("originy", Ursprung.getY());
		fields.put("age", getAlter());
		fields.put("cheats", getCheats());
		fields.put("rasse", getRasse());
		fields.put("steuern", getDefaultsteuer());
		fields.put("monster", this.getMonster());

		return fields;
	}

	/**
	 * Speichert die partei in den XML Report
	 * @param owner - der Eigentümer des Reportes
	 */
	public void SaveXML(XMLWriter xml, Partei owner) {
		// 03.10.2011 -- Monster sind sichtbar -- if (this.isMonster()) return;	// Monster bleiben unsichtbar

		xml.ElementStart("faction");
		xml.ElementAttribute("id", this.getNummerBase36());
		xml.ElementShort("name", getName());
		xml.ElementShort("email", getEMail());
		if (this.getBeschreibung().length() > 0) {
			xml.ElementShort("description", this.getBeschreibung());
		}
		if (this.getWebsite() != null) {
			if (this.getWebsite().length() > 0) {
				xml.ElementShort("website", this.getWebsite());
			}
		}
		if (owner.getNummer() == this.getNummer()) {
			xml.ElementStart("race");
			xml.ElementAttribute("ref", getRasse());
			xml.ElementEnd();
			xml.ElementShort("age", this.getAlter());
			xml.ElementShort("defaulttax", this.getDefaultsteuer());
			xml.ElementShort("nmr", GameRules.getRunde() - this.getNMR());
			for (Steuer s : getSteuern()) {
				xml.ElementStart("tax");
				xml.ElementAttribute("faction", "" + Codierung.toBase36(s.getFaction()) + "");
				xml.ElementData("" + s.getRate() + "");
				xml.ElementEnd();
			}
		}
		if (owner.hatAllianz(this.getNummer())) {
			xml.ElementStart("alliance");
			for (AllianzOption ao : AllianzOption.values()) {
				if (ao != AllianzOption.Alles) {
					if (owner.hatAllianz(this.getNummer(), ao)) {
						xml.ElementShort("option", ao.name());
					}
				}
			}
			xml.ElementEnd();
		}
		xml.ElementEnd();
	}

	public void Delete() {
		new ZATMsg("Partei " + this + " wird gelöscht.");

		// Allianzen zu anderen Völkern
		for (int partnerNr : allianzen.keySet()) {
			Allianz allianz = allianzen.get(partnerNr);
			new Debug("Partei.Delete(): " + allianz.toString());
			Partei partner = Partei.getPartei(partnerNr);
			if (partner != null) {
				partner.setAllianz(this.getNummer(), AllianzOption.Alles, false);
			} else {
				new SysErr("In Partei.Delete() für " + this + " existiert das Partnervolk " + Codierung.toBase36(partnerNr) + " nicht (mehr).");
			}
		}
		allianzen.clear();

		// Steuern
		// --- fällt automatisch weg



		// Parteitarnungen
		for (Unit unit : Unit.CACHE) {
			if (unit.getTarnPartei() == getNummer()) {
				unit.setTarnPartei(0);
			}
		}


		// Inselnamen und -beschreibungen:
		ParteiInselAnker.Initialisieren(this);
		for (Coords c : this.getInselAnker().keySet()) {
			ParteiInselAnker.Entfernen(this, c);
		}
		ParteiInselAnker.Initialisieren(this);
		if (!this.getInselAnker().isEmpty()) {
			throw new IllegalStateException("Konnte nicht alle Insel-Anker von " + this + " entfernen (Partei.Delete()).");
		}

		// Partei-Properties:
		properties.clear();
	}

	public void addKnownRegion(Region r, boolean details, Class<? extends Atlantis> quelle) {
		addKnownRegion(r.getCoords(), details, quelle);
	}

	/**
	 * @param c globale Koordinaten
	 * @param details
	 */
	public void addKnownRegion(Coords c, boolean details, Class<? extends Atlantis> quelle) {
		RegionsSicht rs = new RegionsSicht(GameRules.getRunde(), c, details, quelle);

		// wenn diese Region schon bekannt ist, wird sie nur aufgenommen, wenn sie detailliert ist.
		if (this.knownRegions.containsKey(c)) {
			if (details) {
				if (quelle != Leuchtturm.class) {
					this.knownRegions.put(c, rs);
				} else {
					// Leuchtturm-Wissen ist nachrangig; nur nehmen, wenn sonst keine Details bekannt:
					if (!this.knownRegions.get(c).hasDetails()) {
						this.knownRegions.put(c, rs);
					}
				}
			}
		} else {
			this.knownRegions.put(c, rs);
		}
	}

	/**
	 * eine Regionssicht vollständig entfernen
	 * @param c
	 */
	public void removeKnownRegion(Coords c) {
		this.knownRegions.remove(c);
	}

	/**
	 * @param mitUnsichtbaren - wenn false, werden Regionen ausgeblendet, für die gilt: !this.cansee(region)
	 * @return Set aller dieser Partei derzeit bekannten Regionen
	 */
	public Set<RegionsSicht> getKnownRegions(boolean mitUnsichtbaren) {
		Set<RegionsSicht> retval = new HashSet<RegionsSicht>();
		for (Coords c : knownRegions.keySet()) {
			if (!mitUnsichtbaren) {
				Region r = Region.Load(knownRegions.get(c).getCoords());
				if (!this.canAccess(r)) {
					continue;
				}
			}
			retval.add(knownRegions.get(c));
		}
		return Collections.unmodifiableSet(retval);
	}

	/**
	 * @param c
	 * @return Das RegionsSicht-Objekt dieser Partei an Coords c - oder null, wenn keinerlei Sicht
	 */
	public RegionsSicht getRegionsSicht(Coords c) {
		if (!knownRegions.containsKey(c)) {
			return null;
		}
		return knownRegions.get(c);
	}

	public Set<Partei> getBekannteParteien() {
		Set<Partei> retval = new HashSet<Partei>();

		if (this.getNummer() == 0) { // omniszient:
			retval.addAll(PROXY);
			return retval;
		}

		for (RegionsSicht rs : this.getKnownRegions(false)) { // false = ohne "versteckte" Regionen
			if (!rs.hasDetails()) {
				continue;
			}
			Region r = Region.Load(rs.getCoords());
			if (r == null) {
				new SysErr("Nicht-existenten Region in knownRegions @ " + rs.getCoords() + "?");
				continue;
			}
			for (Unit u : r.getUnits()) {
				// -- 03.10.2011 -- Partei p = Partei.Load(u.getOwner());
				Partei p = Partei.getPartei(u.getTarnPartei());	// es darf nur die Parteitarnung verwendet werden
				// sonst fällt die Parteitarnung
				// -- 03.10.2011 -- auch Monster-Parteien sind wichtig -- if (p.isMonster()) continue;
				// -- 03.10.2011 -- eigene Partei ist auch wichtig -- if (u.getOwner() == this.getNummer()) continue;
				retval.add(p);
			}
		}

		return retval;
	}

	@Override
	public Coords getCoords() {
		throw new UnsupportedOperationException("Partei.getCoords() sollte niemals ausgewertet werden.");
	}

	@Override
	public void setCoords(Coords value) {
		throw new UnsupportedOperationException("Partei.setCoords() sollte niemals verwendet werden.");
	}

	public Coords getPrivateCoords(Coords global) {
		return new Coords(
				global.getX() - this.getUrsprung().getX(),
				global.getY() - this.getUrsprung().getY(),
				global.getWelt());

	}

	public Coords getGlobalCoords(Coords privat) {
		return new Coords(
				privat.getX() + this.getUrsprung().getX(),
				privat.getY() + this.getUrsprung().getY(),
				privat.getWelt());

	}
	
	public int getUserId() {
		return userId;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public int getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}
}
