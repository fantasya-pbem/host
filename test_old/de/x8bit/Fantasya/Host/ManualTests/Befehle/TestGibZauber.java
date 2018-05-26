package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Spells.HainDerTausendEichen;
import de.x8bit.Fantasya.Atlantis.Spells.MeisterDerPlatten;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 *
 * @author hapebe
 */
public class TestGibZauber extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei p = this.getTestWorld().getSpieler1();
		Partei p2 = this.getTestWorld().createPartei(Mensch.class);

		{
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
			getRegions().remove(r);

			Unit mage1 = this.createUnit(p, r);
			Unit mage2 = this.createUnit(p, r);

			mage1.setBeschreibung("Magier der gleichen Partei, alles okay - erwartet: Klappt.");

			mage1.setSkill(Magie.class, 300);
			mage2.setSkill(Magie.class, 300);
			
			mage1.setSpell(new HainDerTausendEichen());

			mage1.Befehle.add("GIB " + mage2.getNummerBase36() + " ZAUBERBUCH \"Hain der 1000 Eichen\"");
			mage1.Befehle.add("LERNE Wahrnehmung");

			mage2.Befehle.add("LERNE Wahrnehmung");

			mage2.setName(this.getName() + " 01");

		}

		{
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0); getRegions().remove(r);

			Unit mage1 = this.createUnit(p, r);
			Unit mage2 = this.createUnit(p, r);

			mage1.setBeschreibung("Magier der gleichen Partei, Empfänger zu untalentiert - erwartet: Fehlermeldung.");

			mage1.setSkill(Magie.class, 300);
			mage2.setSkill(Magie.class, 90);

			mage1.setSpell(new MeisterDerPlatten());

			mage1.Befehle.add("GIB " + mage2.getNummerBase36() + " ZAUBERBUCH \"Meister der Platten\"");
			mage1.Befehle.add("LERNE Wahrnehmung");

			mage2.Befehle.add("LERNE Wahrnehmung");
			mage2.setName(this.getName() + " 02");
		}

		{
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0); getRegions().remove(r);
			Unit mage1 = this.createUnit(p, r);
			r = this.getTestWorld().nurBetretbar(getRegions()).get(0); getRegions().remove(r);
			Unit mage2 = this.createUnit(p, r);

			mage1.setBeschreibung("Magier in verschiedenen Regionen - erwartet: Fehlermeldung");

			mage1.setSkill(Magie.class, 300);
			mage2.setSkill(Magie.class, 300);

			mage1.setSpell(new HainDerTausendEichen());

			mage1.Befehle.add("GIB " + mage2.getNummerBase36() + " ZAUBERBUCH \"Hain der 1000 Eichen\"");
			mage1.Befehle.add("LERNE Wahrnehmung");

			mage2.Befehle.add("LERNE Wahrnehmung");

			mage1.setName(this.getName() + " 03");
			mage2.setName(this.getName() + " 04");
		}

		{
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0); getRegions().remove(r);

			Unit mage1 = this.createUnit(p, r);
			Unit mage2 = this.createUnit(p, r);

			mage1.setBeschreibung("Inexistenter Zauber - erwartet: Fehler.");

			mage1.setSkill(Magie.class, 300);
			mage2.setSkill(Magie.class, 300);

			mage1.setSpell(new HainDerTausendEichen());

			mage1.Befehle.add("GIB " + mage2.getNummerBase36() + " ZAUBERBUCH \"Hain der 3000 Eichen\"");
			mage1.Befehle.add("LERNE Wahrnehmung");

			mage2.Befehle.add("LERNE Wahrnehmung");
			mage1.setName(this.getName() + " 05");
		}

		{
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0); getRegions().remove(r);

			Unit mage1 = this.createUnit(p, r);
			Unit mage2 = this.createUnit(p, r);

			mage1.setBeschreibung("Broken syntax - erwartet: Fehler.");

			mage1.setSkill(Magie.class, 300);
			mage2.setSkill(Magie.class, 300);

			mage1.setSpell(new HainDerTausendEichen());

			mage1.Befehle.add("GIB " + mage2.getNummerBase36() + " ZAUBERBUCH \"ase eer rrerer");
			mage1.Befehle.add("LERNE Wahrnehmung");

			mage2.Befehle.add("LERNE Wahrnehmung");
		}

		{
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0); getRegions().remove(r);

			Unit mage1 = this.createUnit(p, r);
			Unit mage2 = this.createUnit(p2, r);

			mage1.setBeschreibung("Magier verschiedener Parteien, alles okay - erwartet: Klappt.");

			mage1.setSkill(Magie.class, 300);
			mage2.setSkill(Magie.class, 300);

			mage1.setSpell(new HainDerTausendEichen());

			mage1.Befehle.add("GIB " + mage2.getNummerBase36() + " ZAUBERBUCH \"Hain der 1000 Eichen\"");
			mage1.Befehle.add("LERNE Wahrnehmung");

			mage2.Befehle.add("KONTAKTIERE " + mage1.getNummerBase36());
			mage2.Befehle.add("LERNE Wahrnehmung");

			mage2.setName(this.getName() + " 06");
		}

		{
			Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0); getRegions().remove(r);

			Unit mage1 = this.createUnit(p, r);
			Unit mage2 = this.createUnit(p2, r);

			mage1.setBeschreibung("Magier verschiedener Parteien, kein Kontakt - erwartet: Fehler.");

			mage1.setSkill(Magie.class, 300);
			mage2.setSkill(Magie.class, 300);

			mage1.setSpell(new HainDerTausendEichen());

			mage1.Befehle.add("GIB " + mage2.getNummerBase36() + " ZAUBERBUCH \"Hain der 1000 Eichen\"");
			mage1.Befehle.add("LERNE Wahrnehmung");

			mage2.Befehle.add("LERNE Wahrnehmung");

			mage1.setName(this.getName() + " 07");
			mage2.setName(this.getName() + " 08");

		}

		new Info("TestGibZauber Setup.", p);
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04", "05", "06", "07", "08"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");
			String uRef = tokens[1] + "[" + u.getNummerBase36() + "] von [" + Codierung.toBase36(u.getOwner()) + "] - ";

            // unit 01
            if (tokens[1].equals("01")) {
				boolean found = false;
				for (Spell sp : u.getSpells()) {
					if (sp.getClass() == HainDerTausendEichen.class) found = true;
				}
				if (!found) {
					retval = fail(uRef + "Übergabe des Zauberspruchs hat nicht geklappt.");
				}
            }

            // unit 02
            if (tokens[1].equals("02")) {
				if (u.getSpells().contains(new MeisterDerPlatten())) {
					retval = fail(uRef + "Zauber-Übergabe hat wider Erwarten geklappt.");
				}
            }

            // unit 03
            if (tokens[1].equals("03")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("kann einheit") && text.contains("nicht finden")) found = true;
                }
                if (!found) retval = fail(uRef + "GIB ZAUBER - keine Fehlermeldung über falschen Empfänger.");
            }

			// unit 04
            if (tokens[1].equals("04")) {
				boolean found = false;
				for (Spell sp : u.getSpells()) {
					if (sp.getClass() == HainDerTausendEichen.class) found = true;
				}
				if (found) {
					retval = fail(uRef + "Übergabe des Zauberspruchs hat unerwartet geklappt.");
				}
            }

            // unit 05
            if (tokens[1].equals("05")) {
				// mit EVA wird dieser Befehl gar nicht erst zugelassen.
				if (Main.getBFlag("EVA")) continue;
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("kein gültiger befehl") && text.contains("3000")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": GIB ZAUBER - keine Fehlermeldung über inexistenten Zauber.");
            }

            // unit 06
            if (tokens[1].equals("06")) {
				boolean found = false;
				for (Spell sp : u.getSpells()) {
					if (sp.getClass() == HainDerTausendEichen.class) found = true;
				}
				if (!found) {
					retval = fail(uRef + "Übergabe des Zauberspruchs hat nicht geklappt.");
				}
            }

            // unit 07
            if (tokens[1].equals("07")) {
                messages = Message.Retrieve(p, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("hat keinen kontakt") && text.contains("08")) found = true;
                }
                if (!found) retval = fail(uRef + "GIB ZAUBER - keine Fehlermeldung über fehlenden Kontakt (Geber).");
            }

            // unit 08
            if (tokens[1].equals("08")) {
                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("hat versucht") && text.contains("07")) found = true;
                }
                if (!found) retval = fail(uRef + "GIB ZAUBER - keine Fehlermeldung über fehlenden Kontakt (Nehmer).");
            }

		} // next unit

        return retval;
    }

}
