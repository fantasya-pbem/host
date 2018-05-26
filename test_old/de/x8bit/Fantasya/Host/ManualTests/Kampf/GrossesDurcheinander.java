package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 * Test einen Kampf, bei dem drei Parteien einander angreifen: A attackiert B,
 * B attackiert C - und C attackiert A!
 * @author hb
 */
public class GrossesDurcheinander extends TestBase {

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
        Partei e = this.getTestWorld().createPartei(Mensch.class);
        e.setName(this.getName() + "-E");
        Partei f = this.getTestWorld().createPartei(Mensch.class);
        f.setName(this.getName() + "-F");

        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit ua = this.createUnit(a, r);
            ua.setName(this.getName()+" 01");
            ua.setPersonen(200);
            ua.setSkill(Hiebwaffen.class, 450 * ua.getPersonen());
            ua.setItem(Schwert.class, ua.getPersonen());
            ua.setItem(Plattenpanzer.class, 5);
            ua.setItem(Eisenschild.class, 3);
            
            Unit ub = this.createUnit(b, r);
            ub.setName(this.getName()+" 02");
            ub.setPersonen(200);
            ub.setSkill(Hiebwaffen.class, 450 * ub.getPersonen());
            ub.setItem(Schwert.class, ub.getPersonen());

            Unit uc = this.createUnit(c, r);
            uc.setName(this.getName()+" 03");
            uc.setPersonen(100);
            uc.setSkill(Hiebwaffen.class, 1650 * uc.getPersonen());
            uc.setItem(Schwert.class, uc.getPersonen());

            Unit ud = this.createUnit(d, r);
            ud.setName(this.getName()+" 04");
            ud.setPersonen(50);
            ud.setSkill(Hiebwaffen.class, 1650 * ud.getPersonen());
            ud.setItem(Schwert.class, ud.getPersonen());

            Unit ue = this.createUnit(e, r);
            ue.setName(this.getName()+" 05");
            ue.setPersonen(50);
            ue.setSkill(Hiebwaffen.class, 1650 * ue.getPersonen());
            ue.setItem(Schwert.class, ue.getPersonen());

            Unit uf = this.createUnit(f, r);
            uf.setName(this.getName()+" 06");
            uf.setPersonen(10);
            uf.setSkill(Hiebwaffen.class, 1650 * uf.getPersonen());
            uf.setItem(Schwert.class, uf.getPersonen());

            ua.Befehle.add("ATTACKIERE PARTEI " + b.getNummerBase36()); // 1 --> 2
            ua.Befehle.add("ATTACKIERE PARTEI " + d.getNummerBase36()); // 1 --> 4

            ub.Befehle.add("ATTACKIERE PARTEI " + a.getNummerBase36()); // 2 --> 1
            ub.Befehle.add("ATTACKIERE PARTEI " + c.getNummerBase36()); // 2 --> 3
            ub.Befehle.add("ATTACKIERE PARTEI " + d.getNummerBase36()); // 2 --> 4

            uc.Befehle.add("ATTACKIERE PARTEI " + a.getNummerBase36()); // 3 --> 1
            uc.Befehle.add("ATTACKIERE PARTEI " + e.getNummerBase36()); // 3 --> 5
            
            ue.Befehle.add("ATTACKIERE PARTEI " + d.getNummerBase36()); // 5 --> 4
            ue.Befehle.add("ATTACKIERE PARTEI " + f.getNummerBase36()); // 5 --> 6

            uf.Befehle.add("ATTACKIERE PARTEI " + a.getNummerBase36()); // 6 --> 1

            new Info(this.getName() + " Setup in " + r + ".", ua);
        }
    }

    @Override
    protected boolean verifyTest() {
        new TestMsg(this.getName() + " - keine automatische Prüfung möglich!");
        return true;
    }

}
