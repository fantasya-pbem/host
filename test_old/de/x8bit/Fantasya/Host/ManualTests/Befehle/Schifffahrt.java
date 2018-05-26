package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Ships.Boot;
import de.x8bit.Fantasya.Atlantis.Ships.Drachenschiff;
import de.x8bit.Fantasya.Atlantis.Ships.Galeone;
import de.x8bit.Fantasya.Atlantis.Ships.Karavelle;
import de.x8bit.Fantasya.Atlantis.Ships.Langboot;
import de.x8bit.Fantasya.Atlantis.Ships.Tireme;
import de.x8bit.Fantasya.Atlantis.Skills.Burgenbau;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.util.Codierung;

/**
 *
 * @author hb
 */
public class Schifffahrt extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = getTestWorld().createPartei(Mensch.class);
		p.setName(getName() + "-Partei");
        p.setUrsprung(new Coords(0,0,1));

        // enthält NACH und ROUTE
		this.setupEinfacheReisen(); // Einheiten 01 .. 09

        // TODO Zauber testen (Effekte)

        // TODO an- und ablegen testen
        this.setupHafenZeugs(); // Einheiten 1x

        // TODO Beladung testen

    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
        @SuppressWarnings("unused") // Bug in Eclipse - wird weiter unten verwendet
		List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units,
				new String[]
				{"01", "02", "03", "04", "05", "06", "07", "08", "09",
				 "11", "12", "13", "14", "15", "16", "17"}
		);
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");
			String uRef = tokens[1] + "[" + u.getNummerBase36() + "] von [" + Codierung.toBase36(u.getOwner()) + "] - ";

            // unit 01
            if (tokens[1].equals("01")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                    retval = this.fail(uRef + "ist nicht in der erwarteten Region.");
                }
                boolean found = false;
                for (Einzelbefehl eb : u.BefehleExperimental) {
                    if (eb.getBefehlCanonical().equalsIgnoreCase("NACH no PAUSE no no")) found = true;
                }
                if (!found) retval = fail(uRef + "Der verbleibende / neue Befehl wurde nicht gefunden.");
            }

            // unit 02
            if (tokens[1].equals("02")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                    retval = this.fail(uRef + "Ist nicht in der erwarteten Region.");
                }
                messages = Message.Retrieve(null, u.getCoords(), u);
//                boolean found = false;
//                for (Message msg : messages) {
//                    String text = msg.getMessage().toLowerCase();
//                    if (text.contains("") && text.contains("")) found = true;
//                }
//                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben.");
            }

            // 0x Units:
            if ( (tokens[1].equals("03")) || (tokens[1].equals("04"))
                    || (tokens[1].equals("05")) || (tokens[1].equals("06"))
                    || (tokens[1].equals("07")) || (tokens[1].equals("08"))
					|| (tokens[1].equals("09")) ) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                    retval = fail(uRef + "Ist nicht in der erwarteten Region.");
                }

                if (tokens[1].equals("04")) {
                    boolean found = false;
                    for (Einzelbefehl eb : u.BefehleExperimental) {
                        if (eb.getBefehlCanonical().toLowerCase().contains("nach")) found = true;
                    }
                    if (found) retval = fail(uRef + "Der NACH-Befehl sollte nicht mehr da sein.");
                }
            }

            // 1x Units:
			if ( (tokens[1].equals("11")) || (tokens[1].equals("12"))
                    || (tokens[1].equals("13")) || (tokens[1].equals("15"))
					|| (tokens[1].equals("17")) ) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                    retval = fail(uRef + "Ist nicht in der erwarteten Region.");
                }
            }

        } // next unit

        return retval;
    }

    /**
     * Alle Schiffstypen werden per NACH auf die Reise geschickt, teils mit PAUSE,
     * teils mit passenden, teils mit zu kurzen, teils mit zu langen Routen.
     */
    private void setupEinfacheReisen() {
        Partei p = getTestWorld().createPartei(Mensch.class);
		p.setUrsprung(new Coords(0,0,1));
        List<Region> regs = new ArrayList<Region>();

        for (Region maybe : this.getTestWorld().nurTerrain(getRegions(), Ozean.class)) {
            Region step = maybe;
            for (int i = 1; i < 10; i++) {
                regs.add(step);
                Region neighbor = Region.Load(step.getCoords().shift(Richtung.Nordosten));
                if (!(neighbor instanceof Ozean)) {
                    regs.clear();
                    break;
                }
                step = neighbor;
            }

            if (regs.size() > 0) break;

        }
		if (regs.isEmpty()) throw new IllegalStateException("Keine passende Region für " + getName() + " gefunden - einfach nochmal probieren...");
        for (Region r : regs) getRegions().remove(r);
		
        Region r = regs.get(0);
        {
            Unit u = this.createKapitaen(p, r, Boot.class.getSimpleName());
            u.setName( this.getName() + " 01 " + regs.get(1).getCoords().getX() + " " + regs.get(1).getCoords().getY() );
            u.Befehle.add("NACH no PAUSE no PAUSE no NO");

            u = this.createKapitaen(p, r, Boot.class.getSimpleName());
            u.setName( this.getName() + " 02 " + regs.get(2).getCoords().getX() + " " + regs.get(2).getCoords().getY() );
            u.Befehle.add("NACH no no no NO");

            u = this.createKapitaen(p, r, Langboot.class.getSimpleName());
            u.setName( this.getName() + " 03 " + regs.get(4).getCoords().getX() + " " + regs.get(4).getCoords().getY() );
            u.Befehle.add("NACH no no no no no");

            u = this.createKapitaen(p, r, Drachenschiff.class.getSimpleName());
            u.setName( this.getName() + " 04 " + regs.get(5).getCoords().getX() + " " + regs.get(5).getCoords().getY() );
            u.Befehle.add("NACH no no no no no");

            u = this.createKapitaen(p, r, Galeone.class.getSimpleName());
            u.setName( this.getName() + " 05 " + regs.get(5).getCoords().getX() + " " + regs.get(5).getCoords().getY() );
            u.Befehle.add("NACH no no no no no PAUSE sw sw sw sw sw");

            u = this.createKapitaen(p, r, Karavelle.class.getSimpleName());
            u.setName( this.getName() + " 06 " + regs.get(6).getCoords().getX() + " " + regs.get(6).getCoords().getY() );
            u.Befehle.add("NACH no no no no no no no");

            u = this.createKapitaen(p, r, Tireme.class.getSimpleName());
            u.setName( this.getName() + " 07 " + regs.get(8).getCoords().getX() + " " + regs.get(8).getCoords().getY() );
            u.Befehle.add("NACH no no no no no no no no no");

            u = this.createKapitaen(p, r, Drachenschiff.class.getSimpleName());
            u.setName( this.getName() + " 08 " + regs.get(3).getCoords().getX() + " " + regs.get(3).getCoords().getY() );
            u.Befehle.add("NACH no no no");

            u = this.createKapitaen(p, r, Drachenschiff.class.getSimpleName());
            u.setName( this.getName() + " 09 " + regs.get(3).getCoords().getX() + " " + regs.get(3).getCoords().getY() );
            u.Befehle.add("ROUTE no no no PAUSE no PAUSE sw sw sw sw");

            new Info(this.getName() + " Setup in " + r + " (" + r.getCoords() + ").", u, u.getCoords());
        }
    }

    /**
     */
    private void setupHafenZeugs() {
        Partei p = this.getTestWorld().createPartei(Mensch.class);
		p.setUrsprung(new Coords(0,0,1));
        List<Region> regs = new ArrayList<Region>();

        // Berg-Region suchen, die SO und SW Ozean als Nachbarn hat
        for (Region maybe : this.getTestWorld().nurTerrain(getRegions(), Berge.class)) {
            Region so = Region.Load(maybe.getCoords().shift(Richtung.Suedosten));
            Region sw = Region.Load(maybe.getCoords().shift(Richtung.Suedwesten));

            if ((so instanceof Ozean) && (sw instanceof Ozean)) {
                // gotcha!
                regs.add(maybe);
                regs.add(so);
                regs.add(sw);
                break;
            }
        }
		if (regs.size() < 3)  throw new IllegalStateException("Keine passenden Regionen für " + this.getName() + " gefunden - einfach nochmal probieren...");

        for (Region r : regs) getRegions().remove(r);

        Region r = regs.get(0);
        Region so = regs.get(1);
        Region sw = regs.get(2);
        {
            // Bootseise, okay.
            Unit u = this.createKapitaen(p, r, Boot.class.getSimpleName());
            u.setName( this.getName() + " 11 " + so.getCoords().getX() + " " + so.getCoords().getY() );
            u.Befehle.add("NACH sw o");

            // Bootsreise, Problem mit der Küste
            u = this.createKapitaen(p, so, Boot.class.getSimpleName());
            u.setName( this.getName() + " 12 " + r.getCoords().getX() + " " + r.getCoords().getY() );
            u.Befehle.add("NACH nw sw o");

            // Bootsreise, Problem mit der Küste
            u = this.createKapitaen(p, r, Drachenschiff.class.getSimpleName());
            u.setName( this.getName() + " 13 " + sw.getCoords().getX() + " " + sw.getCoords().getY() );
            u.Befehle.add("NACH sw no");
        }


		// und jetzt das ganze nochmal, aber mit Seehafen:

        regs = new ArrayList<Region>();

        // Berg-Region suchen, die SO und SW Ozean als Nachbarn hat
        for (Region maybe : this.getTestWorld().nurTerrain(getRegions(), Berge.class)) {
            so = Region.Load(maybe.getCoords().shift(Richtung.Suedosten));
            sw = Region.Load(maybe.getCoords().shift(Richtung.Suedwesten));

            if ((so instanceof Ozean) && (sw instanceof Ozean)) {
                // gotcha!
                regs.add(maybe);
                regs.add(so);
                regs.add(sw);
                break;
            }
        }

        for (Region rr : regs) getRegions().remove(rr);

        if (regs.isEmpty()) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
        
        r = regs.get(0);
        so = regs.get(1);
        sw = regs.get(2);
        {
			Building burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			burg.setSize(100);

			Unit u = this.createUnit(p, r);
            u.setName( this.getName() + " 14");
			u.setPersonen(10);
			u.setSkill(Burgenbau.class, 1650 * u.getPersonen());
			u.setItem(Silber.class, 20000);
            u.setItem(Holz.class, 200);
            u.setItem(Stein.class, 200);
            u.setItem(Eisen.class, 200);
			u.Befehle.add("MACHE Seehafen");

			// Bootsreise, Problem ohne Hafen
            u = this.createKapitaen(p, r, Drachenschiff.class.getSimpleName());
            u.setName( this.getName() + " 15 " + r.getCoords().getX() + " " + r.getCoords().getY() );
            u.Befehle.add("NACH sw o nw");

            u = this.createUnit(p, r);
            u.setName( this.getName() + " 16");
            u.Befehle.add("BETRETE GEBAEUDE " + burg.getNummerBase36());
			u.Befehle.add("LERNE Tarnung");

			Partei p2 = this.getTestWorld().createPartei(Mensch.class);
			// Bootsreise, Problem mit Seehafen
            u = this.createKapitaen(p2, sw, Boot.class.getSimpleName());
            u.setName( this.getName() + " 17 " + sw.getCoords().getX() + " " + sw.getCoords().getY() );
            u.Befehle.add("NACH no");
        }



    }


}
