package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Spells.GuterWind;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class ZauberGuterWind extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = null; Region ziel = null;

        for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
            Region r1 = Region.Load(maybe.getCoords().shift(Richtung.Nordosten));
            if (!(r1 instanceof Ozean)) continue;
            Region r2 = Region.Load(r1.getCoords().shift(Richtung.Nordosten));
            if (!(r2 instanceof Ozean)) continue;
            Region r3 = Region.Load(r2.getCoords().shift(Richtung.Nordosten));
            if (!(r3 instanceof Ozean)) continue;
            Region r4 = Region.Load(r3.getCoords().shift(Richtung.Nordosten));
            if (!(r4 instanceof Ozean)) continue;

            // gotcha!
            r = maybe;
            ziel = r3;
        }
        getRegions().remove(r);
        getRegions().remove(ziel);

        {
			Unit kapitaen = this.createKapitaen(p, r, "Boot");
			kapitaen.setName(this.getName() + " 01 " + ziel.getCoords().getX() + " " + ziel.getCoords().getY());
			kapitaen.Befehle.add("NACH no no no no");

			Partei p2 = this.getTestWorld().createPartei(Mensch.class);
			p2.setName("Z-Guter Wind");

			Unit magier = this.createMage(p2, r, 6);
			magier.setName(this.getName() + " 02");
			magier.setSpell(new GuterWind());
			magier.Befehle.add("ZAUBERE \"Guter Wind\" " + kapitaen.getNummerBase36());

            new Info(this.getName() + " Setup in " + r + ".", kapitaen, kapitaen.getCoords());
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

            // unit 01
            if (tokens[1].equals("01")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                    retval = fail(tokens[1] + ": Ist nicht korrekt gerudert.");
                }
            }

            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("zaubert") && text.contains("guter wind") && text.contains("01")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Zauber fehlt.");
            }


        } // next unit

        return retval;
    }

}
