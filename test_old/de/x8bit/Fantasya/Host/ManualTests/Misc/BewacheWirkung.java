package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class BewacheWirkung extends TestBase {

    @SuppressWarnings("unused")
	@Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = null;
        
        for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
            if (maybe.Rekruten() > 0) {
                r = maybe;
                break;
            }
        }
        if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
        getRegions().remove(r);

        { // erster Fall: Kein Kontakt, keine Allianz
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
            u.setPersonen(1);
            u.Befehle.add("REKRUTIERE 1");
            u.Befehle.add("LERNE Wahrnehmung");

            Partei p2 = this.getTestWorld().createPartei(Mensch.class);
            p2.setName(this.getName() + "-Bewacher");

            u = this.createUnit(p2, r);
            u.setName(this.getName() + " 02");
            u.setSkill(Hiebwaffen.class, 300 * u.getPersonen());
            u.setItem(Schwert.class, u.getPersonen());
            u.Befehle.add("LERNE Hiebwaffen");
            u.setBewacht(true);

            new Info(this.getName() + "-00 Setup in " + r + ".", u, u.getCoords());
        }

        { // zweiter Fall: Allianz
            for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
                if (maybe.Rekruten() > 0) {
                    r = maybe;
                    break;
                }
            }
            if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
            getRegions().remove(r);

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 11");
            u.setPersonen(1);
            u.Befehle.add("REKRUTIERE 1");
            u.Befehle.add("LERNE Wahrnehmung");

            Partei p2 = this.getTestWorld().createPartei(Mensch.class);
            p2.setName(this.getName() + "-Bewacher");

            u = this.createUnit(p2, r);
            u.setName(this.getName() + " 12");
            u.setSkill(Hiebwaffen.class, 300 * u.getPersonen());
            u.setItem(Schwert.class, u.getPersonen());
            u.Befehle.add("HELFE " + p.getNummerBase36() + " KONTAKTIERE");
            u.Befehle.add("LERNE Hiebwaffen");
            u.setBewacht(true);

            new Info(this.getName() + "-10 Setup in " + r + ".", u, u.getCoords());
        }

        { // dritter Fall: KONTAKTIERE
            for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
                if (maybe.Rekruten() > 0) {
                    r = maybe;
                    break;
                }
            }
            if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
            getRegions().remove(r);

            Unit u1 = this.createUnit(p, r);
            u1.setName(this.getName()+" 21");
            u1.setPersonen(1);
            u1.Befehle.add("REKRUTIERE 1");
            u1.Befehle.add("LERNE Wahrnehmung");

            Partei p2 = this.getTestWorld().createPartei(Mensch.class);
            p2.setName(this.getName() + "-Bewacher");

            Unit u2 = this.createUnit(p2, r);
            u2.setName(this.getName() + " 22");
            u2.setSkill(Hiebwaffen.class, 300 * u2.getPersonen());
            u2.setItem(Schwert.class, u2.getPersonen());
            u2.Befehle.add("KONTAKTIERE " + u1.getNummerBase36());
            u2.Befehle.add("LERNE Hiebwaffen");
            u2.setBewacht(true);

            new Info(this.getName() + "-20 Setup in " + r + ".", u2, u2.getCoords());
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
                if (u.getPersonen() > 1) {
                    retval = fail(tokens[1] + ": Rekrutieren hat unerwartet geklappt.");
                }

                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("kann niemand") && text.contains("rekrutier")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Fehlschlag fehlt.");
            }

            // unit 11
            if (tokens[1].equals("11")) {
                if (u.getPersonen() == 1) {
                    retval = fail(tokens[1] + ": Rekrutieren hat nicht geklappt.");
                }

                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("rekrutier") && text.contains("1 person")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Rekrutierung fehlt.");
            }

            // unit 21
            if (tokens[1].equals("21")) {
                if (u.getPersonen() == 1) {
                    retval = fail(tokens[1] + ": Rekrutieren hat nicht geklappt.");
                }

                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("rekrutier") && text.contains("1 person")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Rekrutierung fehlt.");
            }

        } // next unit

        return retval;
    }

}
