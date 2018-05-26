package de.x8bit.Fantasya.Host.ManualTests;

import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Bergwerk;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Buildings.Sattlerei;
import de.x8bit.Fantasya.Atlantis.Buildings.Schiffswerft;
import de.x8bit.Fantasya.Atlantis.Buildings.Schmiede;
import de.x8bit.Fantasya.Atlantis.Buildings.Werkstatt;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Bergbau;
import de.x8bit.Fantasya.Atlantis.Skills.Ruestungsbau;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.Atlantis.Skills.Segeln;
import de.x8bit.Fantasya.Atlantis.Skills.Waffenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Wagenbau;
import java.util.Collection;

/**
 * Basisklasse für die "ManualTests"
 * @author hapebe
 */
public abstract class TestBase {
	TestWorld testWorld;
	
	public TestBase() { }
		
	public TestWorld getTestWorld() {
		return testWorld;
	}

	public void setTestWorld(TestWorld testWorld) {
		this.testWorld = testWorld;
	}

	public List<Region> getRegions() {
		return testWorld.getAlleRegionen();
	}

	/**
	 * erfüllt inzwischen schon eine kleine Aufgabe, vielleicht werden es noch mehr - 
	 * deswegen nicht abstract.
	 */
	public void setupTest() {
		// eigenes Setup aufrufen
		mySetupTest();

		// alles Speichern
		// ZATBase.ClearProxy();
		
		// alle bisherigen benutzten Flags anzeigen
		// Main.listUsedFlags();
	}
	
	protected abstract void mySetupTest();

    protected abstract boolean verifyTest();

    /**
     * <p>Überprüft im Rahmen von verifyTest(), ob eine Einheit in der erwarteten Region steht - 
     * geht dabei von einem Namensmuster der Einheit aus, das so aussieht:</p>
     * <code>unit.setName(this.getClass().getSimpleName() + " 01 " + coords.getX() + " " + coords.getY());</code>
     * @param tokens Der an Leerzeichen gesplittete Name der Einheit
     * @param c Die tatsächlichen Koordinaten
     * @return true, falls die Erwartungen und die Realität übereinstimmen - sonst false
     */
    protected boolean verifyUnitCoords(String[] tokens, Coords c) {
        int x = Integer.parseInt(tokens[2]);
        int y = Integer.parseInt(tokens[3]);
        if ( (x != c.getX()) || (y != c.getY()) ) {
            return false;
        }
        return true;
    }

    /**
     * @param all Alle Units, in den gesucht werden soll (normalerweise ALLE Units)
     * @param expected Die Test-Namens-Tokens, nach denen gesucht werden soll
     * @return null, wenn alles okay ist! Ansonsten eine Komma-separierte Liste der fehlenden Tokens.
     */
    protected String verifyExpectedUnits(Collection<Unit> all, String[] expected) {
        String retval = null;

        List<String> foundUnits = new ArrayList<String>();
        for (Unit u:all) {
            for (String idx : expected) {
                if (u.getName().startsWith(this.getName() + " " + idx)) {
                    foundUnits.add(idx);
                }
            }
        }
        if (expected.length > foundUnits.size()) {
            StringBuilder sb = new StringBuilder();
            for (String idx : expected) {
                if (!foundUnits.contains(idx)) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(idx);
                }
            }
            retval = sb.toString();
        }
        return retval;
    }

    protected boolean fail(String message) {
        new TestMsg("--- " + message + " ---");
        return false;
    }

	/**
	 * Erzeugt eine Einheit der Partei: 1 Person, 1000 Silber, Rasse entspricht derjenigen der Partei.
	 * @param p
	 * @param r
	 * @return Eine neue Einheit
	 */
	public Unit createUnit(Partei p, Region r) {
		return createUnit(p, r, p.getRasse());
	}

	public Unit createUnit(Partei p, Region r, String rasse) {
		Unit u = Unit.CreateUnit(rasse, p.getNummer(), r.getCoords());
		u.setPersonen(1);
		u.setItem(Silber.class, 1000);
		return u;
	}

	/**
	 * erzeugt einen Kapitän mit dem entsprechenden Schiff
	 * @param p - Partei
	 * @param r - Region
	 * @param schiffstyp - der Schiffstyp
	 * @return der neue Kapitän
	 */
	public Unit createKapitaen(Partei p, Region r, String schiffstyp) {
		Ship schiff = Ship.Create(schiffstyp, r.getCoords());
		schiff.setGroesse(schiff.getConstructionSize());
		schiff.setFertig(true);

		Unit kapitaen = createUnit(p, r);
		kapitaen.setName("Kapitän von Schiff [" + schiff.getNummerBase36() + "]");
		kapitaen.setPersonen(schiff.getMatrosen() / 10 + 1);
		kapitaen.setSkill(Segeln.class, 11 * 10 * 30 / 2 * kapitaen.getPersonen()); // TW 10?
		
		kapitaen.Enter(schiff);

		return kapitaen;
	}

	/**
	 * erzeugt einen Spezialisten für $wasAuchImmer (inkl. Gebäude)
	 * @param p - Partei
	 * @param r - Region
	 * @param bType - GebäudeType (current: "Schmiede", "Sattlerei", "Schiffswerft", "Werkstatt", "Bergwerk")
	 * @param inBuilding - ob die Einheit bereits in seinem Gebäude sitzt (Gebäude wird dann auch erstellt)
	 * @return die neue Einheit
	 */
	public Unit createSpecialist(Partei p, Region r, String bType, boolean inBuilding) {
		Building b = null;
		if (inBuilding) {
			// zuerst brauchen wir eine große Burg:
			Building burg = Building.Create(new Burg().getName(), r.getCoords());
			burg.setSize(500);

			b = Building.Create(bType, r.getCoords());
			b.setSize(10);
		}

		Class<? extends Skill> specSkill = null;
		String befehl = null;
		if (bType.equals(new Schmiede().getName())) {
			specSkill = Waffenbau.class;
			befehl = "MACHE Schwert";
		}
		if (bType.equals(new Sattlerei().getName())) {
			specSkill = Ruestungsbau.class;
			befehl = "MACHE Kettenhemd";
		}
		if (bType.equals(new Schiffswerft().getName())) {
			specSkill = Schiffbau.class;
			befehl = "MACHE Langboot";
		}
		if (bType.equals(new Werkstatt().getName())) {
			specSkill = Wagenbau.class;
			befehl = "MACHE Wagen";
		}
		if (bType.equals(new Bergwerk().getName())) {
			specSkill = Bergbau.class;
			befehl = "MACHE Eisen";
		}

		if (specSkill == null) {
			throw new UnsupportedOperationException("Für diesen Gebäudetyp kann man noch keinen Spezialisten erstellen.");
		}

		Unit spec = createUnit(p, r);
		spec.setName("Spezialist für " + specSkill.getSimpleName());
		spec.setSkill(specSkill, 1080); // TW 9 (Rundung testen...)
		spec.Befehle.add(befehl);
		spec.setItem(Eisen.class, 1000);
		spec.setItem(Holz.class, 1000);
		spec.setPersonen(1);

		if (inBuilding) spec.Enter(b);

		return spec;
	}

	public Unit createMage(Partei p, Region r, int tw) {
		Unit mage = createUnit(p, r);
		mage.setName("Magier");
		mage.setPersonen(1);
		mage.setSkill(Magie.class, Skill.LerntageFuerTW(tw));
		mage.setItem(Silber.class, 10000);
		mage.setAura(tw * tw);
		return mage;
	}

	
    public String getName() {
        return this.getClass().getSimpleName();
    }

}
