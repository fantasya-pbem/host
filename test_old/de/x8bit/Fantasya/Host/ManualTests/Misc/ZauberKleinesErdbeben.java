package de.x8bit.Fantasya.Host.ManualTests.Misc;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Host.ManualTests.*;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Messages.Debug;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Spells.KleinesErdbeben;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.EVA.Kampfzauber;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class ZauberKleinesErdbeben extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
			Unit u = this.createUnit(p, r);
            u.setName(this.getName() + " 01 " + u.getName());

			Partei p2 = this.getTestWorld().createPartei(Mensch.class);
			p2.setName("Z-Kleines Erdbeben");

			Unit magier = this.createMage(p2, r, 6);
			magier.setName(this.getName() + " 02 " + magier.getAura());
			magier.setSpell(new KleinesErdbeben());
			magier.Befehle.add("ZAUBERE \"Kleines Erdbeben\" 4");

			Building burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			burg.setName("Schüttelhütte I");
			burg.setSize(100);

			burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			burg.setName("Schüttelhütte II");
			burg.setSize(100);

			burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			burg.setName("Schüttelhütte III");
			burg.setSize(100);

			u.Enter(burg);

			// Als Kampfzauber:
			r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
			getRegions().remove(r);

			Unit a = this.createUnit(p, r);
            a.setName(this.getName() + " 11");
			a.setPersonen(10);
			a.setSkill(Hiebwaffen.class, 300 * a.getPersonen());
			a.setItem(Schwert.class, a.getPersonen());

			Unit dummy = this.createUnit(p, r);
			dummy.setName("Dummy");
			dummy.setPersonen(50);
			dummy.setItem(Silber.class, dummy.getPersonen() * 10);
			dummy.Befehle.add("LERNE Ausdauer");

			Unit b = this.createUnit(p2, r);
            b.setName(this.getName() + " 12");
			b.setPersonen(20);
			b.setSkill(Hiebwaffen.class, 300 * b.getPersonen());
			b.setItem(Schwert.class, b.getPersonen());

			magier = this.createMage(p2, r, 6);
			magier.setName(this.getName() + " 13 " + magier.getAura());
			magier.setSpell(new KleinesErdbeben());
			magier.setProperty(Kampfzauber.CONFUSIONSPELL, "Kleines Erdbeben 4");
			// magier.Befehle.add("KAMPFZAUBER VERWIRRUNG \"Erdbeben\"");
            magier.setKampfposition(Kampfposition.Hinten);
			magier.Befehle.add("LERNE Magie");

			for (String prop : magier.getProperties()) {
				new Debug("Magier-Property: " + prop + "=" + magier.getStringProperty(prop));
			}

			a.Befehle.add("ATTACKIERE " + b.getNummerBase36());
			b.Befehle.add("LERNE Hiebwaffen");

            new Info(this.getName() + "-Kampf Setup in " + r + ".", u);
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "12", "13"});
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
                    if (text.contains("wurde durch das erdbeben") && text.contains("besch")) found = true; // beschädigt
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Erdbebenschäden fehlt.");

				Building b = Building.getBuilding(u.getGebaeude());
				if ((b == null) || (b.getSize() > 99) ) {
					 retval = fail(tokens[1] + ": Die Burg ist nicht beschädigt worden (oder ganz verschwunden).");
				}
            }

            // unit 02
            if (tokens[1].equals("02")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("sst die erde in") && text.contains("beben")) found = true; // lä...sst
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Zauber fehlt.");
            }


        } // next unit

        return retval;
    }

}
