package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class TestBotschaften extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();

		Partei pa = tw.createPartei(Mensch.class);
		pa.setName(getName()+"-Partei I");

		Partei pb = tw.createPartei(Mensch.class);
		pb.setName(getName()+"-Partei II");

		Partei pc = tw.createPartei(Mensch.class);
		pc.setName(getName()+"-Partei III");

        Region r = tw.nurBetretbar(getRegions()).get(0);
		r.setName(getName()+"-Region");
        getRegions().remove(r);

        // damit sollten die Botschaften im Spieler-Text dann @(0,0,1) ausgewiesen sein:
        pb.setUrsprung(r.getCoords()); 

        {
            // Partei A:
            Unit ua01 = this.createUnit(pa, r);
            ua01.setName(this.getName()+" 01");

            Unit ua02 = this.createUnit(pa, r);
            ua02.setName(this.getName()+" 02");



            // Partei B:
            Unit ub11 = this.createUnit(pb, r);
            ub11.setName(this.getName()+" 11 Sichtbar");

            Unit ub12 = this.createUnit(pb, r);
            ub12.setName(getName() + " 12 Einheiten-getarnt");
            ub12.setSkill(Tarnung.class, Skill.LerntageFuerTW(5));
            ub12.setSichtbarkeit(1); // Einheiten-getarnt

            Unit ub13 = this.createUnit(pb, r);
            ub13.setName(getName() + " 13 Partei-getarnt");
            ub13.setTarnPartei(0);

            Unit ub14 = this.createUnit(pb, r);
            ub14.setName(this.getName()+" 14 Sichtbar");

            // Partei C:
            Unit uc = this.createUnit(pc, r);
            uc.setName(this.getName()+" 21");


            // die eigentlichen Botschaften:
            ua01.Befehle.add("BOTSCHAFT " + ub11.getNummerBase36() + " Hallo Nr. 11!");
            ua01.Befehle.add("@BOTSCHAFT " + ub11.getNummerBase36() + " \"Zweite Botschaft; an 11\"");
            ua01.Befehle.add("BOTSCHAFT EINHEIT " + ub11.getNummerBase36() + " Dritte Botschaft an Nr. 11.");

            ua01.Befehle.add("BOTSCHAFT " + ub12.getNummerBase36() + " Hallo Versteckte!");


            // Partei-Botschaft - wird von ub14 verifiziert.
            ua02.Befehle.add("BOTSCHAFT PARTEI " + pb.getNummerBase36() + " Durchsage an Partei " + pb.getNummerBase36() + ": Blabla!");


            // An Alle, An Alle! (wird ebenfalls von ub14 geprüft)
            ua02.Befehle.add("BOTSCHAFT REGION \"Wenn ich euch erwische!!!\"");



            new Info(this.getName() + " Setup in " + r + ".", r);
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "11", "12", "13", "14", "21"});
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
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;

                // Getarnte Einheit sollte per Botschaft nicht direkt erreichbar sein:
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("einheit") && text.contains("nicht gefunden")) found = true;
                }
                if (!found) retval = fail(uRef + "Botschaft an getarnte Einheit übermittelt / Fehlermeldung fehlt.");

                // @BOTSCHAFT ... Befehl suchen:
                found = false;
                for (Einzelbefehl eb : u.BefehleExperimental) {
                    String befehl = eb.getBefehlCanonical().toLowerCase();
                    if (befehl.contains("@botschaft") && befehl.contains("an 11")) {
                        found = true;
                        break;
                    } 
                }
                if (!found) retval = fail(uRef + "@BOTSCHAFT-Befehl nicht richtig in die neue Vorlage übernommen.");
            }

            // unit 11
            if (tokens[1].equals("11")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("hallo") && text.contains("11")) found = true;
                }
                if (!found) retval = fail(uRef + "Erste Botschaft von 01 nicht gefunden.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("zweite") && text.contains("11")) found = true;
                }
                if (!found) retval = fail(uRef + "Zweite Botschaft von 01 (mit ; im Text) nicht gefunden.");

                found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("dritte") && text.contains("11")) found = true;
                }
                if (!found) retval = fail(uRef + "Dritte Botschaft von 01 (BOTSCHAFT EINHEIT ...) nicht gefunden.");
            }

            // unit 14
            if (tokens[1].equals("14")) {
                messages = Message.Retrieve(null, u.getCoords(), null);
                // new Debug(messages.size() + " PARTEI-BOTSCHAFTEN");
                boolean found = false;
                for (Message msg : messages) {
                    // new Debug("EINHEIT: " + msg.getUnit());
                    if (msg.getUnit() != null) continue; // nur, wenn die Meldung keiner bestimmten Einheit zugewiesen ist:

                    String text = msg.getText().toLowerCase();
                    if (text.contains("angenagelt:") && text.contains("blabla!")) found = true;
                }
                if (!found) retval = fail(uRef + "Partei-Botschaft nicht gefunden.");

                found = false;
                for (Message msg : messages) {
                    // new Debug("EINHEIT: " + msg.getUnit());
                    if (msg.getUnit() != null) continue; // nur, wenn die Meldung keiner bestimmten Einheit zugewiesen ist:

                    String text = msg.getText().toLowerCase();
                    if (text.contains("wenn ich euch erwische!")) found = true;
                }
                if (!found) retval = fail(uRef + "Regions-Botschaft nicht gefunden.");
            }

        } // next unit

        return retval;
    }

}
