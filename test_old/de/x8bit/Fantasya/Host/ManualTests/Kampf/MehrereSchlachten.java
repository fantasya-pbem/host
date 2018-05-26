package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Host.ManualTests.*;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Atlantis.Units.Ork;

/**
 * Hier sollen nacheinander mehrere völlig unabhängige Schlachten ausgetragen
 * werden - d.h. A greift B an und C greift D an, etc. ...
 * @author hb
 */
public class MehrereSchlachten extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei a = this.getTestWorld().createPartei(Mensch.class);
        a.setName(this.getName() + "-A");
        Partei b = this.getTestWorld().createPartei(Mensch.class);
        b.setName(this.getName() + "-B");
        Partei c = this.getTestWorld().createPartei(Ork.class);
        c.setName(this.getName() + "-C");
        Partei d = this.getTestWorld().createPartei(Ork.class);
        d.setName(this.getName() + "-D");

        Partei e = this.getTestWorld().createPartei(Elf.class);
        e.setName(this.getName() + "-E");

        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit ua = this.createUnit(a, r);
            ua.setName(this.getName()+" 01");
            ua.setPersonen(10);
            ua.setSkill(Hiebwaffen.class, 450 * ua.getPersonen());
            ua.setItem(Schwert.class, ua.getPersonen());

            Unit u = this.createUnit(a, r);
            u.setName(this.getName()+" 11");
            u.setPersonen(10);
            u.setSkill(Hiebwaffen.class, 450 * u.getPersonen());
            u.setItem(Schwert.class, u.getPersonen());

            Unit ub = this.createUnit(b, r);
            ub.setName(this.getName()+" 02");
            ub.setPersonen(9);
            ub.setSkill(Hiebwaffen.class, 450 * ub.getPersonen());
            ub.setItem(Schwert.class, ub.getPersonen());

            u = this.createUnit(b, r);
            u.setName(this.getName()+" 12");
            u.setPersonen(9);
            u.setSkill(Hiebwaffen.class, 450 * u.getPersonen());
            u.setItem(Schwert.class, u.getPersonen());

            u = this.createUnit(b, r);
            u.setName(this.getName()+" 13");
            u.setPersonen(1);
            u.setSkill(Hiebwaffen.class, 450 * u.getPersonen());
            u.setItem(Schwert.class, u.getPersonen());

            u = this.createUnit(b, r);
            u.setName(this.getName()+" 14");
            u.setPersonen(1);
            u.setSkill(Hiebwaffen.class, 450 * u.getPersonen());
            u.setItem(Schwert.class, u.getPersonen());

            ua.Befehle.add("ATTACKIERE " + ub.getNummerBase36());
            ub.Befehle.add("ATTACKIERE " + ua.getNummerBase36());


            

            Unit uc = this.createUnit(c, r);
            uc.setName(this.getName()+" 03");
            uc.setPersonen(10);
            uc.setSkill(Speerkampf.class, 450 * uc.getPersonen());
            uc.setItem(Speer.class, uc.getPersonen());

            Unit ud = this.createUnit(d, r);
            ud.setName(this.getName()+" 04");
            ud.setPersonen(10);
            ud.setSkill(Speerkampf.class, 450 * ud.getPersonen());
            ud.setItem(Speer.class, ud.getPersonen());

            uc.Befehle.add("ATTACKIERE " + ud.getNummerBase36());
            ud.Befehle.add("ATTACKIERE " + uc.getNummerBase36());


            Unit ue = this.createUnit(e, r);
            ue.setName(this.getName()+" 99");
            ue.setPersonen(1);
            e.setUrsprung(r.getCoords());


            new Info(this.getName() + " Setup in " + r + ".", ua);
        }
    }

    @Override
    protected boolean verifyTest() {
        new TestMsg(this.getName() + " - keine automatische Prüfung möglich!");
        return true;
    }

}
