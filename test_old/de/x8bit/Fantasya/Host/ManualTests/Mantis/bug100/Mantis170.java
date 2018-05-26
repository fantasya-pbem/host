package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Spell;
import de.x8bit.Fantasya.Atlantis.Spells.HainDerTausendEichen;
import de.x8bit.Fantasya.Atlantis.Spells.KleinesErdbeben;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import java.util.Collection;
import java.util.List;

public class Mantis170 extends TestBase {

	@Override
	protected void mySetupTest() {
		Partei partei = this.getTestWorld().createPartei(Mensch.class);
		Region region = (this.getTestWorld().nurBetretbar(this.getTestWorld().getAlleRegionen())).get(0);
		getRegions().remove(region);
		
		Unit mage1 = this.createUnit(partei, region);
		mage1.setPersonen(1);
		mage1.setName(this.getName() + " 01");
		mage1.setSkill(Magie.class, 30);
		mage1.setAura(100);
		mage1.Befehle.add("LERNE Magie");
		mage1.setItem(Silber.class, 2000);
		
		Unit mage2 = this.createUnit(partei, region);
		mage2.setName(this.getName() + " 02");
		mage2.setPersonen(1);
		mage2.setSkill(Magie.class, 1860);
		mage2.setAura(100);
		mage2.setSpell(new HainDerTausendEichen());
		mage2.setSpell(new KleinesErdbeben());
		mage2.setItem(Silber.class, 0);
		mage2.Befehle.add("GIB " + mage1.getNummerBase36() + " ZAUBERBUCH");
		mage2.Befehle.add("ZEIGE ZAUBERBUCH");
		mage2.Befehle.add("LERNE Magie");
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
				boolean hainOkay = false;
				boolean klErdbebenOkay = true;
				for (Spell sp : u.getSpells()) {
					if (sp instanceof HainDerTausendEichen) hainOkay = true;
					if (sp instanceof KleinesErdbeben) klErdbebenOkay = false;
				}
				if (!hainOkay) {
					retval = fail(tokens[1] + ": Hat den Zauber Hain der ... nicht bekommen.");
				}
				if (!klErdbebenOkay) {
					retval = fail(tokens[1] + ": Hat fälschlich den Zauber Kleines Erdbeben bekommen.");
				}

                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("kleines erdbeben") && text.contains("nichts anfangen")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Keine Meldung über unverständlichen Zauber.");
            }

            if (tokens[1].equals("02")) {
                if (u.getSkill(Magie.class).getLerntage() > 1860) {
                    fail(tokens[1] + ": Magie-Lerntage sind nicht wie erwartet - ohne Silber gelernt?");
                }

                messages = Message.Retrieve(null, u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("wenig") && text.contains("silber")) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Keine Meldung über fehlendes Silber zum Magie-Lernen.");
            }

        } // next unit

        return retval;
        
    }

}
