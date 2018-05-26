package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Helper.Nachfrage;
import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Gewuerz;
import de.x8bit.Fantasya.Atlantis.Items.Juwel;
import de.x8bit.Fantasya.Atlantis.Items.Oel;
import de.x8bit.Fantasya.Atlantis.Items.Seide;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Weihrauch;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Skills.Handel;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class HandelTest extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = null;
		
		Class<? extends Item> produce = null;
		Class<? extends Item> sold1 = Seide.class;
		Class<? extends Item> sold2 = Oel.class;

		for (Region maybe : this.getTestWorld().nurTerrain(getRegions(), Ebene.class)) {
			produce = maybe.getProduce();
			if (produce.equals(sold1)) sold1 = Juwel.class;
			if (produce.equals(sold2)) sold2 = Gewuerz.class;
			r = maybe;
			break;
		}
        if (r == null) throw new IllegalStateException("Keine passende Region für " + getName() + " gefunden - einfach nochmal probieren...");
        getRegions().remove(r);

		String produceName = produce.getSimpleName();
		String sold1Name = sold1.getSimpleName();
		String sold2Name = sold2.getSimpleName();
		String luxuryTokens = " " + produceName + " " + sold1Name + " " + sold2Name;


        {
            r.setName(getName() + "-1 (" + luxuryTokens + " )");

			Building burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			burg.setSize(50);

			Unit u = this.createUnit(p, r);
			u.setPersonen(10);
			u.setSkill(Handel.class, 450 * u.getPersonen());
			u.setItem(sold1, 1000);
			u.setItem(produce, 1);
            u.setName(this.getName() + " 01" + luxuryTokens);
			u.Enter(burg);
			u.Befehle.add("HANDEL VERKAUFE 600 " + sold1Name);
			u.Befehle.add("HANDEL VERKAUFE 200 " + sold2Name);
			u.Befehle.add("HANDEL VERKAUFE 100 " + produceName);

			u = this.createUnit(p, r);
			u.setPersonen(10);
			u.setItem(Silber.class, 100000);
			u.setItem(sold2, 200);
			u.setSkill(Handel.class, 1650 * u.getPersonen());
            u.setName(this.getName() + " 02" + luxuryTokens);
			u.Enter(burg);
			u.Befehle.add("HANDEL KAUFE 1200 " + produceName);
			u.Befehle.add("HANDEL VERKAUFE 1000 " + sold2Name);

			u = this.createUnit(p, r);
			u.setPersonen(10);
			u.setSkill(Handel.class, 450 * u.getPersonen());
			u.setItem(produce, 101);
            u.setName(this.getName()+" 03" + luxuryTokens);
			u.Enter(burg);
			u.Befehle.add("HANDEL VERKAUFE 100 " + produceName);

			u = this.createUnit(p, r);
            u.setName(this.getName() + " 04" + luxuryTokens);
			u.Befehle.add("LERNE Handel");

			for (Nachfrage n : r.getLuxus()) {
				if (n.getNachfrage() > 0) n.setNachfrage(n.getNachfrage() * 5);
			}

            new Info(this.getName() + " Setup#1 in " + r  + " (Produkt: " + produceName + ").", u);
        }

        {
            // zweite Region - löschen der Handelsmeldungen pro Region prüfen.

			// produce = null; // produce bleibt erstmal gesetzt, damit wir hier was anderes kaufen können
			sold1 = Oel.class;
			sold2 = Juwel.class;

            r = null;
            for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
				if (maybe.getProduce().equals(produce)) continue;
				
				produce = maybe.getProduce();
				if (produce.equals(sold1)) sold1 = Weihrauch.class;
				if (produce.equals(sold2)) sold2 = Gewuerz.class;
				r = maybe;
				break;
            }

            if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
            getRegions().remove(r);
            r.setName(getName() + "-2 (" + luxuryTokens + " )");

			produceName = produce.getSimpleName();
			sold1Name = sold1.getSimpleName();
			sold2Name = sold2.getSimpleName();
			luxuryTokens = " " + produceName + " " + sold1Name + " " + sold2Name;

            Building burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			burg.setSize(50);
            
			Unit u = this.createUnit(p, r);
			u.setPersonen(10);
			u.setSkill(Handel.class, 450 * u.getPersonen());
			u.setItem(Gewuerz.class, 1000);
			u.setItem(Juwel.class, 1);
            u.setName(this.getName() + " 05 " + luxuryTokens);
			u.Enter(burg);
			u.Befehle.add("HANDEL VERKAUFE 200 " + produceName);
			u.Befehle.add("HANDEL VERKAUFE 200 " + sold1Name);
			u.Befehle.add("HANDEL VERKAUFE 100 " + sold2Name);

			u = this.createUnit(p, r);
			u.Befehle.add("LERNE Tarnung");

			u = this.createUnit(p, r);
			u.Befehle.add("LERNE Tarnung");

			u = this.createUnit(p, r);
			u.Befehle.add("LERNE Tarnung");

			u = this.createUnit(p, r);
			u.Befehle.add("LERNE Tarnung");

			Partei p2 = this.getTestWorld().createPartei(Elf.class);
			u = this.createUnit(p2, r);
			u.Befehle.add("LERNE Tarnung");

			u = this.createUnit(p2, r);
			u.Befehle.add("LERNE Handel");

			u = this.createUnit(p2, r);
			u.Befehle.add("LERNE Wahrnehmung");


            new Info(this.getName() + " Setup#2 in " + r + " (Produkt: " + produceName + ").", u);
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04", "05"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");
			String uRef = tokens[1] + "[" + u.getNummerBase36() + "] von [" + Codierung.toBase36(u.getOwner()) + "] - ";

			// die "luxurytokens" dekodieren:
			Class<? extends Item> produce = Item.getFor(tokens[2]);
			Class<? extends Item> sold1 = Item.getFor(tokens[3]);
			Class<? extends Item> sold2 = Item.getFor(tokens[4]);

            // unit 01
            if (tokens[1].equals("01")) {
				if (u.getItem(sold1).getAnzahl() != 501) {
					retval = fail(uRef + sold1.getSimpleName() + "bestand ist unerwartet.");
				}

				Region r = Region.Load(u.getCoords());
				if (r.getProduce() != produce) {
					retval = fail(uRef + "Es wird/werden kein(e) " + produce.getSimpleName() + " mehr produziert?!?");
				}

                messages = Message.Retrieve(p, u.getCoords(), u);
				
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("verkauft 1 " + produce.getSimpleName().toLowerCase())) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung über " + produce.getSimpleName() + "verkauf fehlt oder ist unerwartet.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("verkauft 499") && text.contains(sold1.getSimpleName().toLowerCase())) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung über " + sold1.getSimpleName() + "verkauf fehlt oder ist unerwartet.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (
							text.contains("101")
							&& text.contains("nicht verkaufen")
							&& text.contains(sold1.getSimpleName().toLowerCase())
					) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung über zu großes Handelsvolumen fehlt oder ist unerwartet.");
            }

            // unit 02
            if (tokens[1].equals("02")) {
				if (u.getItem(produce).getAnzahl() != 800) {
					retval = fail(tokens[1] + ": " + produce + "bestand ist unerwartet.");
				}

                messages = Message.Retrieve(p, u.getCoords(), u);

                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("kauft 800 ") && text.contains(produce.getSimpleName().toLowerCase())) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über " + produce.getSimpleName() + "kauf fehlt oder ist unerwartet.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("verkauft 200 " + sold2.getSimpleName().toLowerCase())) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über " + sold2.getSimpleName() + "verkauf fehlt oder ist unerwartet.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (
							text.contains("um 400")
							&& text.contains(produce.getSimpleName().toLowerCase())
							&& text.contains("rzen") // rzen für "kürzen"
					) found = true; 
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über zu großes Handelsvolumen fehlt oder ist unerwartet.");
            }

            // unit 03
            if (tokens[1].equals("03")) {
				if (u.getItem(produce).getAnzahl() != 1) {
					retval = fail(tokens[1] + ": " + produce.getSimpleName() + "bestand ist unerwartet.");
				}
			}



        } // next unit

        return retval;
    }

}
