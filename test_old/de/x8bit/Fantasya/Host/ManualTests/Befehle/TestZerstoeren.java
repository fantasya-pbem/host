package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Ships.Langboot;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class TestZerstoeren extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = getTestWorld();
		Partei p = this.getTestWorld().getSpieler1();

        { // Gebäude und Schiffe:
			Region r = tw.nurBetretbar(tw.nurNachbarVon(getRegions(), Ozean.class)).get(0);
			getRegions().remove(r);
			
            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
            u.Befehle.add("ZERSTOERE");


            u = this.createUnit(p, r);
            Building b = Building.Create(Burg.class.getSimpleName(), r.getCoords());
            b.setSize(50);

            u.setName(this.getName()+" 02");
            u.Befehle.add("ZERSTOERE");
            u.Enter(b);

            u = this.createUnit(p, r);
            Ship s = Ship.Create(Langboot.class.getSimpleName(), r.getCoords());
            s.setGroesse(50);
            s.setFertig(true);

            u.setName(this.getName()+" 03");
            u.Befehle.add("ZERSTOERE");
            u.Enter(s);

            new Info(this.getName() + "-Gebäude-und-Schiff Setup in " + r + ".", u, u.getCoords());
        }

        { // Straße ohne Bewacher:
			Region r = tw.nurTerrain(getRegions(), Gletscher.class).get(0);
			getRegions().remove(r);

			Region.Load(r.getCoords()).setStrassensteine(Richtung.Osten, 100);

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 11");
            u.Befehle.add("ZERSTOERE STRASSE O");

			u = this.createUnit(p, r);
            u.setName(this.getName()+" 12");
            u.Befehle.add("ZERSTOERE STRASSE W");

            new TestMsg(this.getName() + "-Straße-ohne-Bewacher Setup in " + r + ".");
        }

        { // Straße MIT Bewacher:
			Region r = tw.nurTerrain(getRegions(), Gletscher.class).get(0);
			getRegions().remove(r);

			Region.Load(r.getCoords()).setStrassensteine(Richtung.Osten, 100);

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 21");
            u.Befehle.add("ZERSTOERE STRASSE O");

            Partei fremde = tw.createPartei(Mensch.class);
			fremde.setName(this.getName()+"-Straßen-Bewacher");

			Unit bewacher = this.createUnit(fremde, r);
			bewacher.setName(this.getName()+" 22");
			bewacher.setPersonen(10);
			bewacher.setSkill(Hiebwaffen.class, 300 * bewacher.getPersonen());
			bewacher.setItem(Schwert.class, bewacher.getPersonen());
			bewacher.setBewacht(true);

            new TestMsg(this.getName() + "-Straße-mit-Bewacher Setup in " + r + ".");
        }

    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
        Partei p = this.getTestWorld().getSpieler1();
        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "11", "12", "21", "22"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("weder in") && text.contains("noch an bord")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Fehlermeldung fehlt.");
            }

            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("geb") && text.contains("wurde zerst")) found = true; // geb für gebäude, zerst für zerstört
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Gebäude-Zerstörung fehlt.");

                if (u.getItem(Stein.class).getAnzahl() < 1) {
                    fail(tokens[1] + ": Keine Abbruchsteine erhalten.");
                }
            }

            // unit 03
            if (tokens[1].equals("03")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("schiff") && text.contains("wurde zerst")) found = true; // zerst für zerstört
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Schiffs-Zerstörung fehlt.");

                if (u.getItem(Holz.class).getAnzahl() < 1) {
                    fail(tokens[1] + ": Kein Holz recycelt.");
                }
            }

            // unit 11
            if (tokens[1].equals("11")) {
				Region r = Region.Load(u.getCoords());
				if (r.getStrassensteine(Richtung.Osten) != 99) {
					retval = fail(tokens[1] + ": Nicht die erwarteten 99 Straßenstein Richtung Osten.");
				}

                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("rt die stra") && text.contains("um 1 punkt")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Straßen-Zerstörung fehlt.");
            }

            // unit 12
            if (tokens[1].equals("12")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("richtung westen") && text.contains("hier keine")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Fehlermeldung über nicht-existente Straße fehlt.");
            }

            // unit 21
            if (tokens[1].equals("21")) {
				Region r = Region.Load(u.getCoords());
				if (r.getStrassensteine(Richtung.Osten) != 100) {
					retval = fail(tokens[1] + ": Nicht die erwarteten 100 Straßenstein Richtung Osten.");
				}

                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("bewacher") && text.contains("lassen das nicht zu")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über verhinderte Straßen-Zerstörung fehlt.");
            }

            // unit 22
            if (tokens[1].equals("22")) {
				// TODO auf Meldung über den Straßen-Zerstörungsversuch prüfen
//                messages = Message.Retrieve(p, u.getCoords(), u);
//                boolean found = false;
//                for (Message msg : messages) {
//                    String text = msg.getMessage().toLowerCase();
//                    if (text.contains("richtung westen") && text.contains("hier keine")) found = true;
//                }
//                if (!found) retval = fail(tokens[1] + ": Fehlermeldung über nicht-existente Straße fehlt.");
            }



        } // next unit

        return retval;
    }

}
