package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Items.Armbrust;
import de.x8bit.Fantasya.Atlantis.Items.Holz;
import de.x8bit.Fantasya.Atlantis.Items.Holzschild;
import de.x8bit.Fantasya.Atlantis.Items.Kriegshammer;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Plattenpanzer;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Seide;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Atlantis.Items.Wagen;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skill;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Skills.Taktik;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;

/**
 *
 * @author hb
 */
public class TestSammelBeute extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
        
        Partei pa = tw.createPartei(Mensch.class);
        pa.setName("Beute-Sammler");

        Partei pb = tw.createPartei(Mensch.class);
        pb.setName("Opfervolk");


        Region r = tw.nurBetretbar(getRegions()).get(0);
		r.setName(getName()+"-Region");
        getRegions().remove(r);

        {
            Unit u = this.createUnit(pa, r);
            u.setName(this.getName()+" 01");
            u.setBeschreibung("Der Anführer und Alles-Sammler");
            u.setPersonen(1);
            u.setSkill(Taktik.class, u.getPersonen() * Skill.LerntageFuerTW(3));
            u.setKampfposition(Kampfposition.Hinten);
            u.Befehle.add("@SAMMEL ALLE BEUTE");
            Unit angreifer = u;

            u = this.createUnit(pa, r);
            u.setName(this.getName()+" 02");
            u.setBeschreibung("Die Wenig-Träger. Dafür bessere Kämpfer!");
            u.setPersonen(10);
            u.setSkill(Taktik.class, u.getPersonen() * Skill.LerntageFuerTW(1));
            u.setSkill(Hiebwaffen.class, u.getPersonen() * Skill.LerntageFuerTW(6));
            u.setItem(Schwert.class, u.getPersonen());
            u.setItem(Plattenpanzer.class, u.getPersonen() / 2);
            u.setKampfposition(Kampfposition.Vorne);

            u = this.createUnit(pa, r);
            u.setName(this.getName()+" 03");
            u.setBeschreibung("Die Viel-Träger. Aber keine richtigen Kämpfer.");
            u.setPersonen(2);
            u.setSkill(Reiten.class, u.getPersonen() * Skill.LerntageFuerTW(2));
            u.setItem(Pferd.class, u.getPersonen() * 2);
            u.setItem(Wagen.class, u.getPersonen());
            u.setItem(Plattenpanzer.class, u.getPersonen());
            u.setKampfposition(Kampfposition.Hinten);


            Unit opfer = this.createUnit(pb, r);
            opfer.setName(this.getName() + " 10");
            opfer.setPersonen(2);
            opfer.setItem(Silber.class, 2500);
            opfer.setItem(Holzschild.class, 10);
            opfer.setItem(Armbrust.class, 5);
            opfer.setItem(Holz.class, 10);
            opfer.setItem(Kriegshammer.class, 2);
            opfer.setItem(Seide.class, 80);
            opfer.setItem(Stein.class, 20);

            angreifer.Befehle.add("ATTACKIERE " + opfer.getNummerBase36());

            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
        List<Message> messages = null;

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(Unit.CACHE, new String[] {"01", "02", "03"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u : Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
                boolean found = false;
                for (Message msg : messages) {
                    String text = msg.getText().toLowerCase();
                    if (text.contains("") && text.contains("")) found = true;
                }
                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben.");
            }
        } // next unit

        return retval;
    }

}
