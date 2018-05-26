package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class RekrutiereMitBewachen extends TestBase {

    @Override
    protected void mySetupTest() {
		TestWorld tw = this.getTestWorld();

        Partei p = tw.getSpieler1();

        { // Alliierte - das muss funktionieren
			Region r = null;
			for (int i=0; i<tw.nurBetretbar(getRegions()).size(); i++) {
				r = tw.nurBetretbar(getRegions()).get(i);
				if (r.Rekruten() > 0) {
					break;
				} else {
					r = null;
				}
			}
			getRegions().remove(r);
			
			{
				Unit u = this.createUnit(p, r);
				u.setName(this.getName() + " 01");
				u.setPersonen(1);
				u.Befehle.add("REKRUTIERE 1");
			}

			{
				Partei freunde = tw.createPartei(Mensch.class);
				freunde.setName(this.getName()+"-Freunde");
				freunde.setAllianz(p.getNummer(), AllianzOption.Kontaktiere, true);

				Unit bewacher = this.createUnit(freunde, r);
				bewacher.setName(this.getName()+" 02");
				bewacher.setPersonen(10);
				bewacher.setSkill(Hiebwaffen.class, 300 * bewacher.getPersonen());
				bewacher.setItem(Schwert.class, bewacher.getPersonen());
				bewacher.setBewacht(true);
			}

            new TestMsg(this.getName() + "-Freunde Setup in " + r + ".");
        }

        { // Fremde - das darf nicht funktionieren
			Region r = null;
			for (int i=0; i<tw.nurBetretbar(getRegions()).size(); i++) {
				r = tw.nurBetretbar(getRegions()).get(i);
				if (r.Rekruten() > 0) {
					break;
				} else {
					r = null;
				}
			}
			getRegions().remove(r);

			{
				Unit u = this.createUnit(p, r);
				u.setName(this.getName() + " 11");
				u.setPersonen(1);
				u.Befehle.add("REKRUTIERE 1");
			}

			{
				Partei fremde = tw.createPartei(Mensch.class);
				fremde.setName(this.getName()+"-Fremde");
				fremde.setAllianz(p.getNummer(), AllianzOption.Kontaktiere, false);

				Unit bewacher = this.createUnit(fremde, r);
				bewacher.setName(this.getName()+" 12");
				bewacher.setPersonen(10);
				bewacher.setSkill(Hiebwaffen.class, 300 * bewacher.getPersonen());
				bewacher.setItem(Schwert.class, bewacher.getPersonen());
				bewacher.setBewacht(true);
			}

            new TestMsg(this.getName() + "-Fremde Setup in " + r + ".");
        }

        { // Alliierte - das muss funktionieren
			Region r = null;
			for (int i=0; i<tw.nurBetretbar(getRegions()).size(); i++) {
				r = tw.nurBetretbar(getRegions()).get(i);
				if (r.Rekruten() > 0) {
					break;
				} else {
					r = null;
				}
			}
			getRegions().remove(r);

			String rekrutiererId = null;
			{
				Unit u = this.createUnit(p, r);
				u.setName(this.getName() + " 21");
				u.setPersonen(1);
				u.Befehle.add("REKRUTIERE 1");

				rekrutiererId = u.getNummerBase36();
			}

			{
				Partei kontaktierer = tw.createPartei(Mensch.class);
				kontaktierer.setName(this.getName()+"-Kontaktierer");
				kontaktierer.setAllianz(p.getNummer(), AllianzOption.Kontaktiere, false);

				Unit bewacher = this.createUnit(kontaktierer, r);
				bewacher.setName(this.getName()+" 22");
				bewacher.setPersonen(10);
				bewacher.setSkill(Hiebwaffen.class, 300 * bewacher.getPersonen());
				bewacher.setItem(Schwert.class, bewacher.getPersonen());
				bewacher.setBewacht(true);
				bewacher.Befehle.add("KONTAKTIERE " + rekrutiererId);
			}

            new TestMsg(this.getName() + "-Kontaktierer Setup in " + r + ".");
        }


    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
        Partei p = this.getTestWorld().getSpieler1();
        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "11", "12", "21", "22"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
				if (u.getPersonen() != 2) {
					retval = fail(tokens[1] + ": Nicht wie erwartet 1 Person rekrutiert.");
				}
            }

            // unit 11
            if (tokens[1].equals("11")) {
				if (u.getPersonen() != 1) {
					retval = fail(tokens[1] + ": Hat unerwartet erfolgreich rekrutiert.");
				}

                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("kann niemanden rekrutieren")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über verhindertes Rekrutieren fehlt.");
            }

            // unit 21
            if (tokens[1].equals("21")) {
				if (u.getPersonen() != 2) {
					retval = fail(tokens[1] + ": Nicht wie erwartet 1 Person rekrutiert.");
				}
            }

        } // next unit

        return retval;
    }

}
