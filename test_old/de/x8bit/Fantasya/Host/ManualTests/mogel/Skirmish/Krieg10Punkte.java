package de.x8bit.Fantasya.Host.ManualTests.mogel.Skirmish;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Units.Echse;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 * testet ob die Punkte (aktuell 10) vergeben werden - ein lesen aus der DB wird nicht getestet
 * 
 * @author mogel
 *
 */

public class Krieg10Punkte extends TestBase {

	@Override
	protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
		Region r = tw.nurBetretbar(getRegions()).get(0);
		
		Partei faction_defender = tw.createPartei(Mensch.class);
		faction_defender.setName("Defender");
		
        Unit defender = this.createUnit(faction_defender, r);
        defender.setKampfposition(Kampfposition.Vorne);
        defender.setName("Defender");
        defender.setPersonen(10);
        defender.setItem(Schwert.class, 10);
        defender.setSkill(Hiebwaffen.class, Skill.LerntageFuerTW(6) * defender.getPersonen());
        defender.Befehle.add("LERNE Hiebwaffen");
        
		Partei faction_attacker = tw.createPartei(Echse.class);
		faction_attacker.setName("Attacker");
        
        Unit attacker = this.createUnit(faction_attacker, r);
        attacker.setKampfposition(Kampfposition.Vorne);
        attacker.setName("Attacker");
        attacker.setPersonen(5);
        attacker.setItem(Schwert.class, 5);
        attacker.setSkill(Hiebwaffen.class, Skill.LerntageFuerTW(6) * attacker.getPersonen());
        attacker.Befehle.add("ATTACKIERE " + defender.getNummerBase36());
	}

	@Override
	protected boolean verifyTest() {
		return false;
	}

}
