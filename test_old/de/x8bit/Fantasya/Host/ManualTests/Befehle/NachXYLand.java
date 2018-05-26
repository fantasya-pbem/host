package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Pegasus;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
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
public class NachXYLand extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
		Partei p = tw.createPartei(Mensch.class);
		p.setName(getName() + "-Volk");

        Region r = null; Region o = null; Region no = null; Region oo = null; Region noo = null; Region r_2_1 = null;

		for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
			o = Region.Load(maybe.getCoords().shift(Richtung.Osten));
			no = Region.Load(maybe.getCoords().shift(Richtung.Nordosten));
			oo = Region.Load(o.getCoords().shift(Richtung.Osten));
			noo = Region.Load(no.getCoords().shift(Richtung.Osten));
			r_2_1 = Region.Load(noo.getCoords().shift(Richtung.Osten));

			if (!o.istBetretbar(null)) continue;
			if (!oo.istBetretbar(null)) continue;
			if (!no.istBetretbar(null)) continue;
			if (!noo.istBetretbar(null)) continue;
			if (!r_2_1.istBetretbar(null)) continue;

			// gotcha!
			r = maybe;
			break;
		}
        if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
        getRegions().remove(r); getRegions().remove(o); getRegions().remove(no); getRegions().remove(noo); getRegions().remove(oo);
		getRegions().remove(r_2_1);

		r.setName(getName()+"-Startregion");
		Coords c = r.getCoords();

        { // ANFANG NACH
            Unit u = this.createUnit(p, r);
            u.setName(this.getName() + " 01 " + noo.getCoords().getX() + " " + noo.getCoords().getY());
			u.setItem(Silber.class, 100);
			u.setItem(Pegasus.class, u.getPersonen());
			u.setSkill(Reiten.class, u.getPersonen() * 1650);
			u.Befehle.add("URSPRUNG " + c.getX() + " " + c.getY() );
			u.Befehle.add("NACH (1 1)");
			u.setBeschreibung("Befehl war: " + u.Befehle.get(u.Befehle.size() - 1));

			u = this.createUnit(p, r);
            u.setName(this.getName()+" 02 Heimchen und Beobachter");

            u = this.createUnit(p, r);
            String befehl = "NACH (1 1)";
			u.setName(this.getName() + " 03 " + o.getCoords().getX() + " " + o.getCoords().getY());
			u.setItem(Silber.class, 100);
			u.Befehle.add(befehl);
			u.setBeschreibung("Befehl war: " + befehl);

            u = this.createUnit(p, r);
            u.setName(this.getName() + " 04 " + r_2_1.getCoords().getX() + " " + r_2_1.getCoords().getY());
			u.setItem(Silber.class, 100);
			u.setItem(Pegasus.class, u.getPersonen());
			u.setSkill(Reiten.class, u.getPersonen() * 1650);
			u.Befehle.add("NACH (1 1) (2 1)");
			u.setBeschreibung("Befehl war: " + u.Befehle.get(u.Befehle.size() - 1));

            u = this.createUnit(p, r);
            u.setName(this.getName() + " 05 " + r_2_1.getCoords().getX() + " " + r_2_1.getCoords().getY());
			u.setItem(Silber.class, 100);
			u.setItem(Pegasus.class, u.getPersonen());
			u.setSkill(Reiten.class, u.getPersonen() * 1650);
			u.Befehle.add("NACH (2 0) (2 1)");
			u.setBeschreibung("Befehl war: " + u.Befehle.get(u.Befehle.size() - 1));

            u = this.createUnit(p, r);
            u.setName(this.getName() + " 06 " + oo.getCoords().getX() + " " + oo.getCoords().getY());
			u.setItem(Silber.class, 100);
			u.setItem(Pegasus.class, u.getPersonen());
			u.setSkill(Reiten.class, u.getPersonen() * 1650);
			u.Befehle.add("NACH (2 0) Pause (2 1)");
			u.setBeschreibung("Befehl war: " + u.Befehle.get(u.Befehle.size() - 1));


            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        } // Ende NACH

        { // Anfang ROUTE
            Unit u = this.createUnit(p, r);
            u.setName(this.getName() + " 11 " + o.getCoords().getX() + " " + o.getCoords().getY());
			u.setItem(Silber.class, 100);
			u.setItem(Pegasus.class, u.getPersonen());
			u.setSkill(Reiten.class, u.getPersonen() * 1650);
			u.Befehle.add("ROUTE (1 0) (0 0)");
			u.setBeschreibung("Befehl war: " + u.Befehle.get(u.Befehle.size() - 1));

            u = this.createUnit(p, r);
            u.setName(this.getName() + " 12 " + o.getCoords().getX() + " " + o.getCoords().getY());
			u.setItem(Silber.class, 100);
			u.setItem(Pegasus.class, u.getPersonen());
			u.setSkill(Reiten.class, u.getPersonen() * 1650);
			u.Befehle.add("ROUTE (1 0) PAUSE (0 0) PAUSE");
			u.setBeschreibung("Befehl war: " + u.Befehle.get(u.Befehle.size() - 1));
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

        String missing = this.verifyExpectedUnits(Unit.CACHE, new String[] {"01", "02", "03", "04", "05", "06", "11", "12"});
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
					if (eb.getBefehlCanonical().equals("NACH (1 1)")) found = true;
				}
				if (!found) retval = fail(tokens[1] + ": Der Zielbefehl ist nicht mehr da (oder anders da...).");
            }

            // unit 04 - Pegasus nach (2, 1)
            if (tokens[1].equals("04")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) retval = fail(tokens[1] + ": Einheit ist nicht wie erwartet gereist.");
            }
            // unit 05 - Pegasus nach (2, 1) aber über (2, 0)
            if (tokens[1].equals("05")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) retval = fail(tokens[1] + ": Einheit ist nicht wie erwartet gereist.");
            }
            // unit 06 - Pegasus nach (2, 1) aber über (2, 0) PAUSE !!!
            if (tokens[1].equals("06")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) retval = fail(tokens[1] + ": Einheit ist nicht wie erwartet gereist.");
				boolean found = false;
				for (Einzelbefehl eb : u.BefehleExperimental) {
					if (eb.getBefehlCanonical().equals("NACH (2 1)")) found = true;
				}
				if (!found) retval = fail(tokens[1] + ": Der Zielbefehl ist nicht mehr da (oder anders da...).");
            }


			// ---- ROUTE: ---------------------------


            // unit 11 - bedauernswerter Pegasus auf der Route (1 0) <-> (0 0)
            if (tokens[1].equals("11")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) retval = fail(tokens[1] + ": Einheit ist nicht wie erwartet gereist.");
            }
            // unit 12
            if (tokens[1].equals("12")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) retval = fail(tokens[1] + ": Einheit ist nicht wie erwartet gereist.");
				boolean found = false;
				for (Einzelbefehl eb : u.BefehleExperimental) {
					if (eb.getBefehlCanonical().equalsIgnoreCase("ROUTE (0 0) PAUSE (1 0) PAUSE")) found = true;
				}
				if (!found) retval = fail(tokens[1] + "/[" + Codierung.toBase36(u.getOwner()) + "]: Der Routenbefehl ist nicht mehr da (oder anders da...).");
            }



        } // next unit

        return retval;
    }

}
