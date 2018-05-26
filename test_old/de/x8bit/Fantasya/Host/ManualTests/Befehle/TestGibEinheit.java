package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.Collection;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Units.Aquaner;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.Sortieren;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.util.Codierung;

/**
 *
 * @author hb
 */
public class TestGibEinheit extends TestBase {

    @Override
    protected void mySetupTest() {
        { // Szenario 1: Menschen sollen Aquaner bekommen:
            Partei aq = getTestWorld().createPartei(Aquaner.class);
            aq.setName(getName() + "-Aquaner");

            Partei me = getTestWorld().createPartei(Mensch.class);
            me.setName(getName() + "-Menschen");

            Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
            r.setName(getName()+" R");
            getRegions().remove(r);

            Unit mig = this.createUnit(aq, r);
            mig.setName(this.getName()+" 01");
			mig.setPersonen(1);
			mig.setItem(Silber.class, 0);
			mig.setSortierung(1); // Unit.sortierGlueck überdecken

            Unit mig2 = this.createUnit(aq, r);
            mig2.setName(this.getName()+" 03");
			mig2.setPersonen(1);
			mig2.setSkill(Magie.class, mig.getPersonen() * 450);
			mig2.setItem(Silber.class, 0);
			mig2.setSortierung(2); // Unit.sortierGlueck überdecken

            Unit mig3 = this.createUnit(aq, r);
            mig3.setName(this.getName()+" 04");
			mig3.setPersonen(1);
			mig3.setItem(Silber.class, 0);
			mig3.setSortierung(3); // Unit.sortierGlueck überdecken

            Unit mig4 = this.createUnit(aq, r);
            mig4.setName(this.getName()+" 05");
			mig4.setPersonen(1);
			mig4.setItem(Silber.class, 0);
			mig4.setSortierung(4); // Unit.sortierGlueck überdecken

			Sortieren.Normalisieren(aq, r); // Unit.sortierGlueck überdecken
			

            Unit u = this.createUnit(me, r);
			u.setPersonen(65); // so sind 2 Migranten möglich
            u.setName(this.getName()+" 02 " + mig.getNummerBase36() + " " + mig2.getNummerBase36());
			u.Befehle.add("KONTAKTIERE " + mig.getNummerBase36());
			u.Befehle.add("KONTAKTIERE " + mig2.getNummerBase36());
			u.Befehle.add("KONTAKTIERE " + mig3.getNummerBase36());
			u.Befehle.add("KONTAKTIERE " + mig4.getNummerBase36());

			Unit alteMagier = createUnit(me, r);
			alteMagier.setName(getName()+" 12 Alte Magier");
			alteMagier.setPersonen(3);
			alteMagier.setSkill(Magie.class, alteMagier.getPersonen() * 90);

			mig.Befehle.add("GIB " + u.getNummerBase36() + " EINHEIT");
			mig2.Befehle.add("GIB " + u.getNummerBase36() + " EINHEIT");
			mig3.Befehle.add("GIB " + u.getNummerBase36() + " EINHEIT");
			mig4.Befehle.add("GIB " + u.getNummerBase36() + " EINHEIT");

            u = this.createUnit(aq, r);
            u.setName(this.getName()+" 11 Beobachter des Ursprungsvolks");

            new Info(getName() + " Setup in " + r + ".", u);
        }
        
        { // Szenario 1: Menschen sollen Aquaner bekommen:
            Partei e1 = getTestWorld().createPartei(Elf.class);
            e1.setName(getName() + "-Alte-Elfen");

            Partei e2 = getTestWorld().createPartei(Elf.class);
            e2.setName(getName() + "-Neue-Elfen");

            Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
            r.setName(getName()+" R-Elfen");
            getRegions().remove(r);

            Unit mig = this.createUnit(e1, r);
            mig.setName(this.getName()+" 21");
			mig.setPersonen(1);
			mig.setItem(Silber.class, 0);
			mig.setSortierung(1); // Unit.sortierGlueck überdecken

			Sortieren.Normalisieren(e1, r); // Unit.sortierGlueck überdecken
			

            Unit u = this.createUnit(e2, r);
			u.setPersonen(65); // so sind 2 Migranten möglich
            u.setName(this.getName()+" 22 " + mig.getNummerBase36());
			u.Befehle.add("KONTAKTIERE " + mig.getNummerBase36());

			mig.Befehle.add("GIB " + u.getNummerBase36() + " EINHEIT");

            u = this.createUnit(e1, r);
            u.setName(this.getName()+" 31 Beobachter der alten Elfen");

            new Info(getName() + " Setup in " + r + ".", u);
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "05", "11", "12", "21", "22", "31"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");
			String uRef = tokens[1] + "[" + u.getNummerBase36() + "] von [" + Codierung.toBase36(u.getOwner()) + "] - ";

            messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
            boolean found = false;

            // unit 02 - Empfänger des Migranten
            if (tokens[1].equals("02")) {
				Unit mig = Unit.Load(Codierung.fromBase36(tokens[2]));
				if (mig.getOwner() != u.getOwner()) {
					String migP = Codierung.toBase36(mig.getOwner());
					String neuP = Codierung.toBase36(u.getOwner());
					retval = fail(uRef + "Der Migrant ist nicht angekommen (Migrant: " + migP + ", Empfänger: " + neuP + ").");
				}

				Unit mig2 = Unit.Load(Codierung.fromBase36(tokens[3]));
				if (mig2.getOwner() == u.getOwner()) {
					String neuP = Codierung.toBase36(u.getOwner());
					retval = fail(uRef + "Der Migrant " + mig2 + " ist wider Erwarten aufgenommen worden (Migranten-Limit? - Empfänger-Partei: " + neuP + ").");
				}

				/*
				messages = Message.Retrieve(Partei.Load(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getMessage().toLowerCase();
                    if (text.contains("") && text.contains("")) found = true;
                }
                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben.");
				*/
            }

            // unit 03 - unerwünschter Migrant (Magier)
            if (tokens[1].equals("03")) {
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("magier keine heimat")) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung über Magier-Migrations-Fehlschlag - fehlt.");
			}

            // unit 05 - unerwünschter Migrant (überzählig)
            if (tokens[1].equals("05")) {
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("kann derzeit keine") && text.contains("fremden aufnehmen")) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung über Migrations-Fehlschlag (zu viele...) - fehlt.");
			}
            
            // unit 22 - elfischer Empfänger des elfischen Migranten
            if (tokens[1].equals("22")) {
				Unit mig = Unit.Load(Codierung.fromBase36(tokens[2]));
				if (mig.getOwner() != u.getOwner()) {
					String migP = Codierung.toBase36(mig.getOwner());
					String neuP = Codierung.toBase36(u.getOwner());
					retval = fail(uRef + "Der Migrant ist nicht angekommen (Migrant: " + migP + ", Empfänger: " + neuP + ").");
				}

                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("hat die einheit") && text.contains("erhalten")) found = true;
                }
                if (!found) retval = fail(uRef + "Meldung über aufgenommenen Migranten fehlt.");
            }
            

        } // next unit

        return retval;
    }

}
