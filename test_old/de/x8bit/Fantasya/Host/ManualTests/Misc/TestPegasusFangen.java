package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Atlantis.Items.Pegasus;
import de.x8bit.Fantasya.Host.ManualTests.*;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Pferdedressur;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class TestPegasusFangen extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Partei pa = tw.createPartei(Mensch.class);
		pa.setName(getName()+"-Wahnsinnige");

		Partei pb = tw.createPartei(Mensch.class);
		pb.setName(getName()+"-Fänger");

        Region r = tw.nurBetretbar(getRegions()).get(0);
		r.setName(getName()+"-Region");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(pa, r);
            u.setName(this.getName()+" 01");
            u.setItem(Pegasus.class, 10);
            u.Befehle.add("GIB BAUERN 11 Pegasi");

            u = this.createUnit(pb, r);
            u.setName(this.getName()+" 02");
            u.setPersonen(1);
            u.setSkill(Pferdedressur.class, Skill.LerntageFuerTW(10));
            u.Befehle.add("MACHE 2 Pegasi");

            u = this.createUnit(pb, r);
            u.setName(this.getName()+" 03");
            u.setPersonen(1);
            u.setSkill(Pferdedressur.class, Skill.LerntageFuerTW(8));
            u.Befehle.add("MACHE 2 Pegasi");

            new Info(this.getName() + " Setup in " + r + ".", r);
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;

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
			String uRef = tokens[1] + "[" + u.getNummerBase36() + "] von [" + Codierung.toBase36(u.getOwner()) + "] - ";

            // unit 01
            if (tokens[1].equals("01")) {
                if (u.getItem(Pegasus.class).getAnzahl() > 0) {
                    retval = fail(uRef + "hat noch Pegasi.");
                }
            }
            
            // unit 02
            if (tokens[1].equals("02")) {
                if (u.getItem(Pegasus.class).getAnzahl() != 1) {
                    retval = fail(uRef + "hat unerwartete Anzahl an Pegasi gefangen (" + u.getItem(Pegasus.class).getAnzahl() + " statt 1).");
                }
            }
            
            // unit 03
            if (tokens[1].equals("03")) {
                if (u.getItem(Pegasus.class).getAnzahl() != 0) {
                    retval = fail(uRef + "hat unerwartete Anzahl an Pegasi gefangen (" + u.getItem(Pegasus.class).getAnzahl() + " statt keine).");
                }
            }
            
        } // next unit

        return retval;
    }

}
