package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Unterhaltung;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class TestVergessen extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Partei p = tw.createPartei(Mensch.class);
		p.setName(getName()+"-Partei");

        Region r = tw.nurBetretbar(getRegions()).get(0);
		r.setName(getName()+"-Region");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
			u.setPersonen(7);
			u.setSkill(Unterhaltung.class, Skill.LerntageFuerTW(3) * u.getPersonen());
			u.Befehle.add("VERGESSEN Unterhaltung //ja, vergessen!");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 02");
			u.setPersonen(9);
			u.setSkill(Unterhaltung.class, Skill.LerntageFuerTW(3) * u.getPersonen());
			u.Befehle.add("VERGESSEN 120 Unterhaltung //ja, vergessen!");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 03");
			u.setPersonen(3);
			u.setSkill(Unterhaltung.class, Skill.LerntageFuerTW(3) * u.getPersonen());
			u.Befehle.add("VERGESSEN Tarnung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 04");
			u.setPersonen(9);
			u.setSkill(Unterhaltung.class, Skill.LerntageFuerTW(3) * u.getPersonen());
			u.Befehle.add("VERGESSEN 450 Unterhaltung //ja, vergessen!");

            new Info(this.getName() + " Setup in " + r + ".", r);
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04"});
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
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("vergessen 180") && text.contains("unterhaltung")) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung über Talent-Vergessen fehlt.");
				
				if (u.getSkill(Unterhaltung.class).getLerntage() > 0) {
					retval = fail(uRef + "hat das Unterhaltungstalent nicht vergessen.");
				}
            }
			
            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("vergessen 120") && text.contains("unterhaltung")) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung über Talent-Vergessen fehlt.");
				
				int lerntageProPerson = Math.round((float)u.getSkill(Unterhaltung.class).getLerntage() / (float)u.getPersonen());
				if ( lerntageProPerson != 60) retval = fail(uRef + "hat nicht die erwarteten Lerntage in Unterhaltung (60).");
            }
			
            // unit 03
            if (tokens[1].equals("03")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("haben gar keine") && text.contains("tarnung")) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung nicht vorhandenes Talent fehlt.");
            }
			
            // unit 04
            if (tokens[1].equals("04")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("vergessen 180") && text.contains("unterhaltung")) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung über Talent-Vergessen fehlt.");
				
				if (u.getSkill(Unterhaltung.class).getLerntage() > 0) {
					retval = fail(uRef + "hat das Unterhaltungstalent nicht vergessen.");
				}
            }
			
        } // next unit

        return retval;
    }

}
