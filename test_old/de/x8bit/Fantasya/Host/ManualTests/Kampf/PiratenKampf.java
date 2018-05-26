package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Host.ManualTests.*;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Regions.Ozean;
import de.x8bit.Fantasya.Atlantis.Ship;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Segeln;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Units.Aquaner;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;

/**
 *
 * @author hb
 */
public class PiratenKampf extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        Partei pa = tw.createPartei(Aquaner.class);
        pa.setName(getName() + "-Rechtschaffene");
        Partei pb = tw.createPartei(Mensch.class);
        pb.setName(getName() + "-Arglose");

        pa.setAllianz(pb.getNummer(), AllianzOption.Kontaktiere, true);
        pa.setAllianz(pb.getNummer(), AllianzOption.Kaempfe, true);
        
        {
            Region r = tw.nurTerrain(getRegions(), Ozean.class).get(0);
            r.setName(getName()+" R");
            getRegions().remove(r);

        
            Unit pirat = this.createKapitaen(pa, r, "Karavelle");
            pirat.setName("Klaus S.");
            pirat.setKampfposition(Kampfposition.Vorne);
            pirat.setPersonen(1);
            pirat.setSkill(Speerkampf.class, pirat.getPersonen() * 450);
            pirat.setItem(Speer.class, pirat.getPersonen());
            pirat.setItem(Eisenschild.class, pirat.getPersonen());
            pirat.setItem(Plattenpanzer.class, pirat.getPersonen());
            pirat.setTarnPartei(0);
//            String idPirat = pirat.getNummerBase36();

            Unit u = this.createUnit(pa, r);
            u.setName("Piraten");
            u.setKampfposition(Kampfposition.Vorne);
            u.setPersonen(10);
            u.setSkill(Hiebwaffen.class, u.getPersonen() * 450);
            u.setSkill(Segeln.class, u.getPersonen() * 450);
            u.setItem(Schwert.class, u.getPersonen());
            u.setItem(Kettenhemd.class, u.getPersonen());
            u.setTarnPartei(0);
            u.Enter(Ship.Load(pirat.getSchiff()));

            Unit ehrbar = this.createKapitaen(pa, r, "Karavelle");
            ehrbar.setName("Tugendhafter Kapitän");
            ehrbar.setKampfposition(Kampfposition.Nicht);
            ehrbar.setPersonen(1);
//            String idEhrbar = ehrbar.getNummerBase36();
            
            u = this.createUnit(pa, r);
            u.setName("Mannschaft");
            u.setKampfposition(Kampfposition.Nicht);
            u.setPersonen(10);
            u.setSkill(Segeln.class, u.getPersonen() * 450);
            u.Enter(Ship.Load(ehrbar.getSchiff()));

            u = this.createUnit(pa, r);
            u.setName("Wächter");
            u.setKampfposition(Kampfposition.Vorne);
            u.setPersonen(1);
            u.setSkill(Hiebwaffen.class, u.getPersonen() * 450);
            u.setItem(Schwert.class, u.getPersonen());
            u.Enter(Ship.Load(ehrbar.getSchiff()));

            Unit b = this.createUnit(pb, r);
            b.setName("Mitfahrer");
            b.setKampfposition(Kampfposition.Nicht);
            b.setPersonen(10);
            b.setItem(Silber.class, b.getPersonen() * 100);
            b.Enter(Ship.Load(ehrbar.getSchiff()));
            String idMitfahrer = b.getNummerBase36();

            pirat.Befehle.add("ATTACKIERE " + idMitfahrer);

            new Info(this.getName() + " Setup in " + r + ".", ehrbar);
        }
    }

    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
