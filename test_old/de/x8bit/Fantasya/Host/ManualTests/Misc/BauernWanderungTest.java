package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Items.Elefant;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Kamel;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Host.ManualTests.*;
import de.x8bit.Fantasya.util.Random;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Wueste;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collections;

/**
 *
 * @author hb
 */
public class BauernWanderungTest extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
		Partei p = tw.getSpieler1();
		p.setName(getName()+"-Partei");

        Region r = null;

		burgenBauen();

		List<Region> ebenen = tw.nurTerrain(getRegions(), Ebene.class);
		Collections.shuffle(ebenen);
		for (int i=0; i<5; i++) {
			r = ebenen.get(i);
			r.setName("Reiche Ebene #" + (i + 1));
			r.setSilber(r.getBauern() * (Random.W(100) + 20));
			r.setBauern(r.getBauern() - 1000);

			if (i == 0) {
				Building b = Building.Create(Burg.class.getSimpleName(), r.getCoords());
				b.setSize(251);

				Unit u = this.createUnit(p, r);
				u.setName(this.getName() + " 01 Burgherr");
				u.Enter(b);
			}

			getRegions().remove(r);
		}
		for (int i=5; i<15; i++) {
			r = ebenen.get(i);
			r.setName("Übervölkerte " + getName() + "-Region");
			r.setBauern(r.getBauern() + r.freieArbeitsplaetze() + 2000);
			r.setSilber(0);

			getRegions().remove(r);
		}

		List<Region> wuesten = tw.nurTerrain(getRegions(), Wueste.class);
		Collections.shuffle(wuesten);
		for (int i=0; i<5; i++) {
			r = wuesten.get(i);
			r.setName("Leere #" + (i+1));
			r.setResource(Holz.class, 10);
			r.setBauern(0);
			r.setSilber(0);

			getRegions().remove(r);
		}
		List<Region> gletscher = tw.nurTerrain(getRegions(), Gletscher.class);
		Collections.shuffle(gletscher);
		for (int i=0; i<5; i++) {
			r = gletscher.get(i);
			r.setName("Reicher Gletscher #" + (i+1));
			r.setResource(Holz.class, 10);
			r.setBauern(0);
			r.setSilber(i * i * 100 + 100);

			getRegions().remove(r);
		}

		setupScenarioA();

        {
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

	private void setupScenarioA() {
		// Gletscher mit 0 Bauern neben Ebene mit 10000 Bauern
        TestWorld tw = this.getTestWorld();
//		Partei p = tw.getSpieler1();

		List<Region> amGletscher = tw.nurNachbarVon(getRegions(), Gletscher.class);
		List<Region> ebenen = tw.nurTerrain(amGletscher, Ebene.class);

		if (ebenen.isEmpty()) throw new RuntimeException("Keine passende Region für ScenarioA gefunden...");

		// die beiden wollen ganz allein im Ozean sein:
		Region e = ebenen.get(0);
		Region g = null;
		for (Region n : e.getNachbarn()) {
			if (g == null) {
				if (n.getClass() == Gletscher.class) {
					g = n;
					continue;
				}
			}
			Region wech = n.cloneAs(Ozean.class);
			wech.setBauern(0);

			Region.CACHE.remove(n.getCoords());
			Region.CACHE.put(wech.getCoords(), wech);
		}
		for (Region n : g.getNachbarn()) {
			if (n.getCoords().equals(e.getCoords())) continue;

			Region wech = n.cloneAs(Ozean.class);
			wech.setBauern(0);

			Region.CACHE.remove(n.getCoords());
			Region.CACHE.put(wech.getCoords(), wech);
		}

		getRegions().remove(e);
		getRegions().remove(g);

		e.setName(getName() + "-Scenario-A-Ebene");
		e.setBauern(10000);
		e.setResource(Holz.class, 0);
		e.setResource(Pferd.class, 0);
		e.setResource(Kamel.class, 0);
		e.setResource(Elefant.class, 0);
		e.setSilber(e.getBauern() * 100);

		Building b = Building.Create(Burg.class.getSimpleName(), e.getCoords());
		b.setSize(250);
		b.setName("Vermehrungsburg");

		g.setName(getName() + "-Scenario-A-Gletscher");
		g.setBauern(0);
		g.setResource(Holz.class, 0);
		g.setResource(Pferd.class, 0);
		g.setResource(Kamel.class, 0);
		g.setResource(Elefant.class, 0);
		g.setSilber(g.getBauern() * 10 + 1000);

	}

	private void burgenBauen() {
		for (Region r : getRegions()) {
			if (!r.istBetretbar(null)) continue;

			if (Random.W(10) > 5) {
				Building b = Building.Create(Burg.class.getSimpleName(), r.getCoords());
				b.setSize(2);
				b.setName("Gutshaus");
				if (Random.W(10) > 6) {
					b.setSize(10);
					if (Random.W(10) > 6) {
						b.setSize(50);
						if (Random.W(10) > 6) {
							b.setSize(250);
							if (Random.W(10) > 6) {
								b.setSize(1250);
							}
						}
					}
				}
			}
		}
	}

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(Unit.CACHE, new String[] {"01"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u : Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("") && text.contains("")) found = true;
                }
                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben.");
            }
        } // next unit

        return retval;
    }

}
