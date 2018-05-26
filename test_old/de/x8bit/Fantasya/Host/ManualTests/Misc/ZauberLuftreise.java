package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Atlantis.Coords;
import java.util.List;

import de.x8bit.Fantasya.Host.ManualTests.*;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Spells.Luftreise;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class ZauberLuftreise extends TestBase {

    @Override
    protected void mySetupTest() {
//        Partei p = this.getTestWorld().getSpieler1();
        Region r = null; Region ziel = null;
        
        for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
            Coords c = maybe.getCoords();
            Coords z = new Coords(c.getX() + 5, c.getY() - 2, c.getWelt());
            Region maybeZiel = Region.Load(z);

            if (maybeZiel.istBetretbar(null)) {
                // gotcha!
                r = maybe; ziel = maybeZiel;
            }
        }
        if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
        getRegions().remove(r);
        getRegions().remove(ziel);

        {
			Partei p2 = this.getTestWorld().createPartei(Mensch.class);
			p2.setName("Z-Luftreise");

			Unit magier = this.createMage(p2, r, 6);
			magier.setName(this.getName() + " 01 " + ziel.getCoords().getX() + " " + ziel.getCoords().getY());
			magier.setSpell(new Luftreise());
			magier.Befehle.add("ZAUBERE \"Luftreise\" +5 -2");

            Unit u = this.createUnit(p2, r);
            u.setName(this.getName() + " 02");

            u = this.createUnit(p2, ziel);
            u.setName(this.getName() + " 03");

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
                if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                    retval = fail(tokens[1] + ": Luftreise hat nicht funktioniert.");
                }

                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("braust") && text.contains("durch die l")) found = true; // l...üfte
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Luftreise fehlt.");
            }

        } // next unit

        return retval;
    }

}
