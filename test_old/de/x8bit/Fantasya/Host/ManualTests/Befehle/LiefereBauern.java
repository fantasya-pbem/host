package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.List;

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
import de.x8bit.Fantasya.Host.EVA.Gib;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class LiefereBauern extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        // TODO Testen, ob man den Bauern im Ozean etwas geben kann.
		{
            // Wir geben den Bauern VIEL(E) Silber, Pferde, Elefanten und Kamele - mit Anzahl.
			Unit u = this.createUnit(p, r);
			u.setItem(Silber.class, 2000000);
			u.setItem(Pferd.class, 1000);
			u.setItem(Elefant.class, 1000);
			u.setItem(Kamel.class, 1000);
			u.setItem(Eisen.class, 1000);
			u.setItem(Stein.class, 1000);
			u.setItem(Holz.class, 2000);
            u.setName(this.getName()+" 01 " + r.getCoords().getX() + " " + r.getCoords().getY());
			u.Befehle.add("LIEFERE Bauern 2000000 Silber");
			u.Befehle.add("LIEFERE Bauern 1000 Pferde");
			u.Befehle.add("LIEFERE Bauern 1000 Elefanten");
			u.Befehle.add("LIEFERE Bauern 1000 Kamele");
			u.Befehle.add("LIEFERE Bauern 1000 Eisen");
			u.Befehle.add("LIEFERE Bauern 1000 Steine");
			u.Befehle.add("LIEFERE Bauern 2000 Holz");

			// Diese netten Leute hier wollen alles an die notleidende Bevölkerung geben.
			u = this.createUnit(p, r);
			u.setPersonen(10);
			u.setName(this.getName() + " 02");
			u.Befehle.add("LIEFERE Bauern ALLES");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

		r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
		getRegions().remove(r);

        {
            // Wir geben den Bauern VIEL(E) Silber, Pferde, Elefanten und Kamele - ohne Anzahl.
			Unit u = this.createUnit(p, r);
			u.setItem(Silber.class, 2000000);
			u.setItem(Pferd.class, 1000);
			u.setItem(Elefant.class, 1000);
			u.setItem(Kamel.class, 1000);
			u.setItem(Eisen.class, 1000);
			u.setItem(Stein.class, 1000);
			u.setItem(Holz.class, 2000);
            u.setName(this.getName()+" 03 " + r.getCoords().getX() + " " + r.getCoords().getY());
			u.Befehle.add("LIEFERE Bauern Silber");
			u.Befehle.add("LIEFERE Bauern Pferde");
			u.Befehle.add("LIEFERE Bauern Elefanten");
			u.Befehle.add("LIEFERE Bauern Kamele");
			u.Befehle.add("LIEFERE Bauern Eisen");
			u.Befehle.add("LIEFERE Bauern Steine");
			u.Befehle.add("LIEFERE Bauern Holz");

			// Wir streben ein einfaches Leben auf dem Lande an.
			u = this.createUnit(p, r);
			u.setItem(Silber.class, 0);
			u.setPersonen(10);
			u.setName(this.getName() + " 04");
			u.Befehle.add("LIEFERE Bauern PERSONEN");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03"});
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
				if (r.getResource(Silber.class).getAnzahl() < 1900000) {
					new TestMsg("Warnung: Silber-Übergabe an die Bauern ist nicht möglich?");
					// retval = fail("01: Silber ist nicht bei den Bauern angekommen.");
				}

				// min. 900 statt min. 1000: Tiere könnten auswandern
				if (r.getResource(Pferd.class).getAnzahl() < 900) {
					retval = fail("01: Pferde sind nicht bei den Bauern angekommen.");
				}
				if (r.getResource(Elefant.class).getAnzahl() < 900) {
					retval = fail("01: Elefanten sind nicht bei den Bauern angekommen.");
				}
				if (r.getResource(Kamel.class).getAnzahl() < 800) {
					retval = fail("01: Kamele sind nicht bei den Bauern angekommen.");
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

//                messages = Message.Retrieve(p, u.getCoords(), u);
//                boolean found = false;
//                for (Message msg : messages) {
//                    String text = msg.getMessage().toLowerCase();
//                    if (text.contains("") && text.contains("")) found = true;
//                }
//                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben.");
            }

            if (tokens[1].equals("02")) {
				if (Main.getBFlag("EVA")) {
					// bei EVA wird der Befehl gar nicht erst zugelassen:
					for (Einzelbefehl eb : u.BefehleExperimental) {
						if (eb.getProzessor() == Gib.class) {
							 retval = fail(tokens[1] + ": GIB Bauern ALLES ist durchgegangen?");
						}
					}
					continue;
				}


                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("liefere bauern alles") && text.contains("kein gültiger")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Keine Fehlermeldung, dass LIEFERE Bauern ALLES nicht erlaubt ist.");
			}

            // unit 03
            if (tokens[1].equals("03")) {
				// die Region sollte SEHR viele von allem haben:
				int x = Integer.parseInt(tokens[2]);
				int y = Integer.parseInt(tokens[3]);
				Region r = Region.Load(x, y, 1);
				if (r.getResource(Silber.class).getAnzahl() < 1900000) {
					new TestMsg("Warnung: Silber-Übergabe an die Bauern ist nicht möglich?");
					// retval = fail("03: Silber ist nicht bei den Bauern angekommen.");
				}

				// min. 900 statt min. 1000: Tiere könnten auswandern
				if (r.getResource(Pferd.class).getAnzahl() < 900) {
					retval = fail("03: Pferde sind nicht bei den Bauern angekommen.");
				}
				if (r.getResource(Elefant.class).getAnzahl() < 900) {
					retval = fail("03: Elefanten sind nicht bei den Bauern angekommen.");
				}
				if (r.getResource(Kamel.class).getAnzahl() < 800) {
					retval = fail("03: Kamele sind nicht bei den Bauern angekommen.");
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

//                messages = Message.Retrieve(p, u.getCoords(), u);
//                boolean found = false;
//                for (Message msg : messages) {
//                    String text = msg.getMessage().toLowerCase();
//                    if (text.contains("") && text.contains("")) found = true;
//                }
//                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben.");
            }

            // unit 04
            if (tokens[1].equals("04")) {
				retval = fail("04: Die Einheit sollte nicht mehr da sein.");
			}


        } // next unit

        return retval;
    }

}
