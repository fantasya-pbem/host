package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skills.Handel;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

public class Mantis233 extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei partei = Partei.getPartei(1);
		Region region = (this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen())).get(0);
		
		Unit unit = this.createUnit(partei, region);
		unit.setSkill(Handel.class, 300);
		unit.Befehle.add("rekrutiere 10");
	}
 
	@Override
	protected boolean verifyTest() { return false; }

}
