package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug300;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

public class Mantis355 extends TestBase {

	@Override
	protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Region r = tw.nurBetretbar(getRegions()).get(0);
		
		Unit u1 = createUnit(tw, r, 1);
		@SuppressWarnings("unused") // Partei braucht Einheit - sonst wird sie beim ZAT gelöscht
		Unit u2 = createUnit(tw, r, 2);
		@SuppressWarnings("unused") // Partei braucht Einheit - sonst wird sie beim ZAT gelöscht
		Unit u3 = createUnit(tw, r, 3);
		
		u1.Befehle.add("BOTSCHAFT REGION \"alles doof hier\"");
	}

	private Unit createUnit(TestWorld tw, Region r, int nummer) {
		Partei p = tw.createPartei(Mensch.class);
		p.setName("Victim " + nummer);
		
        Unit u = this.createUnit(p, r);
        u.setKampfposition(Kampfposition.Vorne);
        u.setName("Victim " + nummer);
        u.setPersonen(1);
        u.setSkill(Hiebwaffen.class, Skill.LerntageFuerTW(6));
        u.Befehle.add("LERNE Hiebwaffen");
        
        return u;
	}

	@Override
	protected boolean verifyTest() {
		// TODO Auto-generated method stub
		return false;
	}

}
