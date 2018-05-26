package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Schmiede;
import de.x8bit.Fantasya.Atlantis.Items.Bogen;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenbau;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 * Schmiede spart kein Holz beim Bogenbau
 * @author hb
 */
public class Mantis225 extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit u = this.createSpecialist(p, r, Schmiede.class.getSimpleName(), true);
			u.setPersonen(10);
			u.setSkill(Bogenbau.class, 90 * u.getPersonen());
			u.setItem(Holz.class, 10);
			u.setItem(Eisen.class, 0);
            u.setName(this.getName()+" 01");
			u.Befehle.clear();
			u.Befehle.add("MACHE Bogen");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
//        Partei p = this.getTestWorld().getSpieler1();
//        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
				if (u.getItem(Bogen.class).getAnzahl() != 10) {
					retval = fail(tokens[1] + ": Unerwartete Anzahl Bögen produziert: " + u.getItem(Bogen.class).getAnzahl());
				}
				if (u.getItem(Holz.class).getAnzahl() != 5) {
					retval = fail(tokens[1] + ": Unerwartete Anzahl Holz verbraucht: " + u.getItem(Holz.class).getAnzahl());
				}

            }
        } // next unit

        return retval;
    }

}
