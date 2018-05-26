package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Skills.Schiffbau;
import de.x8bit.Fantasya.Atlantis.Skills.Segeln;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

public class Mantis138 extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei partei = Partei.getPartei(1);
        Region region = null;
		Richtung r = Richtung.Suedwesten;

        for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
            if (Region.Load(maybe.getCoords().shift(r)) instanceof Ozean) {
                if (Region.Load(maybe.getCoords().shift(r).shift(r)) instanceof Ozean) {
                    // gotcha!
                    region = maybe;
                }
            }
        }
        if (region == null) throw new IllegalStateException("Keine passende Region für " + this.getClass().getSimpleName() + " gefunden - einfach nochmal probieren...");
        getRegions().remove(region);

        Coords start = region.getCoords();
		
		Building werft = Building.Create("Schiffswerft", region.getCoords());
		werft.setSize(500);

		Ship ship = Ship.Create("Tireme", region.getCoords());
		ship.setGroesse(200);
		ship.setFertig(true);

		Unit unit = this.createUnit(partei, region);
        unit.setName(this.getClass().getSimpleName() + " 01 " + start.getX() + " " + start.getY());
        unit.setBeschreibung("Erwartet: Befindet sich in der Werft, aber nicht auf dem Schiff.");
		unit.setGebaeude(werft.getNummer());
		unit.Befehle.add("VERLASSE");
		unit.setItem(Holz.class, 20000);
		unit.setItem(Silber.class, 20000);
		unit.setSkill(Schiffbau.class, 20000);
		ship.setOwner(unit.getNummer());
		unit.setSchiff(ship.getNummer());
		
		werft.setOwner(unit.getNummer());
		
		Unit matrosen = this.createUnit(partei, region);
        Coords ziel = region.getCoords().shift(r).shift(r);
        matrosen.setName(this.getClass().getSimpleName() + " 02 " + ziel.getX() + " " + ziel.getY());
        matrosen.setBeschreibung("Erwartet: Reisen nach " + Region.Load(ziel) + ".");
		matrosen.Befehle.add("NACH " + r.toString() + " " + r.toString());
		matrosen.setPersonen(10);
		matrosen.setSchiff(ship.getNummer());
		matrosen.setSkill(Segeln.class, 200000);
	}

    @Override
    protected boolean verifyTest() {
        String testName = this.getClass().getSimpleName();
        boolean retval = true;

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + testName + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + testName + "...");

        for (Unit u:Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(testName)) continue;

            String[] tokens = u.getName().split("\\ ");

            if (GameRules.getRunde() == 2) {
                // unit 01 - muss noch am Ausgangspunkt und in der Werft sein
                if (tokens[1].equals("01")) {
                    if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                        retval = fail(testName + "-Test " + tokens[1] + ": " + u + " ist nicht mehr in der Startregion.");
                    }

                    Building building = Building.getBuilding(u.getGebaeude());
                    if (building == null) {
                        retval = fail(testName + "-Test " + tokens[1] + ": " + u + " ist nicht in der Werft.");
                    }
                }

                // unit 02 - muss in ihrer Zielregion sein und Kapitän sein.
                if (tokens[1].equals("02")) {
                    if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                        retval = fail(testName + "-Test " + tokens[1] + ": " + u + " ist nicht in der Zielregion angekommen.");
                    }

                    Ship ship = Ship.Load(u.getSchiff());
                    if (ship == null) {
                        retval = fail(testName + "-Test " + tokens[1] + ": " + u + " ist nicht an Bord.");
                    } else {
                        if (ship.getOwner() != u.getNummer()) {
                            retval = fail(testName + "-Test " + tokens[1] + ": " + u + " ist nicht Kapitän.");
                        }
                    }

                }

                continue;
            }

            throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");
        } // next unit

        return retval;
    }

}
