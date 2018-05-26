package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Spells.Erdbeben;
import de.x8bit.Fantasya.Atlantis.Spells.Feuerball;
import de.x8bit.Fantasya.Atlantis.Spells.Feuerwalze;
import de.x8bit.Fantasya.Atlantis.Spells.KleinesErdbeben;
import de.x8bit.Fantasya.Atlantis.Spells.Sturm;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.Kampfzauber;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.util.Codierung;

/**
 *
 * @author hb
 */
public class KampfzauberSetzen extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().createPartei(Mensch.class);
        p.setName(getName() + "-Partei 1");

        {
            Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
            getRegions().remove(r);
            r.setName(getName() + "-Region");

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new Erdbeben());
            u.Befehle.add("KAMPFZAUBER VERWIRRUNG \"Erdbeben\" 1");
            u.Befehle.add("LERNE Unterhaltung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 02");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new Feuerwalze());
            u.Befehle.add("KAMPFZAUBER ANGRIFF Feuerwalze 1234");
            u.Befehle.add("LERNE Unterhaltung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 03");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new Feuerwalze());
            u.Befehle.add("KAMPFZAUBER ANGRIFF Feuerwalze");
            u.Befehle.add("LERNE Unterhaltung");

            p = this.getTestWorld().createPartei(Mensch.class);
            p.setName(getName() + "-Partei 2");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 04");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new Feuerwalze());
            u.Befehle.add("KAMPFZAUBER ANGRIFF Feuerwalze 0");
            u.Befehle.add("LERNE Unterhaltung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 05");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new KleinesErdbeben());
            u.Befehle.add("KAMPFZAUBER VERWIRRUNG Kleines Erdbeben");
            u.Befehle.add("LERNE Unterhaltung");

            // "worst case" - Zauber mit mehreren Namensteilen und Kommentar:
            u = this.createUnit(p, r);
            u.setName(this.getName()+" 06");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new KleinesErdbeben());
            u.Befehle.add("KAMPFZAUBER VERWIRRUNG Kleines Erdbeben 2 // das ist ein Kommentar");
            u.Befehle.add("LERNE Unterhaltung");

            p = this.getTestWorld().createPartei(Mensch.class);
            p.setName(getName() + "-Partei 3");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 07");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new KleinesErdbeben());
            u.Befehle.add("KAMPFZAUBER VERWIRRUNG Steinschlag");
            u.Befehle.add("LERNE Unterhaltung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 08");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new KleinesErdbeben());
            u.Befehle.add("KAMPFZAUBER VERWIRRUNG Steinschlag 1");
            u.Befehle.add("LERNE Unterhaltung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 09");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new Feuerball());
            u.Befehle.add("KAMPFZAUBER ANGRIFF Feuerball");
            u.Befehle.add("LERNE Unterhaltung");

            p = this.getTestWorld().createPartei(Mensch.class);
            p.setName(getName() + "-Partei 4");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 10");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new Feuerball());
            u.Befehle.add("KAMPFZAUBER ANGRIFF Feuerball 2");
            u.Befehle.add("LERNE Unterhaltung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 11");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new Sturm());
            u.Befehle.add("KAMPFZAUBER VERWIRRUNG Sturm");
            u.Befehle.add("LERNE Unterhaltung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 12");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new Sturm());
            u.Befehle.add("KAMPFZAUBER VERWIRRUNG Sturm 2");
            u.Befehle.add("LERNE Unterhaltung");

            p = this.getTestWorld().createPartei(Mensch.class);
            p.setName(getName() + "-Partei 5");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 13");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new Erdbeben());
            u.Befehle.add("KAMPFZAUBER VERWIRRUNG Erdbeben");
            u.Befehle.add("LERNE Unterhaltung");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 14");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new Erdbeben());
            u.Befehle.add("KAMPFZAUBER VERWIRRUNG Erdbeben 2");
            u.Befehle.add("LERNE Unterhaltung");


            u = this.createUnit(p, r);
            u.setName(this.getName()+" 15");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new Feuerwalze());
            u.Befehle.add("KAMPFZAUBER ANGRIFF Feuerwalze");
            u.Befehle.add("LERNE Unterhaltung");

            p = this.getTestWorld().createPartei(Mensch.class);
            p.setName(getName() + "-Partei 6");

            u = this.createUnit(p, r);
            u.setName(this.getName()+" 16");
            u.setSkill(Magie.class, 1650);
            u.setSpell(new Feuerwalze());
            u.Befehle.add("KAMPFZAUBER ANGRIFF Feuerwalze 2");
            u.Befehle.add("LERNE Unterhaltung");



            new Info(this.getName() + " Setup in " + r + ".", u);
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
//        Partei p = this.getTestWorld().getSpieler1();

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(
                units, 
                new String[] {
                    "01", "02", "03", "04", 
                    "05", "06", "07", "08", 
                    "09", "10", "11", "12", 
                    "13", "14", "15", "16"
                });
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
                if (!u.getStringProperty(Kampfzauber.CONFUSIONSPELL, "").equalsIgnoreCase("erdbeben 1")) {
                    retval = this.fail(uRef + "hat den Verwirrungszauber (Erdbeben 1) nicht gesetzt.");
                }
            }

            // unit 02
            if (tokens[1].equals("02")) {
                // sollte keinen aktiven Kampfzauber haben, weil die Syntax (Stufe größer als 99) falsch ist:
                if (u.hasProperty(Kampfzauber.ATTACKSPELL)) {
                    retval = this.fail(uRef + "hat wider Erwarten einen Angriffszauber gesetzt: " + u.getStringProperty(Kampfzauber.ATTACKSPELL));
                }
            }

            // unit 03
            if (tokens[1].equals("03")) {
                // sollte keinen aktiven Kampfzauber haben, weil die Syntax (ohne Stufenangabe) falsch ist:
                if (u.hasProperty(Kampfzauber.ATTACKSPELL)) {
                    retval = this.fail(uRef + "hat wider Erwarten einen Angriffszauber gesetzt: " + u.getStringProperty(Kampfzauber.ATTACKSPELL));
                }
            }

            // unit 04
            if (tokens[1].equals("04")) {
                // sollte keinen aktiven Kampfzauber haben, weil die Syntax (Stufe 0) falsch ist:
                if (u.hasProperty(Kampfzauber.ATTACKSPELL)) {
                    retval = this.fail(uRef + "hat wider Erwarten einen Angriffszauber gesetzt: " + u.getStringProperty(Kampfzauber.ATTACKSPELL));
                }
            }

            // Kleines Erdbeben:
            if (tokens[1].equals("05")) {
                // sollte keinen aktiven Kampfzauber haben, weil die Syntax (ohne Stufenangabe) falsch ist:
                if (u.hasProperty(Kampfzauber.CONFUSIONSPELL)) {
                    retval = this.fail(uRef + "hat wider Erwarten einen Verwirrungszauber gesetzt: " + u.getStringProperty(Kampfzauber.CONFUSIONSPELL));
                }
            }
            if (tokens[1].equals("06")) {
                if (!u.getStringProperty(Kampfzauber.CONFUSIONSPELL, "").equalsIgnoreCase("kleines erdbeben 2")) {
                    retval = this.fail(uRef + "hat den Verwirrungszauber (Kleines Erdbeben 2) nicht gesetzt.");
                }
            }

            // Steinschlag:
            if (tokens[1].equals("07")) {
                // sollte keinen aktiven Kampfzauber haben, weil die Syntax (ohne Stufenangabe) falsch ist:
                if (u.hasProperty(Kampfzauber.CONFUSIONSPELL)) {
                    retval = this.fail(uRef + "hat wider Erwarten einen Verwirrungszauber gesetzt: " + u.getStringProperty(Kampfzauber.CONFUSIONSPELL));
                }
            }
            if (tokens[1].equals("08")) {
                if (!u.getStringProperty(Kampfzauber.CONFUSIONSPELL, "").equalsIgnoreCase("steinschlag 1")) {
                    retval = this.fail(uRef + "hat den Verwirrungszauber (steinschlag 1) nicht gesetzt.");
                }
            }

            // Feuerball:
            if (tokens[1].equals("09")) {
                // sollte keinen aktiven Kampfzauber haben, weil die Syntax (ohne Stufenangabe) falsch ist:
                if (u.hasProperty(Kampfzauber.ATTACKSPELL)) {
                    retval = this.fail(uRef + "hat wider Erwarten einen Angriffszauber gesetzt: " + u.getStringProperty(Kampfzauber.ATTACKSPELL));
                }
            }
            if (tokens[1].equals("10")) {
                if (!u.getStringProperty(Kampfzauber.ATTACKSPELL, "").equalsIgnoreCase("feuerball 2")) {
                    retval = this.fail(uRef + "hat den Angriffszauber (Feuerball 2) nicht gesetzt.");
                }
            }

            // Sturm:
            if (tokens[1].equals("11")) {
                // sollte keinen aktiven Kampfzauber haben, weil die Syntax (ohne Stufenangabe) falsch ist:
                if (u.hasProperty(Kampfzauber.CONFUSIONSPELL)) {
                    retval = this.fail(uRef + "hat wider Erwarten einen Verwirrungszauber gesetzt: " + u.getStringProperty(Kampfzauber.CONFUSIONSPELL));
                }
            }
            if (tokens[1].equals("12")) {
                if (!u.getStringProperty(Kampfzauber.CONFUSIONSPELL, "").equalsIgnoreCase("sturm 2")) {
                    retval = this.fail(uRef + "hat den Verwirrungszauber (Sturm 2) nicht gesetzt.");
                }
            }

            // Erdbeben:
            if (tokens[1].equals("13")) {
                // sollte keinen aktiven Kampfzauber haben, weil die Syntax (ohne Stufenangabe) falsch ist:
                if (u.hasProperty(Kampfzauber.CONFUSIONSPELL)) {
                    retval = this.fail(uRef + "hat wider Erwarten einen Verwirrungszauber gesetzt: " + u.getStringProperty(Kampfzauber.CONFUSIONSPELL));
                }
            }
            if (tokens[1].equals("14")) {
                if (!u.getStringProperty(Kampfzauber.CONFUSIONSPELL, "").equalsIgnoreCase("erdbeben 2")) {
                    retval = this.fail(uRef + "hat den Verwirrungszauber (Erdbeben 2) nicht gesetzt.");
                }
            }

            // Feuerwalze:
            if (tokens[1].equals("15")) {
                // sollte keinen aktiven Kampfzauber haben, weil die Syntax (ohne Stufenangabe) falsch ist:
                if (u.hasProperty(Kampfzauber.ATTACKSPELL)) {
                    retval = this.fail(uRef + "hat wider Erwarten einen Angriffszauber gesetzt: " + u.getStringProperty(Kampfzauber.ATTACKSPELL));
                }
            }
            if (tokens[1].equals("16")) {
                if (!u.getStringProperty(Kampfzauber.ATTACKSPELL, "").equalsIgnoreCase("feuerwalze 2")) {
                    retval = this.fail(uRef + "hat den Angriffszauber (Feuerwalze 2) nicht gesetzt.");
                }
            }





        } // next unit

        return retval;
    }

}
