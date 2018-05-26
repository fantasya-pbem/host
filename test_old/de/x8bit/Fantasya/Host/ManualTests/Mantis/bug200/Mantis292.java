package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Units.Aquaner;
import de.x8bit.Fantasya.Host.GameRules;

/**
 * 0000292: Zaubertext fehlt
 * ( http://www.fantasya-pbem.de/taverne/index.php?page=Thread&postID=3537#post3537 )
 * Der Text neu erlernter Zauber wird nicht mehr angezeigt. Ich tappe bei beim
 * neuen Zauber daher im Dunklen, was er bewirkt.
 *
 * Erwartet: Im NR erscheint die Meldung über den neu erlernten Zauber -
 * inklusive kompletter Beschreibung.
 *
 * @author hb
 */
public class Mantis292 extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
		Partei p = tw.createPartei(Aquaner.class);
        Region r = tw.nurBetretbar(getRegions()).get(0);
		r.setName(getName()+" R");
        getRegions().remove(r);

        {
            Unit u = this.createMage(p, r, 6);
            u.setName(this.getName()+" 01");
			u.setSkill(Magie.class, 0);
			u.Befehle.add("LERNE Magie");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(Unit.CACHE, new String[] {"01"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u : Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), (Coords)null, null); // Unit und Coords sind egal.
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("feuerball") && text.contains("kampfzauber angriff")) found = true;
                    if (text.contains("hain der") && text.contains("-kosten")) found = true;
                    if (text.contains("klauen der") && text.contains("-kosten")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Zauberbuch-Meldung fehlt (oder ist geteilt).");
            }
        } // next unit

        return retval;
    }

}
