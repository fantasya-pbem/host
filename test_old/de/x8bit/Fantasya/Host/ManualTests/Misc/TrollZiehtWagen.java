package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Items.Wagen;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Units.Troll;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class TrollZiehtWagen extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p2 = this.getTestWorld().createPartei(Troll.class);
        p2.setName(this.getName()+"-Volk");
        
        Region r = null;
        for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
            Region r2 = Region.Load(maybe.getCoords().shift(Richtung.Osten));
            if (r2.istBetretbar(null)) {
                r = maybe;
                break;
            }
        }
        if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p2, r);
            u.setName(this.getName()+" 01");
            u.setPersonen(20);
            u.setItem(Wagen.class, 10);
            u.setItem(Stein.class, 23);
            u.Befehle.add("NACH o");

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
                    if (text.contains("von") && text.contains("nach")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Keine Reise / keine Meldung.");
            }
        } // next unit

        return retval;
    }

}
