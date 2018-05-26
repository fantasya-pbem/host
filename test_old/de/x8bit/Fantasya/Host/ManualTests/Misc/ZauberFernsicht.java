package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import java.util.List;

import de.x8bit.Fantasya.Host.ManualTests.*;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Spells.Fernsicht;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class ZauberFernsicht extends TestBase {

    @Override
    protected void mySetupTest() {
//        Partei p = this.getTestWorld().getSpieler1();
        
        // passendes Regions-Paar suchen:
        Region r = null; Region ziel = null;
        for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
            Coords c = maybe.getCoords();
            Coords zielCoords = new Coords(c.getX() - 7, c.getY() + 4, c.getWelt());
            Region maybeZiel = Region.Load(zielCoords);
            if (!maybeZiel.istBetretbar(null)) continue;
            if (!getRegions().contains(maybeZiel)) continue;
            
            // gotcha!
            r = maybe; ziel = maybeZiel;
        }
		r.setName(getName()+"-Region");
        getRegions().remove(r);        
		ziel.setName(getName()+"-Region-Ziel");
        getRegions().remove(ziel);        
        
        {
			Partei p2 = this.getTestWorld().createPartei(Mensch.class);
			p2.setName("Z-Fernsicht Ahnungslose");
            
            Unit u = this.createUnit(p2, ziel);
            u.setName(this.getName() + " 11");
            u.setItem(Silber.class, 100);
            u.setItem(Holz.class, 10);
            u.Befehle.add("LERNE Wahrnehmung");
            
            u = this.createUnit(p2, ziel);
            u.setName(this.getName() + " 12");
            u.setSkill(Tarnung.class, Skill.LerntageFuerTW(1));
            u.setItem(Schwert.class, 10);
            u.Befehle.add("@TARNE EINHEIT");
            u.Befehle.add("LERNE Tarnung");
            
            
			Partei p3 = this.getTestWorld().createPartei(Mensch.class);
			p3.setName("Z-Fernsicht");

			Unit magier = this.createMage(p3, r, 6);
			magier.setName(this.getName() + " 01");
			magier.setSpell(new Fernsicht());
            magier.Befehle.add("URSPRUNG " + r.getCoords().getX() + " " + r.getCoords().getY());
			magier.Befehle.add("ZAUBERE \"Fernsicht\" -7 +4");

            new Info(this.getName() + " Setup in " + r + ".", magier);
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "11", "12"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("in die ferne schweifen") && text.contains("erkennt")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Fernsicht fehlt.");
            }

        } // next unit

        return retval;
    }

}
