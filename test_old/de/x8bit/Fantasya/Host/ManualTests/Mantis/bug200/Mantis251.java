package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Host.ManualTests.*;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 * 0000251: Übergabe an Bauern
 * Es wurden keine Einheiten an die Bauern übergeben.
 * @author hb
 */
public class Mantis251 extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
			u.setPersonen(1);
            u.setName(this.getName()+" 01");

			u = this.createUnit(p, r);
			u.setPersonen(10);
            u.setName(this.getName()+" 02");
			u.Befehle.add("GIB BAUERN PERSONEN");

			u = this.createUnit(p, r);
			u.setPersonen(10);
            u.setName(this.getName()+" 03");
			u.Befehle.add("GIB BAUERN 10 PERSONEN");

			u = this.createUnit(p, r);
			u.setPersonen(10);
            u.setName(this.getName()+" 04");
			u.Befehle.add("GIB BAUERN EINHEIT");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;

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
//                messages = Message.Retrieve(p, u.getCoords(), u);
//                boolean found = false;
//                for (Message msg : messages) {
//                    String text = msg.getMessage().toLowerCase();
//                    if (text.contains("") && text.contains("")) found = true;
//                }
//                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben.");
            }

            if (tokens[1].equals("04") || tokens[1].equals("02") || tokens[1].equals("03")) {
				retval = fail(tokens[1] + ": Einheit sollte eigentlich aufgelöst worden sein.");
			}

        } // next unit

        return retval;
    }

}
