package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Coords;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Units.Troll;
import de.x8bit.Fantasya.Atlantis.Units.Zwerg;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class Diebstahl extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p1 = this.getTestWorld().getSpieler1();
		Partei p2 = this.getTestWorld().createPartei(Troll.class);
		p2.setName(getName() + "-Opfer");
		Partei p3 = this.getTestWorld().createPartei(Zwerg.class);
		p3.setName(getName() + "-Wachsame");
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit u1 = this.createUnit(p1, r);
            u1.setName(this.getName()+" 01");
			u1.setSkill(Tarnung.class, 1350);

            Unit u2 = this.createUnit(p2, r);
            u2.setName(this.getName()+" 02");

			u1.Befehle.add("BEKLAUE " + u2.getNummerBase36());

            Unit u11 = this.createUnit(p1, r);
            u11.setName(this.getName()+" 11");
			u11.setSkill(Tarnung.class, 1350);

            Unit u12 = this.createUnit(p3, r);
            u12.setName(this.getName()+" 12");
			u12.setSkill(Wahrnehmung.class, Skill.LerntageFuerTW(12));

			u11.Befehle.add("BEKLAUE " + u12.getNummerBase36());



			new Info(this.getName() + " Setup in " + r + ".", u1, u1.getCoords());
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "11", "12"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
				if (u.getItem(Silber.class).getAnzahl() <= 990) {
					retval = fail(tokens[1] + ": Hat nichts erbeutet.");
				}
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("") && text.contains("kann 450 silber von")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Keine Meldung über den erfolgreichen Beutezug.");
            }

            // unit 02
            if (tokens[1].equals("02")) {
				if (u.getItem(Silber.class).getAnzahl() >= 990) {
					retval = fail(tokens[1] + ": Ist nicht bestohlen worden.");
				}
                messages = Message.Retrieve(null, (Coords)null, u);
                boolean found = false;
				String s = null;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("erwischt") && text.contains("")) {
						s= msg.getText();
						found = true;
					}
                }
                if (found) retval = fail(tokens[1] + ": Opfer hat eine Meldung bekommen, sollte aber nix bemerken. (" + s + ")");
            }

            // unit 12
            if (tokens[1].equals("12")) {
				if (u.getItem(Silber.class).getAnzahl() < 990) {
					retval = fail(tokens[1] + ": Ist bestohlen worden.");
				}
                messages = Message.Retrieve(null, (Coords)null, u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("erwischt") && text.contains("beim versuchten diebstahl")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Keine Meldung über den ertappten Dieb.");
            }

		} // next unit

        return retval;
    }

}
