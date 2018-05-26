package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Bogen;
import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Holzschild;
import de.x8bit.Fantasya.Atlantis.Items.Katapult;
import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Katapultbedienung;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Troll;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 *
 * @author hb
 */
public class BeispielKampf02 extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
		Partei pa = tw.createPartei(Elf.class);
        pa.setName("Elfen");
		Partei pb = tw.createPartei(Troll.class);
        pb.setName("Trolle");
        Region r = tw.nurBetretbar(getRegions()).get(0);
		// r.setName(getName()+" R");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(pa, r);
            u.setName("Trollverwirrer");
            u.setKampfposition(Kampfposition.Vorne);
            u.setPersonen(10);
            u.setSkill(Speerkampf.class, u.getPersonen() * 450);
            u.setItem(Speer.class, u.getPersonen());
            u.setItem(Holzschild.class, 8);
            u.setItem(Eisenschild.class, 3);
            u.setItem(Plattenpanzer.class, 6);
            u.setItem(Kettenhemd.class, 3);
            u.setTarnPartei(0);
//            String idA = u.getNummerBase36();

            u = this.createUnit(pa, r);
            u.setName("Schützen");
            u.setKampfposition(Kampfposition.Hinten);
            u.setPersonen(10);
            u.setSkill(Bogenschiessen.class, u.getPersonen() * 300);
            u.setItem(Bogen.class, u.getPersonen());
            u.setItem(Plattenpanzer.class, 6);
            u.setItem(Kettenhemd.class, 3);
            u.setTarnPartei(0);

            Unit b = this.createUnit(pb, r);
            b.setName("Prügeltrolle");
            b.setKampfposition(Kampfposition.Vorne);
            b.setPersonen(10);
            b.setSkill(Hiebwaffen.class, b.getPersonen() * 450);
            b.setItem(Schwert.class, b.getPersonen());
            b.setItem(Holzschild.class, b.getPersonen());
            String idB = b.getNummerBase36();

            b = this.createUnit(pb, r);
            b.setName("Felswerfer");
            b.setKampfposition(Kampfposition.Hinten);
            b.setPersonen(1);
            b.setSkill(Katapultbedienung.class, b.getPersonen() * 300);
            b.setItem(Katapult.class, 1);

            b = this.createUnit(pb, r);
            b.setName("Pfeilschlucker");
            b.setKampfposition(Kampfposition.Hinten);
            b.setPersonen(10);

            u.Befehle.add("ATTACKIERE " + idB);

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
