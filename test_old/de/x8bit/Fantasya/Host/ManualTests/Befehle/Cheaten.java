package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Units.Echse;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class Cheaten extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().createPartei(Echse.class);
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        p.setCheats(2);

        {
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
            u.Befehle.add("SET PERSONEN 100");
            u.Befehle.add("LERNE Tarnung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 02");
            u.Befehle.add("SET ITEM Schwert 100");
            u.Befehle.add("LERNE Tarnung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 03");
            u.Befehle.add("SET TALENT Magie 1650");
            u.Befehle.add("LERNE Tarnung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 04 " + this.getTestWorld().getSpieler1().getNummerBase36());
            u.Befehle.add("SET PARTEI " + this.getTestWorld().getSpieler1().getNummerBase36());
            u.Befehle.add("LERNE Tarnung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 05");
            u.Befehle.add("SET SHIP Galeone");
            u.Befehle.add("LERNE Tarnung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 06");
            u.Befehle.add("SET BUILDING Werft");
            u.Befehle.add("LERNE Tarnung");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
//        Partei p = this.getTestWorld().getSpieler1();

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04", "05", "06"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            if (tokens[1].equals("01")) {
                if (u.getPersonen() != 100) retval = fail(tokens[1] + ": SET PERSONEN fehlgeschlagen.");
            }

            if (tokens[1].equals("02")) {
                if (u.getItem(Schwert.class).getAnzahl() != 100) retval = fail(tokens[1] + ": SET ITEM fehlgeschlagen.");
            }

            if (tokens[1].equals("03")) {
                if (u.getSkill(Magie.class).getLerntage() != 1650) retval = fail(tokens[1] + ": SET SKILL fehlgeschlagen.");
            }

            if (tokens[1].equals("04")) {
                if (!("" + u.getOwner()).equals(tokens[2])) retval = fail(tokens[1] + ": SET PARTEI fehlgeschlagen.");
            }

            if (tokens[1].equals("05")) {
                if (u.getSchiff() == 0) retval = fail(tokens[1] + ": SET SHIP fehlgeschlagen.");
            }

            if (tokens[1].equals("06")) {
                if (u.getGebaeude() == 0) retval = fail(tokens[1] + ": SET BUILDING fehlgeschlagen.");
            }

        } // next unit

        return retval;
    }

}
