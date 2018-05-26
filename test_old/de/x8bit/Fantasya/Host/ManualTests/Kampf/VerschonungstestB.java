package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Holzschild;
import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Streitaxt;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Host.ManualTests.*;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Ausdauer;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 * Übermächtige Räuber greifen Gendarmen mit ATTACKIERE PARTEI ... an, deswegen
 * sollten auch unbeteiligte Zivilisten auf Seiten der Gendarmen mit verwickelt
 * werden. Eine weitere Zivilisteneinheit (12) ist jedoch getarnt und sollte nicht
 * attackiert werden.
 * @author hb
 */
public class VerschonungstestB extends TestBase {

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
            Unit raeuber = this.createUnit(pa, r);
            raeuber.setName("Räuber");
            raeuber.setKampfposition(Kampfposition.Vorne);
            raeuber.setPersonen(10);
            raeuber.setSkill(Hiebwaffen.class, raeuber.getPersonen() * 300);
            raeuber.setItem(Streitaxt.class, 10);
            raeuber.setItem(Eisenschild.class, 4);

            Unit braut = this.createUnit(pa, r);
            braut.setName(getName() + " 01 Räuberbräute");
            braut.setBeschreibung("Erwartet: Wird nicht in den Kampf hineingezogen, weil der Verteidiger nur die (aktiven) Angreifer bekämpft.");
            braut.setKampfposition(Kampfposition.Nicht);
            braut.setPersonen(5);

            Unit leichtsinnige = this.createUnit(pa, r);
            leichtsinnige.setName("Leichtsinnige");
            leichtsinnige.setBeschreibung("Erwartet: Wird in den Kampf hineingezogen, weil in der 2. Reihe");
            leichtsinnige.setKampfposition(Kampfposition.Hinten);
            leichtsinnige.setPersonen(5);



            Unit b = this.createUnit(pb, r);
            b.setName("Gendarmen");
            b.setKampfposition(Kampfposition.Vorne);
            b.setPersonen(5);
            b.setSkill(Hiebwaffen.class, b.getPersonen() * 450);
            b.setItem(Schwert.class, b.getPersonen());
            b.setItem(Holzschild.class, b.getPersonen());
            b.setItem(Kettenhemd.class, b.getPersonen());
//            String idB = b.getNummerBase36();

            b = this.createUnit(pb, r);
            b.setName("11 Leutnant Haudrauf");
            b.setKampfposition(Kampfposition.Vorne);
            b.setPersonen(1);
            b.setSkill(Hiebwaffen.class, b.getPersonen() * 840);
            b.setSkill(Reiten.class, b.getPersonen() * 450);
            b.setSkill(Ausdauer.class, b.getPersonen() * 450);
            b.setItem(Schwert.class, b.getPersonen());
            b.setItem(Holzschild.class, b.getPersonen());
            b.setItem(Kettenhemd.class, b.getPersonen());
            b.setItem(Pferd.class, b.getPersonen());

            Unit bu = this.createUnit(pb, r);
            bu.setName("Brave Bürger");
            bu.setBeschreibung("Erwartet: Wird in den Kampf hineingezogen - ATTACKIERE PARTEI ...");
            bu.setKampfposition(Kampfposition.Nicht);
            bu.setPersonen(5);

            bu = this.createUnit(pb, r);
            bu.setName(getName() + " 12 Versteckte Bürger");
            bu.setBeschreibung("Erwartet: Wird nicht in den Kampf hineingezogen - die Räuber können sie nicht entdecken.");
            bu.setSkill(Tarnung.class, Skill.LerntageFuerTW(6));
            bu.setSichtbarkeit(1); // Einheiten-getarnt
            bu.setKampfposition(Kampfposition.Nicht);
            bu.setPersonen(5);


            raeuber.Befehle.add("ATTACKIERE PARTEI " + pb.getNummerBase36());

            new Info(this.getName() + " Setup in " + r + ".", raeuber, raeuber.getCoords());
        }
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

        // erwartet: 01 Räuberbräute, 12 Versteckte Bürger
        String missing = this.verifyExpectedUnits(units, new String[] {"01", "12"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");
			String uRef = tokens[1] + "[" + u.getNummerBase36() + "] von [" + Codierung.toBase36(u.getOwner()) + "] - ";

            // unit 12
            if (tokens[1].equals("12")) {
                Region r = Region.Load(u.getCoords());
                Partei zivile = Partei.getPartei(u.getOwner());
                if (Unit.CACHE.getAll(r.getCoords(),zivile.getNummer()).size() > 1) {
                    retval = fail(uRef + "Es sind noch Gendarmen außer den verschonten am Leben.");
                }
            }
        } // next unit

        return retval;
    }


}
