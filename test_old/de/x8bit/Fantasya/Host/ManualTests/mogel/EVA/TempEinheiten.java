package de.x8bit.Fantasya.Host.ManualTests.mogel.EVA;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

public class TempEinheiten extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei partei = Partei.getPartei(1);
		Region region = (this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen())).get(0);
		
		Unit unit = this.createUnit(partei, region);
		unit.setName("Der Erbauer");
		unit.Befehle.add("MACHE TEMP 1");
		unit.Befehle.add("ENDE");
		unit.Befehle.add("MACHE TEMP 1");
		unit.Befehle.add("ENDE");
		unit.Befehle.add("MACHEN TEMP 2");
		unit.Befehle.add("ENDE");
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
