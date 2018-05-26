package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Units.Echse;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Atlantis.Units.Troll;
import de.x8bit.Fantasya.Host.EVA.Sortieren;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 * Siehe auch GibBauern und GibNull.
 * @author hb
 */
public class TestGibPersonen extends TestBase {

    @Override
    protected void mySetupTest() {
		TestWorld tw = this.getTestWorld();
        Region r = tw.nurBetretbar(getRegions()).get(0);
		r.setName(getName()+" R");
        getRegions().remove(r);

        {
            Partei empfaenger = tw.createPartei(Mensch.class);
			empfaenger.setName(getName()+"-Gastfreundliche Menschen");
			Partei absender = tw.createPartei(Troll.class);
			absender.setName(getName()+"-Emigranten");
			Partei aliens = tw.createPartei(Echse.class);
			aliens.setName(getName()+"-Aliens");

			Unit u = this.createUnit(empfaenger, r);
            u.setName(this.getName()+" 01");
			u.setPersonen(999);
			u.setItem(Silber.class, 11000);

			Unit fremdTroll = createUnit(empfaenger, r, Troll.class.getSimpleName());
			fremdTroll.setName(getName() + " 02");
			fremdTroll.setPersonen(1);
			fremdTroll.setItem(Silber.class, 0);

			Unit v = createUnit(absender, r);
            v.setName(this.getName()+" 03");
			v.setPersonen(2);
			v.Befehle.add("GIB " + fremdTroll.getNummerBase36() + " 1 PERSONEN");
			v.setSortierung(1); // damit uns Unit.sortierGlueck nicht durcheinander würfelt

			Unit w = createUnit(absender, r);
            w.setName(this.getName()+" 04");
			w.setPersonen(10);
			w.Befehle.add("GIB " + fremdTroll.getNummerBase36() + " 12 PERSONEN");
			w.setSortierung(2); // damit uns Unit.sortierGlueck nicht durcheinander würfelt

			Unit x = createUnit(absender, r);
            x.setName(this.getName()+" 05");
			x.setPersonen(100);
			x.Befehle.add("GIB " + fremdTroll.getNummerBase36() + " 100 PERSONEN");
			x.setSortierung(3); // damit uns Unit.sortierGlueck nicht durcheinander würfelt

			Unit y = createUnit(aliens, r);
			y.setName(getName() + " 06");
			y.setPersonen(2);
			y.Befehle.add("GIB " + fremdTroll.getNummerBase36() + " 1 PERSON");
			y.Befehle.add("GIB " + u.getNummerBase36() + " 2 PERSON");
			y.setSortierung(6); // damit uns Unit.sortierGlueck nicht durcheinander würfelt

			// wir nehmen was:
			u.Befehle.add("KONTAKTIERE " + y.getNummerBase36());

			fremdTroll.Befehle.add("KONTAKTIERE " + v.getNummerBase36());
			fremdTroll.Befehle.add("KONTAKTIERE " + w.getNummerBase36());
			fremdTroll.Befehle.add("KONTAKTIERE " + x.getNummerBase36());
			fremdTroll.Befehle.add("KONTAKTIERE " + y.getNummerBase36());



			// Magier-Thema:
			u = this.createUnit(empfaenger, r);
            u.setName(this.getName()+" 11");
			u.setPersonen(3);
			u.setSkill(Magie.class, u.getPersonen() * 300);

			Partei magier = tw.createPartei(Mensch.class);
			magier.setName(getName()+"-Fremde Magier");
			Unit m = createUnit(magier, r);
			m.setName(getName()+ " 12");
			m.setPersonen(3);
			m.setSkill(Magie.class, u.getPersonen() * 300);
			m.Befehle.add("GIB " + u.getNummerBase36() + " 3 PERSONEN");

			u.Befehle.add("KONTAKTIERE " + m.getNummerBase36());

			Sortieren.Normalisieren(r);

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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "05", "06", "11", "12"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");
			String uRef = tokens[1] + "[" + u.getNummerBase36() + "] von [" + Codierung.toBase36(u.getOwner()) + "] - ";

            // unit 03
            if (tokens[1].equals("03")) {
				if (u.getPersonen() != 1) retval = fail(uRef + "Unerwartete Personenzahl: " + u.getPersonen() + ".");

                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("gibt 1 person")) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung zur Übergabe fehlt.");
            }

            // unit 04
            if (tokens[1].equals("04")) retval = fail(uRef + "Die Einheit sollte eigentlich komplett übergeben worden sein.");

            // unit 06
            if (tokens[1].equals("06")) {
				if (u.getPersonen() != 2) retval = fail(uRef + "Unerwartete Personenzahl: " + u.getPersonen() + ".");

                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("kann keine") && text.contains("echse aufnehmen")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Meldung über Fehlschlag fehlt.");
            }

        } // next unit

        return retval;
    }

}
