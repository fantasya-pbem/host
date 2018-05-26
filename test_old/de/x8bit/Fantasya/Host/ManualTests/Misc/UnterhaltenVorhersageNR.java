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
import de.x8bit.Fantasya.Atlantis.Skills.Steuereintreiben;
import de.x8bit.Fantasya.Atlantis.Skills.Unterhaltung;
import de.x8bit.Fantasya.Host.GameRules;

/**
 *
 * @author hb
 */
public class UnterhaltenVorhersageNR extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
		Partei p = tw.getSpieler1();
        {
			Region r = tw.nurBetretbar(getRegions()).get(0);
			r.setName(getName()+"-Region I");
			getRegions().remove(r);

            Unit u = this.createUnit(p, r);
			u.setPersonen(1000);
			u.setSkill(Unterhaltung.class, u.getPersonen() * 1650);
            u.setName(this.getName()+" 01");
			u.Befehle.add("UNTERHALTE");

			u = this.createUnit(p, r);
			u.setPersonen(1000);
			u.setSkill(Steuereintreiben.class, u.getPersonen() * 1650);
			u.setSkill(Hiebwaffen.class, u.getPersonen() * 1650);
			u.setItem(Schwert.class, u.getPersonen());
			u.Befehle.add("TREIBE " + r.getSilber() + " // alles Alt-Vermögen der Region eintreiben");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

        {
			Region r = tw.nurBetretbar(getRegions()).get(0);
			r.setName(getName()+"-Region II");
			getRegions().remove(r);

            Unit u = this.createUnit(p, r);
			u.setPersonen(1000);
			u.setSkill(Unterhaltung.class, u.getPersonen() * 1650);
            u.setName(this.getName()+" 02");
			u.Befehle.add("UNTERHALTE");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

        {
			Region r = tw.nurBetretbar(getRegions()).get(0);
			r.setName(getName()+"-Region III");
			getRegions().remove(r);

            Unit u = this.createUnit(p, r);
			u.setPersonen(1000);
			u.setSkill(Unterhaltung.class, u.getPersonen() * 1650);
            u.setName(this.getName()+" 03");
			u.Befehle.add("UNTERHALTE");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

        {
			Region r = tw.nurBetretbar(getRegions()).get(0);
			r.setName(getName()+"-Region IV");
			getRegions().remove(r);

            Unit u = this.createUnit(p, r);
			u.setPersonen(1000);
			u.setSkill(Unterhaltung.class, u.getPersonen() * 1650);
            u.setName(this.getName()+" 04");
			u.Befehle.add("UNTERHALTE");

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
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("") && text.contains("")) found = true;
                }
                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben.");
            }
        } // next unit

        return retval;
    }

}
