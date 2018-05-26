package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Steuereintreiben;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class TestTreiben extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = null; 
		
		for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
			if (maybe.getSilber() > 1000) {
				r = maybe;
				break;
			}
		}
        if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
			u.setPersonen(10000);
			u.setSkill(Steuereintreiben.class, 1650 * u.getPersonen());
			u.setSkill(Hiebwaffen.class, 30 * u.getPersonen());
			u.setItem(Silber.class, u.getPersonen() * 10);
			u.setItem(Schwert.class, u.getPersonen());
			u.Befehle.add("TREIBE");

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
                    if (text.contains("kann nur") && text.contains("statt 2000000 silber")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über eingeschränktes Steuereintreiben fehlt.");

				if (u.getItem(Silber.class).getAnzahl() == 0) {
					retval = fail(tokens[1] + ": Kein Silber verdient.");
				}

				if (u.getItem(Silber.class).getAnzahl() >= 2000000) {
					retval = fail(tokens[1] + ": Zu viel Silber verdient.");
				}
            }
        } // next unit

        return retval;
    }

}
