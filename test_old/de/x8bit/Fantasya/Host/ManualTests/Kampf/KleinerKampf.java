package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class KleinerKampf extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei a = this.getTestWorld().createPartei(Mensch.class);
        a.setName(this.getName() + "-Partei A");
        Partei b = this.getTestWorld().createPartei(Mensch.class);
        b.setName(this.getName() + "-Partei B");

        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit ua = this.createUnit(a, r);
            ua.setName(this.getName()+" 01");
            ua.setPersonen(100);
            ua.setSkill(Speerkampf.class, 450 * ua.getPersonen());
//            ua.setSkill(Hiebwaffen.class, 30 * ua.getPersonen());
            ua.setItem(Speer.class, ua.getPersonen() /* - 1 */ );
//            ua.setItem(Schwert.class, ua.getPersonen());
            ua.setItem(Kettenhemd.class, 1 * ua.getPersonen());
            ua.setKampfposition(Kampfposition.Vorne);
			ua.setLebenspunkte(ua.maxLebenspunkte() / 3);

//            Unit u = this.createUnit(a, r);
//            u.setName(this.getName()+" 11");
//            u.setPersonen(1);
//            u.setSkill(Speerkampf.class, 1650 * u.getPersonen());
//            u.setItem(Speer.class, u.getPersonen());
//            u.setItem(Kettenhemd.class, u.getPersonen());
//            u.setKampfposition(Kampfposition.Vorne);


            Unit ub = this.createUnit(b, r);
            ub.setName(this.getName()+" 02");
            ub.setPersonen(100);
            ub.setSkill(Hiebwaffen.class, 840 * ub.getPersonen());
//            ub.setSkill(Reiten.class, ub.getPersonen() * 300);
            ub.setItem(Schwert.class, ub.getPersonen() /* - 1 */);
//            ub.setItem(Kriegselefant.class, 1);
            ub.setKampfposition(Kampfposition.Vorne);

            b.setUrsprung(r.getCoords());

            ua.Befehle.add("ATTACKIERE " + ub.getNummerBase36());
            
            new Info(this.getName() + " Setup in " + r + ".", ua, ua.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        new TestMsg(this.getName() + " - keine automatische Prüfung möglich!");
        return true;
    }

}
