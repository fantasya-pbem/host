package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.util.Codierung;

public class Mantis206 extends TestBase {

	@Override
	protected void mySetupTest() {
		Region region = (this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen())).get(0);

		Unit unit = this.createUnit(Partei.getPartei(1), region);
		unit.setItem(Schwert.class, 20);
		unit.setPersonen(20);
		
		Building building = Building.Create("Burg", region.getCoords());
		building.setSize(20);
		unit.setGebaeude(building.getNummer());
		
		unit = this.createUnit(Partei.getPartei(Codierung.fromBase36("dark")), region);
		unit.setPersonen(10);
		unit.setItem(Eisen.class, 5);
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
