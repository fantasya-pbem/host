package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Bogen;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 * testet Fernangriffe aus der zweiten Reihe und das Aufrücken in die erste
 * Reihe, wenn zu wenige Frontkämpfer bereitstehen.
 * @author hb
 */
public class BogenschiessKampf extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei a = this.getTestWorld().createPartei(Mensch.class);
        a.setName(this.getName() + "-A");
        Partei b = this.getTestWorld().createPartei(Mensch.class);
        b.setName(this.getName() + "-B");
        

        {
            Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
            getRegions().remove(r);
            
            Unit ua = this.createUnit(a, r);
            ua.setName(this.getName()+" 01");
            ua.setPersonen(15);
            ua.setSkill(Hiebwaffen.class, 450 * ua.getPersonen());
            ua.setItem(Schwert.class, ua.getPersonen());

            Unit ub = this.createUnit(b, r);
            ub.setName(this.getName()+" 02");
            ub.setPersonen(10);
            ub.setSkill(Hiebwaffen.class, 450 * ub.getPersonen());
            ub.setItem(Schwert.class, ub.getPersonen());

            ua.Befehle.add("ATTACKIERE " + ub.getNummerBase36());

            Unit u = this.createUnit(b, r);
            u.setName(this.getName()+" 03");
            u.setPersonen(20);
            u.setSkill(de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen.class, 450 * u.getPersonen());
            u.setItem(Bogen.class, u.getPersonen());
            u.Befehle.add("KAEMPFE HINTEN");



            new Info(this.getName() + " Setup in " + r + ".", ua, ua.getCoords());
        }

        {
            Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
            getRegions().remove(r);

            Unit ua = this.createUnit(a, r);
            ua.setName(this.getName()+" 11");
            ua.setPersonen(30);
            ua.setSkill(Hiebwaffen.class, 630 * ua.getPersonen());
            ua.setItem(Schwert.class, ua.getPersonen());

            Unit ub = this.createUnit(b, r);
            ub.setName(this.getName()+" 12");
            ub.setPersonen(10);
            ub.setSkill(Hiebwaffen.class, 450 * ub.getPersonen());
            ub.setItem(Schwert.class, ub.getPersonen());

            ua.Befehle.add("ATTACKIERE " + ub.getNummerBase36());

            Unit u = this.createUnit(b, r);
            u.setName(this.getName()+" 13");
            u.setPersonen(10);
            u.setSkill(de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen.class, 450 * u.getPersonen());
            u.setItem(Bogen.class, u.getPersonen());
            u.Befehle.add("KAEMPFE HINTEN");



            new Info(this.getName() + " Setup in " + r + ".", ua, ua.getCoords());
        }

    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
//        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(Unit.CACHE, new String[] {"02", "03", "11"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                retval = this.fail("Der Feind wurde nicht beschossen und besiegt - Einheit 01 ist noch da.");
            }
            // unit 13
            if (tokens[1].equals("13")) {
                retval = this.fail("Bogenschützen wurde nicht an der Front aufgerieben - Einheit 13 ist noch da.");
            }
        } // next unit

        return retval;
    }

}
