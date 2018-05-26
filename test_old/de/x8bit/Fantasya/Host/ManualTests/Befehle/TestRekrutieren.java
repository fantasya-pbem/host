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
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Ork;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 *
 * @author hb
 */
public class TestRekrutieren extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
		TestWorld tw = this.getTestWorld();

		{
			Region r = null;
			for (int i=0; i<tw.nurBetretbar(getRegions()).size(); i++) {
				r = tw.nurBetretbar(getRegions()).get(i);
				if (r.Rekruten() >= 3) {
					break;
				} else {
					r = null;
				}
			}
			if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
			getRegions().remove(r);

            // Migrant - darf nicht rekrutieren:
			Unit u = this.createUnit(p, r, Ork.class.getSimpleName());
            u.setName(this.getName() + " 01");
			u.setPersonen(1);
			u.setItem(Silber.class, 1000);
			u.Befehle.add("REKRUTIERE 1");

            u = this.createUnit(p, r);
            u.setName(this.getName() + " 02");
			u.setPersonen(1);
			u.setItem(Silber.class, 1000);
			u.Befehle.add("REKRUTIERE 1");

			Partei magier = tw.createPartei(Elf.class);
			magier.setName(this.getName() + "-Magier");
			magier.setUrsprung(r.getCoords());
            u = this.createUnit(magier, r);
            u.setName(this.getName() + " 03");
			u.setPersonen(4);
			u.setSkill(Magie.class, u.getPersonen() * 180);
			u.setItem(Silber.class, 1000);
			u.Befehle.add("REKRUTIERE 1");


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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01 - der Migrant darf nicht rekrutieren:
            if (tokens[1].equals("01")) {
				if (u.getPersonen() > 1) retval = fail(tokens[1] + ": Ein Migrant hat erfolgreich Bauern rekrutiert.");
            }

            // unit 02 - hier sollte es klappen:
            if (tokens[1].equals("02")) {
				if (u.getPersonen() <= 1) retval = fail(tokens[1] + ": Einfaches Rekrutieren hat nicht geklappt.");

                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("rekrutiert 1 person")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Rekruten fehlt.");
            }

            // unit 03 - das sollte nicht klappen, es wären zu viele Magier:
            if (tokens[1].equals("03")) {
				if (u.getPersonen() > 4) retval = fail(tokens[1] + ": Magier konnten sich unerlaubt viele Rekruten holen.");

                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("viele magier")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Magier-Limit fehlt.");
            }

        } // next unit

        return retval;
    }

}
