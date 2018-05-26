package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Skills.Strassenbau;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class MacheStrasse extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();

        {
            Region r = this.getTestWorld().nurTerrain(getRegions(), Wald.class).get(0);
            getRegions().remove(r);
            
            Unit u = this.createUnit(p, r);

            u.setName(this.getName()+" 01");
            u.setPersonen(10);
            u.setSkill(Strassenbau.class, 450 * u.getPersonen());
            u.setItem(Stein.class, 60);
            u.Befehle.add("MACHE Strasse SO");

            u = this.createUnit(p, r);

            u.setName(this.getName()+" 02");
            u.setPersonen(10);
            u.setSkill(Strassenbau.class, 300 * u.getPersonen());
            u.setItem(Stein.class, 40);
            u.Befehle.add("MACHE Strasse NW");

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
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("strasse") && text.contains("50 steine")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Straßenbau fehlt.");

                if (u.getItem(Stein.class).getAnzahl() != 10) {
                    retval = fail(tokens[1] + ": Unerwartete Restmenge an Steinen.");
                }

                if (Region.Load(u.getCoords()).getStrassensteine(Richtung.Suedosten) != 50) {
                    retval = fail(tokens[1] + ": Straße nicht gebaut wie erwartet.");
                }
            }

            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("verbaut") && text.contains("40 steine")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Straßenbau fehlt.");

                if (u.getItem(Stein.class).getAnzahl() != 0) {
                    retval = fail(tokens[1] + ": Unerwartete Restmenge an Steinen.");
                }

                if (Region.Load(u.getCoords()).getStrassensteine(Richtung.Nordwesten) != 40) {
                    retval = fail(tokens[1] + ": Straße nicht gebaut wie erwartet.");
                }
            }

        } // next unit

        return retval;
    }

}
