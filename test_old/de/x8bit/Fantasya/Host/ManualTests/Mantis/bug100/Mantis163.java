package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

public class Mantis163 extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei partei = Partei.getPartei(1);
		Region region = this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen()).get(0);
		
		Unit burgenbauer = this.createSpecialist(partei, region, "Werkstatt", true); // eigentlich keiner ab passt schon
		burgenbauer.setItem(Silber.class, 0);
		burgenbauer.Befehle.clear();
		burgenbauer.setName("Burgenbauer");
		
		Building werkstatt = Building.getBuilding(burgenbauer.getGebaeude());
		
		Unit wagner = this.createSpecialist(partei, region, "Werkstatt", false);
		wagner.setItem(Silber.class, 0);
		wagner.Befehle.clear();
		
		Unit someone = this.createUnit(partei, region);
		someone.setItem(Silber.class, 2000);
		
		wagner.Befehle.add("BETRETE GEBAEUDE " + werkstatt.getNummerBase36());
		burgenbauer.Befehle.add("GIB " + wagner.getNummerBase36() + " KOMMANDO");
		someone.Befehle.add("GIBT " + wagner.getNummerBase36() + " 115 SILBER");
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
