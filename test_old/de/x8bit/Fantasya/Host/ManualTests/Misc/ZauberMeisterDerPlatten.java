package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Atlantis.Buildings.Sattlerei;
import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Ruestungsbau;
import de.x8bit.Fantasya.Atlantis.Spells.MeisterDerPlatten;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class ZauberMeisterDerPlatten extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
			Unit u = this.createSpecialist(p, r, Sattlerei.class.getSimpleName(), true);
            u.setName(this.getName() + " 01 " + u.getName());
			u.setPersonen(u.getPersonen() * 2);
			u.setSkill(Ruestungsbau.class, u.getSkill(Ruestungsbau.class).getLerntage() * 2);

			Partei p2 = this.getTestWorld().createPartei(Mensch.class);
			p2.setName("Z-MeisterDerPlatten");

			Unit magier = this.createMage(p2, r, 6);
			magier.setName(this.getName() + " 02");
			magier.setSpell(new MeisterDerPlatten());
			magier.Befehle.add("ZAUBERE \"Meister der Platten\" " + u.getNummerBase36());

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
                    if (text.contains("durch seine erfahrung") && text.contains("1 kettenhemden mehr")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über magische Hilfe fehlt.");

				if (u.getItem(Kettenhemd.class).getAnzahl() != 10) {
					retval = fail(tokens[1] + ": Einheit hat nicht korrekt produziert.");
				}
            }

            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("zaubert") && text.contains("stufe 1") && text.contains("01")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Zauber fehlt.");
            }


        } // next unit

        return retval;
    }

}
