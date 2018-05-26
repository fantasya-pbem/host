package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Holzschild;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Items.Streitaxt;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Units.Troll;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class BasisKampf extends TestBase {

    @Override
    protected void mySetupTest() {
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);
		
        Partei p = this.getTestWorld().getSpieler1();
		Partei p2 = this.getTestWorld().createPartei(Troll.class);
		p2.setName(this.getName() + "-Trolle");
		p2.setUrsprung(r.getCoords());


        {
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
			u.setPersonen(100);
			u.setSkill(Speerkampf.class, 630 * u.getPersonen());
			u.setItem(Speer.class, u.getPersonen());
			u.setItem(Holzschild.class, u.getPersonen());

			Unit gegner = this.createUnit(p2, r);
            gegner.setName(this.getName()+" 02");
			gegner.setPersonen(10);
			gegner.setSkill(Hiebwaffen.class, 300 * gegner.getPersonen());
			gegner.setItem(Streitaxt.class, gegner.getPersonen());

			Unit gegner2 = this.createUnit(p2, r);
            gegner2.setName(this.getName()+" 04");
			gegner2.setPersonen(10);
			gegner2.setSkill(Hiebwaffen.class, 300 * gegner2.getPersonen());
			gegner2.setItem(Streitaxt.class, gegner2.getPersonen());
			gegner2.setItem(Plattenpanzer.class, gegner2.getPersonen());

			u.Befehle.add("ATTACKIERE " + gegner.getNummerBase36());
			u.Befehle.add("LERNE Speerkampf");

			gegner.Befehle.add("LERNE Hiebwaffen");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

        r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

		{
			// Platzhalter für den Gegner, damit er auch noch einen Report bekommt.
            Unit u = this.createUnit(p2, r);
            u.setName(this.getName()+" 03");
		}

    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
        Partei p = this.getTestWorld().getSpieler1();
        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(Unit.CACHE, new String[] {"01", "03"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u : Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                messages = Message.Retrieve(p, (Coords)null, null);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("greif") && text.contains(" an")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Angriff fehlt.");
            }

            // unit 02
            if (tokens[1].equals("02")) {
				retval = fail(tokens[1] + ": Diese Einheit sollte eigentlich besiegt und aufgelöst sein.");
			}

        } // next unit

        return retval;
    }

}
