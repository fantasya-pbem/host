package de.x8bit.Fantasya.Host.ManualTests.Misc;

import java.util.List;

import de.x8bit.Fantasya.Host.ManualTests.*;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Ebene;
import de.x8bit.Fantasya.Atlantis.Regions.Hochland;
import de.x8bit.Fantasya.Atlantis.Regions.Wueste;
import de.x8bit.Fantasya.Atlantis.Spells.SegenDerGoettin;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 * nicht kompatibel mit dem "klassischen" ZAT (v 0.15.11)
 * @author hb
 */
public class ZauberSegenDerGoettin extends TestBase {

    @Override
    protected void mySetupTest() {
		TestWorld tw = this.getTestWorld();

		// für die Magier 01, 02, 03:
		Partei p = tw.createPartei(Mensch.class);
		p.setName(getName()+"-Magier I");
        {
			Region r = this.getTestWorld().nurTerrain(getRegions(), Wueste.class).get(0);
			if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
			r.setName(getName()+" R01");
			getRegions().remove(r);

			Unit u = this.createMage(p, r, 12);
            u.setName(this.getName()+" 01");
			u.setSpell(new SegenDerGoettin());
			u.setAura(12*12);
			u.Befehle.add("ZAUBERE Segen der Goettin");

            new Info(this.getName() + " Setup in " + r + ".", u);
        }

        {
			Region r = this.getTestWorld().nurTerrain(getRegions(), Wueste.class).get(0);
			if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
			r.setName(getName()+" R02");
			getRegions().remove(r);

			Unit u = this.createMage(p, r, 12);
            u.setName(this.getName()+" 02");
			u.setSpell(new SegenDerGoettin());
			u.setAura(11*12);
			u.Befehle.add("ZAUBERE Segen der Göttin");

            new Info(this.getName() + " Setup in " + r + ".", u);
        }

        {
			Region r = this.getTestWorld().nurTerrain(getRegions(), Hochland.class).get(0);
			if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
			r.setName(getName()+" R03");
			getRegions().remove(r);

			Unit u = this.createMage(p, r, 12);
            u.setName(this.getName()+" 03");
			u.setSpell(new SegenDerGoettin());
			u.setAura(12*12);
			u.Befehle.add("ZAUBERE Segen der Göttin");

            new Info(this.getName() + " Setup in " + r + ".", u);
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01 - Wüste zu Ebene
            if (tokens[1].equals("01")) {
				Region r = Region.Load(u.getCoords());
				if (!(r instanceof Ebene)) {
					retval = fail(tokens[1] + ": Region hat sich nicht in eine Ebene verwandelt.");
				}

                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("hat 144 punkte aura") && text.contains("verbraucht")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Aura-Verbrauch fehlt.");

                messages = Message.Retrieve(null, u.getCoords(), null);
                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("der fruchtbarkeit")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Zauber-Folgen fehlt.");
            }

            // unit 02 - zu wenig Aura
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);

                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("erfordert die") && text.contains("magische stufe")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Aura-Mangel fehlt.");
            }

            // unit 03 - nicht in einer Wüste
            if (tokens[1].equals("03")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);

                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("leider befindet sich") && text.contains("nicht in einer")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über falsches Terrain fehlt.");
            }


        } // next unit

        return retval;
    }

}
