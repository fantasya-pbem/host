package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Skills.Unterhaltung;
import de.x8bit.Fantasya.Atlantis.Units.Halbling;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 * 0000242: Einheit hungert trotz Unterhaltung
 * Meine Einheit [4ov] steht als einzige Einheit meines Volkes in einer Region
 * und unterhält. Diese AW hatte sie die Fehlermeldung "Missionar des
 * Flammenden Auges [4ov] kann nur 55 statt 60 Silber mit Unterhalt verdienen".
 * Ist ja OK. Aber dann noch: "Missionar des Flammenden Auges [4ov] hungert in
 * 'Berge Oreqagegikec'."
 * Wie kann das sein, dass sie 55 Silber verdient und trotzdem hungert? Wie
 * gesagt, sonst steht da niemand von mir, der das Silber hätte verfuttern
 * können.
 * 
 * @author hb
 */
public class Mantis242 extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();

        {
            Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
            getRegions().remove(r);
            
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
            u.setSkill(Unterhaltung.class, 180);
            u.setItem(Silber.class, 0);
            u.Befehle.add("UNTERHALTEN");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

        {
            Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
            getRegions().remove(r);

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 02");
            u.setSkill(Unterhaltung.class, 180);
            u.setItem(Silber.class, 0);
            u.Befehle.add("UNTERHALTEN");
            String targetId = u.getNummerBase36();


            Partei p2 = this.getTestWorld().createPartei(Halbling.class);
            p2.setName(this.getName()+"-Diebe");

            u = this.createUnit(p2, r);
            u.setName(this.getName()+" 03");
            u.setSkill(Tarnung.class, 180);
            u.setItem(Silber.class, 0);
            u.Befehle.add("TARNE EINHEIT");
            u.Befehle.add("BEKLAUE " + targetId);

            new Info(this.getName() + "-Diebstahl Setup in " + r + ".", u, u.getCoords());
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
