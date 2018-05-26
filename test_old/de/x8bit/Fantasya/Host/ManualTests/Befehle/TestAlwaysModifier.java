package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.Collection;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class TestAlwaysModifier extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
		r.setName(getName()+" R");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
			u.Befehle.add("LERNE Ausdauer");
			String a = u.getNummerBase36();

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 02");
			u.Befehle.add("GIB " + a + " 1000 Silber");
			u.Befehle.add("@GIB " + a + " 2000 Silber");
			u.Befehle.add("@NUMMER EINHEIT");
			u.Befehle.add("LERNE Ausdauer");
			u.setItem(Silber.class, 10000);

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

            // unit 02
            if (tokens[1].equals("02")) {
                boolean found = false;
				for (String befehl : u.Befehle) {
					befehl = befehl.toLowerCase();
					if (befehl.startsWith("gib")) {
						found = true; break;
					}
				}
                if (found) retval = fail(tokens[1] + ": Normaler GIB-Befehl ist noch vorhanden.");

                found = false;
				for (String befehl : u.Befehle) {
					befehl = befehl.toLowerCase();
					if (befehl.startsWith("@gib")) {
						found = true; break;
					}
				}
                if (!found) retval = fail(tokens[1] + ": @GIB-Befehl ist nicht mehr vorhanden.");

				messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("gibt") && text.contains("2000")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Ausführung von @GIB fehlt.");
            }
        } // next unit

        return retval;
    }

}
