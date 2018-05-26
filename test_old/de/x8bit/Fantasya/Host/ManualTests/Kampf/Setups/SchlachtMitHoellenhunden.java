package de.x8bit.Fantasya.Host.ManualTests.Kampf.Setups;

import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Host.ManualTests.*;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Skills.Monsterkampf;
import de.x8bit.Fantasya.Atlantis.Units.Goblin;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;

/**
 * Erstellt die Ausgangslage einer Test-Schlacht in der DB,
 * mit der dann ein normaler ZAT durchgeführt werden kann.
 * @author hb
 */
public class SchlachtMitHoellenhunden extends Schlacht01 {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
         // nur Setup, damit danach ohne -debug eine AW gefahren werden kann.
        // tw.setContinueWithZAT(false);

		Partei pa = tw.createPartei(Goblin.class);
        pa.setName("Schatten des Lichts");
		Partei pb = tw.createPartei(Mensch.class);
        pb.setName("Verteidiger");
        Partei pc = tw.createPartei(Mensch.class);
        pc.setName("Beobachter");

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

        // System.err.println(getName() + " angelegt - jetzt bitte einen ZAT ausführen!");
    }

    /**
     * @param p Partei der Angreifer
     * @param f Multiplikator der Mann-Stärken
     */
	@Override
    protected void setupAngreifer(Partei p, int f) {
        // hinten:
        Unit hunde = Unit.CreateUnit("Hoellenhund", p.getNummer(), region.getCoords());
        hunde.setPersonen(10);
		hunde.setSkill(Monsterkampf.class, 30 * hunde.getPersonen());
		hunde.setName("Höllenhunde");
        hunde.setKampfposition(Kampfposition.Vorne);
		hunde.Befehle.add(";NEU"); // damit nicht die Befehle in der Monsterplanung überschrieben werden.
				
        angreiferChef = hunde;
    }

    /**
     * @param p Partei der Verteidiger
     * @param f Multiplikator der Mann-Stärken
     */
	@Override
    protected void setupVerteidiger(Partei p, int f) {
        Unit u = createSpeerkaempfer(p, "Wache", 40, Kampfposition.Vorne, 4);
		
        verteidigerChef = u;
    }

}
