package de.x8bit.Fantasya.Host.ManualTests.Misc;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 * Mantis 000226: Reiten zu zweit
 * Ich habe versehentlich einer Einhait aus 2 Personen nur ein Pfert gegeben und
 * sie 2 Regionen weit auf Reise geschickt. Sie haben sich das Pferd geteilt
 * und sind angekommen. Gut für mich , aber wohl nicht im Sinne der Regeln.
 * Einheit kann Reiten Stufe 1
 * @author hb
 */
public class BewegungMitLasten extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = null; Region r1 = null; Region start = null;

		for (Region maybe: getTestWorld().nurBetretbar(getRegions())) {
			// wir suchen zweimal Ozean im NW
			r1 = Region.Load(maybe.getCoords().shift(Richtung.Nordwesten));
			if (r1 == null) continue;
			if (!r1.istBetretbar(null)) continue;
			start = Region.Load(r1.getCoords().shift(Richtung.Nordwesten));
			if (start == null) continue;
			if (!start.istBetretbar(null)) continue;

			// gotcha!
			r = maybe;
			break;
		}
        if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
        getRegions().remove(r);
		getRegions().remove(r1);
		getRegions().remove(start);

        {
            Unit u = this.createUnit(p, start);
			u.setPersonen(2);
			u.setSkill(Reiten.class, 30 * u.getPersonen());
			u.setItem(Pferd.class, 1);
			u.setItem(Speer.class, 2);
            u.setName(this.getName()+" 01 " + r1.getCoords().getX() + " " + r1.getCoords().getY());
			u.Befehle.add("NACH so so");

            u = this.createUnit(p, start);
			u.setPersonen(2);
			u.setSkill(Reiten.class, 30 * u.getPersonen());
			u.setItem(Pferd.class, 1);
			u.setItem(Silber.class, 0);
            u.setName(this.getName()+" 02 " + r.getCoords().getX() + " " + r.getCoords().getY());
			u.Befehle.add("NACH so so");

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
					retval = fail(tokens[1] + ": Die Einheit ist nicht in der erwarteten Region.");
				}
            }

            // unit 02
            if (tokens[1].equals("02")) {
				if (!this.verifyUnitCoords(tokens, u.getCoords())) {
					retval = fail(tokens[1] + ": Die Einheit ist nicht in der erwarteten Region.");
				}
            }
			
		} // next unit

        return retval;
    }

}
