package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

public class Mantis258 extends TestBase {

	@Override
	protected void mySetupTest() {
		TestWorld world = this.getTestWorld();
		Partei partei = world.getSpieler1();
		
		Region r = world.nurNachbarVon(world.nurBetretbar(getRegions()), Ozean.class).get(0); getRegions().remove(r);
		
		Unit u = Unit.CreateUnit("Mensch", partei.getNummer(), r.getCoords());
		u.setPersonen(183);
		u.setSkill(Reiten.class, 183 * 30); // 30 Tage pro Person
		u.setName("Meister des Reitens");
		
		u.Befehle.add("GIB BAUERN 153 Person");
		u.Befehle.add("GIB TEMP 1 10 PERSON");
		u.Befehle.add("MACHE TEMP 1");
		u.Befehle.add("FAULENZEN");
		u.Befehle.add("benenne einheit \"Abkömmlinge des Nichts\"");
		u.Befehle.add("ENDE");
		u.Befehle.add("LERNE Reiten");
	}

	@Override
	protected boolean verifyTest() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
