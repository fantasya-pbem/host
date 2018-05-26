package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug300;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Pegasus;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Units.Aquaner;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

public class Mantis397 extends TestBase {

	@Override
	protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Partei p = tw.createPartei(Aquaner.class);
		p.setName(getName()+"-Partei");
		
		Region r = tw.nurBetretbar(getRegions()).get(0);
		
        Unit u1 = makeIt(p, r);
        u1.setName("Cailleach Muir");
        u1.setSkill(Reiten.class, u1.getLerntage(Reiten.class) + 60);
        u1.setItem(Silber.class, 5000);
        
        Unit u2 = makeIt(p, r);
        u2.setName("Cailleach Abhann");
        u2.setItem(Silber.class, 1000);
        
        u1.Befehle.add("nach no");
        u2.Befehle.add("nach no");
	}

	@Override
	protected boolean verifyTest() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private Unit makeIt(Partei p, Region r) {
        Unit u = this.createUnit(p, r);
        
        u.setName(this.getName()+" 01");
        u.setPersonen(1);
        u.setItem(Plattenpanzer.class, 1);
        u.setItem(Eisenschild.class, 1);
        u.setItem(Pegasus.class, 1);
        u.setSkill(Reiten.class, Skill.LerntageFuerTW(4) * u.getPersonen());
        
        return u;
	}

}
