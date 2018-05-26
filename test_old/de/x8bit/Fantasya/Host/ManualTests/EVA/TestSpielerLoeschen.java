package de.x8bit.Fantasya.Host.ManualTests.EVA;


import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class TestSpielerLoeschen extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = null;
		for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
			if (maybe.getBauern() > 100) {
				// gotcha!
				r = maybe;
			}
		}
        getRegions().remove(r);

        {
            // TEMP-Einheit ohne Rekruten:
			Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
			u.Befehle.add("MACHE TEMP 1");
			u.Befehle.add("BENENNE Einheit " + this.getName() + " 02");
			u.Befehle.add("LERNE Bogenschiessen");
			u.Befehle.add("ENDE");
			u.Befehle.add("GIB TEMP 1 200 Silber");

            // Normale Einheit ohne Personen
			u = this.createUnit(p, r);
			u.setPersonen(1);
            u.setName(this.getName()+" 03");
			u.Befehle.add("MACHE TEMP 2");
			u.Befehle.add("BENENNE Einheit " + this.getName() + " 04");
			u.Befehle.add("LERNE Bogenbau");
			u.Befehle.add("ENDE");
			u.Befehle.add("GIB TEMP 2 1 Personen");

            // Sich lehrende Partei:
            Partei leer = this.getTestWorld().createPartei(Elf.class);
            u = this.createUnit(leer, r);
			u.setPersonen(1);
            u.setName(this.getName() + " 05");
            u.Befehle.add("GIB BAUERN 1 PERSON");


            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
        Partei p = this.getTestWorld().getSpieler1();

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "04"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }

        Partei leer = Partei.getPartei(p.getNummer() + 1);
        if (leer != null) {
            retval = fail("Die leere Partei ist noch vorhanden.");
        }

        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 02, 03, 05 - sollten nicht vorhanden sein
            if (tokens[1].equals("02") ||tokens[1].equals("03") || tokens[1].equals("05")) {
				retval = fail(tokens[1] + ": Die Einheit sollte nicht mehr da sein.");
            }


		} // next unit

        return retval;
    }

}
