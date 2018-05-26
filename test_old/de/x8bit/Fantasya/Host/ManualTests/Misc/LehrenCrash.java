package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Ausdauer;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 * Versuch, den Crash in der AW 117 nachzustellen - zu viele Lehr-Tage ?!?
 * @author hb
 */
public class LehrenCrash extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
            u.setPersonen(6);
            u.setSkill(Ausdauer.class, -1 * Math.abs(188 * u.getPersonen()));
            u.setName(this.getName()+" 01 Schüler");
            u.Befehle.add("LERNE Ausdauer");
            String schueler1 = u.getNummerBase36();

            u = this.createUnit(p, r);
            u.setPersonen(9);
            u.setSkill(Ausdauer.class, Math.abs(82 * u.getPersonen()));
            u.setName(this.getName()+" 02 Schüler");
            u.Befehle.add("LERNE Ausdauer");
            String schueler2 = u.getNummerBase36();

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 03");
            u.setSkill(Ausdauer.class, 1650);
            u.Befehle.add("LEHRE " + schueler1 + " " + schueler2);

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 04");
            u.setPersonen(12);
            u.setSkill(Ausdauer.class, 1650 * u.getPersonen());
            u.Befehle.add("LEHRE " + schueler1 + " " + schueler2);

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 05");
            u.setPersonen(-12);
            u.setSkill(Ausdauer.class, Math.abs(1650 * u.getPersonen()));
            u.Befehle.add("LEHRE " + schueler1 + " " + schueler2);

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 06");
            u.setPersonen(1);
            u.setSkill(Ausdauer.class, 1650 * u.getPersonen());
            u.Befehle.add("LEHRE " + schueler1 + " " + schueler2);


            u = this.createUnit(p, r);
            u.setPersonen(14);
            // u.setSkill(Ausdauer.class, Math.abs(82 * u.getPersonen()));
            u.setName(this.getName()+" 11 Schüler");
            u.Befehle.add("LERNE Ausdauer");
            // u.Befehle.add("LEHRE " + u.Base36());
            String schueler11 = u.getNummerBase36();

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 12");
            u.setPersonen(1);
            u.setSkill(Ausdauer.class, 1650 * u.getPersonen());
            u.Befehle.add("LEHRE " + schueler11 + " " + schueler11 + " " + u.getNummerBase36());

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 13");
            u.setPersonen(1);
            u.setSkill(Ausdauer.class, 1650 * u.getPersonen());
            u.Befehle.add("LEHRE " + schueler11);

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
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("") && text.contains("")) found = true;
                }
                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben.");
            }
        } // next unit

        return retval;
    }

}
