package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Elefant;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Kamel;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Paket;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class LiefereNull extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            // Wir geben 0 VIEL(E) Silber, Pferde, Elefanten und Kamele - mit Anzahl.
			Unit u = this.createUnit(p, r);
			u.setItem(Silber.class, 2000000);
			u.setItem(Pferd.class, 1000);
			u.setItem(Elefant.class, 1000);
			u.setItem(Kamel.class, 1000);
			u.setItem(Eisen.class, 1000);
			u.setItem(Stein.class, 1000);
			u.setItem(Holz.class, 2000);
            u.setName(this.getName()+" 01 " + r.getCoords().getX() + " " + r.getCoords().getY());
			u.Befehle.add("LIEFERE 0 2000000 Silber");
			u.Befehle.add("LIEFERE 0 1000 Pferde");
			u.Befehle.add("LIEFERE 0 1000 Elefanten");
			u.Befehle.add("LIEFERE 0 1000 Kamele");
			u.Befehle.add("LIEFERE 0 1000 Eisen");
			u.Befehle.add("LIEFERE 0 1000 Steine");
			u.Befehle.add("LIEFERE 0 2000 Holz");

			// Wir haben alles und wollen nichts!
			u = this.createUnit(p, r);
			u.setPersonen(10);
			u.setName(this.getName() + " 02");
			for (Paket pack : Paket.getPaket("Items")) {
				Item item = (Item)pack.Klasse;
				u.setItem(item.getClass(), 11);
			}
			u.Befehle.add("LIEFERE 0 ALLES");

            new Info(this.getName() + " Setup in " + r + ".", u);
        }

		r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
		getRegions().remove(r);

        {
            // Wir geben 0 VIEL(E) Silber, Pferde, Elefanten und Kamele - ohne Anzahl.
			Unit u = this.createUnit(p, r);
			u.setItem(Silber.class, 2000000);
			u.setItem(Pferd.class, 1000);
			u.setItem(Elefant.class, 1000);
			u.setItem(Kamel.class, 1000);
			u.setItem(Eisen.class, 1000);
			u.setItem(Stein.class, 1000);
			u.setItem(Holz.class, 2000);
            u.setName(this.getName()+" 03 " + r.getCoords().getX() + " " + r.getCoords().getY());
			u.Befehle.add("LIEFERE 0 Silber");
			u.Befehle.add("LIEFERE 0 Pferde");
			u.Befehle.add("LIEFERE 0 Elefanten");
			u.Befehle.add("LIEFERE 0 Kamele");
			u.Befehle.add("LIEFERE 0 Eisen");
			u.Befehle.add("LIEFERE 0 Steine");
			u.Befehle.add("LIEFERE 0 Holz");

			// Wir wollen nicht mehr!
			u = this.createUnit(p, r);
			u.setItem(Silber.class, 0); // damit das Erbe nicht das Ergebnis verfälscht
			u.setPersonen(10);
			u.setName(this.getName() + " 04");
			u.Befehle.add("LIEFERE 0 10 PERSONEN");

			// Wir haben alles und wollen nichts - und passen auf, dass alles namentlich weggeworfen wird.
			u = this.createUnit(p, r);
			u.setPersonen(10);
			u.setName(this.getName() + " 05");
			for (Paket pack : Paket.getPaket("Items")) {
				Item item = (Item)pack.Klasse;
				u.setItem(item.getClass(), 11);
				u.Befehle.add("LIEFERE 0 " + item.getName());
			}

			// Wir genauso - aber wir können auch die aller-richtigste Syntax verwenden!
			u = this.createUnit(p, r);
			u.setPersonen(10);
			u.setName(this.getName() + " 06");
			for (Paket pack : Paket.getPaket("Items")) {
				Item item = (Item)pack.Klasse;
				u.setItem(item.getClass(), 11);
				u.Befehle.add("LIEFERE 0 \"" + item.getName()+"\"");
			}

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
		}

    }


    @Override
	@SuppressWarnings("unchecked")
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "05", "06"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
				// die Region sollte SEHR viele von allem haben:
				int x = Integer.parseInt(tokens[2]);
				int y = Integer.parseInt(tokens[3]);
				Region r = Region.Load(x, y, 1);
				if (r.getResource(Silber.class).getAnzahl() > 1900000) {
					// retval = fail("01: Silber ist nicht bei den Bauern angekommen.");
				}
				if (r.getResource(Pferd.class).getAnzahl() >= 1000) {
					retval = fail("01: Pferde sind bei den Bauern angekommen.");
				}
				if (r.getResource(Elefant.class).getAnzahl() >= 1000) {
					retval = fail("01: Elefanten sind bei den Bauern angekommen.");
				}
				if (r.getResource(Kamel.class).getAnzahl() >= 1000) {
					retval = fail("01: Kamele sind bei den Bauern angekommen.");
				}
				// aber Resourcen sollten nicht dabei sein:
				if (r.getResource(Stein.class).getAnzahl() >= 1000) {
					retval = fail("01: Steine sind an die Bauern gegangen und jetzt abbaubar!");
				}
				if (r.getResource(Eisen.class).getAnzahl() >= 1000) {
					retval = fail("01: Eisen ist an die Bauern gegangen und jetzt abbaubar!");
				}
				if (r.getResource(Holz.class).getAnzahl() >= 1500) {
					retval = fail("01: Holz ist an die Bauern gegangen und jetzt zu fällen!");
				}

				if (u.getItem(Silber.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Silber vorhanden");
				if (u.getItem(Pferd.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Pferde vorhanden");
				if (u.getItem(Elefant.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Elefanten vorhanden");
				if (u.getItem(Kamel.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Kamele vorhanden");
				if (u.getItem(Holz.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Holz vorhanden");
				if (u.getItem(Eisen.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Eisen vorhanden");
				if (u.getItem(Stein.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Stein vorhanden");

                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("wirft") && text.contains("weg")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldungen über Wegwerfen fehlt.");
            }

            // unit 02
			if (tokens[1].equals("02")) {
				for (Paket pack : Paket.getPaket("Items")) {
					if (u.getItem((Class<? extends Item>) pack.Klasse.getClass()).getAnzahl() > 0) {
						retval = fail(tokens[1] + ": hat noch " + pack.Klasse.getName() + ".");
					}
				}
			}

            // unit 03
            if (tokens[1].equals("03")) {
				// die Region sollte SEHR viele von allem haben:
				int x = Integer.parseInt(tokens[2]);
				int y = Integer.parseInt(tokens[3]);
				Region r = Region.Load(x, y, 1);
				if (r.getResource(Silber.class).getAnzahl() > 1900000) {
					// retval = fail("03: Silber ist nicht bei den Bauern angekommen.");
				}
				if (r.getResource(Pferd.class).getAnzahl() >= 1000) {
					retval = fail("03: Pferde sind bei den Bauern angekommen.");
				}
				if (r.getResource(Elefant.class).getAnzahl() >= 1000) {
					retval = fail("03: Elefanten sind bei den Bauern angekommen.");
				}
				if (r.getResource(Kamel.class).getAnzahl() >= 1000) {
					retval = fail("03: Kamele sind bei den Bauern angekommen.");
				}
				// aber Resourcen sollten nicht dabei sein:
				if (r.getResource(Stein.class).getAnzahl() >= 1000) {
					retval = fail("03: Steine sind an die Bauern gegangen und jetzt abbaubar!");
				}
				if (r.getResource(Eisen.class).getAnzahl() >= 1000) {
					retval = fail("03: Eisen ist an die Bauern gegangen und jetzt abbaubar!");
				}
				if (r.getResource(Holz.class).getAnzahl() >= 1500) {
					retval = fail("03: Holz ist an die Bauern gegangen und jetzt zu fällen!");
				}

				if (u.getItem(Silber.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Silber vorhanden");
				if (u.getItem(Pferd.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Pferde vorhanden");
				if (u.getItem(Elefant.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Elefanten vorhanden");
				if (u.getItem(Kamel.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Kamele vorhanden");
				if (u.getItem(Holz.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Holz vorhanden");
				if (u.getItem(Eisen.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Eisen vorhanden");
				if (u.getItem(Stein.class).getAnzahl() > 0) retval = fail(tokens[1] + ": Stein vorhanden");

                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("wirft") && text.contains("weg")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldungen über Wegwerfen fehlt.");
            }

            // unit 04
            if (tokens[1].equals("04")) {
				retval = fail("04: Die Einheit sollte nicht mehr da sein.");
			}

            // unit 05
			if (tokens[1].equals("05")) {
				for (Paket pack : Paket.getPaket("Items")) {
					if (u.getItem((Class<? extends Item>) pack.Klasse.getClass()).getAnzahl() > 0) {
						retval = fail(tokens[1] + ": hat noch " + pack.Klasse.getName() + ".");
					}
				}
			}

            // unit 06
			if (tokens[1].equals("06")) {
				for (Paket pack : Paket.getPaket("Items")) {
					if (u.getItem((Class<? extends Item>) pack.Klasse.getClass()).getAnzahl() > 0) {
						retval = fail(tokens[1] + ": hat noch " + pack.Klasse.getName() + ".");
					}
				}
			}


        } // next unit

        return retval;
    }

}
