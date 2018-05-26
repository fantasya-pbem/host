package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100;

import java.util.ArrayList;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Bergwerk;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Skills.Bergbau;
import de.x8bit.Fantasya.Atlantis.Skills.Holzfaellen;
import de.x8bit.Fantasya.Atlantis.Skills.Steinbau;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 * 0000181: Eisen- und Steinabbau-Beschränkung nicht korrekt
 * Bei meinem Nachbarn habe ich gesehen, wie er im Berg H'izih (2, 0, 1)
 * (Koordinaten des Yandil-Kults), der eigentlich nur 89 Steine hergeben
 * sollte, 90 abgebaut hat (er hat das auf Nachfrage bestätigt, hat
 * aber keinen Mantis-Account, so daß ich an seiner Stelle die Meldung mache).
 * Es könnte ein Rundungseffekt sein, da mit mehreren Einheiten abgebaut wird.
 * Analog wird im Berg Shinano (3, -1, 1) 80 statt 79 Eisen abgebaut.
 *
 * gelöst, Test bestanden am 30.08.2010
 * Zusatz-Test (Bergwerk...) auch bestanden.
 *
 * @author hapebe
 */
public class Mantis181 extends TestBase {

	protected void mySetupTest() {
		Partei p = this.getTestWorld().getSpieler1();
		Region r1 = this.getTestWorld().nurTerrain(getRegions(), Berge.class).get(0);
		Region r2 = this.getTestWorld().nurTerrain(getRegions(), Berge.class).get(1);

		r1.setName("Mantis #181 R1");
		getRegions().remove(r1);

		r2.setName("Mantis #181 R2");
		getRegions().remove(r2);

		// Bergleute
		Unit b1 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r1.getCoords());
		Unit b2 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r1.getCoords());
		Unit b3 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r1.getCoords());
		b1.setBeschreibung("Eisen in der Region am Anfang: " + r1.getResource(Eisen.class).getAnzahl());

		// Steinbrecher
		Unit s1 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r1.getCoords());
		Unit s2 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r1.getCoords());
		Unit s3 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r1.getCoords());
		s1.setBeschreibung("Stein in der Region am Anfang: " + r1.getResource(Stein.class).getAnzahl());

		// Holzfäller
		Unit h1 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r1.getCoords()); h1.setName("Holzfäller-Chef");
		Unit h2 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r1.getCoords());
		Unit h3 = Unit.CreateUnit(p.getRasse(), p.getNummer(), r1.getCoords());
		h1.setBeschreibung("Holz in der Region am Anfang: " + r1.getResource(Holz.class).getAnzahl());


		List<Unit> bergleute = new ArrayList<Unit>();
		bergleute.add(b1); bergleute.add(b2); bergleute.add(b3);
		for (Unit u:bergleute) {
			u.setPersonen(10);
			u.setSkill(Bergbau.class, 10*450);
			u.setItem(Silber.class, 1000);
			u.Befehle.add("MACHE Eisen");
		}

		List<Unit> steinbrecher = new ArrayList<Unit>();
		steinbrecher.add(s1); steinbrecher.add(s2); steinbrecher.add(s3);
		for (Unit u:steinbrecher) {
			u.setPersonen(10);
			u.setSkill(Steinbau.class, 10*450);
			u.setItem(Silber.class, 1000);
			u.Befehle.add("MACHE Stein");
		}

		List<Unit> holzfaeller = new ArrayList<Unit>();
		holzfaeller.add(h1); holzfaeller.add(h2); holzfaeller.add(h3);
		for (Unit u:holzfaeller) {
			u.setPersonen(10);
			u.setSkill(Holzfaellen.class, 10*450);
			u.setItem(Silber.class, 1000);
			u.Befehle.add("MACHE Holz");
		}


		{ // analog zu Mantis #148 mit einer "persistent" Resource:
			// neue Region, neues Glück!

			// Gebäude:
			Bergwerk g = (Bergwerk)Building.Create(Bergwerk.class.getSimpleName(), r2.getCoords());
			g.setSize(250);

			// Die Bergbauer:
			Unit u = Unit.CreateUnit(p.getRasse(), p.getNummer(), r2.getCoords());
			u.setName("Bergleute");
			u.setBeschreibung("Erwartet: Es wird die doppelte Anzahl des vorhandenen Eisens abgebaut. Hinterher ist wieder genausoviel Eisen wie vorher vorhanden. Eisen vorher: " + r2.getResource(Eisen.class).getAnzahl());
			u.setPersonen(200);
			u.setSkill(Bergbau.class, 200*180); // damit sollten sie im Bergwerk theoretisch 800 Eisen abbauen
			u.setItem(Silber.class, 2000);
			u.Befehle.add("MACHE Eisen");

			g.Enter(u);
		}


		new Info("Mantis #181 Setup in " + r1 + " " + r1.getCoords() + " und " + r2 + " " + r2.getCoords() + ".", p);

		this.getTestWorld().setContinueWithZAT(false);
	}

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
