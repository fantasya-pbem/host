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
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Spells.Feuerball;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.EVA.Kampfzauber;

/**
 *
 * @author hb
 */
public class SimplerMagierKampf extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei a = this.getTestWorld().createPartei(Mensch.class);
        a.setName(this.getName() + "-A");
        Partei b = this.getTestWorld().createPartei(Mensch.class);
        b.setName(this.getName() + "-B");

        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit ua = this.createUnit(a, r);
            ua.setName(this.getName()+" 01");
            ua.setPersonen(1);
            ua.setSkill(Magie.class, 1350 * ua.getPersonen());
            ua.setAura(10 * 10);
            ua.setSkill(Speerkampf.class, 300 * ua.getPersonen());
            ua.setItem(Speer.class, 1);
            ua.setSpell(new Feuerball());
			ua.setProperty(Kampfzauber.ATTACKSPELL, "Feuerball 4");
            // ua.Befehle.add("KAMPFZAUBER ANGRIFF Feuerball 4"); // wird zu spät ausgeführt...

            Unit ub = this.createUnit(b, r);
            ub.setName(this.getName()+" 02");
            ub.setPersonen(3);
            ub.setSkill(Hiebwaffen.class, 300 * ub.getPersonen());
            ub.setItem(Schwert.class, ub.getPersonen() - 1);
            b.setUrsprung(r.getCoords());

            ua.Befehle.add("ATTACKIERE " + ub.getNummerBase36());
            
            new Info(this.getName() + " Setup in " + r + ".", ua);
        }
    }

    @Override
    protected boolean verifyTest() {
        new TestMsg(this.getName() + " - keine automatische Prüfung möglich!");
        return true;
    }

}
