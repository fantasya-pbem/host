package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Host.ManualTests.*;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class TestVerhungern extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Partei p = tw.createPartei(Mensch.class);
		p.setName(getName()+"-Partei");

        Region r = tw.nurBetretbar(getRegions()).get(0);
		r.setName(getName()+"-Region");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
            u.setPersonen(100);
            u.setLebenspunkte(u.maxLebenspunkte() - 5 * u.getPersonen());
            u.setItem(Silber.class, 0);
            u.setSkill(Wahrnehmung.class, 180 * u.getPersonen());
            u.setName(this.getName()+" 01");
            u.Befehle.add("LERNE Wahrnehmung");

            new Info(this.getName() + " Setup in " + r + ".", r);
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");
			String uRef = tokens[1] + "[" + u.getNummerBase36() + "] von [" + Codierung.toBase36(u.getOwner()) + "] - ";

            // unit 01
            if (tokens[1].equals("01")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("verhungern")) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung über Verhungerte fehlt.");
            }
            
            if ((u.getPersonen() > 90) || (u.getPersonen() < 10)) {
                retval = fail(uRef + "unerwartete Personenzahl: " + u.getPersonen());
            }
            
            int talentProPerson = u.getSkill(Wahrnehmung.class).getLerntage() / u.getPersonen();
            if (talentProPerson != 210) {
                // 180 hatte die Einheit, 30 haben sie gelernt
                retval = fail(uRef + "unerwartete Lerntage pro Person: " + talentProPerson);
            }
        } // next unit

        return retval;
    }

}
