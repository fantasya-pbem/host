package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Allianz.AllianzOption;
import de.x8bit.Fantasya.Atlantis.Items.Eisenschild;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Streitaxt;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Host.ManualTests.*;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Units.Halbling;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Atlantis.Units.Troll;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.util.Codierung;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class ParteiTarnungKampf extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();

        {
            Partei pa = tw.createPartei(Troll.class);
            pa.setName("Angreifer");
            Partei pb = tw.createPartei(Mensch.class);
            pb.setName("Echte Gegner");
            Partei pc = tw.createPartei(Halbling.class);
            pc.setName("Geheimniskrämer");
            Partei pd = tw.createPartei(Halbling.class);
            pd.setName("Alliierte von " + pc);
            Partei pe = tw.createPartei(Mensch.class);
            pe.setName("Parallel-Angreifer");
            Partei pf = tw.createPartei(Mensch.class);
            pf.setName("Parallel-Verteidiger");
            Region r = tw.nurBetretbar(getRegions()).get(0);
            r.setName(getName()+"-R1");
            getRegions().remove(r);
            
            Unit ua = this.createUnit(pa, r);
            ua.setName(this.getName()+" 01 Angreifer");
            ua.setKampfposition(Kampfposition.Vorne);
            ua.setPersonen(20);
            ua.setSkill(Hiebwaffen.class, ua.getPersonen() * 450);
            ua.setItem(Streitaxt.class, ua.getPersonen());
            ua.setItem(Eisenschild.class, ua.getPersonen() / 2);

            Unit b = this.createUnit(pa, r);
            b.setName(this.getName()+" 02 Geheimagent beim Feind");
            b.setKampfposition(Kampfposition.Vorne);
            b.setPersonen(1);
            b.setSkill(Hiebwaffen.class, b.getPersonen() * 450);
            b.setItem(Schwert.class, b.getPersonen());
            b.setTarnPartei(pb.getNummer());

            Unit ub = this.createUnit(pb, r);
            ub.setName(this.getName()+" 11 Echte Gegner");
            ub.setKampfposition(Kampfposition.Vorne);
            ub.setPersonen(4);
            ub.setSkill(Hiebwaffen.class, ub.getPersonen() * 450);
            ub.setItem(Schwert.class, ub.getPersonen());

            b = this.createUnit(pb, r);
            b.setName(this.getName()+" 12 Eskapisten");
            b.setKampfposition(Kampfposition.Vorne);
            b.setPersonen(1);
            b.setSkill(Hiebwaffen.class, b.getPersonen() * 450);
            b.setItem(Schwert.class, b.getPersonen());
            b.setTarnPartei(0); // entspricht TARNE PARTEI

            b = this.createUnit(pb, r);
            b.setName(this.getName()+" 13 Verlogene Eskapisten");
            b.setKampfposition(Kampfposition.Vorne);
            b.setPersonen(1);
            b.setSkill(Hiebwaffen.class, b.getPersonen() * 450);
            b.setItem(Schwert.class, b.getPersonen());
            b.setTarnPartei(pc.getNummer()); // entspricht TARNE PARTEI <Partei C>

            Unit c = this.createUnit(pc, r);
            c.setName(this.getName()+" 21 Fremder");
            c.setKampfposition(Kampfposition.Vorne);
            c.setPersonen(1);
            c.setSkill(Hiebwaffen.class, c.getPersonen() * 450);
            c.setItem(Schwert.class, c.getPersonen());

            c = this.createUnit(pc, r);
            c.setName(this.getName()+" 22 Heuchlerischer Fremder");
            c.setKampfposition(Kampfposition.Vorne);
            c.setPersonen(1);
            c.setSkill(Hiebwaffen.class, c.getPersonen() * 450);
            c.setItem(Schwert.class, c.getPersonen());
            c.setTarnPartei(pb.getNummer()); // entspricht TARNE PARTEI <Partei B>

            c = this.createUnit(pc, r);
            c.setName(this.getName()+" 23 Fremder Eskapist");
            c.setKampfposition(Kampfposition.Vorne);
            c.setPersonen(1);
            c.setSkill(Hiebwaffen.class, c.getPersonen() * 450);
            c.setItem(Schwert.class, c.getPersonen());
            c.setTarnPartei(0); // entspricht TARNE PARTEI
            
            Unit d = this.createUnit(pd, r);
            d.setName(this.getName()+" 31 Alliierter von 21");
            d.setKampfposition(Kampfposition.Vorne);
            d.setPersonen(1);
            d.setSkill(Hiebwaffen.class, d.getPersonen() * 450);
            d.setItem(Schwert.class, d.getPersonen());
            Partei.getPartei(d.getOwner()).setAllianz(pc.getNummer(), AllianzOption.Kaempfe, true);

            ua.Befehle.add("ATTACKIERE PARTEI " + pb.getNummerBase36());
            ub.Befehle.add("ATTACKIERE PARTEI " + pa.getNummerBase36());
            
            // ue/pe attackiert uf/pf:
            Unit ue = this.createUnit(pe, r);
            ue.setName(this.getName()+" 51 Parallel-Angreifer");
            ue.setKampfposition(Kampfposition.Vorne);
            ue.setPersonen(10);
            ue.setSkill(Hiebwaffen.class, ue.getPersonen() * 450);
            ue.setItem(Streitaxt.class, ue.getPersonen());
            ue.setItem(Eisenschild.class, ue.getPersonen() / 2);

            Unit uf = this.createUnit(pf, r);
            uf.setName(this.getName()+" 61 Parallel-Verteidiger");
            uf.setKampfposition(Kampfposition.Vorne);
            uf.setPersonen(10);
            uf.setSkill(Hiebwaffen.class, uf.getPersonen() * 450);
            uf.setItem(Schwert.class, uf.getPersonen());

            ue.Befehle.add("ATTACKIERE PARTEI " + pf.getNummerBase36());
            

            new Info(this.getName() + " Setup in " + r + ".", ua);
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "12", "13", "21", "23"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");
			String uRef = tokens[1] + "[" + u.getNummerBase36() + "] von [" + Codierung.toBase36(u.getOwner()) + "] - ";

            // unit 01
            if (tokens[1].equals("11") || tokens[1].equals("22")) {
//                messages = Message.Retrieve(Partei.getPartei(u.getOwner()), u.getCoords(), u);
//                boolean found = false;
//                for (Message msg : messages) {
//                    String text = msg.getMessage().toLowerCase();
//                    if (text.contains("") && text.contains("")) found = true;
//                }
                retval = fail(uRef + "Gegner-Einheit wurde nicht besiegt.");
            }
        } // next unit

        return retval;
    }


}
