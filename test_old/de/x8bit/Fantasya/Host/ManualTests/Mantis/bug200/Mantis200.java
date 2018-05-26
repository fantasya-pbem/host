package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Oel;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Skills.Handel;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

public class Mantis200 extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei partei = Partei.getPartei(1);
		Region region = (this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen())).get(0);
		
		Building burg = Building.Create("Burg", region.getCoords());
		burg.setSize(500);
		
		Unit unit = this.createUnit(partei, region);
		unit.setSkill(Handel.class, 300);
		unit.Befehle.add("HANDEL KAUFE 10 PELZ");
		unit.Befehle.add("HANDEL VERKAUFE 200 OEL");
		unit.setItem(Silber.class, 20000);
		unit.setItem(Oel.class, 200);
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
