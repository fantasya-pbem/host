package de.x8bit.Fantasya.Host.ManualTests.Misc;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Atlantis.Ships.Langboot;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class LangbootUndSteg extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = null; Region start = null;

		for (Region maybe: getTestWorld().nurBetretbar(getRegions())) {
			if (maybe.getClass() == Ebene.class) continue;
			if (maybe.getClass() == Wald.class) continue;
			// wir suchen zweimal Ozean im NW
			Region r1 = Region.Load(maybe.getCoords().shift(Richtung.Nordwesten));
			if (r1 == null) continue;
			if (!(r1 instanceof Ozean)) continue;
			Region r2 = Region.Load(r1.getCoords().shift(Richtung.Nordwesten));
			if (r2 == null) continue;
			if (!(r2 instanceof Ozean)) continue;

			// gotcha!
			r = maybe; start = r2;
			break;
		}
        if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
        getRegions().remove(r);
		getRegions().remove(start);

        {
            // Segler...
			Unit u = this.createKapitaen(p, start, Langboot.class.getSimpleName());
            u.setName(this.getName()+" 01 " + r.getCoords().getX() + " " + r.getCoords().getY());
			u.Befehle.add("NACH so so");

			//... und Stegbauer.
			// zuerst brauchen wir eine große Burg:
			Building burg = Building.Create(new Burg().getName(), r.getCoords());
			burg.setSize(500);

			u = this.createUnit(p, r);
            u.setName(this.getName()+" 02");
			u.setPersonen(5);
			u.setSkill(Burgenbau.class, 450 * u.getPersonen());
			u.setItem(Stein.class, 50);
			u.setItem(Eisen.class, 50);
			u.setItem(Holz.class, 50);
			u.setItem(Silber.class, 10000);
			u.Befehle.add("MACHE Steg");

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
                    retval = fail(tokens[1] + ": Langboot ist nicht in der erwarteten Region.");
                }
            }
        } // next unit

        return retval;
    }

}
