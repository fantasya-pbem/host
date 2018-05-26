package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Items.Gewuerz;
import de.x8bit.Fantasya.Atlantis.Items.Pelz;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Handel;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class ZweiLangeBefehle extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = null;
        for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
            // wird hier Pelz oder Gewürz verkauft?
			if (maybe.getProduce().equals(Pelz.class)) continue;
			if (maybe.getProduce().equals(Gewuerz.class)) continue;
			if (maybe.getBauern() < 100) continue;

			// Nein - gotcha!
			r = maybe;
			break;
        }
        // Den Fall, dass es keine solche Region gibt lassen wir mal außer Betracht....
        getRegions().remove(r);

        { // Versuchen wir's mal:
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
            u.Befehle.add("LERNE Handel");
            u.Befehle.add("NACH w");
            // u.Befehle.add("GIB Bauern PERSONEN"); // nach Mantis #269 jetzt erlaubt

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

        { // ... und nochmal Multi-Lang:
            Building burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
            burg.setSize(100);

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 02");
            u.setSkill(Handel.class, 300);
            u.setItem(Pelz.class, 2);
            u.setItem(Gewuerz.class, 2);
            u.Befehle.add("HANDEL VERKAUFE 1 Pelz");
            u.Befehle.add("LERNE Handel");
            u.Befehle.add("HANDEL VERKAUFE 1 Gewuerz");
            u.Enter(burg);
        }

        // this.getTestWorld().setContinueWithZAT(false);
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        List<Message> messages = null;

        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                if (u.getSkill(Handel.class).getLerntage() != 30) {
                    retval = fail(tokens[1] + ": Einheit hat nicht Handel gelernt wie erwartet.");
                }

                if (Main.getBFlag("EVA")) continue; // Bei EVA werden die Befehle gar nicht erst zugelassen

				messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("nach w") && text.contains("nicht mehr ausführen")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über ungültiges NACH w fehlt.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("gib bauern") && text.contains("nicht mehr ausführen")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über ungültiges GIB Bauern PERSONEN fehlt.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("lerne handel' nicht mehr ausführen")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Fehler-Meldung über gültiges LERNE Handel.");
            }

            // unit 02
            if (tokens[1].equals("02")) {
                if (u.getSkill(Handel.class).getLerntage() > 300) {
                    retval = fail(tokens[1] + ": Einheit hat unerwartet Handel gelernt.");
                }
                if (u.getItem(Pelz.class).getAnzahl() > 1) {
                    retval = fail(tokens[1] + ": Einheit konnte keinen Pelz verkaufen.");
                }
                if (u.getItem(Gewuerz.class).getAnzahl() > 1) {
                    retval = fail(tokens[1] + ": Einheit konnte kein Gewuerz verkaufen.");
                }

                if (Main.getBFlag("EVA")) continue; // Bei EVA werden die Befehle gar nicht erst zugelassen
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("lerne handel") && text.contains("nicht mehr ausführen") && text.contains("verkaufe 1 pelz")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über ungültiges LERNE Handel fehlt oder ist nicht richtig.");
            }

        } // next unit

        return retval;
    }

}
