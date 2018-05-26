package de.x8bit.Fantasya.Host.ManualTests.Misc;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Units.Troll;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class TrollRitt extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p2 = this.getTestWorld().createPartei(Troll.class);
        p2.setName(this.getName()+"-Volk");
        Region r = null; Region ost = null; Region ostost = null;

		for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
			ost = Region.Load(maybe.getCoords().shift(Richtung.Osten));
			ostost = Region.Load(ost.getCoords().shift(Richtung.Osten));
			if (ost.istBetretbar(null) && ostost.istBetretbar(null)) {
				r = maybe;
				break;
			}
		}
        if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
        getRegions().remove(r);
		getRegions().remove(ost);
		getRegions().remove(ostost);

        {
            Unit u = this.createUnit(p2, r);
            u.setName(this.getName()+" 01 " + ost.getCoords().getX() + " " + ost.getCoords().getY());
			u.setSkill(Reiten.class, 300);
			u.setItem(Silber.class, 0);
			u.setItem(Pferd.class, 1);
			u.Befehle.add("NACH o o");

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
				if (!verifyUnitCoords(tokens, u.getCoords())) {
					retval = fail(tokens[1] + ": Nicht in der erwarteten Region angekommen.");
				}
            }
        } // next unit

        return retval;
    }

}
