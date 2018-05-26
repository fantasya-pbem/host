package de.x8bit.Fantasya.Host.ManualTests.mogel.EVA;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.util.Codierung;

public class Kriege extends TestBase {

	@Override
	protected void mySetupTest() {
		Region region = (this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen())).get(0);
		
		Partei dark = new Partei();
		dark.setNummer(Codierung.fromBase36("dark"));
		dark.setMonster(3); // World-Report
		
		Unit m1 = this.createUnit(dark, region);
		m1.setItem(Schwert.class, 1);
		
		Unit p1 = this.createUnit(Partei.getPartei(1), region);
		p1.Befehle.add("ATTACKIERE " + m1.getNummerBase36());
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
