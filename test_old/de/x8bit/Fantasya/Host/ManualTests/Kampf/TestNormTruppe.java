package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Items.Bogen;
import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Holzschild;
import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Host.ManualTests.*;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Taktik;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;

/**
 *
 * @author hb
 */
public class TestNormTruppe extends TestBase {

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
            ua.setPersonen(100);
            ua.setSkill(Hiebwaffen.class, 840 * ua.getPersonen());
            ua.setItem(Schwert.class, ua.getPersonen());
            ua.setItem(Holzschild.class, ua.getPersonen() / 2);
            ua.setItem(Kettenhemd.class, ua.getPersonen() / 2);

            Unit u = this.createUnit(a, r);
            u.setName(this.getName()+" 11");
            u.setPersonen(50);
            u.setSkill(de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen.class, 1650 * ua.getPersonen());
            u.setItem(Bogen.class, u.getPersonen());
            u.Befehle.add("KAEMPFE HINTEN");

            u = this.createUnit(a, r);
            u.setName(this.getName()+" 21");
            u.setPersonen(100);


            Unit ub = this.createUnit(b, r);
            ub.setName(this.getName()+" 02");
            ub.setPersonen(120);
            ub.setSkill(Hiebwaffen.class, 1650 * ub.getPersonen());
            ub.setItem(Schwert.class, ub.getPersonen());

            u = this.createUnit(b, r);
            u.setName(this.getName() + " 12");
            u.setPersonen(1);
            u.setSkill(Taktik.class, 450);
            u.setKampfposition(Kampfposition.Hinten);

            u = this.createUnit(b, r);
            u.setName(this.getName()+" 22");
            u.setPersonen(100);
            u.Befehle.add("KAEMPFE NICHT");

            u = this.createUnit(b, r);
            u.setName(this.getName()+" 32");
            u.setPersonen(100);
            u.setItem(Plattenpanzer.class, u.getPersonen());
            u.setItem(Eisenschild.class, u.getPersonen());
            u.Befehle.add("KAEMPFE NICHT");


            ua.Befehle.add("ATTACKIERE " + ub.getNummerBase36());

            new Info(this.getName() + " Setup in " + r + ".", ua);
        }

//        if (false) {
//            Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
//            getRegions().remove(r);
//
//            Unit ua = this.createUnit(a, r);
//            ua.setName(this.getName()+" 101");
//            ua.setPersonen(10000);
//            ua.setSkill(Hiebwaffen.class, 1650 * ua.getPersonen());
//            ua.setItem(Schwert.class, ua.getPersonen());
//
//            Unit ub = this.createUnit(b, r);
//            ub.setName(this.getName()+" 102");
//            ub.setPersonen(10000);
//            ub.setSkill(Hiebwaffen.class, 1650 * ub.getPersonen());
//            ub.setItem(Schwert.class, ub.getPersonen());
//
//            ua.Befehle.add("ATTACKIERE " + ub.getNummerBase36());
//
//            new Info(this.getName() + "-XXL Setup in " + r + ".", ua);
//        }

    }

    @Override
    protected boolean verifyTest() {
        new TestMsg(this.getName() + " - keine automatische Prüfung möglich!");
        return true;
    }

}
