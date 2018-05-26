package de.x8bit.Fantasya.Host.ManualTests.Misc;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Items.Myhrre;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Handel;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 * <p>Testet verschiedene Schreibweisen für das Item / Luxusgut "Myhhre",
 * und zwar bei HANDEL KAUFE / VERKAUFE und bei GIB / LIEFERE.</p>
 * @author hb
 */
public class TestMyrrheName extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Building b = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			b.setSize(50);

			Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01 Myhrre");
			u.setSkill(Handel.class, 300);
			u.Befehle.add("HANDEL VERKAUFE 50 Myhrre");
			String id1 = u.getNummerBase36();

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 02 Myrrhe");
			u.setSkill(Handel.class, 300);
			u.Befehle.add("HANDEL VERKAUFE 50 Myrrhe");
			String id2 = u.getNummerBase36();

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 03 Myrre");
			u.setSkill(Handel.class, 300);
			u.Befehle.add("HANDEL VERKAUFE 50 Myrre");
			String id3 = u.getNummerBase36();

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 04");
			u.setItem(Myhrre.class, 150);
			u.Befehle.add("GIB " + id1 + " 25 Myhrre");
			u.Befehle.add("LIEFERE " + id1 + " 25 Myhrre");
			u.Befehle.add("GIB " + id2 + " 25 Myrrhe");
			u.Befehle.add("LIEFERE " + id2 + " 25 Myrrhe");
			u.Befehle.add("GIB " + id3 + " 25 Myrre");
			u.Befehle.add("LIEFERE " + id3 + " 25 Myrre");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 05");
			String id5 = u.getNummerBase36();

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 06 falsch");
			u.setItem(Myhrre.class, 150);
			u.Befehle.add("GIB " + id5 + " 25 Myre");
			u.Befehle.add("LIEFERE " + id5 + " 25 Myre");

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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04", "05", "06"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01, 02, 03
            if (tokens[1].equals("01") || tokens[1].equals("02") || tokens[1].equals("03")) {
				if (u.getItem(Myhrre.class).getAnzahl() < 1) {
					retval = fail(tokens[1] + ": Hat keine Myrrhe.");
				}
				if (u.getItem(Myhrre.class).getAnzahl() >= 50) {
					retval = fail(tokens[1] + ": Hat keine Myrrhe verkauft.");
				}
            }

            if (tokens[1].equals("04")) {
				if (u.getItem(Myhrre.class).getAnzahl() > 0) {
					retval = fail(tokens[1] + ": Hat nicht alle Myrrhe übergeben.");
				}
            }

            if (tokens[1].equals("06")) {
				if (u.getItem(Myhrre.class).getAnzahl() == 0) {
					retval = fail(tokens[1] + ": Hat fälschlich alle \"Myre\" übergeben.");
				}
            }

        } // next unit

        return retval;
    }

}
