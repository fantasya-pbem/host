package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Units.Aquaner;
import de.x8bit.Fantasya.Atlantis.Units.Echse;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import java.util.Collection;

/**
 * Zusammenfassung	0000219: HELFE KONTAKTIERE ist kaputt
 *
 * 1) Ich habe in AW 111 einer anderen Partei HELFE KONTAKTIEREN gesetzt.
 * Fehlermeldung: "die Option 'kontaktieren' ist unbekannt"
 *
 * 2) Ich LIEFERE einem Haufen fremder Botschafter und dergleichen jede Runde Silber.
 * Diese Runde bekomme ich da überall "Missionar des Flammenden Auges [134] hat
 * keinen Kontakt mit 'Hiroshige, der seefahrende Samurai [5qx]'" und dergleichen.
 * Ich halte es für unwahrscheinlich, dass mal eben alle fremden Völker ihr HELFE
 * KONTAKTIERE für mich gelöscht haben... insbesondere insofern HELFE KONTAKTIERE
 * ja anscheinend unbekannt ist.
 *
 * Kann man die alten HELFE-Stati rekonstruieren, oder müssen wir dann manuell
 * alle HELFE KONTAKTIERE neu setzen?
 * 
 * @author hapebe
 */
public class Mantis219 extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        Partei alliierte = this.getTestWorld().createPartei(Echse.class);
        Partei fremde = this.getTestWorld().createPartei(Aquaner.class);

        {
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01 Geber");

            Unit alliierter = this.createUnit(alliierte, r);
            alliierter.setName(this.getName()+" 02 Alliierter");
            alliierter.Befehle.add("HELFE " + p.getNummerBase36() + " KONTAKTIEREN");
            alliierter.Befehle.add("// HELFE " + p.getNummerBase36() + " KONTAKTIEREN");
            alliierter.setItem(Silber.class, 0);

            Unit fremder = this.createUnit(fremde, r);
            fremder.setName(this.getName()+" 03 Fremder");
            fremder.setItem(Silber.class, 0);

            u.Befehle.add("GIB " + alliierter.getNummerBase36() + " 100 Silber");
            u.Befehle.add("GIB " + fremder.getNummerBase36() + " 900 Silber");
            u.Befehle.add("// GIB " + alliierter.getNummerBase36() + " 100 Silber");
            u.Befehle.add("// GIB " + fremder.getNummerBase36() + " 900 Silber");

            new Info(this.getName() + " Setup in " + r + ".", u);
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                if (u.getItem(Silber.class).getAnzahl() < 10) {
                    retval = fail(tokens[1] + ": Hoppla, das Silber ist weg (trotz fehlendem KONTAKT).");
                }
            }

            // unit 02
            if (tokens[1].equals("02")) {
                if (u.getItem(Silber.class).getAnzahl() < 10) {
                    retval = fail(tokens[1] + ": HELFE KONTAKT hat nicht geklappt?");
                }

                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("hungert")) found = true;
                }
                if (found) retval = fail(tokens[1] + ": Einheit hungert.");
            }

            // unit 03
            if (tokens[1].equals("03")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("hungert in ")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Hunger fehlt oder es wurde Silber ohne KONTAKT übergeben.");
            }
            
        } // next unit

        return retval;
    }

}
