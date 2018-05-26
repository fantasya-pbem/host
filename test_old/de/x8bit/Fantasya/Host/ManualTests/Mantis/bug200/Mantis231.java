package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ZAT.ZATBase;

public class Mantis231 extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei partei = Partei.getPartei(1);
		Region region = (this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen())).get(0);

		Unit unit1 = this.createSpecialist(partei, region, "Schmiede", true);
		@SuppressWarnings("unused") // Gebäude muss nur erstellt werden
		Building schmiede = Building.getBuilding(unit1.getGebaeude());
		unit1.Befehle.add("GIB TEMP 1 KOMMANDO");
		unit1.Befehle.add("MACHE TEMP 1");
		unit1.Befehle.add("REKRUTIERE 1");
		unit1.Befehle.add("ENDE");
		unit1.Befehle.add("GIB TEMP 1 1000 SILBER");
		
		ZATBase.ClearProxy();
	}

	@Override
	protected boolean verifyTest() {
		throw new UnsupportedOperationException("Not supported yet.");
	}


}
