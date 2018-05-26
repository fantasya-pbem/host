package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Skills.Holzfaellen;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

public class Mantis276 extends TestBase {

	@Override
	protected void mySetupTest() {
		TestWorld world = this.getTestWorld();
		Partei partei = world.getSpieler1();
		
		Region r = world.nurNachbarVon(world.nurBetretbar(getRegions()), Ozean.class).get(0); getRegions().remove(r);
		
		Unit u = Unit.CreateUnit("Mensch", partei.getNummer(), r.getCoords());
		u.setPersonen(10);
		u.setSkill(Holzfaellen.class, 180 * 30); // 30 Tage pro Person
		u.setName("holzfäller");
		u.Befehle.add("MACHE 17 Holz");
		
		r.setResource(Holz.class, 100);
	}

	@Override
	protected boolean verifyTest() {
		for(Unit u : Unit.CACHE)
		{
			if (u.getNummer() != 2) continue;
			for(Einzelbefehl eb : u.BefehleExperimental)
			{
				System.out.println(eb);
			}
		}
		return true;
	}

}
