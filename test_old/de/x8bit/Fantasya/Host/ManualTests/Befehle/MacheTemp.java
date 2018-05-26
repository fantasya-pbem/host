package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenbau;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class MacheTemp extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = null;
		for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
			if (maybe.getBauern() > 100) {
				// gotcha!
				r = maybe;
			}
		}
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
			u.Befehle.add("MACHE TEMP 1");
			u.Befehle.add("BENENNE Einheit " + this.getName() + " 02");
			u.Befehle.add("REKRUTIERE 1");
			u.Befehle.add("LERNE Bogenschiessen");
			u.Befehle.add("ENDE");
			u.Befehle.add("GIB TEMP 1 200 Silber");

            // TEMP-Einheit ohne Rekruten:
			u = this.createUnit(p, r);
            u.setName(this.getName()+" 03");
			u.Befehle.add("MACHE TEMP 2");
			u.Befehle.add("BENENNE Einheit " + this.getName() + " 04");
			u.Befehle.add("LERNE Bogenschiessen");
			u.Befehle.add("ENDE");
			u.Befehle.add("GIB TEMP 2 200 Silber");

            // TEMP-Einheit ohne Rekruten:
			u = this.createUnit(p, r);
			u.setPersonen(2);
            u.setName(this.getName()+" 05");
			u.Befehle.add("MACHE TEMP 3");
			u.Befehle.add("BENENNE Einheit " + this.getName() + " 06");
			u.Befehle.add("LERNE Bogenbau");
			u.Befehle.add("ENDE");
			u.Befehle.add("GIB TEMP 3 1 Personen");

            // Fieser (?) Syntaxfehler:
			u = this.createUnit(p, r);
			u.setPersonen(2);
            u.setName(this.getName()+" 07");
			u.Befehle.add("MACHE TEMP");
			u.Befehle.add("BENENNE Einheit " + this.getName() + " 08");
			u.Befehle.add("REKRUTIERE 1");
			u.Befehle.add("LERNE Katapultbedienung");
			u.Befehle.add("ENDE");

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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "05", "06", "08"});
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
                    if (text.contains("bergibt 200 silber") && text.contains("machetemp 02")) found = true; // bergibt = übergibt
                }
                if (!found) retval = fail(tokens[1] + ": Keine Meldung zur Silber-Gabe an TEMP-Einheit.");
            }

            // unit 02
            if (tokens[1].equals("02")) {
				if (u.getSkill(Bogenschiessen.class).getLerntage() != 30) {
					retval = fail(tokens[1] + ": Lernen hat nicht geklappt.");
				}
            }

			// unit 03
            if (tokens[1].equals("03")) {
				messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("bergibt 200 silber") && text.contains("machetemp 04")) found = true; // bergibt = übergibt
                }
                if (!found) retval = fail(tokens[1] + ": Keine Meldung zur Silber-Gabe an TEMP-Einheit.");

				// das kann bei jeder Einheit landen - ohne Angabe der Unit testen:
				messages = Message.Retrieve(p, u.getCoords(), null);
                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("erbt") && text.contains("machetemp 04")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Keine Meldung über Erbe von leerer TEMP-Einheit.");
            }

            // unit 04 und 07
			if (tokens[1].equals("04") ||tokens[1].equals("07")) {
				retval = fail(tokens[1] + ": Die Einheit sollte nicht mehr existieren.");
			}

			// unit 05
			if (tokens[1].equals("05")) {
				messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("gibt 1 person") && text.contains("machetemp 06")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Keine Meldung zur Personen-Gabe an TEMP-Einheit.");
			}

            // unit 06
            if (tokens[1].equals("06")) {
				if (u.getSkill(Bogenbau.class).getLerntage() != 30) {
					retval = fail(tokens[1] + ": Lernen hat nicht geklappt.");
				}
            }

		} // next unit

        return retval;
    }

}
