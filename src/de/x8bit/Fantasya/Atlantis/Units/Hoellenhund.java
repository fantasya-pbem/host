package de.x8bit.Fantasya.Atlantis.Units;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Cave;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Helper.Monster;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.ZATMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Vulkan;
import de.x8bit.Fantasya.Atlantis.Regions.aktiverVulkan;
import de.x8bit.Fantasya.Atlantis.Skills.Ausdauer;
import de.x8bit.Fantasya.Atlantis.Skills.Monsterkampf;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Host.ZAT.Battle.Krieger;
import de.x8bit.Fantasya.Host.ZAT.Battle.Weapons.MHoellenhund;
import de.x8bit.Fantasya.util.Codierung;
import de.x8bit.Fantasya.util.Random;

/**
 * @author hapebe
 */
public class Hoellenhund extends Monster
{
	public Hoellenhund() {
		RekrutierungsKosten = 0; // können generell nicht rekrutieren
		gewicht = 4000;
		kapazitaet = 4500;

		minHunger = 2;
		maxHunger = 5;
	}

	@Override
	public boolean Rekrutieren(int anzahl) { return false; }
	@Override
	public int Trefferpunkte() { return 60; }
	@Override
	public boolean istSpielerRasse() { return false; }

	/**
	 * Hoellenhunde können "ganz normal" hungern - das ist die einzige 
	 * natürliche Chance, sie wieder loszuwerden.
	 * @return TRUE wenn die Einheit gehungert hat.
	 */
	@Override
	public boolean actionUnterhalt() {
		return essen(5);
	}
	
	
	
	// -------------------------------------------------------------------------------------
	
	@Override
	public void planMonster() {
		// falls wir diesen Monat neu sind, haben wir unseren Plan ja schon:
		for (String b : Befehle) if (b.equals(";NEU")) {
			if (this.getName().startsWith("Einheit")) this.Befehle.add("BENENNE EINHEIT Hoellenhunde");
			return;
		}
		
		Befehle.clear();
		// if (this.getSichtbarkeit() == 0) Befehle.add("TARNE EINHEIT");
		if (this.getTarnPartei() != 0) Befehle.add("TARNE PARTEI");
		if (this.getName().startsWith("Einheit")) this.Befehle.add("BENENNE EINHEIT Hoellenhunde");
		if (this.getKampfposition() != Kampfposition.Vorne) Befehle.add("KÄMPFE VORNE");

		
		Region region = Region.Load(getCoords());
		
		
		// sind andere Hoellenhunde hier, mit einer niedrigeren ID als wir?
		for (Unit other : region.getUnits(this.getOwner())) {
			if ((other.getClass() == Hoellenhund.class) && (other.getNummer() < this.getNummer())) {
				// dann schließen wir uns denen an?
                if (Random.W(6) >= 5) {
                    Befehle.add("GIB " + other.getNummerBase36() + " Silber");
                    Befehle.add("GIB " + other.getNummerBase36() + " " + this.getPersonen() + " PERSONEN");
                    return;
                }
			}
		}
        
        String n = getName();
        if ((getLebenspunkte() * 3) > (maxLebenspunkte() * 2)) {
            // > 2/3 Schaden
            n = "Abgerissene Hoellenhunde";
        } else if ((getLebenspunkte() * 3) > maxLebenspunkte()) {
            // > 1/3 Schaden
            n = "Hungrige Hoellenhunde";
        } else {
            n = "Hoellenhunde";
        }
        if (!getName().equalsIgnoreCase(n)) Befehle.add("BENENNE EINHEIT " + n);

		
		// falls wir etwas erbeutet haben:
		for (Item it : this.getItems()) {
			if (it.getClass() == Silber.class) {
				int maxBehalten = this.getPersonen() * (this.kapazitaet - this.gewicht);
				if (it.getAnzahl() > maxBehalten) Befehle.add("GIB 0 " + (it.getAnzahl() - maxBehalten) + " Silber");
				continue;
			}
			Befehle.add("GIB 0 " + it.getAnzahl() + " " + it.getName()); // wegwerfen!
		}
		
		// falls Höhlen da sind (in der Unterwelt) - 33% Chance, sie zu betreten!
        for (Building b : region.getBuildings()) {
            if (b instanceof Cave) {
                if (Random.W(6) > 4) {
                    Befehle.add("BETRETE GEBÄUDE " + b.getNummerBase36());
                    Befehle.add("NACH " + Richtung.random().getShortcut());
                    return;
                }
            }
        }
		
        
		// List<Unit> players = new ArrayList<Unit>();
		// for(Unit unit : region.getUnits()) if (unit.getOwner() != getOwner()) players.add(unit);
        List<Unit> possibleVictimList = Monster.getAttackableUnitList(this, null);
		if (possibleVictimList.isEmpty()) {
			// kein Silber hier
			List<Region> guteZiele = getGuteWanderziele();
			if ((!guteZiele.isEmpty()) && (Random.W(6) > 3)) {
				Collections.shuffle(guteZiele);
				Richtung ri = region.getCoords().getRichtungNach(guteZiele.get(0).getCoords());
				Befehle.add("NACH " + ri.getShortcut());
			} else {
                if (getItem(Silber.class).getAnzahl() > getPersonen() * 5 * 6) {
                    // mehr als 6 Monate Silber vorrätig:
                    Befehle.add("LERNE Ausdauer");
                } else {
                    Befehle.add("LERNE Monsterkampf");
                }
			}
		} else {
			List<String> gegner = new ArrayList<String>();
			for (Unit u : possibleVictimList) {
                if (istWuetend()) { // auf jeden Fall alle angreifen
                    gegner.add(u.getNummerBase36());
                    continue;
                }
				if (Random.W(6) > 3) gegner.add(u.getNummerBase36());
			}
			if (!gegner.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (String id : gegner) sb.append(id).append(" ");
				Befehle.add("ATTACKIERE " + sb.substring(0, sb.length() - 1));
				
				List<Region> guteZiele = getGuteWanderziele();
				if (!guteZiele.isEmpty()) {
					Collections.shuffle(guteZiele);
					Richtung ri = region.getCoords().getRichtungNach(guteZiele.get(0).getCoords());
					Befehle.add("NACH " + ri.getShortcut());
				} else {
					Befehle.add("LERNE Monsterkampf");
				}
			} else {
				new Info(this + " entschließen sich, die armen Kreaturen in " + region + " diesmal zu verschonen.", this);
                if (getItem(Silber.class).getAnzahl() > getPersonen() * 5 * 6) {
                    // mehr als 6 Monate Silber vorrätig:
                    Befehle.add("LERNE Ausdauer");
                } else {
                    Befehle.add("LERNE Monsterkampf");
                }
			}
		}
	}
    
    /**
     * @return true, wenn mehr als 2/3 Schaden erlitten wurde
     */
    private boolean istWuetend() {
        return (getLebenspunkte() * 3) > (maxLebenspunkte() * 2);
    }
	
	protected List<Region> getGuteWanderziele() {
		List<Region> alleZiele = Region.Load(this.getCoords()).getNachbarn();
		List<Region> guteZiele = new ArrayList<Region>();
		for (Region ziel : alleZiele) {
			if (ziel.istBetretbar(this) && (ziel.getClass() != aktiverVulkan.class)) guteZiele.add(ziel);
		}
		return guteZiele;
	}
	
	/**
	 * @param coords Die Einheiten wird bei coords ausgesetzt.
	 * @return 
	 */
	private static Unit createHoellenhunde(Coords coords) {
		Unit unit = null;
		unit = Unit.CreateUnit("Hoellenhund", Codierung.fromBase36("dark"), coords);
        
        Region r = Region.Load(coords);
		unit.Befehle.add(";NEU");
		// unit.Befehle.add("TARNE einheit");
		unit.Befehle.add("KAEMPFE vorne");
		
		List<Region> ziele = ((Hoellenhund)unit).getGuteWanderziele();
		if (!ziele.isEmpty()) {
			Collections.shuffle(ziele);
			Richtung ri = coords.getRichtungNach(ziele.get(0).getCoords());
			unit.Befehle.add("NACH " + ri.getShortcut() + " " + ri.getShortcut());
		} else {
			unit.Befehle.add("LERNE Monsterkampf");
		}
		
		unit.setPersonen(Random.W(4) + Random.W(6) - 1);
		unit.setSkill(Monsterkampf.class, 30 * unit.getPersonen());
		unit.setSkill(Wahrnehmung.class, Skill.LerntageFuerTW(20) * unit.getPersonen());

        new Info("Die Erde von " + r + " speit " + unit.getPersonen() + " Hoellenhunde!", r);
		
        return unit;
	}

	
	/**
	 * Aussetzen neuer Höllenhunde
	 */
	public static void NeueRunde() {
		new ZATMsg("Monsterplanung: Höllenhunde");
		
		for (Region r : Region.CACHE.values()) {
			if (!(r instanceof aktiverVulkan)) continue;
			
			if (Random.W(6) > 3) {
				// neue Höllenhunde!
				int neu = Random.W(4) - 1;
				if (neu <= 0) neu = 1;
				for (int i=0; i < neu; i++) {
					createHoellenhunde(r.getCoords());
				}
			}
		}
	}

	/**
	 * setzt die entsprechenden Waffen
	 * @param krieger - dieser Krieger wird gesetzt
	 */
	@Override
	public void Factory(Krieger krieger) {
		krieger.weapons.add(new MHoellenhund(this));
	}

	@Override
	public boolean Lernen(Class<? extends Skill> skill)	{
		if (skill == Monsterkampf.class) return allowLernen(skill);	// Hoellenhunde dürfen Monsterkampf lernen
		if (skill == Ausdauer.class) return allowLernen(skill);	// Hoellenhunde dürfen Ausdauer lernen
		
		return super.Lernen(skill);
	}


	@Override
	public String getRassenName() {
		if (this.getPersonen() == 1) return getRasse();
		return "Hoellenhunde";
	}
	
	/**
	 * um das Lernen zu beschleunigen, starten Hoellenhunde schon bei 30 Lerntagen mit T5 in Monsterkampf.
	 * @param skill
	 * @return 
	 */
	@Override
	public int Talentwert(Skill skill) {
		int tw = super.Talentwert(skill);
		
		if (skill.getClass() == Monsterkampf.class) tw +=  4;

		// und jetzt noch spezifisch nach Terrain -
		// Hoellenhunde bekommen Monsterkampf +5 auf Vulkanen
		if (skill.getClass().equals(Monsterkampf.class)) {
			Region r = Region.Load(this.getCoords());
			if ((r.getClass() == Vulkan.class) || (r.getClass() == aktiverVulkan.class)) tw +=  5;
		}

		return (tw < 0 ? 0 : tw);
	}

    @Override
    public void meldungenAuswerten() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
	
}
