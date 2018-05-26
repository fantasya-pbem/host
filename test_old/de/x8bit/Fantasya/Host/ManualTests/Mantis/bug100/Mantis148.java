package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Saegewerk;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Atlantis.Skills.Holzfaellen;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hapebe
 */
public class Mantis148 extends TestBase {

	protected void mySetupTest() {
		Partei p = this.getTestWorld().getSpieler1();
		Region r = this.getTestWorld().nurTerrain(getRegions(), Wald.class).get(0);

		// Gebäude:
		Saegewerk g = (Saegewerk)Building.Create(Saegewerk.class.getSimpleName(), r.getCoords());
		g.setSize(250);

		// Die Holzfäller:
		Unit u = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
		u.setName("Holzfäller");
		u.setBeschreibung("Erwartet: 600 Holz werden abgebaut, es gibt knapp 300 Bäume weniger. Bäume vorher: " + r.getResource(Holz.class).getAnzahl());
		u.setPersonen(200);
		u.setSkill(Holzfaellen.class, 200*90); // damit sollten sie im Sägewerk 600 Bäume erledigen können
		u.setItem(Eisen.class, 100);
		u.setItem(Holz.class, 140);
		u.setItem(Silber.class, 20000);
		u.setLongOrder("MACHE Holz");

		g.Enter(u);
		new Info("Mantis #148 Setup in " + r + " " + r.getCoords() + ".", p);
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
