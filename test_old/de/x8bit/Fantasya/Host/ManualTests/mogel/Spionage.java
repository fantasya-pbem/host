package de.x8bit.Fantasya.Host.ManualTests.mogel;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

public class Spionage extends TestBase {

	@Override
	protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Region r = tw.nurBetretbar(getRegions()).get(0);
		
		Unit v1 = createVictim(tw, r, 1);		// kein Aufpasser
		Unit v2 = createVictim(tw, r, 2);		// Aufpasser dumm
		Unit v3 = createVictim(tw, r, 3);		// Aufpasser gleich
		Unit v4 = createVictim(tw, r, 4);		// Aufpasser besser
//		Unit v5 = createVictim(tw, r, 5);		// Aufpasser bei anderer Partei

        Unit a1 = createAgent(tw, r, null, 1);								// kein Talent
        Unit a2 = createAgent(tw, r, Partei.getPartei(a1.getOwner()), 2);	// kein Aufpasser
        Unit a3 = createAgent(tw, r, Partei.getPartei(a1.getOwner()), 3);	// Aufpasser dumm
        Unit a4 = createAgent(tw, r, Partei.getPartei(a1.getOwner()), 4);	// Aufpasser gleich
        Unit a5 = createAgent(tw, r, Partei.getPartei(a1.getOwner()), 5);	// Aufpasser besser
        
        // kein Talent
        // -- zwischen a1 und v1
        a1.Befehle.add("spioniere " + v1.getNummerBase36());
        a1.setSkill(de.x8bit.Fantasya.Atlantis.Skills.Spionage.class, 0);
        
        // kein Aufpasser
        // -- zwischen a2 und v1
        a2.Befehle.add("spioniere " + v1.getNummerBase36());
        
        // Aufpasser dumm
        // -- zwischen a3 und v2
        v2.setSkill(Wahrnehmung.class, Skill.LerntageFuerTW(3));
        a3.Befehle.add("spioniere " + v2.getNummerBase36());

        // Aufpasser gleich
        // -- zwischen a4 und v3
        v3.setSkill(Wahrnehmung.class, Skill.LerntageFuerTW(6));
        a4.Befehle.add("spioniere " + v3.getNummerBase36());
        
        // Aufpasser besser
        // -- zwischen a5 und v4
        v4.setSkill(Wahrnehmung.class, Skill.LerntageFuerTW(7));
        a5.Befehle.add("spioniere " + v4.getNummerBase36());
	}

	private Unit createVictim(TestWorld tw, Region r, int nummer) {
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
	
	private Unit createAgent(TestWorld tw, Region r, Partei p, int nummer) {
		if (p == null) {
			p = tw.createPartei(Mensch.class);
			p.setName("Agent " + nummer);
		}
		
        Unit u = this.createUnit(p, r);
        u.setKampfposition(Kampfposition.Vorne);
        u.setName("Agent " + nummer);
        u.setPersonen(1);
        u.setSkill(de.x8bit.Fantasya.Atlantis.Skills.Spionage.class, Skill.LerntageFuerTW(6));
        
        return u;
	}

	@Override
	protected boolean verifyTest() {
		// TODO Auto-generated method stub
		return false;
	}

}
