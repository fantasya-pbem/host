package de.x8bit.Fantasya.Host.ManualTests.Kampf;

import de.x8bit.Fantasya.Atlantis.Items.Kettenhemd;
import de.x8bit.Fantasya.Atlantis.Items.Kriegselefant;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Speer;
import de.x8bit.Fantasya.Atlantis.Kampfposition;
import de.x8bit.Fantasya.Host.ManualTests.*;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Skills.Speerkampf;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import java.util.Collection;

/**
 *
 * @author hb
 */
public class AttackiereParteiTest extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei a = this.getTestWorld().createPartei(Mensch.class);
        a.setName(this.getName() + "-Partei A");
        Partei b = this.getTestWorld().createPartei(Mensch.class);
        b.setName(this.getName() + "-Partei B");

        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Unit ua = this.createUnit(a, r);
            ua.setName(this.getName()+" 01");
            ua.setPersonen(1);
            ua.setSkill(Speerkampf.class, 450 * ua.getPersonen());
            ua.setItem(Speer.class, ua.getPersonen() /* - 1 */ );
            ua.setItem(Kettenhemd.class, 1 /* ua.getPersonen() */ );
            ua.setKampfposition(Kampfposition.Vorne);

            Unit u = this.createUnit(a, r);
            u.setName(this.getName()+" 11");
            u.setPersonen(1);
            u.setSkill(Speerkampf.class, 1650 * u.getPersonen());
            u.setItem(Speer.class, u.getPersonen());
            u.setItem(Kettenhemd.class, u.getPersonen());
            u.setKampfposition(Kampfposition.Vorne);


            Unit ub = this.createUnit(b, r);
            ub.setName(this.getName()+" 02");
            ub.setPersonen(10);
            ub.setSkill(Hiebwaffen.class, 840 * ub.getPersonen());
            ub.setSkill(Reiten.class, ub.getPersonen() * 300);
            ub.setItem(Schwert.class, ub.getPersonen() /* - 1 */);
            ub.setItem(Kriegselefant.class, 1);
            ub.setKampfposition(Kampfposition.Vorne);
            b.setUrsprung(r.getCoords());

            ub.Befehle.add("ATTACKIERE PARTEI " + a.getNummerBase36());
            
            new Info(this.getName() + " Setup in " + r + ".", ua, ua.getCoords());
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

        String missing = this.verifyExpectedUnits(units, new String[] {"02"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }

        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                retval = fail(tokens[1] + ": Einheit 01 hat überlebt.");
            }

            // unit 11
            if (tokens[1].equals("11")) {
                retval = fail(tokens[1] + ": Einheit 11 hat überlebt.");
            }
        }


        return retval;
    }

}
