package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Ausdauer;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 * Test einen Kampf, bei dem drei Parteien einander angreifen: A attackiert B,
 * B attackiert C - und C attackiert A!
 * @author hb
 */
public class Viereck extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei a = this.getTestWorld().createPartei(Mensch.class);
        a.setName(this.getName() + "-A");
        Partei b = this.getTestWorld().createPartei(Mensch.class);
        b.setName(this.getName() + "-B");
        Partei c = this.getTestWorld().createPartei(Mensch.class);
        c.setName(this.getName() + "-C");
        Partei d = this.getTestWorld().createPartei(Mensch.class);
        d.setName(this.getName() + "-D");

        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit ua = this.createUnit(a, r);
            ua.setName(this.getName()+" 01");
            ua.setPersonen(12);
            ua.setSkill(Hiebwaffen.class, 450 * ua.getPersonen());
            ua.setItem(Schwert.class, ua.getPersonen());
            ua.setItem(Plattenpanzer.class, 5);
            ua.setItem(Eisenschild.class, 3);

            Unit ub = this.createUnit(b, r);
            ub.setName(this.getName()+" 02");
            ub.setPersonen(13);
            ub.setSkill(Hiebwaffen.class, 450 * ub.getPersonen());
            ub.setItem(Schwert.class, ub.getPersonen());

            Unit uc = this.createUnit(c, r);
            uc.setName(this.getName()+" 03");
            uc.setPersonen(10);
            uc.setSkill(Hiebwaffen.class, 1650 * uc.getPersonen());
            uc.setSkill(Ausdauer.class, 1650 * uc.getPersonen());
            uc.setItem(Schwert.class, uc.getPersonen() - 5);

            Unit ud = this.createUnit(d, r);
            ud.setName(this.getName()+" 04");
            ud.setPersonen(10);
            ud.setSkill(Hiebwaffen.class, 1650 * uc.getPersonen());
            ud.setSkill(Ausdauer.class, 1650 * uc.getPersonen());
            ud.setItem(Schwert.class, uc.getPersonen() - 5);

            ua.Befehle.add("ATTACKIERE " + ub.getNummerBase36());
            ub.Befehle.add("ATTACKIERE " + uc.getNummerBase36());
            uc.Befehle.add("ATTACKIERE " + ud.getNummerBase36());
            ud.Befehle.add("ATTACKIERE " + ua.getNummerBase36());

            new Info(this.getName() + " Setup in " + r + ".", ua);
        }
    }

    @Override
    protected boolean verifyTest() {
        new TestMsg(this.getName() + " - keine automatische Prüfung möglich!");
        return true;
    }

}
