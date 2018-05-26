package de.x8bit.Fantasya.Atlantis.Units;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Monster;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.WKnueppel;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.Random;
import de.x8bit.Fantasya.util.StringUtils;
import de.x8bit.Fantasya.util.lang.NonsenseTexter;

/**
 * @author mogel
 */
public class Goblin extends Monster
{
	public Goblin()
	{
		RekrutierungsKosten = 50;

		minHunger = 1;
		maxHunger = 2;
	}

	@Override
	public boolean Rekrutieren(int anzahl)
	{
		boolean retval = super.Rekrutieren(anzahl);
		
		getSkill(Hiebwaffen.class).setLerntage(getSkill(Hiebwaffen.class).getLerntage() + 180 * anzahl);	// T3
		getSkill(Tarnung.class).setLerntage(getSkill(Tarnung.class).getLerntage() + 180 * anzahl);	// T3

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
	
	/**
	 * 1. TODO: Hungernde Goblins greifen an
	 * 2. Reiche Goblins sind meistens sehr passiv und üben sich in Tarnung.
	 * 3. Sind in der Region Einheiten mit Silber anwesend?
	 *    a) Ja -
	 *       1. Abwechselnd stehlen und Tarnung lernen.
	 *       2. Haben wir mehr Silber als 100 pro Person und Talentpunkt Tarnung?
	 *          a) Ja - Nachwuchs rekrutieren; oder TEMP abspalten, falls wir mehr als 20 sind.
	 *          b) Nein - Nachwuchs rekrutieren
	 *    b) Nein - wenn es bewohnte Nachbarregionen gibt, dorthin wandern.
	 */
	@Override
	public void planMonster() {

		// falls wir diesen Monat neu sind, haben wir unseren Plan ja schon:
		for (String b : Befehle) if (b.equals(";NEU")) {
			if (this.getName().startsWith("Einheit")) this.Befehle.add("BENENNE EINHEIT " + GoblinName());
			return;
		}

        int silberVerfuegbar = getItem(Silber.class).getAnzahl();
		
		Befehle.clear();
		if (this.getSichtbarkeit() == 0) Befehle.add("TARNE EINHEIT");
		if (this.getTarnPartei() != 0) Befehle.add("TARNE PARTEI");
		
		if (this.getName().startsWith("Einheit")) this.Befehle.add("BENENNE EINHEIT " + GoblinName());

        
		Region region = Region.Load(getCoords());


        //TODO Hungernde Goblins greifen an
		if (this.getKampfposition() != Kampfposition.Nicht) Befehle.add("KÄMPFE NICHT");


        // "Bitten" uns Hoellenhunde um eine kleine Spende?
        if (silberVerfuegbar > 1000) {
            Unit erpresser = null;
            for (Unit u : region.getUnits(this.getOwner())) {
                if (u instanceof Hoellenhund) { erpresser = u; break; }
            }
            if (erpresser != null) {
                int summe = ((15 + Random.W(20)) * silberVerfuegbar) / 100; // 16% .. 35%
                Befehle.add("GIB " + erpresser.getNummerBase36() + " " + summe + " Silber");
                Befehle.add("// Wir wurden von " + erpresser + " erpresst - " + summe + " Silber...");
                new Info("Wir bitten die Goblins " + this + " um eine kleine Spende ...", erpresser);
                silberVerfuegbar -= summe;
            }
        }

		
		// wenn wir reich sind - na dann ab in die Höhle!
		if (silberVerfuegbar > this.getPersonen() * 500) {
			if (Random.W(10) < 10) {
				Befehle.add("LERNE Tarnung");
				return;
			}
		}

		
		
		
		// sind andere Goblins hier, mit einer niedrigeren ID als wir?
		for (Unit other : region.getUnits(this.getOwner())) {
			if ((other.getClass() == Goblin.class) && (other.getNummer() < this.getNummer())) {
				// dann: auswandern
				List<Region> guteZiele = getGuteWanderziele();
				if (!guteZiele.isEmpty()) {
					Collections.shuffle(guteZiele);
					Richtung ri = region.getCoords().getRichtungNach(guteZiele.get(0).getCoords());
					Befehle.add("NACH " + ri.getShortcut());
					Befehle.add("// den Kumpels von " + other + " ausgewichen nach " + ri.getShortcut());
					return;
				}
			}
		}
		
		
        
		List<Unit> players = new ArrayList<Unit>();
		for(Unit unit : region.getUnits()) if ((unit.getOwner() != getOwner()) && (unit.getItem(Silber.class).getAnzahl() > 0)) players.add(unit);
		if (players.isEmpty())
		{
			// kein Silber hier
			List<Region> guteZiele = getGuteWanderziele();
			if (!guteZiele.isEmpty()) {
				Collections.shuffle(guteZiele);
				Richtung ri = region.getCoords().getRichtungNach(guteZiele.get(0).getCoords());
				Befehle.add("NACH " + ri.getShortcut());
			} else {
				Befehle.add("LERNE Tarnung");
			}

		} else
		{
			Unit victim = players.get(Random.rnd(0, players.size()));
			int modus = getIntegerProperty("goblin.modus", 0);
			if (modus == 0)	{
				Befehle.add("BEKLAUE " + victim.getNummerBase36());
				// TODO Erfolg berücksichtigen (Einheiten-Property?)
			} else {
				Befehle.add("LERNE Tarnung");
			}
			setProperty("goblin.modus", 1 - modus);
			
			// Abspaltung bzw. Rekrutierung
			if (Talentwert(Tarnung.class) * this.getPersonen() * 100 < getItem(Silber.class).getAnzahl()) {
				if (getPersonen() < 20)	{
					Befehle.add("REKRUTIERE " + Random.rnd(1, 3));
				} else {
					// wenn wir noch ganz neu sind, dann niemand abspalten...
					if (this.getName().startsWith("Einheit")) return;
					
					if (Random.rnd(0, 100) < 80) {
						String stolzerTitel = NonsenseTexter.makeNonsenseWort(Random.W(2));
                        int geschenk = Random.rnd(300, 600);
						
						Befehle.add("MACHE TEMP " + getNummerBase36());
						Befehle.add("TARNE Einheit");
						Befehle.add("REKRUTIERE " + Random.rnd(1, 5));
						Befehle.add("KÄMPFE nicht");
						Befehle.add("BENENNE EINHEIT " + StringUtils.ucfirst(stolzerTitel) + " " + this.getName());
						Befehle.add("NACH " + Richtung.random());
						Befehle.add("ENDE");
						Befehle.add("GIB TEMP " + getNummerBase36() + " " + geschenk + " Silber");
						Befehle.add("GIB TEMP " + getNummerBase36() + " " + Random.rnd(1, 5) + " Personen");

                        silberVerfuegbar -= geschenk;
					} else {
						Befehle.add("REKRUTIERE 1");
					}
				}
			}
		}
	}
	
	protected List<Region> getGuteWanderziele() {
		List<Region> alleZiele = Region.Load(this.getCoords()).getNachbarn();
		List<Region> guteZiele = new ArrayList<Region>();
		for (Region ziel : alleZiele) if (ziel.istBetretbar(this) && !ziel.anwesendeParteien().isEmpty()) guteZiele.add(ziel);
		return guteZiele;
	}
	
	protected static String GoblinName() {
		String vorname = NonsenseTexter.makeNonsenseWort(1);
		vorname = vorname.replaceAll("i", "a");
		vorname = vorname.replaceAll("o", "u");
		vorname = vorname.replaceAll("m", "r");
		
		String nachname = NonsenseTexter.makeNonsenseWort(Random.W(2));
		nachname = nachname.replaceAll("i", "a");
		nachname = nachname.replaceAll("b", "z");
		String name = StringUtils.ucfirst(vorname) + " " + StringUtils.ucfirst(nachname);
		
		return name;
	}
	
	/**
	 * @param insel 
	 */
	private static void spawnGoblinsEVA(int insel) {
		List<Region> regionen = Region.getInselRegionen(insel, true);
		
		// überprüfen ob Goblins schon da sind
		for(Region r: regionen) {
			for(Unit unit : r.getUnits()) {
				if (unit.getClass().equals(Goblin.class)) {
					// wenn ja, dann noch ein paar Kollegen anheuern?
					if (!r.istBewacht(unit, AllianzOption.Kontaktiere)) {
						unit.setItem(Silber.class, unit.getItem(Silber.class).getAnzahl() +  Random.rnd(300, 700));
						unit.Befehle.clear();
						unit.Befehle.add("LERNE Tarnung");
						unit.Befehle.add("REKRUTIERE " + Random.rnd(1, 5));
						unit.Befehle.add(";NEU");
						return;
					}
				}
			}
		}

		Unit goblin = createGoblinEVA(insel, null);
		goblin.setItem(Silber.class, Random.rnd(300, 700 * goblin.getPersonen()));
		goblin.Befehle.add("REKRUTIERE " + Random.rnd(1, 5));
		
		
		goblin.Befehle.add("BENENNE EINHEIT " + GoblinName());
	}

	/**
	 * <p>Erzeugt eine neue Goblin-Einheit irgendwo auf der Insel oder in der 
	 * angegebenen Region.</p>
	 * <p>Die Einheit kann aus 1 .. 44 Goblins bestehen, leicht "linksschief",
	 * Pi mal Daumen etwa 12.</p>
	 * @param insel
	 * @param coords Wenn null, wird zufällig eine Region bestimmt. Ansonsten wird die Einheiten in coords ausgesetzt.
	 * @return 
	 */
	private static Unit createGoblinEVA(int insel, Coords coords) {
		List<Region> regionen = Region.getInselRegionen(insel, true);
		Unit unit = null;
		if (coords == null) {
			unit = Unit.CreateUnit("Goblin", Codierung.fromBase36("dark"), regionen.get(Random.rnd(0, regionen.size())).getCoords());
		} else	{
			unit = Unit.CreateUnit("Goblin", Codierung.fromBase36("dark"), coords);
		}
		unit.Befehle.add(";NEU");
		unit.Befehle.add("TARNE einheit");
		unit.Befehle.add("KAEMPFE nicht");
		unit.Befehle.add("TARNE partei");
		unit.Befehle.add("LERNE Tarnung");
		
		unit.setPersonen(Random.W(4) + (Random.W(20) * Random.W(20)) / 10);
		unit.setSkill(Tarnung.class, 180 * unit.getPersonen());
		unit.setSkill(Hiebwaffen.class, 180 * unit.getPersonen());

		return unit;
	}

	
	/*
	 * Aussetzen & Planen der Goblin-Weltherrschaft:
	 * 
	 * Alle Inseln älter als ("inselkennung.spieler" - 3) abgeklappern,
	 * mit einer gewissen Chance (siehe Code) tauchen dort ganz neue Goblins auf.
	 */
	public static void NeueRunde() {
		new ZATMsg("Monsterplanung: Goblins");
		for(int insel = 0; insel < GameRules.getInselkennungSpieler() - 3; insel++) {
			
			// Chanche für diese Insel berechnen
			// Für jede Region gibt es eine 1%-Chance, dass diese Runde IRGENDWO auf 
			// dieser Insel neue Goblins auftauchen. Es wird immer nur maximal eine neue 
			// Einheit ausgesetzt.
			boolean nachwuchs = false;
			for (int i=0; i<Region.getInselRegionen(insel, true).size(); i++) {
				if (Random.W(100) == 1) {
					nachwuchs = true;
					break;
				}
			}
			new Debug("Goblin-Nachwuchs auf Insel " + insel + ": " + (nachwuchs?"JA":"nein"));
			if (!nachwuchs) continue;
			
			spawnGoblinsEVA(insel);
		}
	}

	/**
	 * setzt die entsprechenden Goblin-Waffen
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
	public boolean istSpielerRasse() { return false;	}

	@Override
	public String getRassenName() {
		if (this.getPersonen() == 1) return getRasse();
		return "Goblins";
	}

    @Override
    public void meldungenAuswerten() {
        // NOP
    }
}
