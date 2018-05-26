package de.x8bit.Fantasya.Atlantis.Units;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Monster;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Alpaka;
import de.x8bit.Fantasya.Atlantis.Items.Kamel;
import de.x8bit.Fantasya.Atlantis.Items.Resource;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.AnimalResource;
import de.x8bit.Fantasya.Atlantis.Items.MagicItem;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.SysErr;
import de.x8bit.Fantasya.Atlantis.Messages.SysMsg;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WKnueppel;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StringUtils;
import de.x8bit.Fantasya.util.lang.NonsenseTexter;
import java.util.Collections;

public class Kobold extends Monster {
    private final static String STATUS_STEHLEN_ERFOLGREICH = "geklaut!";
    private final static String STATUS_STEHLEN_ERFOLGLOS = "nix";
    private final static String STATUS_ERWISCHT = "erwischt!";
    private final static String STATUS_REKRUTIEREN_BEWACHT = "rekrutieren:bewacht";
    
	public Kobold()	{
		RekrutierungsKosten = 150;

		minHunger = 1;
		maxHunger = 2;
	}
    
    protected String statusStehlen = null;
    protected int statusStehlenEinheit = -1;
    protected String statusRekrutieren = null;

	@Override
	public boolean Rekrutieren(int anzahl)
	{
		boolean retval = super.Rekrutieren(anzahl);
		
		getSkill(Hiebwaffen.class).setLerntage(getSkill(Hiebwaffen.class).getLerntage() + Skill.LerntageFuerTW(8) * anzahl);	// T8
		getSkill(Tarnung.class).setLerntage(getSkill(Tarnung.class).getLerntage() + Skill.LerntageFuerTW(8) * anzahl);	// T8

		return retval;
	}

	@Override
	public int Trefferpunkte() { return 10; }
	

	/**
	 * alle Monster können per Default nicht hungern -> überschreiben
	 */
    @Override
	public boolean actionUnterhalt() {
		return super.actionUnterhaltNormal();
	}
    
    // -------------------------------------------------------------------------------------
	
	@Override
	public void planMonster() {
		// falls wir diesen Monat neu sind, haben wir unseren Plan ja schon:
		for (String b : Befehle) {
            if (b.equals(";NEU")) {
                if (this.getName().startsWith("Einheit")) this.Befehle.add("BENENNE EINHEIT " + KoboldName());
                return;
            }
            if (b.startsWith("// STATUS")) {
                b = b.replace("// STATUS ", "");
                String[] parts = b.split("\\s");
                if (parts.length == 1) {
                    if (parts[0].equals(STATUS_REKRUTIEREN_BEWACHT)) setStatusRekrutieren(STATUS_REKRUTIEREN_BEWACHT);
                } else if (parts.length == 2) {
                    if (parts[0].equals(STATUS_STEHLEN_ERFOLGREICH)) setStatusStehlen(STATUS_STEHLEN_ERFOLGREICH, Codierung.fromBase36(parts[1]));
                    if (parts[0].equals(STATUS_STEHLEN_ERFOLGLOS)) setStatusStehlen(STATUS_STEHLEN_ERFOLGLOS, Codierung.fromBase36(parts[1]));
                    if (parts[0].equals(STATUS_ERWISCHT)) setStatusStehlen(STATUS_ERWISCHT, Codierung.fromBase36(parts[1]));
                }
            }
		}
		
		Befehle.clear();
		if (this.getName().startsWith("Einheit")) this.Befehle.add("BENENNE EINHEIT " + KoboldName());
		
		if (this.getSichtbarkeit() == 0) Befehle.add("TARNE EINHEIT");
		if (this.getTarnPartei() != 0) Befehle.add("TARNE PARTEI");
		//TODO Hungernde Kobolde greifen an (?)
		if (this.getKampfposition() != Kampfposition.Nicht) Befehle.add("KÄMPFE NICHT");
		
		Region region = Region.Load(getCoords());
        
		// ist etwas nennenswertes vorgefallen?
        if (getStatusStehlen() != null) {
            if (getStatusStehlen().equals(STATUS_ERWISCHT)) {
                // wenn die andere Einheit noch da ist...
                Unit polizist = Unit.Load(getStatusStehlenEinheit());
                if (polizist != null) {
                    if (polizist.getCoords().equals(this.getCoords())) {
                        // hier sind wir nicht sicher!
                        
                        // Kollegen in der Stadt warnen:
                        for (Unit u : region.getUnits(this.getOwner())) {
                            if (u.getNummer() == this.getNummer()) continue;
                            if (u.getOwner() == this.getOwner()) {
                                u.Befehle.add("// STATUS " + STATUS_ERWISCHT + " " + polizist.getNummerBase36());
                            }
                        }
                        
                        if (gibAuswanderBefehle(false)) { // Reise nicht erzwingen
                            Befehle.add("// " + polizist + " hat uns erwischt, und wir hauen jetzt ab.");
                            return; // nichts außer diesem hier machen
                        } else {
                            Befehle.add("// " + polizist + " hat uns erwischt, und wir können nicht einfach fliehen...");
                            new SysMsg("Koboldpanik bei " + this + " in " + region + " (sind erwischt worden und können nicht fliehen)!");

                            if (panik()) return; // sonst: Good luck!
                        }
                    } // endif polizist ist noch in der Region
                } // endif polizist != null
            }
            
            if (getStatusStehlen().equals(STATUS_STEHLEN_ERFOLGREICH)) {
                // wenn die andere Einheit noch da ist...
                Unit opfer = Unit.Load(getStatusStehlenEinheit());
                if ((opfer != null) && (this.cansee(opfer))) {
                    if (Random.W(100) <= 50) {
                        Befehle.add("BEKLAUE " + opfer.getNummerBase36());
                        Befehle.add("// erfolgreichen Diebstahl fortsetzen: BEKLAUE " + opfer.getNummerBase36());
                        return; // nichts weiter zu tun!
                    } else {
                        Befehle.add("LERNE Tarnung");
                        // Tradition simulieren:
                        new Info("FAKE: " + this + " kann 123 Silber von " + opfer + " stehlen.", this);
                    }
                }
            }
        }
        
        
        
        // sind andere Kobolde hier, mit einer niedrigeren ID als wir?
		for (Unit other : region.getUnits(this.getOwner())) {
			if ((other.getClass() == Kobold.class) && (other.getNummer() < this.getNummer())) {
				if (Random.W(100) <= 50) {
                    // dann: auswandern
                    if (gibAuswanderBefehle(false)) { // Reise nicht erzwingen
                        Befehle.add("// den Kumpels von " + other + " ausweichen.");
                        return; // nichts außer diesem hier machen
                    } else {
                        Befehle.add("// können den Kumpels von " + other + " nicht ausweichen.");
                        boolean auchSilber = false;
                        if (getItem(Silber.class).getAnzahl() > getPersonen() * 10 * 600) {
                            auchSilber = true;
                            Befehle.add("// außerdem sind wir superreich und legen sogar Silber ab.");
                        }
                        gibItems(other, 80, auchSilber);
                    }
                    break;
                }
                if (Random.W(100) <= 20) { // also in 10% aller Fälle (20% von 50%)
                    // dann: anschließen
                    Befehle.add("// den Kumpels von " + other + " anschließen.");
                    Befehle.add("GIB " + other.getNummerBase36() + " ALLES");
                    Befehle.add("// GIB " + other.getNummerBase36() + " ALLES");
                    Befehle.add("GIB " + other.getNummerBase36() + " " + getPersonen() + " PERSONEN");
                    Befehle.add("// GIB " + other.getNummerBase36() + " " + getPersonen() + " PERSONEN");
                    return; // nichts anderes vornehmen
                }
			}
		}
		

		
		List<Unit> players = new ArrayList<Unit>();
		for(Unit unit : region.getUnits()) if ((unit.getOwner() != getOwner()) && (unit.getItem(Silber.class).getAnzahl() > 0)) players.add(unit);
        
		if (players.isEmpty()) {
			// kein Spieler-Silber hier
            
            // können wir reisen?
            if (this.canWalk()) {
                if (gibAuswanderBefehle(false)) return; // Reise nicht erzwingen
            } else {
                // argh, wir sind zu schwer!
                if (getItem(Silber.class).getAnzahl() < getPersonen() * 12 * 10) {
                    // wenn wir Silber für weniger als 12 Monate haben - Panik!
                    new SysMsg("Koboldpanik bei " + this + " in " + region + " (weniger als ein Jahresvorrat vorhanden)!");
                    
                    if (panik()) return;
                } else {
                    Befehle.add("// uns geht's gut - also: Kommt Zeit, kommt Rat.");
                }
            }
            
            // sind wir superreich?
            if (getItem(Silber.class).getAnzahl() > getPersonen() * 10 * 600) {
                // mehr als fünfzig Jahresvorräte...
                Befehle.add("// verdammt, sind wir reich!!!");
                gibSpaltungsBefehle(0, 0, Random.W(6)+2);
                gibTempItems(10, true); // hier bekommen die Neulinge hoffentlich auch genug Silber
            }
            
            Befehle.add("LERNE Tarnung");
		} else {
            // hier gibt es lohnende Opfer:
			Unit victim = players.get(Random.rnd(0, players.size() - 1));
			int modus = getIntegerProperty("kobold.modus", 0);
			if (modus == 0)	{
				Befehle.add("BEKLAUE " + victim.getNummerBase36());
			} else {
				if (Random.W(2) == 1) {
					Befehle.add("LERNE Tarnung");
				} else {
					Befehle.add("LERNE Hiebwaffen");
				}
			}
			setProperty("kobold.modus", 1 - modus);
			
			// Abspaltung bzw. Rekrutierung
			if (Talentwert(Tarnung.class) * 10 * getPersonen() < getItem(Silber.class).getAnzahl())	{
				if (getPersonen() < 4) {
					if (Random.W(100) < 20) Befehle.add("REKRUTIERE " + Random.W(2));
				} else if (Random.W(100) < 50) {
                    // ein paar von uns wollen weg:
                    gibSpaltungsBefehle(Random.W(3), Random.rnd(600, 1200), Random.W(2)-1);
                    gibTempItems(10, false); // 10%, aber kein Silber
				} else if (Random.W(6) == 6) {
					Befehle.add("REKRUTIERE 1");
				}
			}
		}
	}
    
    /**
     * versucht, so viel wie möglich stehen und liegen zu lassen und die Flucht anzutreten
     * @return true, wenn die Flucht möglich scheint (also befohlen ist)
     */
    protected boolean panik() {
        // Scheiß auf das Zeug, jedenfalls nach und nach...
        if (this.hatTiere()) entlasseTiere(90);
        int kapa = this.gesamteFreieKapazitaet(false);
        int ladung = this.getGewicht();
        if (ladung > kapa) {
			ladung -= rohstoffeWegwerfen();
        }
        if (ladung > kapa) {
            ladung -= vermischtesWegwerfen(80, false); // kein Silber!
        }
        if (ladung > kapa) {
            // wenn wir für mehr als 24 Monate haben,
            int silberZuViel = getItem(Silber.class).getAnzahl() - 24 * 10 * getPersonen();
            if (silberZuViel > 0) {
                // die Hälfte des Überflusses kommt auf den Acker:
                Befehle.add("GIB 0 " + (silberZuViel / 2) + " Silber");
                Befehle.add("// Panik & Reichtum zusammen: GIB 0 " + (silberZuViel / 2) + " Silber");
                ladung -= (silberZuViel / (2 * 100));
            }
        }
        
        if (ladung <= kapa) return gibAuswanderBefehle(true); // Reisebefehl erzwingen
        
        return false;
    }
    
    protected void gibSpaltungsBefehle(int personenGeben, int silberGeben, int rekrutieren) {
        Richtung ri = null;
        List<Region> ziele = getGuteWanderziele();
        if (!ziele.isEmpty()) {
            Collections.shuffle(ziele);
            ri = this.getCoords().getRichtungNach(ziele.get(0).getCoords());
        } else {
            ri = Richtung.random();
        }
        Befehle.add("MACHE TEMP " + getNummerBase36());
        Befehle.add("BENENNE EINHEIT " + KoboldName());
        Befehle.add("// " + this + " hat uns abgespalten");
        if (rekrutieren > 0) {
            Befehle.add("REKRUTIERE " + rekrutieren);
        }
        Befehle.add("TARNE Einheit");
        Befehle.add("KÄMPFE nicht");
        Befehle.add("NACH " + ri.getShortcut());
        Befehle.add("ENDE");
        if (personenGeben > 0) {
            Befehle.add("GIB TEMP " + getNummerBase36() + " " + personenGeben + " PERSONEN");
        }
        if (silberGeben > 0) {
            Befehle.add("GIB TEMP " + getNummerBase36() + " " + silberGeben + " Silber");
        }
    }
    
    protected void gibItems(Unit sie, int prozent, boolean auchSilber) {
        for (Item it : getItems()) {
            if ((!auchSilber) && (it.getClass() == Silber.class)) continue;
            
            int cnt = 0;
            for (int i=0; i<it.getAnzahl(); i++) {
                if (Random.W(100) <= prozent) cnt++;
            }
            
            if (cnt > 0) {
                Befehle.add("GIB " + sie.getNummerBase36() + " " + cnt + " " + it.getName());
                Befehle.add("// GIB " + sie.getNummerBase36() + " " + cnt + " " + it.getName());
            }
        }
    }
    
    /**
     * ergibt nur in Verbindung mit gibSpaltungsBefehle Sinn...
     */
    protected void gibTempItems(int prozent, boolean auchSilber) {
        for (Item it : getItems()) {
            if ((!auchSilber) && (it.getClass() == Silber.class)) continue;
            
            int cnt = 0;
            for (int i=0; i<it.getAnzahl(); i++) {
                if (Random.W(100) <= prozent) cnt++;
            }
            
            if (cnt > 0) {
                Befehle.add("GIB TEMP " + getNummerBase36() + " " + cnt + " " + it.getName());
                Befehle.add("// GIB TEMP " + getNummerBase36() + " " + cnt + " " + it.getName());
            }
        }
    }
    
    protected boolean gibAuswanderBefehle(boolean erzwingen) {
        if (!erzwingen) {
            if (!this.canWalk()) return false;
        }
        
		Region region = Region.Load(getCoords());
        
        // mögliche Wanderziele:
        List<Region> guteZiele = getGuteWanderziele();
        List<Region> schlechteZiele = new ArrayList<Region>();
        for (Region r : getMachbareWanderziele()) {
            if (!guteZiele.contains(r)) schlechteZiele.add(r);
        }
		
        if (!guteZiele.isEmpty()) {
            Collections.shuffle(guteZiele);
            Richtung ri = region.getCoords().getRichtungNach(guteZiele.get(0).getCoords());
            Befehle.add("NACH " + ri.getShortcut());
            Befehle.add("// auswandern in ein gutes Land: NACH " + ri.getShortcut());
            return true;
        }
        if (!schlechteZiele.isEmpty()) {
            Collections.shuffle(schlechteZiele);
            Richtung ri = region.getCoords().getRichtungNach(schlechteZiele.get(0).getCoords());
            Befehle.add("NACH " + ri.getShortcut());
            Befehle.add("// auswandern in ein schlechtes Land: NACH " + ri.getShortcut());
            return true;
        }
        // keine Chance:
        Befehle.add("// wir wären gern ausgewandert, aber es gibt keine Ziele. :-(");
        return false;
    }
    
    protected void entlasseTiere(int prozent) {
        for (Item it : getItems()) {
            if (!(it instanceof AnimalResource)) continue;
            int cnt = 0;
            for (int i=0; i<it.getAnzahl(); i++) {
                if (Random.W(100) <= prozent) cnt++;
            }
            
            // 1 Kamel oder Alpaka pro Person sind eher besser, also behalten:
            if ((it.getClass() == Kamel.class) || (it.getClass() == Alpaka.class)) {
                int maximalWeg = it.getAnzahl() - this.getPersonen();
                cnt = Math.min(cnt, maximalWeg);
            }
            
            if (cnt > 0) {
                Befehle.add("GIB BAUERN " + cnt + " " + it.getName());
                Befehle.add("// GIB BAUERN " + cnt + " " + it.getName());
            }
        }
    }
	
    /**
     * @return die erwartete Gewichtsreduzierung
     */
    protected int rohstoffeWegwerfen() {
        int erleichterung = 0;
        for (Item it : getItems()) {
            if (!(it instanceof Resource)) {
				continue;
			}
            if (it instanceof AnimalResource) continue;
            
            if (it.getAnzahl() > 0) {
                erleichterung += it.getAnzahl() * it.getGewicht();
                Befehle.add("GIB 0 " + it.getAnzahl() + " " + it.getName());
                Befehle.add("// GIB 0 " + it.getAnzahl() + " " + it.getName());
            }
        }
        Befehle.add("// vom Wegwerfen der Rohstoffe erhoffen wir uns eine Erleicherung um " + erleichterung + " cGE");
        return erleichterung;
    }
	
    /**
     * @return die erwartete Gewichtsreduzierung
     */
    protected int vermischtesWegwerfen(int prozent, boolean auchSilber) {
        int erleichterung = 0;
        for (Item it : getItems()) {
            if (it instanceof Resource) continue;
            if (it instanceof MagicItem) continue;
            if ((!auchSilber) && it.getClass() == Silber.class) continue;
            
            int cnt = 0;
            for (int i=0; i<it.getAnzahl(); i++) {
                if (Random.W(100) <= prozent) cnt++;
            }
            
            if (cnt > 0) {
                erleichterung += cnt * it.getGewicht();
                Befehle.add("GIB 0 " + cnt + " " + it.getName());
                Befehle.add("// GIB 0 " + cnt + " " + it.getName());
            }
        }
        Befehle.add("// vom Wegwerfen von Zeugs erhoffen wir uns eine Erleicherung um " + erleichterung + " cGE");
        return erleichterung;
    }
	

    /** 
	 * <p>Kümmert sich um das globale Koboldwesen.</p>
	 *
	 * Aussetzen der Kobolde
	 * 
	 * dazu werden alle Inseln kleiner als settings -> "inselkennung.spieler" abgeklappert
	 * 1. ist auf einer Insel ein Kobold -> weiter mit 5.
	 * 2. ist auf der Insel KEIN Wahrnehmer mit 1650LT -> break
	 * 3. beliebige Region (ideal eine ohne Wahrnehmer und mind. zwei verschiedene Völker)
	 *     -> also keine Einheit der Spieler vorhanden
	 *     -> r.alter wird nicht beachtet
	 * 4. Einheit wird erstellt
	 *     -> ca. 10% Chance für diese Insel
	 *     -> 1 bis 5 Kobolde pro Einheit
	 * 5. Kobolde wandern willkürlich und suchen Spieler
	 * 6. abwechselnd (pro Runde halt)
	 *     -> Spieler wird beklaut
	 *     -> Kobolde lernen Hiebwaffen
	 * 7. mehr als 10 * TW(Tarnung) Silber
	 *     -> neuer Einheit mit 10% Chance
	 *     -> Punkt 3 und gleich Wandern in Nachbarregion (zur Verteilung)
	 */
	public static void NeueRunde() {
		new ZATMsg("Monsterplanung: Kobolde");
		for(int insel = 0; insel < GameRules.getInselkennungSpieler() - 3; insel++) {
			if (Random.W(100) <= 20)spawnKoboldEVA(insel);
		}
	}
	
	private static void spawnKoboldEVA(int insel) {
		List<Region> regionen = Region.getInselRegionen(insel, true);
		
		// überprüfen ob Kobolde schon da sind
		boolean k = false;	// Kobold
		boolean w = false;	// Wahrnehmung >= 10 auf der Insel vorhanden 
		for(Region r: regionen)	{
			if (k) break;
			for(Unit unit : r.getUnits()) {
				if (k) break;
				if (unit.getClass().equals(Kobold.class)) k = true;
				if (unit.Talentwert(Wahrnehmung.class) >= 10) w = true;
			}
		}
		if (k) return;
		if (!w) {
			new Debug("Kein Wahrnehmer >T9 auf Insel #" + insel + " --> keine Kobolde.");
			return;
		}

		Unit kobold = createKoboldEVA(insel, null);
		int anzahl = Random.W(4) + Random.W(4);
		kobold.setPersonen(anzahl);
		kobold.setItem(Silber.class, Random.rnd(100 * anzahl, 300 * anzahl));
		kobold.setSkill(Tarnung.class, Skill.LerntageFuerTW(6) * anzahl);
		kobold.setSkill(Hiebwaffen.class, Skill.LerntageFuerTW(3) * anzahl);
        // wenn später neue Kobolde rekrutiert werden, sind diese noch talentierter!
		kobold.Befehle.add("LERNE Tarnung");
	}

	private static Unit createKoboldEVA(int insel, Coords coords) {
		List<Region> regionen = Region.getInselRegionen(insel, true);
		Unit unit = null;
		if (coords == null)	{
			unit = Unit.CreateUnit("Kobold", Codierung.fromBase36("dark"), regionen.get(Random.rnd(0, regionen.size())).getCoords());
		} else {
			unit = Unit.CreateUnit("Kobold", Codierung.fromBase36("dark"), coords);
		}
		unit.Befehle.add("TARNE einheit");
		unit.Befehle.add("KÄMPFE nicht");
		unit.Befehle.add("TARNE partei");
		unit.Befehle.add(";NEU");
		
		// das muss von Hand gesetzt werden ... sonst wird die Einheit gleich
		// nach dem Krieg wieder geschluckt -.-
		unit.setPersonen(1);
		
		return unit;
	}
	
	protected static String KoboldName() {
		String vorname = NonsenseTexter.makeNonsenseWort(Random.W(2));
		if (Random.W(2) == 1) {
			vorname = vorname.replaceAll("a", "o");
			vorname = vorname.replaceAll("e", "i");
			vorname = vorname.replaceAll("u", "o");
		} else {
			vorname = vorname.replaceAll("o", "a");
			vorname = vorname.replaceAll("e", "i");
			vorname = vorname.replaceAll("u", "i");
		}
		
		String nachname = NonsenseTexter.makeNonsenseWort(1);
		if (Random.W(2) == 1) {
			nachname = nachname.replaceAll("a", "o");
			nachname = nachname.replaceAll("e", "i");
			nachname = nachname.replaceAll("u", "o");
		} else {
			nachname = nachname.replaceAll("o", "a");
			nachname = nachname.replaceAll("e", "i");
			nachname = nachname.replaceAll("u", "i");
		}
		nachname = nachname.replaceAll("ln", "d");
		nachname = nachname.replaceAll("tz", "b");
		nachname = nachname.replaceAll("sch", "b");
		String name = StringUtils.ucfirst(vorname) + " " + StringUtils.ucfirst(nachname);
		if (Random.W(6) < 3) name = StringUtils.ucfirst(nachname);
		
		return name;
	}
	
	
	protected List<Region> getGuteWanderziele() {
		List<Region> alleZiele = Region.Load(this.getCoords()).getNachbarn();
		List<Region> guteZiele = new ArrayList<Region>();
		for (Region ziel : alleZiele) {
            if (ziel.istBetretbar(this) && !ziel.anwesendeParteien().isEmpty()) {
                boolean nettDort = false;
                for (Unit jemand : ziel.getUnits()) {
                    if (jemand.getOwner() == this.getOwner()) continue;
                    if (jemand.getOwner() == Codierung.fromBase36("tier")) continue;
                    if (jemand.getItem(Silber.class).getAnzahl() <= 0) continue;
                    nettDort = true;
                    break;
                }
                if (nettDort) guteZiele.add(ziel);
            }
        }
		return guteZiele;
	}

	protected List<Region> getMachbareWanderziele() {
		List<Region> ziele = new ArrayList<Region>();
		for (Region ziel : Region.Load(this.getCoords()).getNachbarn()) if (ziel.istBetretbar(this)) ziele.add(ziel);
		return ziele;
	}


	/**
	 * setzt die entsprechenden Kobold-Waffen
	 * @param krieger - dieser Krieger wird gesetzt
	 */
	@Override
	public void Factory(Krieger krieger)
	{
		krieger.weapons.add(new WKnueppel(this));
	}

	@Override
	public boolean Lernen(Class<? extends Skill> skill)
	{
		return allowLernen(skill);	// Goblins dürfen lernen
	}

	@Override
	public boolean istSpielerRasse() { return false; }

	@Override
	public String getRassenName() {
		if (this.getPersonen() == 1) return getRasse();
		return "Kobolde";
	}

    @Override
    public void meldungenAuswerten() {
        List<Message> messages = Message.Retrieve(null, (Coords)null, this);
        for (Message msg : messages) {
            String txt = msg.getText().toLowerCase();
            
            if (txt.contains("kann niemanden rekrutieren")) {
                // feiner Unterschied: Wenn es keine Rekruten gibt, lautet die Meldung "kann niemand rekrutieren".
                Befehle.add("// STATUS " + STATUS_REKRUTIEREN_BEWACHT);
            }

            if (txt.contains("kann") && txt.contains("silber") && txt.contains("stehlen")) {
                List<String> ids = StringUtils.idsIn(txt);
                if (ids.size() == 2) {
                    Befehle.add("// STATUS " + STATUS_STEHLEN_ERFOLGREICH + " " + ids.get(1));
                } else {
                    new SysErr("Kobold.meldungenAuswerten() macht offenbar falsche Annahmen zum Meldungsformat (erfolgreicher Diebstahl)");
                }
            } else if (txt.contains("opfer des diebstahls") && txt.contains("nichts")) {
                List<String> ids = StringUtils.idsIn(txt);
                if (ids.size() == 1) {
                    Befehle.add("// STATUS " + STATUS_STEHLEN_ERFOLGLOS + " " + ids.get(0));
                } else {
                    new SysErr("Kobold.meldungenAuswerten() macht offenbar falsche Annahmen zum Meldungsformat (erfolgloser Diebstahl)");
                }
            } else if (txt.contains("beim diebstahl") && txt.contains("erwischt")) {
                List<String> ids = StringUtils.idsIn(txt);
                if (ids.size() == 2) {
                    Befehle.add("// STATUS " + STATUS_ERWISCHT + " " + ids.get(1));
                } else {
                    new SysErr("Kobold.meldungenAuswerten() macht offenbar falsche Annahmen zum Meldungsformat (beim Diebstahl ertappt)");
                }
            }
        }
    }

    public String getStatusRekrutieren() {
        return statusRekrutieren;
    }

    public void setStatusRekrutieren(String statusRekrutieren) {
        this.statusRekrutieren = statusRekrutieren;
    }

    public String getStatusStehlen() {
        return statusStehlen;
    }

    public void setStatusStehlen(String statusStehlen, int einheit) {
        this.statusStehlen = statusStehlen;
        this.statusStehlenEinheit = einheit;
    }

    public int getStatusStehlenEinheit() {
        return statusStehlenEinheit;
    }

}
