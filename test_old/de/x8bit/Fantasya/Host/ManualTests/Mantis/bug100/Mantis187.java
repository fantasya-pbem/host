package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Schmiede;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Waffenbau;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.util.Codierung;

/**
 *
 * @author hapebe
 */
public class Mantis187 extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei p = this.getTestWorld().getSpieler1();
		Region r = getRegions().get(0);

		// Gebäude:
		Schmiede schmiede = (Schmiede)Building.Create("Schmiede", r.getCoords());
		schmiede.setSize(48);

		// Die Schmiede selbst:
		Unit u = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
		u.setPersonen(40);
		u.setSkill(Waffenbau.class, 40*840);
		u.setItem(Eisen.class, 100);
		u.setItem(Holz.class, 140);
		u.setItem(Silber.class, 10000);
		u.Befehle.add("MACHE Kriegshammer");
		u.setLongOrder("MACHE Kriegshammer");

		schmiede.Enter(u);

		int schmiedeNummer = u.getNummer();

		// der Stein-Geber:
		u = Unit.CreateUnit(p.getRasse(), p.getNummer(), r.getCoords());
		u.setPersonen(1);
		u.setItem(Stein.class, 170);
		u.setItem(Silber.class, 100);
		u.Befehle.add("LIEFERE " + Codierung.toBase36(schmiedeNummer) + " 170 Stein");
		u.setLongOrder("LIEFERE " + Codierung.toBase36(schmiedeNummer) + " 170 Stein");
		
		new Info("Mantis #167 Setup in " + r + " " + r.getCoords() + ".", p);
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
