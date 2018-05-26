package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Wagen;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import java.util.Collection;

/**
 * 0000243: Falsche Berechnung der Transportkapazität im NR
 * Im NR ist mir eine eigenartige Angabe der Transportkapazitäten aufgefallen.
 * Stimmt das nur im NR nicht, oder ist da auch intern (also zur tatsächlichen
 * Berechnung der Kapazitäten) etwas falsch.
 *
 * Beispiele:
 * [1yp], 1 Aquaner, Kapazität: 47 frei, hat: 2 Pferde.
 * Hat kein Reittalent müsste also total überladen sein.
 *
 * [70d], 1 Aquaner, Talente: Reiten 2 Kapazität: 167 frei, hat: 1 Wagen und 2 Pferde.
 * Selbst wenn die eine Person zu Fuß geht, sind da nur 147GE frei!
 *
 * [5dk], 1 Aquaner, Talente: Reiten 1 Kapazität: 26 frei, hat: 1 Pferd und 1 Speer.
 * Hier stimmt es, weil man davon ausgehen kann/muss, dass er zu Fuß geht.
 *
 * Bitte checken wie das nun WIRKLICH ist (ich fürchte das
 * Transportkapazitätenausrechnungsproblem ist imer noch nicht ganz gelöst.)
 * 
 * @author hb
 */
public class Mantis243 extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
            u.setItem(Silber.class, 10);
            u.setItem(Pferd.class, 2);
            u.setName(this.getName()+" 01");

            u = this.createUnit(p, r);
            u.setItem(Silber.class, 10);
            u.setItem(Pferd.class, 2);
            u.setSkill(Reiten.class, 90);
            u.setName(this.getName()+" 02");

            u = this.createUnit(p, r);
            u.setItem(Silber.class, 10);
            u.setItem(Pferd.class, 2);
            u.setItem(Wagen.class, 1);
            u.setName(this.getName()+" 03");

            u = this.createUnit(p, r);
            u.setItem(Silber.class, 10);
            u.setItem(Pferd.class, 2);
            u.setItem(Wagen.class, 1);
            u.setSkill(Reiten.class, 30);
            u.setName(this.getName()+" 04");

            u = this.createUnit(p, r);
            u.setItem(Silber.class, 10);
            u.setItem(Pferd.class, 2);
            u.setItem(Wagen.class, 1);
            u.setSkill(Reiten.class, 90);
            u.setName(this.getName()+" 05");

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
