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
import de.x8bit.Fantasya.Atlantis.Skills.Handel;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class TestBestaetigt extends TestBase {

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
			u.Befehle.add("LERNE Wahrnehmung");
			u.Befehle.add("@REKRUTIERE 1");
			u.Befehle.add("// dies ist ein Kommentar, der bleibt.");
			u.Befehle.add("; und die ist ein Kommentar, der verschwindet.");
			u.Befehle.add("BESTÄTIGT BIS 10");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 02");
			u.Befehle.add("LERNE Hiebwaffen");
			u.Befehle.add("BESTÄTIGT BIS 1");

            u = this.createUnit(p, r);
			String schueler1 = u.getNummerBase36();
            u.setName(this.getName()+" 03");
			u.Befehle.add("LERNE Handel T5");

            u = this.createUnit(p, r);
			String schueler2 = u.getNummerBase36();
            u.setName(this.getName()+" 04");
			u.Befehle.add("LERNE Handel T1");
			
            u = this.createUnit(p, r);
            u.setName(this.getName()+" 05");
			u.setPersonen(1);
			u.setSkill(Handel.class, Skill.LerntageFuerTW(6));
			u.Befehle.add("LEHRE " + schueler1 + " " + schueler2);
			u.setBeschreibung("Erwartet: Nicht bestätigt, weil [" + schueler2 + "] ausgelernt hat.");
			
            u = this.createUnit(p, r);
            u.setName(this.getName()+" 06");
			u.setPersonen(1);
			u.setSkill(Handel.class, Skill.LerntageFuerTW(6));
			u.Befehle.add("LEHRE " + schueler1);
			u.setBeschreibung("Erwartet: Bestätigt, weil alle Schüler [" + schueler1 + "] bestätigt sind.");

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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04", "05", "06"});
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
				boolean found = false;
				for (String befehl : u.Befehle) {
					if (befehl.equalsIgnoreCase(";bestaetigt")) { found = true; break; }
				}
				if (!found) retval = fail(uRef + "(BESTÄTIGE BIS 10) ist nicht bestätigt worden.");
			}
				
            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("ist abgelaufen") && text.contains("bis")) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung über abgelaufene Bestätigung fehlt.");
				
				found = false;
				for (String befehl : u.Befehle) {
					if (befehl.equalsIgnoreCase(";bestaetigt")) { found = true; break; }
				}
				if (found) retval = fail(uRef + "ist ungewollt bestätigt worden.");
            }
			
            // unit 03
            if (tokens[1].equals("06")) {
				boolean found = false;
				for (String befehl : u.Befehle) {
					if (befehl.equalsIgnoreCase(";bestaetigt")) { found = true; break; }
				}
				if (!found) retval = fail(uRef + "Dauerlerner (T5) ist nicht bestätigt worden.");
			}
			
            // unit 04
            if (tokens[1].equals("04")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("erreicht talentwert 1 in handel")) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung über erreichtes Lernziel fehlt.");
				
				found = false;
				for (String befehl : u.Befehle) {
					if (befehl.equalsIgnoreCase(";bestaetigt")) { found = true; break; }
				}
				if (found) retval = fail(uRef + "ist ungewollt bestätigt worden.");
            }
			
            // unit 05 - Lehrer I
            if (tokens[1].equals("05")) {
				boolean found = false;
				for (String befehl : u.Befehle) {
					if (befehl.equalsIgnoreCase(";bestaetigt")) { found = true; break; }
				}
				if (found) retval = fail(uRef + "Lehrer ist ungewollt bestätigt worden.");
			}
				
            // unit 06 - Lehrer II
            if (tokens[1].equals("06")) {
				boolean found = false;
				for (String befehl : u.Befehle) {
					if (befehl.equalsIgnoreCase(";bestaetigt")) { found = true; break; }
				}
				if (!found) retval = fail(uRef + "Lehrer ist nicht bestätigt worden.");
			}
        } // next unit

        return retval;
    }

}
