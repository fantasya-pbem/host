package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Skills.Unterhaltung;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class UnterhaltungsTest extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurTerrain(getRegions(), Ebene.class).get(0);
        getRegions().remove(r);
        r.setName(getName() + "-Region I");

        {
            Unit u = this.createUnit(p, r);
            u.setPersonen(1000);
            u.setSkill(Unterhaltung.class, u.getPersonen() * 450);
            u.setItem(Silber.class, 10 * u.getPersonen());
            u.setName(this.getName()+" 01");
            u.Befehle.add("UNTERHALTEN");

            r = this.getTestWorld().nurTerrain(getRegions(), Ebene.class).get(0);
            getRegions().remove(r);
            r.setName(getName() + "-Region II");

            u = this.createUnit(p, r);
            u.setPersonen(1);
            u.setSkill(Unterhaltung.class, u.getPersonen() * 300); // T4
            u.setItem(Silber.class, 10 * u.getPersonen());
            u.setName(this.getName()+" 02");
            u.Befehle.add("UNTERHALTEN 17");

            u = this.createUnit(p, r);
            u.setPersonen(1);
            u.setSkill(Unterhaltung.class, u.getPersonen() * 300); // T4
            u.setItem(Silber.class, 10 * u.getPersonen());
            u.setName(this.getName()+" 03");
            u.Befehle.add("unterhalte 1000000000");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
        Partei p = this.getTestWorld().getSpieler1();
        List<Message> messages = null;

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
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("kann nur") && text.contains("statt") && text.contains("verdienen")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Unterhaltung fehlt.");

                if (u.getItem(Silber.class).getAnzahl() <= 0) {
                    retval = fail(tokens[1] + ": Nichts verdient mit Unterhaltung.");
                }
            }

            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("verdient 17")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Unterhaltung fehlt.");

                if (u.getItem(Silber.class).getAnzahl() <= 0) {
                    retval = fail(tokens[1] + ": Nichts verdient mit Unterhaltung.");
                }
            }

            // unit 03
            if (tokens[1].equals("03")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("verdient 80")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Unterhaltung fehlt.");

                if (u.getItem(Silber.class).getAnzahl() <= 0) {
                    retval = fail(tokens[1] + ": Nichts verdient mit Unterhaltung.");
                }
            }

        } // next unit

        return retval;
    }

}
