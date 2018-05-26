package de.x8bit.Fantasya.Host.ManualTests.Misc;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.util.Random;

/**
 *
 * @author hapebe
 */
public class TestRegionsSicht extends TestBase {

    public TestRegionsSicht () {
        super();
    }

    @Override
	protected void mySetupTest() {
		Partei p = this.getTestWorld().getSpieler1();
		List<Region> lands = this.getTestWorld().nurBetretbar(getRegions());
		Region r = null;

		// nach einer Region suchen, die drei Nachbarn im Südosten hat...
		Region r1 = null, r2 = null, r3 = null;
		for (Region r0 : lands) {
			r1 = Region.Load(r0.getCoords().shift(Richtung.Suedosten));
			if (!r1.istBetretbar(null)) continue;
			r2 = Region.Load(r1.getCoords().shift(Richtung.Suedosten));
			if (!r2.istBetretbar(null)) continue;
			r3 = Region.Load(r2.getCoords().shift(Richtung.Suedosten));
			if (!r3.istBetretbar(null)) continue;

			// gotcha!
			r = r0; 
			getRegions().remove(r);
			getRegions().remove(r1);
			getRegions().remove(r2);
			getRegions().remove(r3);
			break;
		}
		if (r == null) throw new IllegalStateException("Keine Region zum Wander-Test gefunden - einfach nochmal versuchen.");
		r.setStrassensteine(Richtung.Suedosten, r.getSteineFuerStrasse());
		r1.setStrassensteine(Richtung.Nordwesten, r1.getSteineFuerStrasse());
		r1.setStrassensteine(Richtung.Suedosten, r1.getSteineFuerStrasse());
		r2.setStrassensteine(Richtung.Nordwesten, r2.getSteineFuerStrasse());
		r2.setStrassensteine(Richtung.Suedosten, r2.getSteineFuerStrasse());
		r3.setStrassensteine(Richtung.Nordwesten, r3.getSteineFuerStrasse());

		{
			Unit u = this.createUnit(p, r);
			u.setSkill(Reiten.class, 90);
			u.setItem(Pferd.class, 1);
			u.Befehle.add("NACH SO SO SO SO SO");
			u.setBeschreibung("Erwartet: Reitet 4 Regionen weit auf Straßen, Durchreise-Regionen tauchen im CR auf.");
		}


		{
			r = lands.get(0); lands.remove(r);
			@SuppressWarnings("unused") // Einheit landed im Proxy
			Unit u = this.createUnit(p, r);
		}

		for (int i=0; i<10; i++) {
			int idx = Random.rnd(0, lands.size() - 1);
			r = lands.get(idx); lands.remove(r);
			@SuppressWarnings("unused") // Einheit landed im Proxy
			Unit u = this.createUnit(p, r);
		}


		new Info("TestRegionsSicht Setup.", p);
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
