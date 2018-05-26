package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Holzschild;
import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Streitaxt;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Ausdauer;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 *
 * @author hb
 */
public class BeispielKampf01 extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
		Partei pa = tw.createPartei(Mensch.class);
        pa.setName("Aussätzige");
		Partei pb = tw.createPartei(Mensch.class);
        pb.setName("Loyale Royalisten");
        Region r = tw.nurBetretbar(getRegions()).get(0);
		// r.setName(getName()+" R");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(pa, r);
            u.setName("Räuber");
            u.setKampfposition(Kampfposition.Vorne);
            u.setPersonen(5);
            u.setSkill(Hiebwaffen.class, u.getPersonen() * 300);
            u.setItem(Streitaxt.class, 5);
            u.setItem(Eisenschild.class, 1);
//            String idA = u.getNummerBase36();

            Unit b = this.createUnit(pb, r);
            b.setName("Gendarmen");
            b.setKampfposition(Kampfposition.Vorne);
            b.setPersonen(5);
            b.setSkill(Hiebwaffen.class, b.getPersonen() * 450);
            b.setItem(Schwert.class, b.getPersonen());
            b.setItem(Holzschild.class, b.getPersonen());
            b.setItem(Kettenhemd.class, b.getPersonen());
            String idB = b.getNummerBase36();

            b = this.createUnit(pb, r);
            b.setName("Leutnant Haudrauf");
            b.setKampfposition(Kampfposition.Vorne);
            b.setPersonen(1);
            b.setSkill(Hiebwaffen.class, b.getPersonen() * 840);
            b.setSkill(Reiten.class, b.getPersonen() * 450);
            b.setSkill(Ausdauer.class, b.getPersonen() * 450);
            b.setItem(Schwert.class, b.getPersonen());
            b.setItem(Holzschild.class, b.getPersonen());
            b.setItem(Kettenhemd.class, b.getPersonen());
            b.setItem(Pferd.class, b.getPersonen());

            u.Befehle.add("ATTACKIERE " + idB);

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
