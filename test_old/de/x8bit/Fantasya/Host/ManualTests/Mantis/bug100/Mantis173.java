package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.util.Codierung;

public class Mantis173 extends TestBase {

	@Override
	protected void mySetupTest() {
		Region region = (this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen())).get(0);

		Unit unit1 = this.createUnit(Partei.getPartei(1), region);
		unit1.setItem(Schwert.class, 20);
		unit1.setPersonen(1);
		
		Unit unit2 = this.createUnit(Partei.getPartei(Codierung.fromBase36("dark")), region);
		unit2.setPersonen(1);
		unit2.setItem(Silber.class, 0);
		
		unit1.Befehle.add("GIB e 20 SILBER");
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
