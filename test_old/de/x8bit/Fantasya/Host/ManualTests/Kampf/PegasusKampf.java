package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Item;
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
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Host.ManualTests.*;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Armbrustschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Ausdauer;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Katapultbedienung;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Ork;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 * Erstellt die Ausgangslage einer Test-Schlacht in der DB,
 * mit der dann ein normaler ZAT durchgeführt werden kann.
 * @author hb
 */
public class PegasusKampf extends TestBase {

    Region region;

    Unit angreiferChef;
    Unit verteidigerChef;

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();

		Partei pa = tw.createPartei(Elf.class);
        pa.setName("Flieger");
		Partei pb = tw.createPartei(Ork.class);
        pb.setName("Reiter");

        region = tw.nurBetretbar(getRegions()).get(0);
		region.setName(getName() + "-Schlachtfeld");
        getRegions().remove(region);

        {
            setupAngreifer(pa, 20); // zweite Parameter jeweils: Multiplikator der Mann-Stärken
            setupVerteidiger(pb, 25);

            angreiferChef.Befehle.add("ATTACKIERE " + verteidigerChef.getNummerBase36());

            new Info(this.getName() + " Setup in " + region + ".", angreiferChef);
            new Info(this.getName() + " Setup in " + region + ".", verteidigerChef);
        }
    }

    /**
     * @param p Partei der Angreifer
     * @param f Multiplikator der Mann-Stärken
     */
    protected void setupAngreifer(Partei p, int f) {
        // vorne:
        Unit u = createSchwertkaempfer(p, this.getName() + " 01 Luftwaffe", 1 * f, Kampfposition.Vorne, 10);
        ruesteAus(u, new Pegasus());
        ausdauer(u, 6);
        u.setSkill(Reiten.class, Skill.LerntageFuerTW(10) * u.getPersonen());

        angreiferChef = u;
    }

    /**
     * @param p Partei der Verteidiger
     * @param f Multiplikator der Mann-Stärken
     */
    protected void setupVerteidiger(Partei p, int f) {
        // vorne:
        Unit u = createSchwertkaempfer(p, this.getName() + " 02 Geerdete", 1 * f, Kampfposition.Vorne, 10);
        beritten(u);
        ausdauer(u, 6);
        u.setSkill(Reiten.class, Skill.LerntageFuerTW(10) * u.getPersonen());

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
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
		
        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");
			String uRef = tokens[1] + "[" + u.getNummerBase36() + "] von [" + Codierung.toBase36(u.getOwner()) + "] - ";

            // unit 01
            if (tokens[1].equals("02")) {
                retval = fail(uRef + "die sollten den Luftangriff eher nicht überstehen.");
            }
        } // next unit

        return retval;
    }


}
