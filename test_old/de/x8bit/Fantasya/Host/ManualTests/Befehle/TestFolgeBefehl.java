package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Ships.Boot;
import de.x8bit.Fantasya.Atlantis.Ships.Langboot;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Ork;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 * Tests zum FOLGE-Befehl.
 *
 * Fälle:
 * <ul><li>eigener Einheit folgen; diese reist nicht (01, 02)</li>
 * <li>eigener Einheit korrekt folgen (03, 04)</li>
 * <li>eigener Einheit folgen; der Anführer kann nicht so schnell laufen wie der Folgende(05, 06)</li>
 * <li>fremder Einheit folgen, alles okay. (07, 08)</li>
 * <li>fremder getarnter Einheit folgen - sollte fehlschlagen. (09, 10)</li>
 * <li>einem Schiff folgen (Variante FOLGE SCHIFF xy) (11-20: gleiche Fälle wie 01-10)</li>
 *
 * <li>fremder getarnter Einheit eines Alliierten (KONTAKTIERE) folgen - sollte klappen (101, 102).</li>
 * <li>fremder TEMP-Einheit folgen (103, 104, 105)</li>
 * 
 * <li>nicht-existentem Schiff folgen, nicht-existenter Einheit/TEMP-Einheit folgen. (106, 108, 110)</li>
 * <li>sich selbst folgen (112 ;-) )</li>
 * </ul>
 *
 * Der Test ist nicht mehr ZAT-kompatibel (EVA-Umstrukturierung Ende 2010/Anfang 2011...)
 *
 * @author hapebe
 */
public class TestFolgeBefehl extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();

		setupLandFolgen(p);
		setupSchifffahrt(p);
    }

	/**
	 * Erzeugt die Land-Einheiten
	 * @param p
	 */
	private void setupLandFolgen(Partei p) {
		Region r = null;
		Richtung ri1 = null;
		Region r1 = null;
		Richtung ri2 = null;
		Region r2 = null;
		for(Region maybeR0 : this.getTestWorld().nurBetretbar(getRegions())) {
			for (Region maybeR1 : maybeR0.getNachbarn()) {
				if (maybeR1.istBetretbar(null)) {
					if (!getRegions().contains(maybeR1)) continue;
					for (Region maybeR2 : maybeR1.getNachbarn()) {
						if (maybeR2.istBetretbar(null) && (!maybeR2.getCoords().equals(maybeR0.getCoords()))) {
							if (!getRegions().contains(maybeR2)) continue;
							// gotcha!
							r = maybeR0;
							r1 = maybeR1;
							ri1 = maybeR0.getCoords().getRichtungNach(r1.getCoords());
							r2 = maybeR2;
							ri2 = r1.getCoords().getRichtungNach(r2.getCoords());

							break;
						}
					}
					if (r != null) break;
				}
			}
			if (r != null) break;
		}

		if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + "-LandVerfolgung gefunden - einfach nochmal probieren...");
		getRegions().remove(r);
		getRegions().remove(r1);
		getRegions().remove(r2);

		r.setName(getName() + "-Straße-0");
		r1.setName(getName() + "-Straße-1");
		r2.setName(getName() + "-Straße-2");

		Unit u = this.createUnit(p, r);
		u.setName(this.getName()+" 01");
		u.Befehle.add("LERNE Wahrnehmung");
        u.setBeschreibung(u.Befehle.get(0));
		String fauler = u.getNummerBase36();

		u = this.createUnit(p, r);
		u.setName(this.getName()+" 02 " + r.getCoords().getX() + " " + r.getCoords().getY());
		u.Befehle.add("FOLGE EINHEIT " + fauler);
        u.setBeschreibung(u.Befehle.get(0));

		u = this.createUnit(p, r);
		u.setName(this.getName()+" 03 " + r2.getCoords().getX() + " " + r2.getCoords().getY());
		u.getSkill(Reiten.class).setLerntage(180 * u.getPersonen());
		u.getItem(Pferd.class).setAnzahl(u.getPersonen());
		u.Befehle.add("NACH " + ri1.getShortcut() + " " + ri2.getShortcut());
        u.setBeschreibung(u.Befehle.get(0));
		String reiter = u.getNummerBase36();

		u = this.createUnit(p, r);
		u.setName(this.getName()+" 04 " + r2.getCoords().getX() + " " + r2.getCoords().getY());
		u.getSkill(Reiten.class).setLerntage(180 * u.getPersonen());
		u.getItem(Pferd.class).setAnzahl(u.getPersonen());
		u.Befehle.add("FOLGEN EINHEIT " + reiter);
        u.setBeschreibung(u.Befehle.get(0));

		u = this.createUnit(p, r);
		u.getItem(Silber.class).setAnzahl(100);
		u.setName(this.getName()+" 05 " + r1.getCoords().getX() + " " + r1.getCoords().getY());
		u.Befehle.add("NACH " + ri1.getShortcut() + " " + ri2.getShortcut());
		String wanderer = u.getNummerBase36();

		u = this.createUnit(p, r);
		u.setName(this.getName()+" 06 " + r1.getCoords().getX() + " " + r1.getCoords().getY());
		u.getSkill(Reiten.class).setLerntage(180 * u.getPersonen());
		u.getItem(Pferd.class).setAnzahl(u.getPersonen());
		u.Befehle.add("FOLGE EINHEIT " + wanderer);
        u.setBeschreibung(u.Befehle.get(0));

		Partei fremde = this.getTestWorld().createPartei(Elf.class);
		fremde.setName(getName()+"-Fremde");

		u = this.createUnit(fremde, r1);
		u.setName(this.getName()+" 07 " + r2.getCoords().getX() + " " + r2.getCoords().getY());
		u.getItem(Silber.class).setAnzahl(100);
		u.Befehle.add("TARNE EINHEIT"); // das sollte ja nicht funktionieren
		u.Befehle.add("ROUTE " + ri2.getShortcut() + " PAUSE " + ri2.invert().getShortcut() + " PAUSE");
		String fremder = u.getNummerBase36();

		u = this.createUnit(p, r1);
		u.setName(this.getName()+" 08 " + r2.getCoords().getX() + " " + r2.getCoords().getY());
		u.getItem(Silber.class).setAnzahl(100);
		u.Befehle.add("FOLGE EINHEIT " + fremder);
        u.setBeschreibung(u.Befehle.get(0));

		u = this.createUnit(fremde, r1);
		u.setName(this.getName()+" 09 " + r2.getCoords().getX() + " " + r2.getCoords().getY());
		u.getItem(Silber.class).setAnzahl(100);
		u.setSkill(Tarnung.class, 1650 * u.getPersonen());
		u.Befehle.add("TARNE EINHEIT");
		u.Befehle.add("ROUTE " + ri2.getShortcut() + " PAUSE " + ri2.invert().getShortcut() + " PAUSE");
		String fremderGetarnter = u.getNummerBase36();

		u = this.createUnit(p, r1);
		u.setName(this.getName()+" 10 " + r1.getCoords().getX() + " " + r1.getCoords().getY());
		u.getItem(Silber.class).setAnzahl(100);
		u.Befehle.add("FOLGE EINHEIT " + fremderGetarnter);
        u.setBeschreibung(u.Befehle.get(0));


		// Spezialfälle:
		Partei alliierte = this.getTestWorld().createPartei(Ork.class);
		alliierte.setName(getName()+"-Alliierte");
		String kurs = ri1.invert().getShortcut();
		
		// getarnter alliierter Einheit folgen:
		u = this.createUnit(alliierte, r1);
		u.setName(this.getName()+" 101 " + r.getCoords().getX() + " " + r.getCoords().getY());
		u.getItem(Silber.class).setAnzahl(100);
		u.setSkill(Tarnung.class, 1650 * u.getPersonen());
		u.Befehle.add("HELFE " + p.getNummerBase36() + " KONTAKTIERE");
		u.Befehle.add("TARNE EINHEIT");
		u.Befehle.add("NACH " + kurs);
		String alliierterGetarnter = u.getNummerBase36();

		u = this.createUnit(p, r1);
		u.setName(this.getName()+" 102 " + r.getCoords().getX() + " " + r.getCoords().getY());
		u.getItem(Silber.class).setAnzahl(100);
		u.Befehle.add("FOLGE EINHEIT " + alliierterGetarnter);
        u.setBeschreibung(u.Befehle.get(0));

		
		// fremder, allierter TEMP-Einheit folgen
		u = this.createUnit(alliierte, r1);
		u.setName(this.getName()+" 103 " + r1.getCoords().getX() + " " + r1.getCoords().getY());
		u.setPersonen(2);
		u.getItem(Silber.class).setAnzahl(100);
		u.setSkill(Tarnung.class, 1650 * u.getPersonen());
		// u.Befehle.add("HELFE " + p.Base36() + " KONTAKTIERE");
		u.Befehle.add("TARNE EINHEIT");
		u.Befehle.add("GIB TEMP alli 1 PERSONEN");
		u.Befehle.add("GIB TEMP alli 50 Silber");
		u.Befehle.add("MACHE TEMP alli");
		u.Befehle.add("BENENNE EINHEIT \"" + getName() + " 105 " + r.getCoords().getX() + " " + r.getCoords().getY() + "\"");
		u.Befehle.add("TARNE EINHEIT");
		u.Befehle.add("NACH " + kurs);
		u.Befehle.add("ENDE");
		u.Befehle.add("LERNE Ausdauer");

		u = this.createUnit(p, r1);
		u.setName(this.getName()+" 104 " + r.getCoords().getX() + " " + r.getCoords().getY());
		u.getItem(Silber.class).setAnzahl(100);
		u.Befehle.add("FOLGE EINHEIT TEMP alli");
        u.setBeschreibung(u.Befehle.get(0));


		// nicht existenten Einheiten und Schiffen folgen:
		u = this.createUnit(p, r1);
		u.setName(this.getName()+" 106 " + r1.getCoords().getX() + " " + r1.getCoords().getY());
		u.getItem(Silber.class).setAnzahl(100);
		u.Befehle.add("FOLGE SCHIFF 70tp");
        u.setBeschreibung(u.Befehle.get(0));

		u = this.createUnit(p, r1);
		u.setName(this.getName()+" 108 " + r1.getCoords().getX() + " " + r1.getCoords().getY());
		u.getItem(Silber.class).setAnzahl(100);
		u.Befehle.add("FOLGE EINHEIT 5hhj");
        u.setBeschreibung(u.Befehle.get(0));

		u = this.createUnit(p, r1);
		u.setName(this.getName()+" 110 " + r1.getCoords().getX() + " " + r1.getCoords().getY());
		u.getItem(Silber.class).setAnzahl(100);
		u.Befehle.add("FOLGE EINHEIT TEMP 9pqx");
        u.setBeschreibung(u.Befehle.get(0));
		

		// sich selbst folgen
		u = this.createUnit(p, r1);
		u.setName(this.getName()+" 112 " + r1.getCoords().getX() + " " + r1.getCoords().getY());
		u.getItem(Silber.class).setAnzahl(100);
		u.Befehle.add("FOLGE EINHEIT " + u.getNummerBase36());
        u.setBeschreibung(u.Befehle.get(0));



		new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
	}

	/**
	 * Erzeugt die Schifffahrts-Einheiten
	 * @param p
	 */
	private void setupSchifffahrt(Partei p) {
		Region r = null;
		Richtung ri1 = null;
		Region r1 = null;
		Richtung ri2 = null;
		Region r2 = null;
		for(Region maybeR0 : this.getTestWorld().nurTerrain(getRegions(), Ozean.class)) {
			for (Region maybeR1 : maybeR0.getNachbarn()) {
				if (!getRegions().contains(maybeR1)) continue;
				if (maybeR1 instanceof Ozean) {
					for (Region maybeR2 : maybeR1.getNachbarn()) {
						if ((maybeR2 instanceof Ozean) && (!maybeR2.getCoords().equals(maybeR0.getCoords()))) {
							if (!getRegions().contains(maybeR2)) continue;
							// gotcha!
							r = maybeR0;
							r1 = maybeR1;
							ri1 = maybeR0.getCoords().getRichtungNach(r1.getCoords());
							r2 = maybeR2;
							ri2 = r1.getCoords().getRichtungNach(r2.getCoords());

							break;
						}
					}
					if (r != null) break;
				}
			}
			if (r != null) break;
		}

		if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + "-Seefahrt gefunden - einfach nochmal probieren...");
		getRegions().remove(r);
		getRegions().remove(r1);
		getRegions().remove(r2);

		Region.Load(r.getCoords()).setName(getName() + "-Strecke-0");
		r1.setName(getName() + "-Strecke-1");
		r2.setName(getName() + "-Strecke-2");


		Unit u = this.createKapitaen(p, r, Boot.class.getSimpleName());
		u.setName(this.getName()+" 11");
		u.Befehle.add("LERNE Wahrnehmung");
		String fauler = Codierung.toBase36(u.getSchiff());

		u = this.createKapitaen(p, r, Boot.class.getSimpleName());
		u.setName(this.getName()+" 12 " + r.getCoords().getX() + " " + r.getCoords().getY());
		u.Befehle.add("FOLGE SCHIFF " + fauler);

		u = this.createKapitaen(p, r, Langboot.class.getSimpleName());
		u.setName(this.getName()+" 13 " + r1.getCoords().getX() + " " + r1.getCoords().getY());
		u.Befehle.add("NACH " + ri1.getShortcut() + " " + ri2.getShortcut() + " " +  ri2.invert().getShortcut());
        u.setBeschreibung("fährt voraus - " + u.Befehle.get(0));
		String schneller = Codierung.toBase36(u.getSchiff());

		u = this.createKapitaen(p, r, Boot.class.getSimpleName());
		u.setName(this.getName()+" 14 " + r2.getCoords().getX() + " " + r2.getCoords().getY());
		u.Befehle.add("FOLGEN SCHIFF " + schneller);
        u.setBeschreibung(u.Befehle.get(0));

		u = this.createKapitaen(p, r, Boot.class.getSimpleName());
		u.getItem(Silber.class).setAnzahl(100);
		u.setName(this.getName()+" 15 " + r2.getCoords().getX() + " " + r2.getCoords().getY());
		u.Befehle.add("NACH " + ri1.getShortcut() + " " + ri2.getShortcut() + " " + ri2.invert().getShortcut());
		String langsamer = Codierung.toBase36(u.getSchiff());

		u = this.createKapitaen(p, r, Boot.class.getSimpleName());
		u.setName(this.getName()+" 16 " + r2.getCoords().getX() + " " + r2.getCoords().getY());
		u.Befehle.add("FOLGE SCHIFF " + langsamer);
        u.setBeschreibung(u.Befehle.get(0));

		Partei fremde = this.getTestWorld().createPartei(Elf.class);
		fremde.setName(getName()+"-Fremde-Schiffer");

		u = this.createKapitaen(fremde, r1, Boot.class.getSimpleName());
		u.setName(this.getName()+" 17 " + r2.getCoords().getX() + " " + r2.getCoords().getY());
		u.Befehle.add("TARNE EINHEIT"); // das sollte ja nicht funktionieren
		u.Befehle.add("ROUTE " + ri2.getShortcut() + " PAUSE " + ri2.invert().getShortcut() + " PAUSE");
		String fremder = Codierung.toBase36(u.getSchiff());

		u = this.createKapitaen(p, r1, Boot.class.getSimpleName());
		u.setName(this.getName()+" 18 " + r2.getCoords().getX() + " " + r2.getCoords().getY());
		u.Befehle.add("FOLGE SCHIFF " + fremder);
        u.setBeschreibung(u.Befehle.get(0));

		u = this.createKapitaen(fremde, r1, Boot.class.getSimpleName());
		u.setName(this.getName()+" 19 " + r.getCoords().getX() + " " + r.getCoords().getY());
		u.setSkill(Tarnung.class, 1650 * u.getPersonen());
		u.Befehle.add("TARNE EINHEIT");
		u.Befehle.add("ROUTE " + ri1.invert().getShortcut() + " PAUSE");
		String fremderGetarnter = Codierung.toBase36(u.getSchiff());

		u = this.createKapitaen(p, r1, Boot.class.getSimpleName());
		u.setName(this.getName()+" 20 " + r1.getCoords().getX() + " " + r1.getCoords().getY());
		u.Befehle.add("FOLGE SCHIFF " + fremderGetarnter);
        u.setBeschreibung(u.Befehle.get(0));

		new Info(this.getName() + "-Schifffahrt Setup in " + r + ".", u, u.getCoords());
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

        String missing = this.verifyExpectedUnits(
				units,
				new String[] {
					"01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
					"11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
					"101", "102", "103", "104", "105", "06", "07", "08", "09", "10",
				}
		);
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

			// wenn Koordinaten für die Einheit angegeben sind:
			if (tokens.length == 4) {
				if (!this.verifyUnitCoords(tokens, u.getCoords())) {
					retval = fail(tokens[1] + " ist nicht in der erwarteten Region.");
				}
			}

            // 112 - Selbst-Folger
            if (tokens[1].equals("112")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("selbst") && text.contains("kommt nicht voran")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Selbst-Folgen fehlt.");
            }
        } // next unit

        return retval;
    }

}
