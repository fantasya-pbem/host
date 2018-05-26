package de.x8bit.Fantasya.Host.ManualTests.Misc;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Halbling;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class MagierRekrutieren extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurTerrain(getRegions(), Ebene.class).get(0);
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
			u.setSkill(Magie.class, 1800);
			u.Befehle.add("REKRUTIERE 9");
			u.Befehle.add("LERNE Wahrnehmung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 02");
			u.setSkill(Magie.class, 1800);
			u.Befehle.add("REKRUTIERE 1");
			u.Befehle.add("LERNE Wahrnehmung");

            Partei p2 = this.getTestWorld().createPartei(Elf.class);
            
            u = this.createUnit(p2, r);
            u.setPersonen(4);
            u.setName(this.getName()+" 03");
			u.setSkill(Magie.class, 1800);
			u.Befehle.add("LERNE Wahrnehmung");
            
            u = this.createUnit(p2, r);
            u.setName(this.getName()+" 04");
            u.Befehle.add("REKRUTIERE 9");
			u.Befehle.add("LERNE Wahrnehmung");

            Partei p3 = this.getTestWorld().createPartei(Halbling.class);

            u = this.createUnit(p3, r);
            u.setPersonen(4);
            u.setName(this.getName()+" 05");
			u.setSkill(Magie.class, 1800);
			u.Befehle.add("LERNE Wahrnehmung");

            u = this.createUnit(p3, r);
            u.setName(this.getName()+" 06");
            u.Befehle.add("REKRUTIERE 9");
			u.Befehle.add("LERNE Wahrnehmung");


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

            // unit 01
            if (tokens[1].equals("01")) {
				if (u.getPersonen() > 1) {
					retval = fail(tokens[1] + ": Zuviele Magier rekrutiert.");
				}
            }

			// unit 02
            if (tokens[1].equals("02")) {
				if (u.getPersonen() < 2) {
					retval = fail(tokens[1] + ": Magier rekrutieren hat nicht geklappt.");
				}
            }

			// unit 04
            if (tokens[1].equals("04")) {
				if (u.getPersonen() == 1) {
					retval = fail(tokens[1] + ": Normales Rekrutieren bei erlaubter Magier-Anzahl hat nicht geklappt.");
				}
            }

			// unit 06
            if (tokens[1].equals("06")) {
				if (u.getPersonen() < 10) {
					retval = fail(tokens[1] + ": Normales Rekrutieren bei zuvielen Magiern hat nicht geklappt.");
				}
            }
            
        } // next unit

        return retval;
    }

}
