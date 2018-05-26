package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Balsam;
import de.x8bit.Fantasya.Atlantis.Items.Juwel;
import de.x8bit.Fantasya.Atlantis.Items.Seide;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Skills.Handel;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

public class Mantis215 extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei partei = Partei.getPartei(1);
		Region region = (this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen())).get(0);
		
		Building burg = Building.Create("Burg", region.getCoords());
		burg.setSize(1323);
		
		Unit unit = this.createUnit(partei, region);
		unit.setSkill(Handel.class, 30);
		unit.setPersonen(1);
		unit.setItem(Silber.class, 20000);
		unit.setGebaeude(burg.getNummer());
		unit.setItem(Seide.class, 20);
		unit.setItem(Balsam.class, 20);
		unit.setItem(Juwel.class, 20);
		
		unit.Befehle.add("HANDEL VERKAUFE 5 Balsam");
		unit.Befehle.add("HANDEL VERKAUFE 5 Seide");
		unit.Befehle.add("HANDEL VERKAUFE 5 Juwel");
	}

	@Override
	protected boolean verifyTest() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
