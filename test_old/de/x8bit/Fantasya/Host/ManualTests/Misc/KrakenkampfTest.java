package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Host.ManualTests.*;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Streitaxt;
import de.x8bit.Fantasya.Atlantis.Kampfposition;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Ausdauer;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Units.Krake;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class KrakenkampfTest extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Partei p = tw.createPartei(Mensch.class);
		p.setName(getName()+"-Partei");

        Region r = tw.nurTerrain(tw.getAlleRegionen(), Ozean.class).get(0);
		r.setName(getName()+"-Region-A");
        getRegions().remove(r);

        {
            Unit u = this.createKapitaen(p, r, "Langboot");
            u.setName(this.getName()+" 01");
            
            // 20 Kraken erzeugen:
            for (int i=10; i<30; i++) {
                Unit k = Krake.spawn(r);
                k.setName(this.getName()+" " + i);
            }

            new Info(this.getName() + " Setup in " + r + ".", r);
        }
        
        r = tw.nurTerrain(tw.getAlleRegionen(), Ozean.class).get(0);
		r.setName(getName()+"-Region-B");
        getRegions().remove(r);        

        {
            Unit u = this.createKapitaen(p, r, "Galeone");
            u.setName(this.getName()+" 02");
            u.setKampfposition(Kampfposition.Nicht);
            int schiff = u.getSchiff();
            
            u = this.createUnit(p, r);
            u.setName(this.getName()+" 03");
            u.setSchiff(schiff);
            u.setPersonen(200);
            u.setSkill(Hiebwaffen.class, u.getPersonen() * Skill.LerntageFuerTW(10));
            u.setSkill(Ausdauer.class, u.getPersonen() * Skill.LerntageFuerTW(5));
            u.setItem(Streitaxt.class, u.getPersonen());
            u.setItem(Plattenpanzer.class, u.getPersonen());
            u.setKampfposition(Kampfposition.Vorne);
            
            // 20 Kraken erzeugen:
            for (int i=30; i<50; i++) {
                Unit k = Krake.spawn(r);
                k.setName(this.getName()+" " + i);
                k.setPersonen(1);
            }

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

        String missing = this.verifyExpectedUnits(units, new String[] {"02", "03"});
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
                fail(uRef + "der Langboot-Kapitän hat die Kraken überlebt.");
            }
        } // next unit

        return retval;
    }

}
