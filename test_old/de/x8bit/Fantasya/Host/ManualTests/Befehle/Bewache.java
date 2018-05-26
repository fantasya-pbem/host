package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class Bewache extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();

        {
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
			getRegions().remove(r);
			
            Building burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			burg.setSize(10);

			Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
			u.setPersonen(10);
			u.setSkill(Hiebwaffen.class, 3000);
			u.setItem(Schwert.class, 10);
			u.Enter(burg);
			u.Befehle.add("BEWACHE NICHT");
			u.Befehle.add("BEWACHE");
			u.Befehle.add("NACH nw no o so sw w");

	
			u = this.createUnit(p, r);
            u.setName(this.getName()+" 02");
			u.setPersonen(10);
			u.setSkill(Hiebwaffen.class, 3000);
			u.setItem(Schwert.class, 10);
			u.setBewacht(true);
			u.Befehle.add("BEWACHE NICHT");
			u.Befehle.add("LERNE Hiebwaffen");


			u = this.createUnit(p, r);
            u.setName(this.getName()+" 03");
			u.setPersonen(10);
			u.setSkill(Hiebwaffen.class, 3000);
			u.setItem(Schwert.class, 10);
			u.Befehle.add("BEWACHE");
			u.Befehle.add("LERNE Hiebwaffen");


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
                messages = Message.Retrieve(p, (Coords)null, u); // Die Koordinaten können sich durch NACH geändert haben!
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("bewacht") && text.contains("nicht")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über BEWACHE NICHT fehlt.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("bewacht") && (!text.contains("nicht"))) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über BEWACHE fehlt.");
			}

            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(p, (Coords)null, u); // Die Koordinaten können sich durch NACH geändert haben!
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("bewacht") && text.contains("nicht")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über BEWACHE NICHT fehlt.");

				if (u.getBewacht()) retval = fail(tokens[1] + ": Die Einheit bewacht immer noch.");
			}

            // unit 03
            if (tokens[1].equals("03")) {
                messages = Message.Retrieve(p, (Coords)null, u); // Die Koordinaten können sich durch NACH geändert haben!
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("bewacht") && !text.contains("nicht")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über BEWACHE fehlt.");

				if (!u.getBewacht()) retval = fail(tokens[1] + ": Die Einheit bewacht unerwartet nicht.");
			}

        } // next unit

        return retval;
    }

}
