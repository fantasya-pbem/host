package de.x8bit.Fantasya.Host.ManualTests.Misc;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Kriegselefant;
import de.x8bit.Fantasya.Atlantis.Items.Kriegsmastodon;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;
import de.x8bit.Fantasya.util.Codierung;

/**
 *
 * @author hb
 */
public class KriegselefantenVerhungern extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Partei p1 = tw.createPartei(Mensch.class);
		p1.setName(getName()+"-Partei 1");

		Partei p2 = tw.createPartei(Mensch.class);
		p2.setName(getName()+"-Partei 2");

		Partei p3 = tw.createPartei(Mensch.class);
		p3.setName(getName()+"-Partei 3");

        Region r = tw.nurBetretbar(getRegions()).get(0);
		r.setName(getName()+"-Region");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p1, r);
			u.setItem(Silber.class, 0 * u.getPersonen());
			u.setItem(Kriegselefant.class, u.getPersonen());
            u.setName(this.getName()+" 01 Elefantenkrieger");

            u = this.createUnit(p2, r);
			u.setItem(Silber.class, 0 * u.getPersonen());
			u.setItem(Kriegsmastodon.class, u.getPersonen());
            u.setName(this.getName()+" 02 Mastodonkrieger");

            u = this.createUnit(p3, r);
			u.setPersonen(100);
			u.setItem(Silber.class, (15 * u.getPersonen()) / 2);
			u.setItem(Kriegselefant.class, u.getPersonen());
            u.setName(this.getName()+" 03 Viele Elefantenkrieger");

            new Info(this.getName() + " Setup in " + r + ".", r);
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
//        List<Message> messages = null;

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
			String uRef = tokens[1] + "[" + u.getNummerBase36() + "] von [" + Codierung.toBase36(u.getOwner()) + "] - ";

            // unit 01
            if (tokens[1].equals("01")) {
				if (u.getItem(Kriegselefant.class).getAnzahl() > 0) {
					retval = fail(uRef + " hat noch " + u.getItem(Kriegselefant.class).getAnzahl() + " Kriegselefant(en).");
				}
            }

            // unit 02
            if (tokens[1].equals("02")) {
				if (u.getItem(Kriegsmastodon.class).getAnzahl() > 0) {
					retval = fail(uRef + " hat noch " + u.getItem(Kriegsmastodon.class).getAnzahl() + " Kriegsmastodon(ten).");
				}
            }
			
            // unit 03
            if (tokens[1].equals("03")) {
				int kes = u.getItem(Kriegselefant.class).getAnzahl();
				if ((kes < 25) || (kes > 75)) {
					retval = fail(uRef + " ist nicht im erwarteten Bereich überlebender Kriegselefanten: (25 < (x=" + kes + ") < 75).");
				}
            }
			
		} // next unit

        return retval;
    }

}
