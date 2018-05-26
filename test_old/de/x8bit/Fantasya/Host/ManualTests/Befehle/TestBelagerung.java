package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Items.Katapult;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Katapultbedienung;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class TestBelagerung extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Partei p2 = this.getTestWorld().createPartei(Mensch.class);
            p2.setName(this.getName() + "-Burgherren");

            Building burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
            burg.setSize(500);
            
            Unit u = this.createUnit(p2, r);
            u.setName(this.getName()+" 01");
            u.setPersonen(500);
            u.Enter(burg);


            u = this.createUnit(p, r);
            u.setName(this.getName()+" 02");
            u.setPersonen(100);
            u.setSkill(Katapultbedienung.class, 300 * u.getPersonen());
            u.setItem(Katapult.class, u.getPersonen());
            u.Befehle.add("BELAGERE " + burg.getNummerBase36());

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
                    if (text.contains("wird") && text.contains("belagert")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung 'belagert' fehlt.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("digt burg")) found = true; // beschädigt
                }
                if (!found) retval = fail(tokens[1] + ": Meldung 'beschädigt' fehlt.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("erh") && text.contains("stein")) found = true; // erhält
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über erhaltene Steine fehlt.");

                if (u.getItem(Stein.class).getAnzahl() < 1) {
                    retval = fail(tokens[1] + ": Keine Steine erhalten.");
                }
            }

            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("belagert burg")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung 'belagert' fehlt.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("digt burg")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung 'beschädigt' fehlt.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("erh") && text.contains("stein")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über erhaltene Steine fehlt.");

                if (u.getItem(Stein.class).getAnzahl() < 1) {
                    retval = fail(tokens[1] + ": Keine Steine erhalten.");
                }
            }



        } // next unit

        return retval;
    }

}
