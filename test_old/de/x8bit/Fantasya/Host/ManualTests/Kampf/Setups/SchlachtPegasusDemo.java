package de.x8bit.Fantasya.Host.ManualTests.Kampf.Setups;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Armbrust;
import de.x8bit.Fantasya.Atlantis.Items.Bogen;
import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Holzschild;
import de.x8bit.Fantasya.Atlantis.Items.Katapult;
import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Atlantis.Items.Kriegselefant;
import de.x8bit.Fantasya.Atlantis.Items.Pegasus;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Armbrustschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Ausdauer;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Katapultbedienung;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 * Erstellt die Ausgangslage einer Test-Schlacht in der DB,
 * mit der dann ein normaler ZAT durchgeführt werden kann.
 * @author hb
 */
public class SchlachtPegasusDemo extends TestBase {

    Region region;

    Unit angreiferChef;
    Unit verteidigerChef;

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
         // nur Setup, damit danach ohne -debug eine AW gefahren werden kann.
        tw.setContinueWithZAT(false);

		Partei pa = tw.createPartei(Mensch.class);
        pa.setName("Pegasus-Reiter");
		Partei pb = tw.createPartei(Mensch.class);
        pb.setName("Pferde-Reiter");
        Partei pc = tw.createPartei(Mensch.class);
        pc.setName("Menschen");

        region = tw.nurBetretbar(getRegions()).get(0);
		region.setName(getName() + "-Schlachtfeld");
        getRegions().remove(region);

        Unit beobachter = this.createUnit(pc, region);
        beobachter.setName("Beobachter");
        beobachter.setPersonen(1);

        {
            setupAngreifer(pa, 10); // zweite Parameter jeweils: Multiplikator der Mann-Stärken
            setupVerteidiger(pb, 20);

            angreiferChef.Befehle.add("ATTACKIERE " + verteidigerChef.getNummerBase36());

            new Info(this.getName() + " Setup in " + region + ".", angreiferChef);
            new Info(this.getName() + " Setup in " + region + ".", verteidigerChef);
            new Info(this.getName() + " Setup in " + region + ".", beobachter);
        }

        System.err.println(getName() + " angelegt - jetzt bitte einen ZAT ausführen!");
    }

    /**
     * @param p Partei der Angreifer
     * @param f Multiplikator der Mann-Stärken
     */
    protected void setupAngreifer(Partei p, int f) {
        Unit u = createSchwertkaempfer(p, "Flieger", f, Kampfposition.Vorne, 6);
        u.setItem(Pegasus.class, u.getPersonen());
        u.setSkill(Reiten.class, u.getPersonen() * Skill.LerntageFuerTW(6));
        
        angreiferChef = u;
    }

    /**
     * @param p Partei der Verteidiger
     * @param f Multiplikator der Mann-Stärken
     */
    protected void setupVerteidiger(Partei p, int f) {
        Unit u = createSchwertkaempfer(p, "Reiter", f, Kampfposition.Vorne, 6);
        u.setItem(Pferd.class, u.getPersonen());
        u.setSkill(Reiten.class, u.getPersonen() * Skill.LerntageFuerTW(6));
        
        verteidigerChef = u;
    }

    protected Unit createUnit(Partei p, String name, int personen, Kampfposition pos) {
        Unit u = this.createUnit(p, region);
        u.setName(name);
        u.setKampfposition(pos);
        u.setPersonen(personen);

        return u;
    }

    protected Unit createSchwertkaempfer(Partei p, String name, int personen, Kampfposition pos, int tw) {
        Unit u = createUnit(p, name, personen, pos);
        u.setItem(Schwert.class, u.getPersonen());
        u.setSkill(Hiebwaffen.class, u.getPersonen() * Skill.LerntageFuerTW(tw));

        return u;
    }

    protected Unit createSpeerkaempfer(Partei p, String name, int personen, Kampfposition pos, int tw) {
        Unit u = createUnit(p, name, personen, pos);
        u.setItem(Speer.class, u.getPersonen());
        u.setSkill(Speerkampf.class, u.getPersonen() * Skill.LerntageFuerTW(tw));

        return u;
    }

    protected Unit createBogenschuetzen(Partei p, String name, int personen, Kampfposition pos, int tw) {
        Unit u = createUnit(p, name, personen, pos);
        u.setItem(Bogen.class, u.getPersonen());
        u.setSkill(Bogenschiessen.class, u.getPersonen() * Skill.LerntageFuerTW(tw));

        return u;
    }

    protected Unit createArmbrustschuetzen(Partei p, String name, int personen, Kampfposition pos, int tw) {
        Unit u = createUnit(p, name, personen, pos);
        u.setItem(Armbrust.class, u.getPersonen());
        u.setSkill(Armbrustschiessen.class, u.getPersonen() * Skill.LerntageFuerTW(tw));

        return u;
    }

    protected Unit createArtillerie(Partei p, String name, int personen, Kampfposition pos, int tw) {
        Unit u = createUnit(p, name, personen, pos);
        u.setItem(Katapult.class, u.getPersonen());
        u.setSkill(Katapultbedienung.class, u.getPersonen() * Skill.LerntageFuerTW(tw));

        return u;
    }

    protected Unit createElefantenreiter(Partei p, String name, int personen, Kampfposition pos, int tw) {
        Unit u = createUnit(p, name, personen, pos);
        u.setItem(Kriegselefant.class, u.getPersonen());
        u.setSkill(Reiten.class, u.getPersonen() * Skill.LerntageFuerTW(tw));

        return u;
    }

    protected void panzereSchwer(Unit u) {
        u.setItem(Plattenpanzer.class, u.getPersonen());
        u.setItem(Eisenschild.class, u.getPersonen());
    }

    protected void panzereLeicht(Unit u) {
        u.setItem(Kettenhemd.class, u.getPersonen());
        u.setItem(Holzschild.class, u.getPersonen());
    }

    protected void ruesteAus(Unit u, Item ruestung) {
        u.setItem(ruestung.getClass(), u.getPersonen());
    }

    protected void beritten(Unit u) {
        u.setItem(Pferd.class, u.getPersonen());
        u.setSkill(Reiten.class, 300 * u.getPersonen());
    }

    protected void ausdauer(Unit u, int tw) {
        u.setSkill(Ausdauer.class, Skill.LerntageFuerTW(tw) * u.getPersonen());
    }



    @Override
    protected boolean verifyTest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
