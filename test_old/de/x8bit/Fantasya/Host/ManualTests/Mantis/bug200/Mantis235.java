package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Units.Halbling;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class Mantis235 extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Building burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			burg.setSize(100);

			Unit u1 = this.createUnit(p, r);
            u1.setName(this.getName()+" 01");
			u1.Enter(burg);

			Partei p2 = this.getTestWorld().createPartei(Halbling.class);
            Unit u2 = this.createUnit(p2, r);
            u2.setName(this.getName()+" 02");

            u1.Befehle.add("GIB " + u2.getNummerBase36() + " 100 Silber");
			u1.Befehle.add("HELFE " + p2.getNummerBase36() + " GIB");
			u1.Befehle.add("LERNE Wahrnehmung");

            u2.Befehle.add("GIB " + u1.getNummerBase36() + " 100 Silber");
			u2.Befehle.add("LERNE Wahrnehmung");

            Unit u3 = this.createUnit(p2, r);
            u3.setName(this.getName()+" 03");
			u3.Befehle.add("BETRETE GEBAEUDE " + burg.getNummerBase36());
			u3.Befehle.add("LERNE Wahrnehmung");
			
            new Info(this.getName() + " Setup in " + r + ".", u1, u1.getCoords());
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
				if (u.getItem(Silber.class).getAnzahl() != 1090) {
					retval = fail(tokens[1] + ": Da stimmt was mit HELFE GIB nicht.");
				}

//                messages = Message.Retrieve(p, u.getCoords(), u);
//                boolean found = false;
//                for (Message msg : messages) {
//                    String text = msg.getMessage().toLowerCase();
//                    if (text.contains("") && text.contains("")) found = true;
//                }
//                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben.");
            }

            // unit 03
            if (tokens[1].equals("03")) {
				if (u.getGebaeude() != 0) {
					retval = fail(tokens[1] + ": Wurde unerlaubt in die Burg gelassen.");
				}
            }

        } // next unit

        return retval;
    }

}
