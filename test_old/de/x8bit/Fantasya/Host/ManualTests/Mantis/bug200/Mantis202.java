package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Schiffswerft;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Ships.Langboot;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 *
 * @author hapebe
 */
public class Mantis202 extends TestBase {

	protected void mySetupTest() {
		TestWorld world = this.getTestWorld();

		Partei p = world.getSpieler1();

		{
			Region r = world.nurNachbarVon(world.nurBetretbar(getRegions()), Ozean.class).get(0); getRegions().remove(r);
			Unit u = this.createSpecialist(p, r, Schiffswerft.class.getSimpleName(), true);
			u.setBeschreibung("Erwartet: Fängt an, ein Langboot zu bauen. Es wird die Hälfte des Holzes eingespart.");
		}

		{ // und noch der Fall der Fertigstellung:
			Region r = world.nurNachbarVon(world.nurBetretbar(getRegions()), Ozean.class).get(0); getRegions().remove(r);

			Unit kap = this.createKapitaen(p, r, Langboot.class.getSimpleName());
			Ship s = Ship.Load(kap.getSchiff());
			s.setGroesse(45);
			s.setFertig(false);

			Unit u = this.createSpecialist(p, r, Schiffswerft.class.getSimpleName(), true);
			u.Befehle.clear();
			u.Befehle.add("MACHE Schiff " + s.getNummerBase36());
			u.setBeschreibung("Erwartet: Stellt Langboot " + s + " fertig. Es wird die Hälfte des Holzes eingespart.");
		}


		new Info("Mantis #202 Setup.", p);
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
