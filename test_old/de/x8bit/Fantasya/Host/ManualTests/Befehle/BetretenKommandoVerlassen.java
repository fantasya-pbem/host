package de.x8bit.Fantasya.Host.ManualTests.Befehle;


import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Ships.Langboot;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 *
 * @author hb
 */
public class BetretenKommandoVerlassen extends TestBase {

    @Override
    protected void mySetupTest() {
		TestWorld tw = this.getTestWorld();
        Partei p = tw.getSpieler1();

        String fernesSchiff = null;
		{
			Region r = tw.nurNachbarVon(tw.nurBetretbar(getRegions()), Ozean.class).get(0);
			getRegions().remove(r);
			
			Building b = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			b.setSize(50);

			Ship s = Ship.Create(Langboot.class.getSimpleName(), r.getCoords());
			s.setGroesse(s.getConstructionSize());
			s.setFertig(true);

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+ " 01");
			u.Befehle.add("BETRETE GEBAEUDE " + b.getNummerBase36());
			u.Befehle.add("LERNE Burgenbau");

            u = this.createUnit(p, r);
            u.setName(this.getName()+ " 02");
			u.Befehle.add("BETRETE SCHIFF " + s.getNummerBase36());
			u.Befehle.add("LERNE Segeln");

            new Info(this.getName() + "-Betreten Setup in " + r + ".", u, u.getCoords());

			fernesSchiff = s.getNummerBase36();
        }

        {
			Region r = tw.nurNachbarVon(tw.nurBetretbar(getRegions()), Ozean.class).get(0);
			getRegions().remove(r);

			Building b = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			b.setSize(50);

			Ship s = Ship.Create(Langboot.class.getSimpleName(), r.getCoords());
			s.setGroesse(s.getConstructionSize());
			s.setFertig(true);

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+ " 03 Alter");
			u.Befehle.add("VERLASSE");
			u.Befehle.add("LERNE Burgenbau");
			u.Enter(b);

            u = this.createUnit(p, r);
            u.setName(this.getName()+ " 04 Alter");
			u.Befehle.add("VERLASSE");
			u.Befehle.add("LERNE Segeln");
			u.Enter(s);

            u = this.createUnit(p, r);
            u.setName(this.getName()+ " 05 Neuer");
			u.Befehle.add("LERNE Burgenbau");
			u.Enter(b);

            u = this.createUnit(p, r);
            u.setName(this.getName()+ " 06 Neuer");
			u.Befehle.add("LERNE Segeln");
			u.Enter(s);

            u = this.createUnit(p, r);
            u.setName(this.getName()+ " 07 Fern-Passagier");
			u.setBeschreibung("Versucht, auf ein Schiff zu gelangen, das gar nicht in der Region ist.");
			u.Befehle.add("LERNE Segeln");
			u.Befehle.add("BETRETE SCHIFF " + fernesSchiff);
			u.Befehle.add("// BETRETE SCHIFF " + fernesSchiff);

            new Info(this.getName() + "-Verlassen Setup in " + r + ".", u, u.getCoords());
        }

        {
			Region r = null;
			for (Region maybe : tw.nurNachbarVon(tw.nurBetretbar(getRegions()), Ozean.class)) {
				if (maybe.getBauern() >= 100) {
					// gotcha!
					r = maybe;
					break;
				}
			}
            if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getClass().getSimpleName() + " (TEMP-Übergaben) gefunden - einfach nochmal probieren...");
			getRegions().remove(r);

			Building b = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			b.setSize(50);

			Ship s = Ship.Create(Langboot.class.getSimpleName(), r.getCoords());
			s.setGroesse(s.getConstructionSize());
			s.setFertig(true);

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+ " 13 Alter");
			u.Befehle.add("LERNE Burgenbau");
			u.Befehle.add("MACHE TEMP abc");
			u.Befehle.add("BENENNE EINHEIT \"" + this.getName() + " 15 Neuer\"");
			u.Befehle.add("REKRUTIERE 1");
			u.Befehle.add("LERNE Steinbau");
			u.Befehle.add("ENDE");
			u.Befehle.add("GIB TEMP abc 500 Silber");
			u.Befehle.add("GIB TEMP abc KOMMANDO");
			u.Enter(b);

            u = this.createUnit(p, r);
            u.setName(this.getName()+ " 14 Alter");
			u.Befehle.add("GIB TEMP def KOMMANDO");
			u.Befehle.add("LERNE Segeln");
			u.Enter(s);

			u = this.createUnit(p, r);
			u.Befehle.add("MACHE TEMP def");
			u.Befehle.add("BENENNE EINHEIT \"" + this.getName() + " 16 Neuer\"");
			u.Befehle.add("REKRUTIERE 1");
			u.Befehle.add("BETRETE SCHIFF " + s.getNummerBase36());
			u.Befehle.add("LERNE Schiffbau");
			u.Befehle.add("ENDE");
			u.Befehle.add("GIB TEMP def 500 Silber");
			u.Befehle.add("LERNE Speerkampf");

            new Info(this.getName() + "-Gib-Kommando Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
//        Partei p = this.getTestWorld().getSpieler1();

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04", "05", "06", "07", "13", "14", "15", "16"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            
			// passive Kommando-Übernahme: Es ist keiner drin gewesen.

			if (tokens[1].equals("01")) {
				if (u.getGebaeude() == 0) retval = fail(tokens[1] + ": Betreten der Burg hat nicht geklappt.");
            }

            if (tokens[1].equals("02")) {
				if (u.getSchiff() == 0) retval = fail(tokens[1] + ": Betreten des Schiffs hat nicht geklappt.");
            }

            
			// passive Kommando-Übergabe: Besitzer verlässt

			if (tokens[1].equals("03")) {
				if (u.getGebaeude() != 0) retval = fail(tokens[1] + ": Verlassen der Burg hat nicht geklappt.");
            }

            if (tokens[1].equals("04")) {
				if (u.getSchiff() != 0) retval = fail(tokens[1] + ": Verlassen des Schiffs hat nicht geklappt.");
            }

            if (tokens[1].equals("05")) {
				int burg = u.getGebaeude();
				if (burg == 0) {
					retval = fail(tokens[1] + ": Ist nicht in der Burg.");
					continue;
				}
				Building b = Building.getBuilding(burg);
				if (b == null) {
					retval = fail(tokens[1] + ": Die Burg existiert nicht.");
					continue;
				}
				if (b.getOwner() != u.getNummer()) retval = fail(tokens[1] + ": Die Kommando-Übergabe hat nicht geklappt (Burg verlassen).");
            }

            if (tokens[1].equals("06")) {
				int schiff = u.getSchiff();
				if (schiff == 0) {
					retval = fail(tokens[1] + ": Ist nicht auf dem Schiff.");
					continue;
				}
				Ship s = Ship.Load(schiff);
				if (s == null) {
					retval = fail(tokens[1] + ": Das Schiff existiert nicht.");
					continue;
				}
				if (s.getOwner() != u.getNummer()) retval = fail(tokens[1] + ": Die Kommando-Übergabe hat nicht geklappt (Schiff verlassen).");
            }

            if (tokens[1].equals("07")) {
				int schiff = u.getSchiff();
				if (schiff != 0) {
					retval = fail(tokens[1] + ": Hat ein Schiff betreten, das gar nicht in der Region ist.");
				}
            }



            // kooperative Kommando-Übergabe (alle bleiben drin, explizit per GIB KOMMANDO)

			if (tokens[1].equals("13")) {
				if (u.getGebaeude() == 0) retval = fail(tokens[1] + ": Ist nicht in der Burg.");
            }

            if (tokens[1].equals("14")) {
				if (u.getSchiff() == 0) retval = fail(tokens[1] + ": Ist nicht auf dem Schiff.");
            }

            if (tokens[1].equals("15")) {
				int burg = u.getGebaeude();
				if (burg == 0) {
					retval = fail(tokens[1] + ": Ist nicht in der Burg.");
					continue;
				}
				Building b = Building.getBuilding(burg);
				if (b == null) {
					retval = fail(tokens[1] + ": Die Burg existiert nicht.");
					continue;
				}
				if (b.getOwner() != u.getNummer()) retval = fail(tokens[1] + ": Die Kommando-Übergabe hat nicht geklappt (Burg verlassen).");
            }

            if (tokens[1].equals("16")) {
				int schiff = u.getSchiff();
				if (schiff == 0) {
					retval = fail(tokens[1] + ": Ist nicht auf dem Schiff.");
					continue;
				}
				Ship s = Ship.Load(schiff);
				if (s == null) {
					retval = fail(tokens[1] + ": Das Schiff existiert nicht.");
					continue;
				}
				if (s.getOwner() != u.getNummer()) retval = fail(tokens[1] + ": Die Kommando-Übergabe hat nicht geklappt (Schiff verlassen).");
            }


        } // next unit

        return retval;
    }

}
