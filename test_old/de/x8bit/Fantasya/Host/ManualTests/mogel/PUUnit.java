package de.x8bit.Fantasya.Host.ManualTests.mogel;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Skills.Alchemie;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ZAT.ZATBase;

/**
 * Performance Upgrade für Einheiten
 * @author mogel
 */
public class PUUnit extends TestBase {
	
	@Override
	public void mySetupTest() {
		Partei p = Partei.getPartei(1);
		List<Region> regionen = this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen());
		
		Unit unit = this.createUnit(p, regionen.get(0));
		unit.setItem(Holz.class, 20);
		unit.setItem(Eisen.class, 20);
		unit.setItem(Silber.class, 200);
		unit.setSkill(Alchemie.class, 500);
		unit.setSkill(Burgenbau.class, 500);
		unit.Befehle.add("NACH no");
		unit.Befehle.add("MACHE TEMP 1");
		unit.Befehle.add("REKRUTIERE 1");
		unit.Befehle.add("ENDE");
		unit.setProperty("demo", 1);
		unit.setProperty("deom2", "bar");
		
		Unit unit2 = this.createUnit(p, regionen.get(0));
		unit2.Befehle.add("KONTAKTIERE " + unit.getNummerBase36());
		unit2.Befehle.add("KONTAKTIERE C");
		unit2.Befehle.add("KONTAKTIERE TEMP 1");
		unit2.Befehle.add("GIB irgendwas");
		unit2.Befehle.add("GIB temp 1 200 SILBER");
		
		ZATBase.ClearProxy();
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
