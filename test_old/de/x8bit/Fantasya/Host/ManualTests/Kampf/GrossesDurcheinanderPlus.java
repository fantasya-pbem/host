package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
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
public class GrossesDurcheinanderPlus extends TestBase {

    @Override
    protected void mySetupTest() {
        // erster Schlachtkomplex:
        Partei a = this.getTestWorld().createPartei(Mensch.class);
        a.setName("*A*");
        Partei b = this.getTestWorld().createPartei(Mensch.class);
        b.setName("*B*");
        Partei c = this.getTestWorld().createPartei(Mensch.class);
        c.setName("*C*");
        Partei d = this.getTestWorld().createPartei(Mensch.class);
        d.setName("*D*");
        Partei e = this.getTestWorld().createPartei(Mensch.class);
        e.setName("*E*");
        Partei f = this.getTestWorld().createPartei(Mensch.class);
        f.setName("*F*");
        
        // zweiter Schlachtkomplex:
        Partei g = this.getTestWorld().createPartei(Mensch.class);
        g.setName("*G*");
        Partei h = this.getTestWorld().createPartei(Mensch.class);
        h.setName("*H*");
        Partei i = this.getTestWorld().createPartei(Mensch.class);
        i.setName("*I*");
        
        // dritter Komplex - Unbeteiligte und "Unbeteiligte":
        Partei j = this.getTestWorld().createPartei(Mensch.class);
        j.setName("*J*");
        
        

        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit ua = this.createUnit(a, r);
            ua.setName("A1");
            ua.setPersonen(200);
            ua.setSkill(Hiebwaffen.class, 450 * ua.getPersonen());
            ua.setItem(Schwert.class, ua.getPersonen());
            ua.setItem(Plattenpanzer.class, 5);
            ua.setItem(Eisenschild.class, 3);
            
            // Partei a, getarnet als Partei b:
            Unit u = this.createUnit(a, r);
            u.setName("A2(B)");
            u.setPersonen(20);
            u.setSkill(Hiebwaffen.class, 450 * u.getPersonen());
            u.setItem(Schwert.class, u.getPersonen());
            u.setItem(Plattenpanzer.class, u.getPersonen());
            u.setItem(Eisenschild.class, u.getPersonen() / 2);
            u.setTarnPartei(b.getNummer());
            String unitVonAalsB = u.getNummerBase36();
            
            // Partei A, getarnet als Partei G: (aus dem 2. Schlachtkomplex - sollte die beiden nicht verbinden!)
            u = this.createUnit(a, r);
            u.setName("A3(G)");
            u.setPersonen(20);
            u.setSkill(Hiebwaffen.class, 450 * u.getPersonen());
            u.setItem(Schwert.class, u.getPersonen());
            u.setTarnPartei(g.getNummer());

            Unit ub = this.createUnit(b, r);
            ub.setName("B1");
            ub.setPersonen(200);
            ub.setSkill(Hiebwaffen.class, 450 * ub.getPersonen());
            ub.setItem(Schwert.class, ub.getPersonen());

            
            
            Unit uc = this.createUnit(c, r);
            uc.setName("C1");
            uc.setPersonen(100);
            uc.setSkill(Hiebwaffen.class, 1650 * uc.getPersonen());
            uc.setItem(Schwert.class, uc.getPersonen());

            // Partei c, getarnet als Partei b:
            u = this.createUnit(c, r);
            u.setName("C2(B)");
            u.setPersonen(20);
            u.setSkill(Hiebwaffen.class, 450 * u.getPersonen());
            u.setItem(Schwert.class, u.getPersonen());
            u.setItem(Plattenpanzer.class, u.getPersonen());
            u.setItem(Eisenschild.class, u.getPersonen() / 2);
            u.setTarnPartei(b.getNummer());
            
            
            
            Unit ud = this.createUnit(d, r);
            ud.setName("D1");
            ud.setPersonen(50);
            ud.setSkill(Hiebwaffen.class, 1650 * ud.getPersonen());
            ud.setItem(Schwert.class, ud.getPersonen());

            // Partei d, getarnet als Partei b:
            u = this.createUnit(d, r);
            u.setName("D2(B)");
            u.setPersonen(10);
            u.setSkill(Hiebwaffen.class, 450 * u.getPersonen());
            u.setItem(Schwert.class, u.getPersonen());
            u.setItem(Plattenpanzer.class, u.getPersonen());
            u.setItem(Eisenschild.class, u.getPersonen() / 2);
            u.setTarnPartei(b.getNummer());
            
            // Partei D, getarnet als Partei J: (aus dem 3. Komplex - "sicherer Hafen")
            u = this.createUnit(d, r);
            u.setName("D3(J)");
            u.setPersonen(50);
            u.setSkill(Hiebwaffen.class, 450 * u.getPersonen());
            u.setItem(Schwert.class, u.getPersonen());
            u.setTarnPartei(j.getNummer());
            
            
            
            Unit ue = this.createUnit(e, r);
            ue.setName("E1");
            ue.setPersonen(50);
            ue.setSkill(Hiebwaffen.class, 1650 * ue.getPersonen());
            ue.setItem(Schwert.class, ue.getPersonen());

            
            
            Unit uf = this.createUnit(f, r);
            uf.setName("F1");
            uf.setPersonen(10);
            uf.setSkill(Hiebwaffen.class, 1650 * uf.getPersonen());
            uf.setItem(Schwert.class, uf.getPersonen());
            
            // Partei f, getarnet als Partei b:
            u = this.createUnit(f, r);
            u.setName("F2(B)");
            u.setPersonen(10);
            u.setSkill(Hiebwaffen.class, 450 * u.getPersonen());
            u.setItem(Schwert.class, u.getPersonen());
            u.setTarnPartei(b.getNummer());
            
            
            
            // zweiter Schlachtkomplex; Parteien G, H, I
            Unit ug = this.createUnit(g, r);
            ug.setName("G1");
            ug.setPersonen(100);
            ug.setSkill(Hiebwaffen.class, 1650 * ug.getPersonen());
            ug.setItem(Schwert.class, ug.getPersonen());
            
            // Partei G, getarnet als Partei A: (aus dem 1. Schlachtkomplex - sollte die beiden nicht verbinden!)
            Unit ug2 = this.createUnit(g, r);
            ug2.setName("G2(A)");
            ug2.setPersonen(50);
            ug2.setSkill(Hiebwaffen.class, 450 * ug2.getPersonen());
            ug2.setItem(Schwert.class, ug2.getPersonen());
            ug2.setTarnPartei(a.getNummer());

            Unit uh = this.createUnit(h, r);
            uh.setName("H1");
            uh.setPersonen(100);
            uh.setSkill(Hiebwaffen.class, 1650 * uh.getPersonen());
            uh.setItem(Schwert.class, uh.getPersonen());
            
            
            Unit ui = this.createUnit(i, r);
            ui.setName("I1");
            ui.setPersonen(50);
            ui.setSkill(Hiebwaffen.class, 1650 * ui.getPersonen());
            ui.setItem(Schwert.class, ui.getPersonen());
            
            // Partei I, getarnet als Partei D: (aus dem 1. Schlachtkomplex - sollte die beiden nicht verbinden!)
            u = this.createUnit(i, r);
            u.setName("I2(D)");
            u.setPersonen(10);
            u.setSkill(Hiebwaffen.class, 450 * u.getPersonen());
            u.setItem(Schwert.class, u.getPersonen());
            u.setTarnPartei(d.getNummer());
            
            
            
            // dritter Komplex - Unbeteiligte und "Unbeteiligte":
            Unit uj = this.createUnit(j, r);
            uj.setName("J1");
            uj.setPersonen(100);
            uj.setSkill(Hiebwaffen.class, 1650 * ui.getPersonen());
            uj.setItem(Schwert.class, ui.getPersonen());
            
            // Partei J, getarnet als Partei A: (aus dem 1. Schlachtkomplex - sollte die beiden nicht verbinden!)
            Unit uj2 = this.createUnit(j, r);
            uj2.setName("J2(A)");
            uj2.setPersonen(50);
            uj2.setSkill(Hiebwaffen.class, 450 * uj2.getPersonen());
            uj2.setItem(Schwert.class, uj2.getPersonen());
            uj2.setTarnPartei(a.getNummer());
            
            

            // erster Schlachtkomplex:
            ua.Befehle.add("ATTACKIERE PARTEI " + b.getNummerBase36()); // 2 --> 3
            ua.Befehle.add("ATTACKIERE PARTEI " + d.getNummerBase36()); // 2 --> 5

            ub.Befehle.add("ATTACKIERE PARTEI " + a.getNummerBase36()); // 3 --> 2
            ub.Befehle.add("ATTACKIERE PARTEI " + c.getNummerBase36()); // 3 --> 4
            ub.Befehle.add("ATTACKIERE PARTEI " + d.getNummerBase36()); // 3 --> 5
            ub.Befehle.add("ATTACKIERE " + unitVonAalsB);               // 3 --> [2][3]

            uc.Befehle.add("ATTACKIERE PARTEI " + a.getNummerBase36()); // 4 --> 2
            uc.Befehle.add("ATTACKIERE PARTEI " + e.getNummerBase36()); // 4 --> 6
            
            ue.Befehle.add("ATTACKIERE PARTEI " + d.getNummerBase36()); // 6 --> 5
            ue.Befehle.add("ATTACKIERE PARTEI " + f.getNummerBase36()); // 6 --> 7

            uf.Befehle.add("ATTACKIERE PARTEI " + a.getNummerBase36()); // 7 --> 2
            
            // zweiter Schlachtkomplex:
            ug.Befehle.add("ATTACKIERE PARTEI " + h.getNummerBase36()); // 8 --> 9
            ug2.Befehle.add("ATTACKIERE PARTEI " + b.getNummerBase36()); // G([8][2]) -> B[3]
            uh.Befehle.add("ATTACKIERE PARTEI " + i.getNummerBase36()); // 9 --> 10
            ui.Befehle.add("ATTACKIERE PARTEI " + g.getNummerBase36()); // 10 --> 8
            
            // dritter (Schlacht-)Komplex:
            uj2.Befehle.add("ATTACKIERE PARTEI " + b.getNummerBase36()); // J[b][2] --> B[3]

            new Info(this.getName() + " Setup in " + r + ".", ua);
        }
    }

    @Override
    protected boolean verifyTest() {
        new TestMsg(this.getName() + " - keine automatische Prüfung möglich!");
        return true;
    }

}
