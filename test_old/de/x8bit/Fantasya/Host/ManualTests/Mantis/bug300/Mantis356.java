package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug300;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Spells.Erdbeben;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

public class Mantis356 extends TestBase {

	@Override
	protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Partei p = tw.createPartei(Mensch.class);
		p.setName(getName()+"-Partei");
		
		Region r = tw.nurBetretbar(getRegions()).get(0);
		
        Unit u = this.createUnit(p, r);
        u.setKampfposition(Kampfposition.Vorne);
        u.setName("Magier");
        u.setPersonen(1);
        u.setSkill(Magie.class, Skill.LerntageFuerTW(6) * u.getPersonen());
        u.setSpell(new Erdbeben());
        
        u.Befehle.add("ZEIGE Zauberbuch");
	}

	@Override
	protected boolean verifyTest() {
		// TODO Auto-generated method stub
		return false;
	}

}
