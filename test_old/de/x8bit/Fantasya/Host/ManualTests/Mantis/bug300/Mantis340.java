package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug300;

import de.x8bit.Fantasya.Atlantis.Items.Pegasus;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 * 0000340: Kapazitätsberechnung falsch
 * Einen Einheit, hat 1 Pegasus (und kann ihn auch reiten) und 1540 Silber
 * Der CR meint dazu:
 * 7540;weight
 * 16;freieKapazitaet
 * 0;freieKapazitaetBeritten
 * 75;Gesamtgewicht
 *
 * Ich habe das schon im Forum angeschnitten.
 * Laut CR kann der Reiter reiten, de facto aber nicht weil nähmlich um 0,4 überladen!
 *
 * Es sollte also im CR stehen (wenn schon 'gerundet' wird, eigentlich ist das hier kein runden sondern 'int' bzw. 'ceil'):
 * 16;freieKapazitaet
 * -1;freieKapazitaetBeritten
 * 76;Gesamtgewicht
 * 
 * @author hb
 */
public class Mantis340 extends TestBase {

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
            u.setName(this.getName()+" 01");
            u.setPersonen(1);
            u.setSkill(Reiten.class, Skill.LerntageFuerTW(6) * u.getPersonen());
            u.setItem(Pegasus.class, u.getPersonen());
            u.setItem(Silber.class, 1540 + 10); // 10 werden ja noch vor dem Report-Schreiben aufgegessen

            new Info(this.getName() + " Setup in " + r + " mit Partei " + p + ".", r);
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
