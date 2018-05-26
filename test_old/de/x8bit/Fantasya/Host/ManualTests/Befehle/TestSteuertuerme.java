package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Buildings.Steuerturm;
import de.x8bit.Fantasya.Atlantis.Items.Juwel;
import de.x8bit.Fantasya.Atlantis.Items.Pelz;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Skills.Handel;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Steuereintreiben;
import de.x8bit.Fantasya.Atlantis.Skills.Unterhaltung;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class TestSteuertuerme extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurTerrain(getRegions(), Ebene.class).get(0);
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
			u.setPersonen(10);
			u.setSkill(Handel.class, 180 * u.getPersonen());
			u.setItem(Juwel.class, 150);
			u.setItem(Pelz.class, 150);
			u.setItem(Silber.class, u.getPersonen() * 10);
            u.setName(this.getName() + " 01");
			u.Befehle.add("HANDEL VERKAUFE 150 Juwel");
			u.Befehle.add("HANDEL VERKAUFE 150 Pelz");

            u = this.createUnit(p, r);
			u.setPersonen(1);
			u.setSkill(Unterhaltung.class, 180 * u.getPersonen());
			u.setItem(Silber.class, u.getPersonen() * 10);
            u.setName(this.getName() + " 02");
			u.Befehle.add("UNTERHALTE");



			Partei p2 = this.getTestWorld().createPartei(Mensch.class);
			p2.setName(this.getName() + "-Finanzbeamte");

			Building burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			burg.setSize(50);
			burg.setName(this.getName() + "-Burg");

			Building turm = Building.Create(Steuerturm.class.getSimpleName(), r.getCoords());
			turm.setSize(20);
			turm.setName(this.getName() + "-Finanzamt");

			u = this.createUnit(p2, r);
			u.setName(this.getName() + " 03");
			u.Enter(burg);
			u.setItem(Silber.class, u.getPersonen() * 10);
			u.Befehle.add("STEUERN 25 " + p.getNummerBase36());

            u = this.createUnit(p2, r);
            u.setName(this.getName()+" 04");
			u.Enter(turm);
			u.setPersonen(10);
			u.setSkill(Steuereintreiben.class, 90 * u.getPersonen());
			u.setSkill(Hiebwaffen.class, 30 * u.getPersonen());
			u.setItem(Silber.class, u.getPersonen() * 10 + 50); // Unterhalt für Steuerturm
			u.setItem(Schwert.class, u.getPersonen());
			u.Befehle.add("TREIBE");

            u = this.createUnit(p2, r);
            u.setName(this.getName()+" 05");
			u.Enter(turm);
			u.setPersonen(1);
			u.setSkill(Steuereintreiben.class, 180 * u.getPersonen());
			u.setSkill(Hiebwaffen.class, 30 * u.getPersonen());
			u.setItem(Silber.class, u.getPersonen() * 10);
			u.setItem(Schwert.class, u.getPersonen());
			u.Befehle.add("TREIBE");



            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04", "05"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        int nettoEinkommen = 0;
        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("muss bei einem") && text.contains("zahlen")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Besteuerung fehlt.");

                nettoEinkommen += u.getItem(Silber.class).getAnzahl();
            }

            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("muss bei einem") && text.contains("zahlen")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Besteuerung fehlt.");

                nettoEinkommen += u.getItem(Silber.class).getAnzahl();
            }

            // unit 03
            if (tokens[1].equals("03")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("wird auf 25%")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Änderung des Steuersatzes fehlt.");
            }

            // unit 04
            if (tokens[1].equals("04")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("zahlt 50 silber unterhalt")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Steuerturm-Unterhalt fehlt.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("steuersatz") && text.contains("400 silber")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Besteuerungs-Einnahmen fehlt.");
            }

            // unit 05
            if (tokens[1].equals("05")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("steuersatz") && text.contains("60 silber")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Besteuerungs-Einnahmen fehlt.");

                if (u.getItem(Silber.class).getAnzahl() != 55) {
                    retval = fail(tokens[1] + ": Unerwarteter Silberbestand - Einnahmen oder Ausgaben (Upkeep!) falsch.");
                }
            }


        } // next unit

        if (nettoEinkommen != 2160 - 460) {
            retval = fail("Unerwartetes Nettoeinkommen: " + nettoEinkommen + " Silber.");
        }

        return retval;
    }

}
