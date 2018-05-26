package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import java.util.List;

import de.x8bit.Fantasya.Host.ManualTests.*;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Spells.KlauenDerTiefe;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class ZauberKlauen extends TestBase {

    @Override
    protected void mySetupTest() {
//        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
			Partei p2 = this.getTestWorld().createPartei(Mensch.class);
			p2.setName("Z-Klauen der Tiefe");

			Unit magier = this.createMage(p2, r, 6);
			magier.setName(this.getName() + " 01 " + magier.getAura());
			magier.setSpell(new KlauenDerTiefe());
			magier.Befehle.add("ZAUBERE \"Klauen der Tiefe\" 4");

            new Info(this.getName() + " Setup in " + r + ".", magier, magier.getCoords());
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
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("besitzt nun") && text.contains("eisen")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über neues Eisen fehlt.");

                if (u.getItem(Eisen.class).getAnzahl() <= 0) {
                    retval = fail(tokens[1] + ": Hat nix bekommen.");
                }
            }

        } // next unit

        return retval;
    }

}
