package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Wald;
import de.x8bit.Fantasya.Atlantis.Ships.Boot;
import de.x8bit.Fantasya.Atlantis.Ships.Drachenschiff;
import de.x8bit.Fantasya.Atlantis.Ships.Langboot;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;
import de.x8bit.Fantasya.util.Codierung;

/**
 * <p>Test für die Koordinaten-basierten Befehlsvarianten von NACH und ROUTE.</p>
 * <p>Die grundlegende Funktion des Reisens bitte lieber mit den Tests "Nach"
 * und "Route" testen...</p>
 * @author hb
 */
public class NachXYMeer extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
		Partei p = tw.createPartei(Mensch.class);
		p.setName(getName() + "-Volk");

        Region r_0_0 = null; Region r_0_1 = null; Region r_1_1 = null; Region r_2_1 = null; // Ozean
		Region r_m1_0 = null;
		Region r_1_0 = null; // Land

		for (Region maybe : tw.nurTerrain(getRegions(), Ozean.class)) {
			r_m1_0 = Region.Load(maybe.getCoords().shift(Richtung.Westen));
			r_0_1 = Region.Load(maybe.getCoords().shift(Richtung.Nordosten));
			r_1_1 = Region.Load(r_0_1.getCoords().shift(Richtung.Osten));
			r_2_1 = Region.Load(r_1_1.getCoords().shift(Richtung.Osten));

			r_1_0 = Region.Load(maybe.getCoords().shift(Richtung.Osten));
			if (!r_1_0.istBetretbar(null)) continue; // r_1_0 muss Land sein.
			if (r_1_0.getClass() == Ebene.class) continue;
			if (r_1_0.getClass() == Wald.class) continue; // soll nicht erreichbar für größere Schiffe sein.

			if (!(r_m1_0 instanceof Ozean)) continue;
			if (!(r_0_1 instanceof Ozean)) continue;
			if (!(r_1_1 instanceof Ozean)) continue;
			if (!(r_2_1 instanceof Ozean)) continue;

			// gotcha!
			r_0_0 = maybe;
			break;
		}
        if (r_0_0 == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
		getRegions().remove(r_0_0);
		getRegions().remove(r_m1_0);
		getRegions().remove(r_1_0);
		getRegions().remove(r_1_1);
		getRegions().remove(r_2_1);
		getRegions().remove(r_1_0);

		r_0_0.setName(getName()+"-Startregion");
		Coords c = r_0_0.getCoords();

        { // NACH ...
            Unit u = this.createKapitaen(p, r_0_0, Boot.class.getSimpleName());
            u.setName(this.getName() + " 01 " + r_1_0.getCoords().getX() + " " + r_1_0.getCoords().getY());
			u.setItem(Silber.class, 100);
			u.Befehle.add("URSPRUNG " + c.getX() + " " + c.getY() );
			u.Befehle.add("NACH (2 1)");
			u.setBeschreibung("Befehl war: " + u.Befehle.get(u.Befehle.size() - 1));

			u = this.createKapitaen(p, r_0_0, Boot.class.getSimpleName());
            u.setName(this.getName()+" 02 Heimchen und Beobachter");

            u = this.createKapitaen(p, r_0_0, Boot.class.getSimpleName());
            String befehl = "NACH (0 1) (2 1)";
			u.setName(this.getName() + " 03 " + r_1_1.getCoords().getX() + " " + r_1_1.getCoords().getY());
			u.setItem(Silber.class, 100);
			u.Befehle.add(befehl);
			u.setBeschreibung("Befehl war: " + befehl);

            u = this.createKapitaen(p, r_0_0, Boot.class.getSimpleName());
            befehl = "NACH (0 1) PAUSE (2 1)";
			u.setName(this.getName() + " 04 " + r_0_1.getCoords().getX() + " " + r_0_1.getCoords().getY());
			u.setItem(Silber.class, 100);
			u.Befehle.add(befehl);
			u.setBeschreibung("Befehl war: " + befehl);

            u = this.createKapitaen(p, r_0_0, Boot.class.getSimpleName());
            befehl = "NACH (0 1)";
			u.setName(this.getName() + " 05 " + r_0_1.getCoords().getX() + " " + r_0_1.getCoords().getY());
			u.setItem(Silber.class, 100);
			u.Befehle.add(befehl);
			u.setBeschreibung("Befehl war: " + befehl);

            new Info(this.getName() + " Setup in " + r_0_0 + ".", u, u.getCoords());
        } // Ende NACH

        { // ROUTE
            // sollte vor der Küste von (1 0) stranden:
			Unit u = this.createKapitaen(p, r_2_1, Langboot.class.getSimpleName());
            String befehl = "ROUTE (1 0) (-1 0) PAUSE (2 1)";
            u.setName(this.getName() + " 11");
			u.setItem(Silber.class, 100);
			u.Befehle.add(befehl);
			u.setBeschreibung("Befehl war: " + befehl);

            // sollte in (0 0) ankommen:
			u = this.createKapitaen(p, r_2_1, Langboot.class.getSimpleName());
            befehl = "ROUTE (0 1) (0 0) PAUSE (2 1)";
            u.setName(this.getName() + " 12 " + r_0_0.getCoords().getX() + " " + r_0_0.getCoords().getY());
			u.setItem(Silber.class, 100);
			u.Befehle.add(befehl);
			u.setBeschreibung("Befehl war: " + befehl);

            // Schnellpendler:
			u = this.createKapitaen(p, r_1_1, Drachenschiff.class.getSimpleName());
            befehl = "ROUTE (2 1) (1 1)";
            u.setName(this.getName() + " 13 " + r_2_1.getCoords().getX() + " " + r_2_1.getCoords().getY());
			u.setItem(Silber.class, 100);
			u.Befehle.add(befehl);
			u.setBeschreibung("Befehl war: " + befehl);
		}


    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
//        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(Unit.CACHE, new String[] {"01", "02", "03", "04", "05", "11", "12", "13"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) retval = fail(tokens[1] + ": Einheit ist nicht wie erwartet gereist.");
            }

            // unit 03
            if (tokens[1].equals("03")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) retval = fail(tokens[1] + ": Einheit ist nicht wie erwartet gereist.");
				boolean found = false;
				for (Einzelbefehl eb : u.BefehleExperimental) {
					if (eb.getBefehlCanonical().equals("NACH (2 1)")) found = true;
				}
				if (!found) retval = fail(tokens[1] + ": Der Zielbefehl ist nicht mehr da (oder anders da...).");
            }

            // unit 04
            if (tokens[1].equals("04")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) retval = fail(tokens[1] + ": Einheit ist nicht wie erwartet gereist.");
				boolean found = false;
				for (Einzelbefehl eb : u.BefehleExperimental) {
					if (eb.getBefehlCanonical().equals("NACH (2 1)")) found = true;
				}
				if (!found) retval = fail(tokens[1] + ": Der Zielbefehl ist nicht mehr da (oder anders da...).");
            }

            // unit 05
            if (tokens[1].equals("05")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) retval = fail(tokens[1] + ": Einheit ist nicht wie erwartet gereist.");
            }
			

            // unit 06
            if (tokens[1].equals("06")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) retval = fail(tokens[1] + ": Einheit ist nicht wie erwartet gereist.");
				boolean found = false;
				for (Einzelbefehl eb : u.BefehleExperimental) {
					if (eb.getBefehlCanonical().equals("NACH (2 1)")) found = true;
				}
				if (!found) retval = fail(tokens[1] + ": Der Zielbefehl ist nicht mehr da (oder anders da...).");
            }


			// ---- ROUTE: ---------------------------


            // unit 11 - Langboot sollte an Land aufgehalten werden, der Befehl noch original vorhanden
            if (tokens[1].equals("11")) {
				boolean found = false;
				for (Einzelbefehl eb : u.BefehleExperimental) {
					if (eb.getBefehlCanonical().equalsIgnoreCase("ROUTE (1 0) (-1 0) PAUSE (2 1)")) found = true;
				}
				if (!found) retval = fail(tokens[1] + ": Der Zielbefehl ist nicht mehr da (oder anders da...).");
            }

            // unit 12
            if (tokens[1].equals("12")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) retval = fail(tokens[1] + ": Einheit ist nicht wie erwartet gereist.");
				boolean found = false;
				for (Einzelbefehl eb : u.BefehleExperimental) {
					if (eb.getBefehlCanonical().equalsIgnoreCase("ROUTE (2 1) (0 1) (0 0) PAUSE")) found = true;
				}
				if (!found) retval = fail(tokens[1] + "/[" + Codierung.toBase36(u.getOwner()) + "]: Der Routenbefehl ist nicht mehr da (oder anders da...).");
            }

            // unit 13 - Pendler
            if (tokens[1].equals("13")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) retval = fail(tokens[1] + ": Einheit ist nicht wie erwartet gereist.");
				boolean found = false;
				for (Einzelbefehl eb : u.BefehleExperimental) {
					if (eb.getBefehlCanonical().equalsIgnoreCase("ROUTE (1 1) (2 1)")) found = true;
				}
				if (!found) retval = fail(tokens[1] + "/[" + Codierung.toBase36(u.getOwner()) + "]: Der Routenbefehl ist nicht mehr da (oder anders da...).");
            }



        } // next unit

        return retval;
    }

}
