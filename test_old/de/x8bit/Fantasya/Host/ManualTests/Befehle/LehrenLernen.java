package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.EVA.Sortieren;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class LehrenLernen extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();

		Partei fremde = this.getTestWorld().createPartei(Elf.class);
		fremde.setName(getName() + "-Elfen");

        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
		r.setName(getName() + "-Region");
        getRegions().remove(r);

        {
            Unit lehrer = this.createUnit(p, r);
            lehrer.setName(this.getName()+" 01");
            lehrer.setPersonen(1);
            lehrer.setSkill(Speerkampf.class, 30);
			lehrer.setSortierung(1);

            Unit u = this.createUnit(p, r);
            u.setPersonen(5);
            u.setName(this.getName()+" 02");
            u.Befehle.add("LERNEN Speerkampf");
            u.setSortierung(2);

            lehrer.Befehle.add("LEHRE " + u.getNummerBase36() + " " + u.getNummerBase36() + " 12345 TEMP 19 -456");
			lehrer.setName(this.getName()+" 01 " + u.getNummerBase36());

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 03");
            u.Befehle.add("LERNE Maumau spielen");
			u.setSortierung(3);

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 04");
            u.Befehle.add("LERNE Tarnung");
			u.setSortierung(4);

            Partei p2 = this.getTestWorld().createPartei(Mensch.class);
			p2.setName(this.getName()+"-Magier");

			u = this.createUnit(p2, r);
            u.setName(this.getName()+" 05");
            u.Befehle.add("LERNE Magie");
			u.setSortierung(5);

            u = this.createUnit(p2, r);
			u.setPersonen(3);
            u.setName(this.getName()+" 06");
            u.Befehle.add("LERNE Magie");
			u.setSortierung(6);


			u = this.createUnit(p, r);
			u.setPersonen(1);
			u.setName(getName() + " 11");
			u.setBeschreibung("Lehrer der Fremden");
			u.setSkill(Wahrnehmung.class, Skill.LerntageFuerTW(10));
			u.setSortierung(11);

			Unit fremdeEinheit = this.createUnit(fremde, r);
			fremdeEinheit.setPersonen(10);
			fremdeEinheit.setName(getName() + " 12");
			fremdeEinheit.setBeschreibung("Fremde sollen auch ohne KONTAKT Unterricht bekommen können.");
			fremdeEinheit.Befehle.add("LERNE Wahrnehmung");
			u.setSortierung(12);

			u.Befehle.add("LEHRE " + fremdeEinheit.getNummerBase36());

			Sortieren.Normalisieren(r);

            new Info(this.getName() + " Setup in " + r + ".", u);
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04", "05", "11", "12"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
				// Lehrer
                boolean found = false;
				for (Einzelbefehl eb : u.BefehleExperimental) {
					if (eb.getBefehlCanonical().equals("LEHRE " + tokens[2])) found = true;
				}
                if (!found) retval = fail(tokens[1] + ": Der LEHRE-Befehl wurde nicht wie erwartet korrigiert.");

                messages = Message.Retrieve(p, u.getCoords(), u);
                found = false;
                for (Message msg : messages) {
                    String text = msg.getText();
                    if (text.contains(this.getName()+" 02") && text.contains("mit 150 Lehrtagen")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über geleistete Lehrstunden fehlt.");
            }

            // unit 02
            if (tokens[1].equals("02")) {
				// Schüler
				if (u.getSkill(Speerkampf.class).getLerntage() != u.getPersonen() * 60) {
					retval = fail(tokens[1] + ": Hat nicht die korrekte Anzahl Lerntage erzielt.");
				}
            }

            // unit 03
            if (tokens[1].equals("03")) {
				if (Main.getBFlag("EVA")) continue; // bei EVA wird der Befehl erst gar nicht zugelassen

				// Maumau-Spieler
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("lerne maumau") && text.contains("gültig")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung bei unbekanntem Talent fehlt.");
            }

            // unit 04
            if (tokens[1].equals("04")) {
				// Schüler
				if (u.getSkill(Tarnung.class).getLerntage() != u.getPersonen() * 30) {
					retval = fail(tokens[1] + ": Hat nicht die korrekte Anzahl Lerntage erzielt.");
				}
            }

            // unit 05
            if (tokens[1].equals("05")) {
				// lernt allein Magie
				if (u.getSkill(Magie.class).getLerntage() != u.getPersonen() * 30) {
					retval = fail(tokens[1] + ": Hat nicht korrekt Magie gelernt.");
				}
            }

            // unit 06
            if (tokens[1].equals("06")) {
				// lernt allein Magie
				if (u.getSkill(Magie.class).getLerntage() > 0) {
					retval = fail(tokens[1] + ": Hat unerlaubt Magie gelernt.");
				}
            }

            // unit 11
            if (tokens[1].equals("11")) {
				// Lehrer für fremde Partei
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("lehrt") && text.contains("mit 300")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung zur Lehrtätigkeit für fremde Partei fehlt.");
			}

            // unit 12
            if (tokens[1].equals("12")) {
				// lernt allein Magie
				if (u.getSkill(Wahrnehmung.class).getLerntage() < u.getPersonen() * 60) {
					retval = fail(tokens[1] + ": Wir können nicht so gut wahrnehmen wie erwartet.");
				}
            }


        } // next unit

        return retval;
    }

}
