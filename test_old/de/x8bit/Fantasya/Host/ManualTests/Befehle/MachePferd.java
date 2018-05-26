package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.Collection;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Pegasus;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Pferdedressur;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class MachePferd extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().createPartei(Mensch.class);
		p.setName(this.getName() + "-Menschen");
		
        Region r = null;
        
        for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
            if (maybe.getResource(Pferd.class).getAnzahl() > 10) {
                r = maybe;
                break;
            }
        }
        if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
        getRegions().remove(r);

        {
            Region.Load(r.getCoords()).setResource(Pferd.class, 10000);

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
            u.setPersonen(1000);
            u.setSkill(Pferdedressur.class, 1650 * u.getPersonen());
            u.setItem(Pferd.class, 0);
            u.Befehle.add("MACHE Pferd");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

        {
            r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
            Region.Load(r.getCoords()).setResource(Pferd.class, 10000);
			getRegions().remove(r);

            Partei p2 = this.getTestWorld().createPartei(Mensch.class);
            p2.setName(this.getName() + "-Trickser");

            Unit u = this.createUnit(p2, r);
            u.setName(this.getName()+" 02");
            u.setPersonen(1000);
            u.setSkill(Pferdedressur.class, 1650 * u.getPersonen());
            u.setItem(Pferd.class, 100);
            u.Befehle.add("GIB BAUERN 100 Pferd");
            u.Befehle.add("MACHE Pferd");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
//        Partei p = this.getTestWorld().getSpieler1();
        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("pegas") && text.contains("darunter")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Pegasi fehlt.");

                if (u.getItem(Pegasus.class).getAnzahl() == 0) {
                    retval = fail(tokens[1] + ": Keine Pegasi gefangen.");
                }
            }

            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("pegas") && text.contains("darunter")) found = true;
                }
                if (found) retval = fail(tokens[1] + ": Meldung über Pegasi vorhanden.");

                if (u.getItem(Pferd.class).getAnzahl() != 10000) {
                    retval = fail(tokens[1] + ": Pferdefang stimmt nicht.");
                }
            }
        } // next unit

        return retval;
    }

}
