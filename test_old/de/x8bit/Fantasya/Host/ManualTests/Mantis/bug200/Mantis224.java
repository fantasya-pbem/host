package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Werkstatt;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Wagen;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Wagenbau;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import java.util.Collection;

/**
 * 0000224: Werkstatt funktioniert trotz mehr Personen als Größenpunkte
 * ... oder die Sparmeldung "Einheitenname [##] spart 115 von 230 sonst
 * benötigten Holz." stimmt nicht.
 * Die Werkstatt ist 5 groß, die darinsitzende Einheit 10. Die darinsitzende
 * Einheit war allerdings mal auch nur 5 groß und hat irgendwann 5 Personen
 * dazubekommen, glaube ich.
 * @author hb
 */
public class Mantis224 extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = null;
        
        for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
            if (maybe.getBauern() > 200) {
                r = maybe;
                break;
            }
        }
        getRegions().remove(r);


        {
            Unit u = this.createSpecialist(p, r, Werkstatt.class.getSimpleName(), true);
            u.setPersonen(10);
            u.setName(this.getName()+" 01");
            u.setSkill(Wagenbau.class, 180 * u.getPersonen());
            u.setItem(Silber.class, 10000);
            u.setItem(Holz.class, 100);
            u.Befehle.add("REKRUTIERE 10");
            u.Befehle.add("MACHE Wagen");

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
                if (u.getSkill(Wagenbau.class).getLerntage() != 90 * u.getPersonen()) { // sollten 90 Tage pro Kopf haben und aus der Werkstatt geflogen sein (?)
                    fail(tokens[1] + ": Wagenbau-Lerntage sind nicht wie erwartet.");
                }

                if (u.getItem(Wagen.class).getAnzahl() != 26) {
                    fail(tokens[1] + ": Es sind nicht 26, sondern " + u.getItem(Wagen.class).getAnzahl() + " Wagen produziert worden.");
                }

                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("spart 32") && text.contains("von 130")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Spar-Meldung ist nicht wie erwartet.");
            }
        } // next unit

        return retval;
    }

}
