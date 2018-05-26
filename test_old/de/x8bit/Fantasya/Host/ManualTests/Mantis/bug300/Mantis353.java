package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug300;

import de.x8bit.Fantasya.Atlantis.Items.Pegasus;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 * 0000353: Pegasi verschwinden im Kampf
 * Beschreibung	Liegt zugegebenermassen schon etwas zurück: in AW 185 hatte
 * meine Einheit [ctL] noch zwei Pegasi, hat dann Monster angegriffen und
 * hatte in AW 186 plötzlich keine Pegasi mehr. Und ich habe den Eindruck,
 * als würden Pegasi *immer* verschwinden, wenn man damit kämpft, die scheinen
 * also nicht einfach zufällig im Kampf draufzugehen (was ja auch denkbar wäre).
 *
 * Könnte mit folgendem Bug verwandt sein:
 * http://www.fantasya-pbem.de/mantis/view.php?id=336 [^]
 *
 * @author hb
 */
public class Mantis353 extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Partei p = tw.createPartei(Mensch.class);
		p.setName(getName()+"-Partei");

   		Partei p2 = tw.createPartei(Mensch.class);
		p2.setName(getName()+"-Gegner");


        Region r = tw.nurBetretbar(getRegions()).get(0);
		r.setName(getName()+"-Region");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(p, r);
            u.setKampfposition(Kampfposition.Vorne);
            u.setName(this.getName()+" 01");
            u.setPersonen(10);
            u.setItem(Pegasus.class, u.getPersonen() - 2);
            u.setItem(Schwert.class, u.getPersonen());
            u.setSkill(Hiebwaffen.class, Skill.LerntageFuerTW(8) * u.getPersonen());
            u.setSkill(Reiten.class, Skill.LerntageFuerTW(6) * u.getPersonen());

            Unit g = this.createUnit(p2, r);
            g.setKampfposition(Kampfposition.Vorne);
            g.setName(this.getName()+" 02");
            g.setPersonen(10);
            g.setItem(Pegasus.class, g.getPersonen() - 2);
            g.setItem(Schwert.class, g.getPersonen());
            g.setSkill(Hiebwaffen.class, Skill.LerntageFuerTW(8) * g.getPersonen());
            g.setSkill(Reiten.class, Skill.LerntageFuerTW(6) * g.getPersonen());

            u.Befehle.add("ATTACKIERE " + g.getNummerBase36());

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
                    if (text.contains("") && text.contains("")) found = true;
                }
                if (found) retval = fail(uRef + "Skeleton nicht überschrieben.");
            }
        } // next unit

        return retval;
    }

}
