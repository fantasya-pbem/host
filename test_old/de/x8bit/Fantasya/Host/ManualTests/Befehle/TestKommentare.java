package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class TestKommentare extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
		r.setName(getName()+" R");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
			u.Befehle.add("// permanenter Kommentar");
			u.Befehle.add("lerne ausdauer");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 02");
			u.Befehle.add("; einmaliger Kommentar");
			u.Befehle.add("lerne ausdauer");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 03");
			u.Befehle.add("lerne ausdauer //Inline-Kommentar");


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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                boolean found = false;
				for (String befehl : u.Befehle) {
					if (befehl.equals("// permanenter Kommentar")) {
						found = true; break;
					}
				}
                if (!found) retval = fail(tokens[1] + ": Permanenter Kommentar nicht mehr vorhanden oder verändert.");
            }

            // unit 02
            if (tokens[1].equals("02")) {
                boolean found = false;
				for (String befehl : u.Befehle) {
					if (befehl.toLowerCase().contains("einmaliger")) {
						found = true; break;
					}
				}
                if (found) retval = fail(tokens[1] + ": Einmaliger Kommentar noch vorhanden.");
            }

            // unit 03
            if (tokens[1].equals("03")) {
                boolean found = false;
				for (String befehl : u.Befehle) {
					befehl = befehl.toLowerCase();
					if (befehl.startsWith("lerne") && befehl.contains("//") && befehl.contains("inline")) {
						found = true; break;
					}
				}
                if (!found) retval = fail(tokens[1] + ": Inline-Kommentar nicht mehr vorhanden oder verändert.");
            }

        } // next unit

        return retval;
    }

}
