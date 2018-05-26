package de.x8bit.Fantasya.Host.ManualTests.Misc;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Coords;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Ships.Boot;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Zwerg;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 * <p>0000156: Anzeige der Durchreise</p>
 * <p>Könnte man in einer Region eine Meldung anzeigen, welche Einheiten 
 * und/oder welche Schiffe durchgereist sind? Etwa "Inselwanderer 
 * [foo] hat die Region durchquert." und "Titanic [Schiff bar] 
 * hat die Region durchquert."</p>
 *
 * @author hapebe
 * @since 2010-09-15
 */
public class DurchreiseTest extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei partei = this.getTestWorld().getSpieler1();
        Partei fremd = this.getTestWorld().createPartei(Elf.class);
		Partei alliiert = this.getTestWorld().createPartei(Zwerg.class);

        fremd.setName(getName() + "-Lauscher");

		alliiert.setName(this.getName() + "-Alliierte");

        { // Szenario 1: Durchreise zu Lande, eigene Einheit schaut zu,
			// alliierte Einheit schaut zu.
            Region r = null; Region ost = null; Region west = null;
            for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
                ost = Region.Load(maybe.getCoords().shift(Richtung.Osten));
                west = Region.Load(maybe.getCoords().shift(Richtung.Westen));
                if (ost.istBetretbar(null) && west.istBetretbar(null)) {
					if (!getRegions().contains(ost)) continue;
					if (!getRegions().contains(west)) continue;
                    // gotcha!
                    r = maybe;
                    break;
                }
            }
            if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getClass().getSimpleName() + " gefunden - einfach nochmal probieren...");
            getRegions().remove(r); getRegions().remove(ost); getRegions().remove(west);

            Unit reiter = this.createUnit(partei, west);
            reiter.setName(this.getClass().getSimpleName() + " 01 " + ost.getCoords().getX() + " " + ost.getCoords().getY());
            reiter.setBeschreibung("Erwartet: Reitet von West nach Ost, nach " + ost + ". Es gibt keine Ertappt-Meldung, weil ich mit dem fremden Beobachter alliiert bin.");
            reiter.setSkill(Reiten.class, 30);
            reiter.setItem(Pferd.class, 1);
            reiter.Befehle.add("HELFE " + alliiert.getNummerBase36() + " KONTAKTIERE");
			reiter.Befehle.add("NACH o o");

            Unit beob1 = this.createUnit(partei, r);
            beob1.setName(this.getClass().getSimpleName() + " 02");
            beob1.setBeschreibung("Erwartet: Steht einfach so rum, keine Meldung über Durchreise - der Reisende gehört zu uns.");

            Unit beob2 = this.createUnit(alliiert, r);
            beob2.setName(this.getClass().getSimpleName() + " 03");
            beob2.setBeschreibung("Erwartet: Steht einfach so rum, keine Meldung über Durchreise, weil mit dem Reisenden KONTAKTIERE-alliiert.");
			beob2.Befehle.add("HELFE " + partei.getNummerBase36() + " KONTAKTIERE");
        }

        { // Szenario 2: Durchreise zu Lande, fremde Einheit schaut zu.
            Region r = null; Region ost = null; Region west = null;
            for (Region maybe : this.getTestWorld().nurBetretbar(getRegions())) {
                ost = Region.Load(maybe.getCoords().shift(Richtung.Osten));
                west = Region.Load(maybe.getCoords().shift(Richtung.Westen));
                if (ost.istBetretbar(null) && west.istBetretbar(null)) {
					if (!getRegions().contains(ost)) continue;
					if (!getRegions().contains(west)) continue;
                    // gotcha!
                    r = maybe;
                    break;
                }
            }
            if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getClass().getSimpleName() + " gefunden - einfach nochmal probieren...");
            getRegions().remove(r); getRegions().remove(ost); getRegions().remove(west);

            Unit reiter = this.createUnit(partei, west);
            reiter.setName(this.getClass().getSimpleName() + " 21 " + ost.getCoords().getX() + " " + ost.getCoords().getY());
            reiter.setBeschreibung("Erwartet: Reitet von West nach Ost, nach " + ost + ". Bekommt eine Warnung, dass die Durchreise bemerkt wurde.");
            reiter.setSkill(Reiten.class, 30);
            reiter.setItem(Pferd.class, 1);
            reiter.Befehle.add("NACH o o");

            Unit beob1 = this.createUnit(fremd, r);
            beob1.setName(getName() + " 22");
            beob1.setBeschreibung("Erwartet: Steht einfach so rum, Meldung über Durchreise steht bei der Region.");
        }

        { // Szenario 3: Durchreise zur See, fremde Einheit schaut zu
            Region r = null; Region ost = null; Region west = null;
            for (Region maybe : this.getTestWorld().nurTerrain(getRegions(), Ozean.class)) {
                ost = Region.Load(maybe.getCoords().shift(Richtung.Osten));
                west = Region.Load(maybe.getCoords().shift(Richtung.Westen));
                if ((ost instanceof Ozean) && (west instanceof Ozean)) {
					if (!getRegions().contains(ost)) continue;
					if (!getRegions().contains(west)) continue;
                    // gotcha!
                    r = maybe;
                    break;
                }
            }
            if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getClass().getSimpleName() + " gefunden - einfach nochmal probieren...");
            getRegions().remove(r); getRegions().remove(ost); getRegions().remove(west);

            Unit fahrer = this.createKapitaen(partei, west, Boot.class.getSimpleName());
            fahrer.setName(this.getClass().getSimpleName() + " 31 " + ost.getCoords().getX() + " " + ost.getCoords().getY());
            fahrer.setBeschreibung("Erwartet: Rudert von West nach Ost, nach " + ost + ". Bekommt eine Warnung, dass die Durchreise bemerkt wurde.");
            fahrer.Befehle.add("NACH o o");

            Unit beob1 = this.createKapitaen(fremd, r, Boot.class.getSimpleName());
            beob1.setName(this.getClass().getSimpleName() + " 32");
            beob1.setBeschreibung("Erwartet: Schwimmt einfach so rum, Meldung über Durchreise steht bei der Region.");
        }

        { // Szenario 4: Wie 3, aber mit ROUTE statt NACH
            Region r = null; Region ost = null; Region west = null;
            for (Region maybe : this.getTestWorld().nurTerrain(getRegions(), Ozean.class)) {
                ost = Region.Load(maybe.getCoords().shift(Richtung.Osten));
                west = Region.Load(maybe.getCoords().shift(Richtung.Westen));
                if ((ost instanceof Ozean) && (west instanceof Ozean)) {
					if (!getRegions().contains(ost)) continue;
					if (!getRegions().contains(west)) continue;
                    // gotcha!
                    r = maybe;
                    break;
                }
            }
            if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getClass().getSimpleName() + " gefunden - einfach nochmal probieren...");
            getRegions().remove(r); getRegions().remove(ost); getRegions().remove(west);

            Unit fahrer = this.createKapitaen(partei, west, Boot.class.getSimpleName());
            fahrer.setName(this.getClass().getSimpleName() + " 41 " + ost.getCoords().getX() + " " + ost.getCoords().getY());
            fahrer.setBeschreibung("Erwartet: Rudert von West nach Ost, nach " + ost + ". Bekommt eine Warnung, dass die Durchreise bemerkt wurde.");
            fahrer.Befehle.add("ROUTE o o PAUSE w w PAUSE");

            Unit mitfahrer = this.createUnit(fremd, west);
            mitfahrer.setName(this.getClass().getSimpleName() + " 43");
            mitfahrer.setBeschreibung("Erwartet: Fährt im Boot mit, keine Meldung über Durchreise des Kapitäns.");
			mitfahrer.setSchiff(fahrer.getSchiff());

            Unit beob1 = this.createKapitaen(fremd, r, Boot.class.getSimpleName());
            beob1.setName(this.getClass().getSimpleName() + " 42");
            beob1.setBeschreibung("Erwartet: Schwimmt einfach so rum, Meldung über Durchreise steht bei der Region.");
        }

	}

    @Override
    protected boolean verifyTest() {
        String testName = this.getClass().getSimpleName();
        boolean retval = true;

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + testName + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + testName + "...");
        if (!verifyScenario1()) retval = false;
        if (!verifyScenario2()) retval = false;
        if (!verifyScenario3()) retval = false;
        if (!verifyScenario4()) retval = false;

        

        if (GameRules.getRunde() > 3) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        return retval;
    }

    private boolean verifyScenario1() {
        String testName = this.getClass().getSimpleName();
        boolean retval = true;

        for (Unit u:Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(testName)) continue;

            String[] tokens = u.getName().split("\\ ");

            if (GameRules.getRunde() == 2) {
                // unit 01 - der Reiter muss am Ziel sein und soll KEINE Warnung
                // bekommen (wurde nur von seiner eigenen Partei gesehen).
                if (tokens[1].equals("01")) {
                    if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                        retval = fail(tokens[1] + ": " + u + " ist nicht in der Zielregion.");
                    }

                    List<Message> messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                    boolean found = false;
                    for (Message msg : messages) {
                        if (
                                msg.getText().toLowerCase().contains("bei der durchreise")
                                && msg.getText().toLowerCase().contains("beobachtet worden")
                        ) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        retval = fail(tokens[1] + ": ertappt-Nachricht fälschlich vorhanden (nur eigene Partei und Allierter schauen zu).");
                    }
                }

                // unit 02 und 03 - soll keine Durchreisemeldung bekommen.
                if (tokens[1].equals("02") || tokens[1].equals("03")) {
                    // alle Nachrichten zu den Koordinaten (Region, nicht Einheit)
                    List<Message> messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), null);
                    boolean found = false;
                    for (Message msg : messages) {
                        if (
                                msg.getText().toLowerCase().contains("beobachter")
                                && msg.getText().toLowerCase().contains("melden")
                                && msg.getText().contains(testName + " 01")
                        ) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        retval = fail(tokens[1] + ": Nachricht über Durchreise vorhanden - soll aber nicht (gleiche Partei).");
                    }
                }
            }

        } // next unit

        return retval;
    }

    private boolean verifyScenario2() {
        String testName = this.getClass().getSimpleName();
        boolean retval = true;

        for (Unit u:Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(testName)) continue;

            String[] tokens = u.getName().split("\\ ");

            if (GameRules.getRunde() == 2) {
                // Der Reiter muss am Ziel sein und SOLL EINE Warnung
                // bekommen (wurde von einer fremden Partei gesehen).
                if (tokens[1].equals("21")) {
                    if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                        retval = fail(tokens[1] + ": " + u + " ist nicht in der Zielregion.");
                    }

                    List<Message> messages = Message.Retrieve(Partei.getPartei(u.getOwner()), (Coords)null, u);
                    boolean found = false;
                    for (Message msg : messages) {
                        if (
                                msg.getText().toLowerCase().contains("bei der durchreise")
                                && msg.getText().toLowerCase().contains("beobachtet worden")
                        ) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        retval = fail(tokens[1] + ": ertappt-Nachricht fehlt (fremde Partei!).");
                    }
                }

                // unit 22 - muss die Durchreisemeldung bekommen.
                if (tokens[1].equals("22")) {
                    // alle Nachrichten zu den Koordinaten (Region, nicht Einheit)
                    List<Message> messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), null);
                    boolean found = false;
                    for (Message msg : messages) {
                        if (
                                msg.getText().toLowerCase().contains("beobachter")
                                && msg.getText().toLowerCase().contains("melden")
                                && msg.getText().contains(testName + " 21")
                        ) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        retval = fail(tokens[1] + ": Keine Nachricht über Durchreise vorhanden (fremde Partei).");
                    }
                }
            }

        } // next unit

        return retval;
    }

    private boolean verifyScenario3() {
        String testName = this.getClass().getSimpleName();
        boolean retval = true;

        for (Unit u:Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(testName)) continue;

            String[] tokens = u.getName().split("\\ ");

            if (GameRules.getRunde() == 2) {
                // Der Käptn muss am Ziel sein und SOLL EINE Warnung
                // bekommen (wurde von einer fremden Partei gesehen).
                if (tokens[1].equals("31")) {
                    if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                        retval = fail(tokens[1] + ": " + u + " ist nicht in der Zielregion.");
                    }

                    List<Message> messages = Message.Retrieve(Partei.getPartei(u.getOwner()), (Coords)null, u);
                    boolean found = false;
                    for (Message msg : messages) {
                        if (
                                msg.getText().toLowerCase().contains("bei der durchreise")
                                && msg.getText().toLowerCase().contains("beobachtet worden")
                        ) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        retval = fail(tokens[1] + ": ertappt-Nachricht fehlt (fremde Partei!).");
                    }
                }

                // unit 32 - muss die Durchreisemeldung bekommen.
                if (tokens[1].equals("32")) {
                    // alle Nachrichten zu den Koordinaten (Region, nicht Einheit)
                    List<Message> messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), null);
                    boolean found = false;
                    for (Message msg : messages) {
                        if (
                                msg.getText().toLowerCase().contains("beobachter")
                                && msg.getText().toLowerCase().contains("melden")
                                && msg.getText().toLowerCase().contains("schiff") // Teil des Default-Names von Schiffen
                        ) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        retval = fail(tokens[1] + ": Keine Nachricht über Durchreise vorhanden (fremde Partei).");
                    }
                }
            }

        } // next unit

        return retval;
    }


    private boolean verifyScenario4() {
        String testName = this.getClass().getSimpleName();
        boolean retval = true;

        for (Unit u:Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(testName)) continue;

            String[] tokens = u.getName().split("\\ ");

            if (GameRules.getRunde() == 2) {
                // Der Käptn muss am Ziel sein und SOLL EINE Warnung
                // bekommen (wurde von einer fremden Partei gesehen).
                if (tokens[1].equals("41")) {
                    if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                        retval = fail(tokens[1] + ": " + u + " ist nicht in der Zielregion.");
                    }

                    List<Message> messages = Message.Retrieve(Partei.getPartei(u.getOwner()), (Coords)null, u);
                    boolean found = false;
                    for (Message msg : messages) {
                        if (
                                msg.getText().toLowerCase().contains("bei der durchreise")
                                && msg.getText().toLowerCase().contains("beobachtet worden")
                        ) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        retval = fail(tokens[1] + ": ertappt-Nachricht fehlt (fremde Partei!).");
                    }
                }

                // unit 42 - muss die Durchreisemeldung bekommen.
                if (tokens[1].equals("42")) {
                    // alle Nachrichten zu den Koordinaten (Region, nicht Einheit)
                    List<Message> messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), null);
                    boolean found = false;
                    for (Message msg : messages) {
                        if (
                                msg.getText().toLowerCase().contains("beobachter")
                                && msg.getText().toLowerCase().contains("melden")
                                && msg.getText().toLowerCase().contains("schiff") // Teil des Default-Names von Schiffen
                        ) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        retval = fail(tokens[1] + ": Keine Nachricht über Durchreise vorhanden (fremde Partei).");
                    }
                }
            }

        } // next unit

        return retval;
    }
}
