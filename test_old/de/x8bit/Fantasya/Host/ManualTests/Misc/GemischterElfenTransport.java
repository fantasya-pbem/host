package de.x8bit.Fantasya.Host.ManualTests.Misc;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Gewuerz;
import de.x8bit.Fantasya.Atlantis.Items.Kamel;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Items.Wagen;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class GemischterElfenTransport extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().createPartei(Elf.class);
		p.setName(getName() + "-Partei");

        Region r = null;
		Region r2 = null;
        for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
            r2 = Region.Load(maybe.getCoords().shift(Richtung.Osten));
            if (r2.istBetretbar(null)) {
                r = maybe; break;
            }
        }
        if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
        getRegions().remove(r);
		getRegions().remove(r2);

        {
			Coords target = r.getCoords().shift(Richtung.Osten);

			Unit u = this.createUnit(p, r);
            u.setName(this.getName() + " 01 " + target.getX() + " " + target.getY() );
			u.setPersonen(10);
			u.setSkill(Reiten.class, u.getPersonen() * 90);
			u.setItem(Silber.class, 0);

			u.setItem(Pferd.class, 6);
			u.setItem(Kamel.class, 14);

			u.setItem(Wagen.class, 1);

			u.setItem(Stein.class, 9);
			u.setItem(Gewuerz.class, 299);

			u.Befehle.add("NACH o");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
//        Partei p = this.getTestWorld().getSpieler1();
//        List<Message> messages = null;

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
					retval = fail(tokens[1] + ": Einheit ist nicht gereist.");
				}

                /*
				messages = Message.Retrieve(Partei.Load(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getMessage().toLowerCase();
                    if (text.contains("") && text.contains("")) found = true;
                }
                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben.");
				*/
            }
        } // next unit

        return retval;
    }

}
