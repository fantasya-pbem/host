package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Skills.Segeln;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 *
 * @author hapebe
 */
public class Mantis177 extends TestBase {

    @Override
	protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        // tw.setContinueWithZAT(false);

		Partei p = tw.getSpieler1();
		Region r = tw.nurNachbarVon(tw.nurBetretbar(getRegions()), Ozean.class).get(0);

        // wohin segeln wir denn so?
		Richtung richtg = tw.getRichtung(r, Ozean.class);
        Coords ziel = r.getCoords().shift(richtg);
        
        Unit u = this.createUnit(p, r);
        u.setName(this.getClass().getSimpleName() + " 01 " + ziel.getX() + " " + ziel.getY());
		u.setSkill(Segeln.class, 450);

		Ship ship = Ship.Create("Boot", r.getCoords());
		ship.setGroesse(5);
		ship.setFertig(true);
        u.Enter(ship);

        // u.Befehle.add("BETRETE SCHIFF " + ship.Base36());
        u.Befehle.add("NUMMER SCHIFF m177");
        u.Befehle.add("NACH " + richtg);
        u.setBeschreibung("Erwartet: Ändert die Nummer des Schiffs auf m177 und legt ab.");

        new Info("Mantis #177 Setup in " + r + " " + r.getCoords() + ".", p);
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
                if (tokens[1].equals("01")) {
                    int x = Integer.parseInt(tokens[2]);
                    int y = Integer.parseInt(tokens[3]);

                    if ( (x != u.getCoords().getX()) || (y != u.getCoords().getY()) ) {
                        this.fail(testName + "-Test " + tokens[1] + " ist fehlgeschlagen - NUMMER SCHIFF, Nummer stimmt nicht. (" + u + ", " + u.getCoords()+")");
                        retval = false;
                    }

                    Ship ship = Ship.Load(u.getSchiff());
                    if ( !ship.getNummerBase36().equals("m177") ) {
                        this.fail(testName + "-Test " + tokens[1] + " ist fehlgeschlagen - NUMMER SCHIFF, Reise gescheitert. (" + u + ", " + u.getCoords()+")");
                        retval = false;
                    }
                }

                continue;
            }

            throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");
        } // next unit

        return retval;
    }

}
