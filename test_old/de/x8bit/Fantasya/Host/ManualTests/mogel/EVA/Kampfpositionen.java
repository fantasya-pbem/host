package de.x8bit.Fantasya.Host.ManualTests.mogel.EVA;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.util.Codierung;

public class Kampfpositionen extends TestBase {

	@Override
	protected void mySetupTest() {
		Region region = (this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen())).get(0);
		
		Partei dark = new Partei();
		dark.setNummer(Codierung.fromBase36("dark"));
		dark.setMonster(3); // World-Report
		
		Datenbank db = new Datenbank("settings");
		db.SaveSettings(GameRules.INSELKENNUNG_SPIELER, 10);
		db.Close();
		
		Unit unit = this.createUnit(Partei.getPartei(1), region);
		unit.setPersonen(1);
		unit.Befehle.add("KAEMPFE hinten");
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
