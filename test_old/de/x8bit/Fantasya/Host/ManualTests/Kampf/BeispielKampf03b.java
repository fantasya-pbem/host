package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Bogen;
import de.x8bit.Fantasya.Atlantis.Items.Katapult;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Katapultbedienung;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 *
 * @author hb
 */
public class BeispielKampf03b extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
		Partei pa = tw.createPartei(Mensch.class);
        pa.setName("Genaue");
		Partei pb = tw.createPartei(Mensch.class);
        pb.setName("Brutale");
        Region r = tw.nurBetretbar(getRegions()).get(0);
		// r.setName(getName()+" R");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(pa, r);
            u.setName("Genaue Pikeniere");
            u.setKampfposition(Kampfposition.Vorne);
            u.setPersonen(10);
            u.setSkill(Speerkampf.class, u.getPersonen() * 450);
            u.setItem(Speer.class, u.getPersonen());
            u.setItem(Plattenpanzer.class, u.getPersonen());
//            String idA = u.getNummerBase36();

            Unit schuetzen = this.createUnit(pa, r);
            schuetzen.setName("Genaue Schützen");
            schuetzen.setKampfposition(Kampfposition.Hinten);
            schuetzen.setPersonen(12);
            schuetzen.setSkill(Bogenschiessen.class, schuetzen.getPersonen() * 1650);
            schuetzen.setItem(Bogen.class, schuetzen.getPersonen());

            Unit b = this.createUnit(pb, r);
            b.setName("Brutale Pikeniere");
            b.setKampfposition(Kampfposition.Vorne);
            b.setPersonen(10);
            b.setSkill(Speerkampf.class, b.getPersonen() * 450);
            b.setItem(Speer.class, b.getPersonen());
            b.setItem(Plattenpanzer.class, b.getPersonen());
            String idB = b.getNummerBase36();

            b = this.createUnit(pb, r);
            b.setName("Brutaler Katapultschütze");
            b.setKampfposition(Kampfposition.Hinten);
            b.setPersonen(1);
            b.setSkill(Katapultbedienung.class, b.getPersonen() * 300);
            b.setSkill(Tarnung.class, b.getPersonen() * Skill.LerntageFuerTW(10));
            b.setItem(Katapult.class, 1);
            String idKatapultschuetze = b.getNummerBase36();

            b = this.createUnit(pb, r);
            b.setName("Brutale Schützen");
            b.setKampfposition(Kampfposition.Hinten);
            b.setPersonen(9);
            b.setSkill(Bogenschiessen.class, b.getPersonen() * 1650);
            b.setItem(Bogen.class, b.getPersonen());
            b.Befehle.add("ATTACKIERE vorne");

            u.Befehle.add("ATTACKIERE " + idB);
            schuetzen.Befehle.add("ATTACKIERE GEZIELT " + idKatapultschuetze);

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
