package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Armbrust;
import de.x8bit.Fantasya.Atlantis.Items.Bogen;
import de.x8bit.Fantasya.Atlantis.Items.Katapult;
import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Armbrustschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Katapultbedienung;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class KatapultKampf extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei a = this.getTestWorld().createPartei(Mensch.class);
        a.setName(this.getName() + "-A");
        Partei b = this.getTestWorld().createPartei(Mensch.class);
        b.setName(this.getName() + "-B");
        Partei c = this.getTestWorld().createPartei(Mensch.class);
        c.setName(this.getName() + "-Beobachter");

        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit ua = this.createUnit(a, r);
            ua.setName(this.getName()+" 01");
            ua.setPersonen(1);
            ua.setSkill(Katapultbedienung.class, 450 * ua.getPersonen());
            ua.setItem(Katapult.class, ua.getPersonen());
            ua.setKampfposition(Kampfposition.Hinten);

            Unit u = this.createUnit(a, r);
            u.setName(this.getName()+" 11");
            u.setPersonen(10);
            u.setSkill(Speerkampf.class, 1650 * u.getPersonen());
            u.setItem(Speer.class, u.getPersonen());
            u.setItem(Kettenhemd.class, u.getPersonen());
            u.setKampfposition(Kampfposition.Vorne);


            Unit ub = this.createUnit(b, r);
            ub.setName(this.getName()+" 02");
            ub.setPersonen(10);
            ub.setSkill(Speerkampf.class, 1650 * u.getPersonen());
            ub.setItem(Speer.class, u.getPersonen());
            ub.setItem(Kettenhemd.class, u.getPersonen());
            ub.setKampfposition(Kampfposition.Vorne);

            u = this.createUnit(b, r);
            u.setName(this.getName()+" 12");
            u.setPersonen(2);
            u.setSkill(de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen.class, 1650 * u.getPersonen());
            u.setItem(Bogen.class, u.getPersonen());
            u.setSkill(Armbrustschiessen.class, 1650 * u.getPersonen());
            u.setItem(Armbrust.class, u.getPersonen());
            u.setKampfposition(Kampfposition.Hinten);
            
            b.setUrsprung(r.getCoords());

            ua.Befehle.add("ATTACKIERE " + ub.getNummerBase36());


            Unit uc = this.createUnit(c, r);
            uc.setName(this.getName()+" 03");
            uc.setPersonen(1);
            c.setUrsprung(r.getCoords());

            
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

        String missing = this.verifyExpectedUnits(Unit.CACHE, new String[] {"01", "11", "03"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            if (tokens[1].equals("02")) {
                retval = this.fail("Der Feind wurde nicht aufgerieben - Einheit 02 ist noch da.");
            }
            if (tokens[1].equals("12")) {
                retval = this.fail("Der Feind wurde nicht aufgerieben - Einheit 12 ist noch da.");
            }
        } // next unit

        return retval;
    }

}
