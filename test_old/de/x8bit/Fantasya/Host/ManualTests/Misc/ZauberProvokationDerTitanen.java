package de.x8bit.Fantasya.Host.ManualTests.Misc;

import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Berge;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Gletscher;
import de.x8bit.Fantasya.Atlantis.Regions.Hochland;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Regions.Sumpf;
import de.x8bit.Fantasya.Atlantis.Regions.Wueste;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Ships.Boot;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Skills.Segeln;
import de.x8bit.Fantasya.Atlantis.Spells.ProvokationDerTitanen;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 * nicht kompatibel mit dem "klassischen" ZAT (v 0.15.11)
 * @author hb
 */
public class ZauberProvokationDerTitanen extends TestBase {

    @Override
    protected void mySetupTest() {
		TestWorld tw = this.getTestWorld();

		// für die Magier 01, 02, 03:
		Partei p = tw.createPartei(Mensch.class);
		p.setName(getName()+"-Magier I");
        {
			Region r = this.getTestWorld().nurTerrain(getRegions(), Sumpf.class).get(0);
			if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
			r.setName(getName()+" R01");
			getRegions().remove(r);

			Unit u = this.createMage(p, r, 11);
            u.setName(this.getName()+" 01");
			u.setSpell(new ProvokationDerTitanen());
			u.setAura(11*11);
			u.Befehle.add("ZAUBERE Provokation der Titanen");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

        {
			Region r = this.getTestWorld().nurTerrain(getRegions(), Ebene.class).get(0);
			if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
			r.setName(getName()+" R02");
			getRegions().remove(r);

			Unit u = this.createMage(p, r, 11);
            u.setName(this.getName()+" 02");
			u.setSkill(Magie.class, (12*11 * 30)/2);
			u.setSpell(new ProvokationDerTitanen());
			u.setAura(11*11);
			u.Befehle.add("ZAUBERE Provokation der Titanen");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

        {
			Region r = this.getTestWorld().nurTerrain(getRegions(), Hochland.class).get(0);
			if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
			r.setName(getName()+" R03");
			getRegions().remove(r);

			Unit u = this.createMage(p, r, 11);
            u.setName(this.getName()+" 03");
			u.setSpell(new ProvokationDerTitanen());
			u.setAura(11*11);
			u.Befehle.add("ZAUBERE Provokation der Titanen");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

		// für die Magier 04, 05, 06:
		p = tw.createPartei(Mensch.class);
		p.setName(getName()+"-Magier II");

        {
			Region r = this.getTestWorld().nurTerrain(getRegions(), Berge.class).get(0);
			if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
			r.setName(getName()+" R04");
			getRegions().remove(r);

			Unit u = this.createMage(p, r, 11);
            u.setName(this.getName()+" 04");
			u.setSpell(new ProvokationDerTitanen());
			u.setAura(11*11);
			u.Befehle.add("ZAUBERE Provokation der Titanen");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

        { // Ozean zu Sumpf (mit Schiff)
			Region r = this.getTestWorld().nurTerrain(getRegions(), Ozean.class).get(0);
			if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
			r.setName(getName()+" R05");
			getRegions().remove(r);

			Ship s = Ship.Create(Boot.class.getSimpleName(), r.getCoords());
			s.setName(getName() + "-Provokation-Boot");
			s.setGroesse(s.getConstructionSize());

			Unit u = this.createMage(p, r, 11);
            u.setName(this.getName()+" 05");
			u.setSkill(Segeln.class, 300);
			u.setSpell(new ProvokationDerTitanen());
			u.setAura(11*11);
			u.Befehle.add("ZAUBERE Provokation der Titanen");
			u.setItem(Silber.class, 4000); // um das Boot nicht zu überladen
			u.Enter(s);

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

		// für die Magier 07, 08, 09:
		p = tw.createPartei(Mensch.class);
		p.setName(getName()+"-Magier III");

        {
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
			if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
			r.setName(getName()+" R07");
			getRegions().remove(r);

			Unit u = this.createMage(p, r, 11);
            u.setName(this.getName()+" 07");
			u.setSpell(new ProvokationDerTitanen());
			u.setAura(10*11); // zu wenig Aura
			u.Befehle.add("ZAUBERE Provokation der Titanen");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

        {
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
			if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
			r.setName(getName()+" R08");
			getRegions().remove(r);

			Unit u = this.createMage(p, r, 11);
            u.setName(this.getName()+" 08");
			u.setSpell(new ProvokationDerTitanen());
			u.setAura(11*11);
			u.Befehle.add("ZAUBERE Provokation der Titanen");
			u.setItem(Silber.class, 40); // zu wenig Silber

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }

        {
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
			if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
			r.setName(getName()+" R09");
			getRegions().remove(r);

			Unit u = this.createMage(p, r, 10);
            u.setName(this.getName()+" 09");
			u.setSpell(new ProvokationDerTitanen());
			u.setAura(11*11);
			u.Befehle.add("ZAUBERE Provokation der Titanen");

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }



    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
//        Partei p = this.getTestWorld().getSpieler1();
        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04", "05", "07", "08", "09"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01 - Sumpf zu Wüste
            if (tokens[1].equals("01")) {
				Region r = Region.Load(u.getCoords());
				if (!(r instanceof Wueste)) {
					retval = fail(tokens[1] + ": Region hat sich nicht in eine Wüste verwandelt.");
				}

                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);

                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("hat 121 punkte aura") && text.contains("verbraucht")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Aura-Verbrauch fehlt.");

                messages = Message.Retrieve(null, u.getCoords(), null);
                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("der boden hob sich permanent")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Zauber-Folgen fehlt.");
            }

            // unit 02 - Ebene zu Hochland
            if (tokens[1].equals("02")) {
				Region r = Region.Load(u.getCoords());
				if (!(r instanceof Hochland)) {
					retval = fail(tokens[1] + ": Region hat sich nicht in ein Hochland verwandelt.");
				}

				if (r.getResource(Eisen.class).getAnzahl() > 20) {
					retval = fail(tokens[1] + ": Es gibt nicht die erwarteten Eisen-Vorräte.");
				}
				if (r.getResource(Stein.class).getAnzahl() > 20) {
					retval = fail(tokens[1] + ": Es gibt nicht die erwarteten Stein-Vorkommen.");
				}

                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);

                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("hat 121 punkte aura") && text.contains("verbraucht")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Aura-Verbrauch fehlt.");

                messages = Message.Retrieve(null, u.getCoords(), null);
                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("der boden hob sich permanent")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Zauber-Folgen fehlt.");
            }

            // unit 03 - Hochland zu Berge
            if (tokens[1].equals("03")) {
				Region r = Region.Load(u.getCoords());
				if (!(r instanceof Berge)) {
					retval = fail(tokens[1] + ": Region hat sich nicht in ein Gebirge verwandelt.");
				}

				int eisen = r.getResource(Eisen.class).getAnzahl();
				int steine = r.getResource(Stein.class).getAnzahl();
				if ((eisen < 70) || (eisen >130)) {
					retval = fail(tokens[1] + ": Es gibt nicht die erwarteten Eisen-Vorräte.");
				}
				if ((steine < 70) || (steine >130)) {
					retval = fail(tokens[1] + ": Es gibt nicht die erwarteten Stein-Vorkommen.");
				}

                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);

                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("hat 121 punkte aura") && text.contains("verbraucht")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Aura-Verbrauch fehlt.");

                messages = Message.Retrieve(null, u.getCoords(), null);
                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("der boden hob sich permanent")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Zauber-Folgen fehlt.");
            }

            // unit 04 - Berge zu Gletscher
            if (tokens[1].equals("04")) {
				Region r = Region.Load(u.getCoords());
				if (!(r instanceof Gletscher)) {
					retval = fail(tokens[1] + ": Region hat sich nicht in einen Gletscher verwandelt.");
				}

				if (r.getResource(Eisen.class).getAnzahl() > 20) {
					retval = fail(tokens[1] + ": Es gibt nicht die erwarteten Eisen-Vorräte.");
				}
				if (r.getResource(Stein.class).getAnzahl() > 20) {
					retval = fail(tokens[1] + ": Es gibt nicht die erwarteten Stein-Vorkommen.");
				}

                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);

                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("hat 121 punkte aura") && text.contains("verbraucht")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Aura-Verbrauch fehlt.");

                messages = Message.Retrieve(null, u.getCoords(), null);
                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("der boden hob sich permanent")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Zauber-Folgen fehlt.");
            }

            // unit 05 - Ozean zu Sumpf, aus einem Boot heraus:
            if (tokens[1].equals("05")) {
				Region r = Region.Load(u.getCoords());
				if (!(r instanceof Sumpf)) {
					retval = fail(tokens[1] + ": Region hat sich nicht in einen Sumpf verwandelt.");
				}

                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);

                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("hat 121 punkte aura") && text.contains("verbraucht")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Aura-Verbrauch fehlt.");

                messages = Message.Retrieve(null, u.getCoords(), null);
                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("der boden hob sich permanent")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Zauber-Folgen fehlt.");
            }



            // unit 07 - zu wenig Aura
            if (tokens[1].equals("07")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);

                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("mindestens im") && text.contains("grade verspottet werden")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über unzureichende Spruch-Stufe fehlt.");
            }

            // unit 08 - zu wenig Silber
            if (tokens[1].equals("08")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);

                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("fehlt") && text.contains("silber")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Silber-Mangel fehlt.");
            }

            // unit 09 - zu wenig Talent
            if (tokens[1].equals("09")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);

                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("ist verwirrt")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über unverständlichen Zauber fehlt.");
            }


        } // next unit

        return retval;
    }

}
