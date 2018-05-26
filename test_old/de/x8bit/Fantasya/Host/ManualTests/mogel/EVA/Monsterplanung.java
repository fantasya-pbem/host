package de.x8bit.Fantasya.Host.ManualTests.mogel.EVA;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Host.Datenbank;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.util.Codierung;

public class Monsterplanung extends TestBase {

	@Override
	protected void mySetupTest() {
		// muss hochgesetzt werden, sonst funktionieren einige Monsteraussetzungen nicht
		Datenbank db = new Datenbank("settings");
		db.SaveSettings(GameRules.INSELKENNUNG_SPIELER, 10);
		db.Close();
		
		// Monsterpartei erstellen
		Partei dark = new Partei();
		dark.setNummer(Codierung.fromBase36("dark"));
		dark.setMonster(3); // World-Report
		
		// das Opfer laden
		Partei partei = Partei.getPartei(1);
		Region region = (this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen())).get(0);
		Unit unit = this.createUnit(partei, region);
		unit.setSkill(Wahrnehmung.class, 20000);
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
