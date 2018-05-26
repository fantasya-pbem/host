package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.Sortieren;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class SortierTest extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit u2 = this.createUnit(p, r);
            u2.setName(this.getName()+" 02");
			u2.Befehle.add("LERNE Wahrnehmung");
            String id2 = u2.getNummerBase36();
			u2.setSortierung(1); // damit uns das Unit.sortierGlueck keine Streiche spielt

            Unit u3 = this.createUnit(p, r);
            u3.setName(this.getName()+" 03");
			u3.Befehle.add("LERNE Wahrnehmung");
			u3.setSortierung(2); // damit uns das Unit.sortierGlueck keine Streiche spielt
            String id3 = u3.getNummerBase36();

            Unit u5 = this.createUnit(p, r);
            u5.setName(this.getName()+" 05");
			u5.Befehle.add("LERNE Wahrnehmung");
			u5.setSortierung(3); // damit uns das Unit.sortierGlueck keine Streiche spielt
            String id5 = u5.getNummerBase36();

            Unit u1 = this.createUnit(p, r);
            u1.setName(this.getName()+" 01");
            u1.getItem(Pferd.class).setAnzahl(1);
			u1.Befehle.add("@SORTIERE VORNE // ja, ja");
			u1.Befehle.add("LERNE Wahrnehmung");
			u1.setSortierung(4); // damit uns das Unit.sortierGlueck keine Streiche spielt
//            String id1 = u1.getNummerBase36();

            Unit u4 = this.createUnit(p, r);
            u4.setName(this.getName()+" 04");
            u4.setPersonen(2);
			u4.Befehle.add("SORTIERE VOR " + id5);
            u4.Befehle.add("GIB TEMP t1 1 PERSON");
			u4.Befehle.add("LERNE Wahrnehmung");
            u4.Befehle.add("MACHE TEMP t1");
            u4.Befehle.add("BENENNE EINHEIT \"" + this.getName()+ " 06\"");
            u4.Befehle.add("LERNE Wahrnehmung");
            u4.Befehle.add("ENDE");
			u4.setSortierung(5); // damit uns das Unit.sortierGlueck keine Streiche spielt
            String id4 = u4.getNummerBase36();

			Sortieren.Normalisieren(p, r);

            u1.Befehle.add("GIB " + id2 + " 1 Pferd");
            u2.Befehle.add("GIB " + id3 + " 1 Pferd");
            u3.Befehle.add("GIB " + id4 + " 1 Pferd");
            u4.Befehle.add("GIB " + id5 + " 1 Pferd");

            new Info(this.getName() + " Setup in " + r + ".", u1);
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
//        Partei p = this.getTestWorld().getSpieler1();
//        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04", "05", "06"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            int nr = Integer.parseInt(tokens[1]);
            if (u.getSortierung() != nr*10) {
                retval = fail(tokens[1] + ": Sortierung stimmt nicht: " + u + ", Sortierung: " + u.getSortierung());
            }

            // unit 01
            if (tokens[1].equals("01")) {
                if (u.getItem(Pferd.class).getAnzahl() > 0) {
                    retval = fail(tokens[1] + ": Erste Übergabe hat nicht funktioniert.");
                }
            }

            // unit 05
            if (tokens[1].equals("05")) {
                if (u.getItem(Pferd.class).getAnzahl() != 1) {
                    retval = fail(tokens[1] + ": Befehle sind möglicherweise nicht in der richtigen Reihenfolge verarbeitet worden.");
                }
            }

        } // next unit

        return retval;
    }

}
