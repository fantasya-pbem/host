package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Building;
import de.x8bit.Fantasya.Atlantis.Buildings.Burg;
import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Host.ManualTests.*;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;

/**
 *
 * @author hb
 */
public class TestBurgenKampf extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
		Partei pa = tw.createPartei(Mensch.class);
        pa.setName("Belagerer");
		Partei pb = tw.createPartei(Mensch.class);
        pb.setName("Insassen");
        Region r = tw.nurBetretbar(getRegions()).get(0);
		// r.setName(getName()+" R");
        getRegions().remove(r);

        {
            Unit a = this.createUnit(pa, r);
            a.setName("Stürmende Pikeniere");
            a.setKampfposition(Kampfposition.Vorne);
            a.setPersonen(10);
            a.setSkill(Speerkampf.class, a.getPersonen() * 450);
            a.setItem(Speer.class, a.getPersonen());
            a.setItem(Kettenhemd.class, a.getPersonen());
//            String idA = a.getNummerBase36();

            Building burg = Building.Create(Burg.class.getSimpleName(), r.getCoords());
			burg.setName("Schutzburg");
			burg.setSize(50);
			
			Unit b = this.createUnit(pb, r);
            b.setName("Verteidigende Pikeniere");
            b.setKampfposition(Kampfposition.Vorne);
            b.setPersonen(10);
            b.setSkill(Speerkampf.class, b.getPersonen() * 450);
            b.setItem(Speer.class, b.getPersonen());
            b.setItem(Kettenhemd.class, b.getPersonen());
			b.Enter(burg);
            String idB = b.getNummerBase36();

            a.Befehle.add("ATTACKIERE " + idB);

            new Info(this.getName() + " Setup in " + r + ".", a);
        }
    }

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
