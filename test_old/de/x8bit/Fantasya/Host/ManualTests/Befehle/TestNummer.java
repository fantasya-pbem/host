package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Ships.Langboot;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Halbling;
import de.x8bit.Fantasya.Atlantis.Units.Ork;
import de.x8bit.Fantasya.Atlantis.Units.Troll;
import de.x8bit.Fantasya.Atlantis.Units.Zwerg;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class TestNummer extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().createPartei(Troll.class);
		p.setName(this.getName()+"-Trolle");
		
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);
        r.setName(getName() + "-Region");

        {
            // gültige Nummer
			Unit u = this.createUnit(p, r);
            u.setName(this.getName() + " 01 " + u.getNummerBase36());
			u.setItem(Stein.class, 2);
			u.Befehle.add("GIB 0 1 Stein"); // damit es auch eine Meldung umzunummerieren gibt
			u.Befehle.add("NUMMER EINHEIT 6Lnb");
            u.setBeschreibung(u.Befehle.get(0));

			// vergebene Nummer
			u = this.createUnit(p, r);
            u.setName(this.getName() + " 02 " + u.getNummerBase36());
			String vergeben = u.getNummerBase36();
			
			u = this.createUnit(p, r);
            u.setName(this.getName() + " 03 " + u.getNummerBase36());
			u.Befehle.add("NUMMER EINHEIT " + vergeben);
            u.setBeschreibung(u.Befehle.get(0));

			// "überkreuz" (gegenseitiger Wechsel der Nummer)?


			// Burgen:

            // gültige Nummer
			u = this.createUnit(p, r);
			u.Befehle.add("NUMMER GEBAEUDE abc");
            u.setBeschreibung(u.Befehle.get(0));

			Building b = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			b.setSize(10);

            u.setName(this.getName() + " 11 " + b.getNummerBase36());
			u.Enter(b);

			// vergebene Nummer
			u = this.createUnit(p, r);
            u.setName(this.getName() + " 12");
			b = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			b.setSize(10);

			u.Enter(b);
			vergeben = b.getNummerBase36();


			u = this.createUnit(p, r);
            u.setName(this.getName() + " 13");
			u.Befehle.add("NUMMER BURG " + vergeben);
            u.setBeschreibung(u.Befehle.get(0));

			b = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			b.setSize(10);

			u.Enter(b);

			// keine Burg:
			u = this.createUnit(p, r);
            u.setName(this.getName() + " 14");
			u.Befehle.add("NUMMER BURG def");
            u.setBeschreibung(u.Befehle.get(0));


			// Schiffe:

            // gültige Nummer
			u = this.createUnit(p, r);
			u.Befehle.add("NUMMER SCHIFF abc");
            u.setBeschreibung(u.Befehle.get(0));

			Ship s = Ship.Create(Langboot.class.getSimpleName(), r.getCoords());
			s.setGroesse(10);

            u.setName(this.getName() + " 21 " + s.getNummerBase36());
			u.Enter(s);

			// vergebene Nummer
			u = this.createUnit(p, r);
            u.setName(this.getName() + " 22");
			s = Ship.Create(Langboot.class.getSimpleName(), r.getCoords());
			s.setGroesse(10);

			u.Enter(s);
			vergeben = s.getNummerBase36();


			u = this.createUnit(p, r);
            u.setName(this.getName() + " 23");
			u.Befehle.add("NUMMER SCHIFF " + vergeben);
            u.setBeschreibung(u.Befehle.get(0));

			s = Ship.Create(Langboot.class.getSimpleName(), r.getCoords());
			s.setGroesse(10);

			u.Enter(s);

			// kein Schiff:
			u = this.createUnit(p, r);
            u.setName(this.getName() + " 24");
			u.Befehle.add("NUMMER SCHIFF def");
            u.setBeschreibung(u.Befehle.get(0));





			// VOLK:


            // gültige Nummer
			Partei p3 = this.getTestWorld().createPartei(Elf.class);
			p3.setName(this.getName()+"-ABC-Elfen");
			u = this.createUnit(p3, r);
			u.setItem(Stein.class, 2);
			u.Befehle.add("GIB 0 1 Stein"); // damit es auch eine Meldung umzunummerieren gibt
			u.Befehle.add("NUMMER VOLK abc");
            u.setName(this.getName() + " 31 " + p3.getNummerBase36());

			// vergebene Nummer
			Partei p4 = this.getTestWorld().createPartei(Halbling.class);
			p4.setName(this.getName()+"-Halblinge");
			u = this.createUnit(p4, r);
            u.setName(this.getName() + " 32");
			
			vergeben = p4.getNummerBase36();


			Partei p5 = this.getTestWorld().createPartei(Zwerg.class);
			p5.setName(this.getName()+"-Zwerge");
			u = this.createUnit(p5, r);
            u.setName(this.getName() + " 33 " + p5.getNummerBase36());
			u.Befehle.add("NUMMER VOLK " + vergeben);


			Partei p2 = this.getTestWorld().createPartei(Ork.class);
			p2.setName(this.getName()+"-Allianz-Orks");

			u = this.createUnit(p2, r);
			u.setName(this.getName() + " 35 Allianzpartner war: " + p3.getNummerBase36());
			u.Befehle.add("HELFE " + p3.getNummerBase36()); // ABC-Elfen
			u.Befehle.add("STEUER " + p3.getNummerBase36() + " 15"); // ABC-Elfen



            new Info(this.getName() + " Setup in " + r + ".", u);
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
		
        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, 
				new String[] {"01", "02", "03", "11", "12", "13", "14", "21", "22", "23", "24", "31", "32", "33", "35"}
		);
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;
            Partei p = Partei.getPartei(u.getOwner());

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                messages = Message.Retrieve(null, (Coords)null, u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("ndere von [" + tokens[2].toLowerCase()) && text.contains("auf [6lnb")) found = true; //ändere
                }
                if (!found) retval = fail(tokens[1] + " von [" + p.getNummerBase36() + "]: NUMMER EINHEIT fehlgeschlagen.");
            }

            // unit 03
            if (tokens[1].equals("03")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("ist nicht frei") && text.contains("verwende [" + u.getNummerBase36().toLowerCase())) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": NUMMER EINHEIT (belegte ID) fehlgeschlagen.");
            }



			// BURG:

            // unit 11
            if (tokens[1].equals("11")) {
				Building b = Building.getBuilding(u.getGebaeude());
				if (!b.getNummerBase36().equals("abc")) {
					retval = fail(tokens[1] + ": NUMMER BURG fehlgeschlagen.");
				}

                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("ndere von " + tokens[2]) && text.contains("auf abc")) found = true; // ändere
                }
                if (!found) retval = fail(tokens[1] + ": NUMMER BURG fehlgeschlagen.");
            }

            // unit 13
            if (tokens[1].equals("13")) {
				Building b = Building.getBuilding(u.getGebaeude());

                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("ist nicht frei") && text.contains("verwende " + b.getNummerBase36())) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": NUMMER BURG (belegte ID) fehlgeschlagen.");
            }

            // unit 14
            if (tokens[1].equals("14")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("ist in keinem geb")) found = true; // gebäude
                }
                if (!found) retval = fail(tokens[1] + ": NUMMER BURG - Fehlermeldung (kein Gebäude) fehlt..");
            }




			// SCHIFF:

            // unit 21
            if (tokens[1].equals("21")) {
				Ship s = Ship.Load(u.getSchiff());
				if (!s.getNummerBase36().equals("abc")) {
					retval = fail(tokens[1] + ": NUMMER SCHIFF fehlgeschlagen.");
				}

                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("ndere von [" + tokens[2]) && text.contains("auf [abc")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": NUMMER SCHIFF fehlgeschlagen.");
            }

            // unit 23
            if (tokens[1].equals("23")) {
				Ship s = Ship.Load(u.getSchiff());

                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("ist nicht frei") && text.contains("verwende [" + s.getNummerBase36())) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": NUMMER SCHIFF (belegte ID) fehlgeschlagen.");
            }

            // unit 24
            if (tokens[1].equals("24")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("ist auf keinem schiff")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": NUMMER SCHIFF - Fehlermeldung (kein Schiff) fehlt..");
            }




			// VOLK:

            // unit 31
            if (tokens[1].equals("31")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("ndere von") && text.contains("auf [abc")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": NUMMER VOLK - Erfolgsmeldung über Nummern-Änderung fehlt.");

				if (u.getOwner() != Codierung.fromBase36("abc")) {
					retval = fail(tokens[1] + ": NUMMER VOLK - nicht geändert.");
				}
				if (u.getTarnPartei() != Codierung.fromBase36("abc")) {
					retval = fail(tokens[1] + ": NUMMER VOLK - Tarnpartei nicht geändert.");
				}
            }

            // unit 33
            if (tokens[1].equals("33")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("ist nicht frei")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": NUMMER VOLK - Fehlermeldung (belegte ID) fehlt..");

				int alteNummer = Codierung.fromBase36(tokens[2]);
				if (alteNummer == u.getOwner()) {
					retval = fail(tokens[1] + ": NUMMER VOLK - nicht geändert.");
				}
            }

            // unit 35
            if (tokens[1].equals("35")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("allianz f") && text.contains("r testnummer-abc")) found = true; // "für"
                }
                if (!found) retval = fail(tokens[1] + ": NUMMER VOLK - Meldung über Allianz fehlt.");

				found = false;
				for (int partnerNr : p.getAllianzen().keySet()) {
					if (partnerNr == Codierung.fromBase36("abc")) found = true;
				}
				if (!found) {
					retval = fail(tokens[1] + ": NUMMER VOLK - Allianz fehlt oder die Nummer wurde nicht angepasst.");
				}
            }




        } // next unit

        return retval;
    }

}
