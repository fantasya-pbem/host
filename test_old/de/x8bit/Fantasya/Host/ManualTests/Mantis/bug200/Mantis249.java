package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Steinbau;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 * 0000249: Abbau trotz Bewachen
 * Runde 115:
 * Orzammar Silberberg (1, -4), Berge, 3 Bäume, 369 Bauern, $31579 Silber.
 * Hier können folgende Tiere gefangen werden: 611 Pferde. Es kann 105 Eisen
 * und 94 Steine abgebaut werden.
 *
 * Die Region wird von Granock, Herrscher ueber Orzammar [1r9], bewacht
 * - Feldspaat [47b], Koenigreich Cormyr [i], 50 Mensch, hat: Silberbeutel.
 * - Feldspaat [47d], Koenigreich Cormyr [i], 50 Mensch, hat: Silberbeutel.
 *
 * Rd 116:
 * Orzammar Silberberg (1, -4), Berge, 3 Bäume, 369 Bauern, $31956 Silber.
 * Hier können folgende Tiere gefangen werden: 611 Pferde. Es kann 105 Eisen
 * und 94 Steine abgebaut werden.
 *
 * Die Region wird von Granock, Herrscher ueber Orzammar [1r9], bewacht
 * - Feldspaat [47b], Koenigreich Cormyr [i], 50 Mensch, hat: Silberbeutel
 * und 50 Steine.
 * 
 * @author hb
 */
public class Mantis249 extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurTerrain(getRegions(), Berge.class).get(0);
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01 Bewacher");
            u.setSkill(Hiebwaffen.class, 30 * u.getPersonen());
            u.setItem(Schwert.class, 1 * u.getPersonen());
            u.setBewacht(true);

            Partei p2 = this.getTestWorld().createPartei(Mensch.class);
            p2.setName(this.getName()+"-Raubbauer");

            u = this.createUnit(p2, r);
            u.setName(this.getName()+" 02 Raubbauer");
            u.setSkill(Steinbau.class, 90 * u.getPersonen());
            u.Befehle.add("MACHE Stein");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
//        Partei p = this.getTestWorld().getSpieler1();
        List<Message> messages = null;

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


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                if (!u.getBewacht()) retval = fail(tokens[1] + ": Einheit bewacht nicht.");
            }

            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("01 bewacher") && text.contains("verhinder")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über fehlgeschlagenen Abbau fehlt.");

                if (u.getItem(Stein.class).getAnzahl() > 0) {
                    retval = fail(tokens[1] + ": Trotz Bewachung Steine abgebaut.");
                }
            }

        } // next unit

        return retval;
    }

}
