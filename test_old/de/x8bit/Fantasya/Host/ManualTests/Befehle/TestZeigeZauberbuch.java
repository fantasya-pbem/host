package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Spells.Erdbeben;
import de.x8bit.Fantasya.Atlantis.Spells.GuterWind;
import de.x8bit.Fantasya.Atlantis.Spells.HainDerTausendEichen;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Host.GameRules;

/**
 *
 * @author hb
 */
public class TestZeigeZauberbuch extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
		Partei p = tw.createPartei(Elf.class);
        Region r = tw.nurBetretbar(getRegions()).get(0);
		r.setName(getName()+" R");
        getRegions().remove(r);

        {
            Unit u = this.createMage(p, r, 6);
            u.setName(this.getName()+" 01");
			u.setSpell(new Erdbeben());
			u.setSpell(new HainDerTausendEichen());
			u.setSpell(new GuterWind());
			u.Befehle.add("ZEIGE ZAUBERBUCH");

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


        for (Unit u:Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), (Coords)null, null);
                int found = 0;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("erdbeben") && text.contains("kampfzauber verwirrung")) found++;
                    if (text.contains("hain der") && text.contains("-kosten")) found++;
                    if (text.contains("guter wind") && text.contains("-kosten")) found++;
                }
                if (found < 3) retval = fail(tokens[1] + ": Ein oder mehrere Zauber-Infos fehlen.");
            }
        } // next unit

        return retval;
    }

}
