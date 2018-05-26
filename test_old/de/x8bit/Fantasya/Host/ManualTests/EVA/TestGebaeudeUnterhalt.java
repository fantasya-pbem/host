package de.x8bit.Fantasya.Host.ManualTests.EVA;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Schmiede;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class TestGebaeudeUnterhalt extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();

        { // allein, gar kein Silber:
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
			getRegions().remove(r);
			
            Unit u = this.createSpecialist(p, r, Schmiede.class.getSimpleName(), true);
			u.setPersonen(1);
			u.setItem(Silber.class, 0);
            u.setName(this.getName()+" 01");

			Building b = Building.getBuilding(u.getGebaeude());
			b.setFunktion(false); // das sollte dazu führen, dass nächste Runde Verfall eintritt

            new Info(this.getName() + "-01 Setup in " + r + ".", u, u.getCoords());
        }

        { // Silber reicht nur mit Einsammeln (Besitzer hat aber auch ne Kleinigkeit).
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
			getRegions().remove(r);

            Unit u = this.createSpecialist(p, r, Schmiede.class.getSimpleName(), true);
			u.setPersonen(1);
			u.setItem(Silber.class, 25); // 15 brauche ich für mich!
            u.setName(this.getName()+" 02");

			Building b = Building.getBuilding(u.getGebaeude());
			b.setFunktion(false); // das sollte (theoretisch) dazu führen, dass nächste Runde Verfall eintritt

			// unser Kumpel leiht uns aber was - machst du doch, oder?
			u = this.createUnit(p, r);
            u.setName(this.getName()+" 12");
			u.setItem(Silber.class, 110);

            new Info(this.getName() + "-02 Setup in " + r + ".", u, u.getCoords());
        }

        {
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
			getRegions().remove(r);

            Unit u = this.createSpecialist(p, r, Schmiede.class.getSimpleName(), true);
			u.setPersonen(1);
			u.setItem(Silber.class, 500); // wir habens dicke.
            u.setName(this.getName()+" 03");

			Building b = Building.getBuilding(u.getGebaeude());
			b.setFunktion(false); // das sollte dazu führen, dass nächste Runde Verfall eintritt

            new Info(this.getName() + "-03 Setup in " + r + ".", u, u.getCoords());
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "12", "03"});
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
                    if (text.contains("nur 0 von 100") && text.contains("unterhalt")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über fehlendes Silber - fehlt.");

				Building b = Building.getBuilding(u.getGebaeude());
				if ((b == null) || (b.getSize() >= 10)) {
					 retval = fail(tokens[1] + ": Gebäude ist nicht verfallen.");
				}
            }

            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("sammelt") && text.contains("75")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über gesammeltes Silber fehlt.");

				Building b = Building.getBuilding(u.getGebaeude());
				if ((b == null) || (b.getSize() != 10)) {
					 retval = fail(tokens[1] + ": Gebäude ist verfallen.");
				}
            }

            // unit 12
            if (tokens[1].equals("12")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("borgt") && text.contains("75 silber")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über gespendetes Silber fehlt.");

				if (u.getItem(Silber.class).getAnzahl() != 10) {
					 retval = fail(tokens[1] + ": Silber-Bilanz ist nicht wie erwartet.");
				}
            }

            // unit 03
            if (tokens[1].equals("03")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("zahlt 100 silber unterhalt")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Unterhalt fehlt.");

				Building b = Building.getBuilding(u.getGebaeude());
				if ((b == null) || (b.getSize() != 10)) {
					 retval = fail(tokens[1] + ": Gebäude ist verfallen.");
				}
            }

        } // next unit

        return retval;
    }

}
