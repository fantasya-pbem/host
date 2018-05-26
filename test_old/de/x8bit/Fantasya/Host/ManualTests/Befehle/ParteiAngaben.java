package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Ships.Boot;
import de.x8bit.Fantasya.Atlantis.Units.Aquaner;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class ParteiAngaben extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().createPartei(Aquaner.class);
		p.setName(this.getName()+"-Aquaner");

        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        { // ADRESSE
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
            u.Befehle.add("ADRESSE test@foo.bar");
        }

        { // HOMEPAGE
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 02");
            u.Befehle.add("HOMEPAGE http://www.foo.bar/02");
        }

        { // WEBSITE
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 03");
            u.Befehle.add("WEBSITE http://www.foo.bar/03");
        }

        { // URSPRUNG
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 04 " + (p.getUrsprung().getX()+1) + " " + (p.getUrsprung().getY()+2));
            u.Befehle.add("URSPRUNG 1 2");
        }

        { // PASSWORT
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 05");
            u.Befehle.add("PASSWORT \"asdf\"");
        }

        { // BENENNE VOLK
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 06");
            u.Befehle.add("BENENNE VOLK \"Biester\"");
        }

        { // BENENNE EINHEIT
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 07");
            u.Befehle.add("BENENNE einheit \"" + this.getName() + " 07 Biest\"");
        }

		// burg wird auch für #18 gebraucht
		Building burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
		{ // BENENNE REGION und GEBAEUDE
			burg.setSize(2);

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 08");
            u.Befehle.add("BENENNE REGION \"Biestingen\"");
            u.Befehle.add("BENENNE GEBAEUDE \"Biestburg\"");
			u.Enter(burg);

        }

		{ // BENENNE SCHIFF
			Ship s = Ship.Create(Boot.class.getSimpleName(), r.getCoords());
			s.setGroesse(5);
			s.setFertig(true);

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 09");
            u.Befehle.add("BENENNE SCHIFF \"Biestboot\"");
			u.Enter(s);
        }

        { // BESCHREIBE VOLK
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 16");
            u.Befehle.add("BESCHREIBE VOLK Die Biester");
        }

        { // BESCHREIBE EINHEIT
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 17");
            u.Befehle.add("BESCHREIBE einheit \"Das Biest\"");
        }

		{ // BESCHREIBE REGION und GEBAEUDE
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 18");
            u.Befehle.add("BESCHREIBE region Ja, ja - Biestingen");
            u.Befehle.add("BESCHREIBE GEBAEUDE \"Die Biestburg\"");
			u.Enter(burg);
        }

		{ // BESCHREIBE SCHIFF
			Ship s = Ship.Create(Boot.class.getSimpleName(), r.getCoords());
			s.setGroesse(5);
			s.setFertig(true);

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 19");
            u.Befehle.add("BESCHREIBE SCHIFF \"Das Biestboot\"");
			u.Enter(s);
        }
	}

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
        Partei p = null;
		for (Partei maybe : Partei.PROXY) {
			if (maybe.getName().equals("Biester")) {
				p = maybe;
				break;
			}
		}

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04", "05", "06", "07", "08", "09", "16", "17", "18", "19"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }

        List<Message> messages = null;

        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                if (!p.getEMail().equals("test@foo.bar")) retval = fail(tokens[1] + ": E-Mail nicht korrekt geändert - " + p.getEMail());
                
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    if (msg.getText().toLowerCase().contains("test@foo.bar")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Keine Meldung über geänderte Mail-Adresse.");
            }

            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    if (msg.getText().toLowerCase().contains("http://www.foo.bar/02")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Keine Meldung über geänderte Homepage.");
			}

            // unit 03
            if (tokens[1].equals("03")) {
                if (!p.getWebsite().equals("http://www.foo.bar/03")) retval = fail(tokens[1] + ": Website nicht korrekt geändert - " + p.getWebsite());

                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    if (msg.getText().toLowerCase().contains("http://www.foo.bar/03")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Keine Meldung über geänderte Website.");
			}

            // unit 04
            if (tokens[1].equals("04")) {
				if (p.getUrsprung().getX() != Integer.parseInt(tokens[2])) {
					retval = fail(tokens[1] + ": X-Koordinate des Ursprungs stimmt nicht.");
				}
				if (p.getUrsprung().getY() != Integer.parseInt(tokens[3])) {
					retval = fail(tokens[1] + ": Y-Koordinate des Ursprungs stimmt nicht.");
				}

                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    if (msg.getText().toLowerCase().contains("ursprung")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Keine Meldung über geänderten Ursprung.");
			}

            // unit 05
            if (tokens[1].equals("05")) {
                if (!p.getPassword().equals("asdf")) retval = fail(tokens[1] + ": Passwort nicht korrekt geändert - " + p.getPassword());
				
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    if (msg.getText().toLowerCase().contains("asdf")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Keine Meldung über geändertes Passwort.");
			}

            // unit 06
            if (tokens[1].equals("06")) {
                if (!p.getName().equals("Biester")) retval = fail(tokens[1] + ": Volks-Name nicht korrekt geändert - " + p.getName());
			}

            // unit 07
            if (tokens[1].equals("07")) {
                if (!u.getName().equals(this.getName() + " 07 Biest")) retval = fail(tokens[1] + ": Einheiten-Name nicht korrekt geändert - " + u.getName());
			}

            // unit 08
            if (tokens[1].equals("08")) {
				Region r = Region.Load(u.getCoords());
                if (!r.getName().equals("Biestingen")) retval = fail(tokens[1] + ": Regions-Name nicht korrekt geändert - " + r.getName());

				Building b = Building.getBuilding(u.getGebaeude());
				if (b == null) {
					retval = fail(tokens[1] + ": Die Burg ist verschwunden.");
				} else {
					if (!b.getName().equals("Biestburg")) retval = fail(tokens[1] + ": Die Benennung der Burg ist nicht korrekt geändert worden - " + b.getName());
				}
			}

            // unit 09
            if (tokens[1].equals("09")) {
				Ship s = Ship.Load(u.getSchiff());
				if (s == null) {
					retval = fail(tokens[1] + ": Das Schiff ist verschwunden.");
				} else {
					if (!s.getName().equals("Biestboot")) retval = fail(tokens[1] + ": Die Benennung des Schiffs ist nicht korrekt geändert worden - " + s.getName());
				}
			}

            // unit 16
            if (tokens[1].equals("16")) {
				new TestMsg("Warnung: BESCHREIBE VOLK ist nicht implementiert.");
                // if (!p.getBeschreibung().equals("Die Biester")) retval = fail(tokens[1] + ": Volks-Beschreibung nicht korrekt geändert - " + p.getBeschreibung());
			}

            // unit 17
            if (tokens[1].equals("17")) {
                if (!u.getBeschreibung().equals("Das Biest")) retval = fail(tokens[1] + ": Einheiten-Beschreibung nicht korrekt geändert - " + u.getBeschreibung());
			}

            // unit 18
            if (tokens[1].equals("18")) {
				Region r = Region.Load(u.getCoords());
                if (!r.getBeschreibung().equals("Ja, ja - Biestingen")) retval = fail(tokens[1] + ": Regions-Beschreibung nicht korrekt geändert - " + r.getBeschreibung());

				Building b = Building.getBuilding(u.getGebaeude());
				if (b == null) {
					retval = fail(tokens[1] + ": Die Burg ist verschwunden.");
				} else {
					if (!b.getBeschreibung().equals("Die Biestburg")) retval = fail(tokens[1] + ": Die Beschreibung der Burg ist nicht korrekt geändert worden - " + b.getBeschreibung());
				}
			}

            // unit 19
            if (tokens[1].equals("19")) {
				Ship s = Ship.Load(u.getSchiff());
				if (s == null) {
					retval = fail(tokens[1] + ": Das Schiff ist verschwunden.");
				} else {
					if (!s.getBeschreibung().equals("Das Biestboot")) retval = fail(tokens[1] + ": Die Beschreibung des Schiffs ist nicht korrekt geändert worden - " + s.getBeschreibung());
				}
			}

		} // next unit
        
        return retval;
    }

}
