package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Units.Halbling;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class TestStirb extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p1 = this.getTestWorld().createPartei(Mensch.class);
        Partei p2 = this.getTestWorld().createPartei(Halbling.class);

        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p1, r);
            u.setName(this.getName()+" 01");
            u.Befehle.add("LERNE Wahrnehmung");

            u = this.createUnit(p1, r);
            u.setName(this.getName()+" 02");
			u.setItem(Stein.class, 2);
			u.Befehle.add("GIB 0 1 Stein"); // damit es verwaiste Meldung(en) gibt.
            u.Befehle.add("STIRB \"" + p1.getPassword() + "\"");
            u.Befehle.add("LERNE Schiffbau");

            // Beobachter von einer anderen Partei
            u = this.createUnit(p2, r);
            u.setName(this.getName()+" 03 " + p1.getNummer());
            u.Befehle.add("LERNE Tarnung");

            new Info(this.getName() + " Setup in " + r + ".", u);
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

        String missing = this.verifyExpectedUnits(units, new String[] {"03"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01 und 02
            if (tokens[1].equals("01") ||tokens[1].equals("02")) {
                retval = fail(tokens[1] + ": Die Einheit sollte eigentlich nicht mehr da sein.");
            }

            // unit 03
            if (tokens[1].equals("03")) {
                Partei gestorben = Partei.getPartei(Integer.parseInt(tokens[2]));
                if (gestorben != null) {
                    retval = fail(tokens[1] + ": Partei ist nicht gestorben.");
                }
            }
        } // next unit

        return retval;
    }

}
