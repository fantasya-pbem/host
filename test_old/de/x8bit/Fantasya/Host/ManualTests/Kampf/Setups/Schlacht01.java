package de.x8bit.Fantasya.Host.ManualTests.Kampf.Setups;

import de.x8bit.Fantasya.Atlantis.Item;
import de.x8bit.Fantasya.Atlantis.Items.Armbrust;
import de.x8bit.Fantasya.Atlantis.Items.Bogen;
import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Holzschild;
import de.x8bit.Fantasya.Atlantis.Items.Katapult;
import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Atlantis.Items.Kriegselefant;
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
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Armbrustschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Ausdauer;
import de.x8bit.Fantasya.Atlantis.Skills.Bogenschiessen;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Katapultbedienung;
import de.x8bit.Fantasya.Atlantis.Skills.Magie;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Skills.Taktik;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Spells.Feuerball;
import de.x8bit.Fantasya.Atlantis.Units.Elf;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Atlantis.Units.Ork;
import de.x8bit.Fantasya.Host.EVA.Kampfzauber;

/**
 * Erstellt die Ausgangslage einer Test-Schlacht in der DB,
 * mit der dann ein normaler ZAT durchgeführt werden kann.
 * @author hb
 */
public class Schlacht01 extends TestBase {

    Region region;

    Unit angreiferChef;
    Unit verteidigerChef;

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
         // nur Setup, damit danach ohne -debug eine AW gefahren werden kann.
        tw.setContinueWithZAT(false);

		Partei pa = tw.createPartei(Elf.class);
        pa.setName("Elfen");
		Partei pb = tw.createPartei(Ork.class);
        pb.setName("Orks");
        Partei pc = tw.createPartei(Mensch.class);
        pc.setName("Menschen");

        region = tw.nurBetretbar(getRegions()).get(0);
		region.setName(getName() + "-Schlachtfeld");
        getRegions().remove(region);

        Unit beobachter = this.createUnit(pc, region);
        beobachter.setName("Beobachter");
        beobachter.setPersonen(1);

        {
            setupAngreifer(pa, 20); // zweite Parameter jeweils: Multiplikator der Mann-Stärken
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
        // hinten:
        Unit enelias = createUnit(p, "Enelias der Mutige", 1, Kampfposition.Hinten);
        enelias.setSkill(Taktik.class, Skill.LerntageFuerTW(8));
        panzereLeicht(enelias);
        beritten(enelias);
        ausdauer(enelias, 6);

        angreiferChef = enelias;

        // hinten:
        Unit leibgarde = createSpeerkaempfer(p, "Enelias' Leibgarde", 15 * f, Kampfposition.Hinten, 10);
        panzereSchwer(leibgarde);
        beritten(leibgarde);
        ausdauer(leibgarde, 7);

        Unit u = createBogenschuetzen(p, "Nördliche Waldläufer", 100 * f, Kampfposition.Hinten, 8);
        u.setItem(Kettenhemd.class, u.getPersonen());
        u.Befehle.add("ATTACKIERE HINTEN");

        u = createBogenschuetzen(p, "Nordöstliche Waldläufer", 100 * f, Kampfposition.Hinten, 9);
        u.setItem(Kettenhemd.class, u.getPersonen());
        u.Befehle.add("ATTACKIERE HINTEN");

        u = createBogenschuetzen(p, "Ältere Waldläufer", 20 * f, Kampfposition.Hinten, 11);
        panzereSchwer(u);
        u.Befehle.add("ATTACKIERE HINTEN");

        u = createArtillerie(p, "Aus der Art Geschlagene", 2 * f, Kampfposition.Hinten, 6);
        panzereSchwer(u);
        beritten(u);

        // vorne:
        u = createSpeerkaempfer(p, "Hüter der Lichtung", 20 * f, Kampfposition.Vorne, 8);
        panzereSchwer(u);
        ausdauer(u, 6);

        u = createSpeerkaempfer(p, "Flurwachen", 100 * f, Kampfposition.Vorne, 7);
        panzereLeicht(u);
        beritten(u);
        ausdauer(u, 4);

        u = createSpeerkaempfer(p, "Rekruten", 100 * f, Kampfposition.Vorne, 6);
        panzereLeicht(u);
        beritten(u);

        u = createSpeerkaempfer(p, "Zirkel der zwei Dutzend", 24 * f, Kampfposition.Vorne, 12);
        panzereLeicht(u);
        beritten(u);
        ausdauer(u, 10);
        u.setSkill(Wahrnehmung.class, Skill.LerntageFuerTW(10) * u.getPersonen());
        u.setSkill(Tarnung.class, Skill.LerntageFuerTW(4) * u.getPersonen());

        u = createElefantenreiter(p, "Die Grauen", 5 * f, Kampfposition.Vorne, 10);
        panzereLeicht(u);
    }

    /**
     * @param p Partei der Verteidiger
     * @param f Multiplikator der Mann-Stärken
     */
    protected void setupVerteidiger(Partei p, int f) {
        // hinten:
        Unit ratz = createUnit(p, "Ratzenbroich", 1, Kampfposition.Hinten);
        ratz.setSkill(Taktik.class, Skill.LerntageFuerTW(5));
        panzereSchwer(ratz);
        ausdauer(ratz, 6);

        verteidigerChef = ratz;

        Unit u = createMage(p, region, 6);
        u.setName("Iggnax");
        u.setSkill(Magie.class, Skill.LerntageFuerTW(6));
        u.setAura(20);
        u.setSpell(new Feuerball());
        u.setProperty(Kampfzauber.ATTACKSPELL, "ZAUBERE Feuerball 2");


        u = createArtillerie(p, "Dreckwerfer", 20 * f, Kampfposition.Hinten, 8);
        panzereSchwer(u);

        u = createSchwertkaempfer(p, "Anführer", 10 * f, Kampfposition.Hinten, 12);
        panzereSchwer(u);
        beritten(u);
        ausdauer(u, 6);

        // vorne:
        u = createElefantenreiter(p, "Schmutzige Ritter", 10 * f, Kampfposition.Vorne, 6);
        panzereSchwer(u);

        u = createSchwertkaempfer(p, "Horde", 150 * f, Kampfposition.Vorne, 6);
        panzereLeicht(ratz);
        ausdauer(u, 4);

        u = createSchwertkaempfer(p, "Truppe", 200 * f, Kampfposition.Vorne, 5);
        panzereLeicht(ratz);
        ausdauer(u, 3);

        u = createSchwertkaempfer(p, "Rekruten", 200 * f, Kampfposition.Vorne, 2);
        u.setItem(Eisenschild.class, u.getPersonen());
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
