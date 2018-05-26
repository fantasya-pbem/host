package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Tarnung;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Atlantis.Units.Echse;
import de.x8bit.Fantasya.Atlantis.Units.Halbling;
import de.x8bit.Fantasya.Atlantis.Units.Zwerg;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hb
 */
public class TarnenTest extends TestBase {

    @Override
    protected void mySetupTest() {
        Partei p = this.getTestWorld().getSpieler1();
        Region r = this.getTestWorld().nurBetretbar(getRegions()).get(0);
        getRegions().remove(r);

        {
            Partei beobachter = this.getTestWorld().createPartei(Halbling.class);
            beobachter.setName(this.getName()+"-Beobachter");

            Partei tarner = this.getTestWorld().createPartei(Zwerg.class);
            tarner.setName(this.getName()+"-Tarner");

            Partei echsen = this.getTestWorld().createPartei(Echse.class);
            echsen.setName(this.getName()+"-Echsen");

            Unit u = this.createUnit(p, r);
            u.setName(this.getName()+" 01");
            u.setSkill(Wahrnehmung.class, 1650 * u.getPersonen());

            Unit tarner1 = this.createUnit(tarner, r);
            tarner1.setName(this.getName()+ " 02");
            tarner1.setSkill(Tarnung.class, 450);
            tarner1.Befehle.add("TARNE EINHEIT");

            Unit tarner2 = this.createUnit(tarner, r);
            tarner2.setName(this.getName()+ " 03");
            tarner2.setSkill(Tarnung.class, 3000);
            tarner2.Befehle.add("TARNE EINHEIT");

            Unit beobachter1 = this.createUnit(beobachter, r);
            beobachter1.setName(this.getName()+ " 04");
            beobachter1.setSkill(Wahrnehmung.class, 450);
            beobachter1.Befehle.add("TARNE RASSE Elf");

            Unit echse1 = this.createUnit(echsen, r);
            echse1.setName(this.getName()+ " 05");
            echse1.Befehle.add("TARNE RASSE Halbling");
            echse1.Befehle.add("TARNE VOLK " + beobachter.getNummerBase36());



            new Info(this.getName() + " Setup in " + r + ".", u, u.getCoords());
        }
    }

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
//        Partei p = this.getTestWorld().getSpieler1();

        if ((GameRules.getRunde() > 3) || (GameRules.getRunde() < 2)) throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");

        if (GameRules.getRunde() == 3) {
            new TestMsg("In Runde 3 gibt es keine Überprüfungen in " + this.getName() + ".");
            return retval;
        }

        new TestMsg("Verifiziere " + this.getName() + "...");

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04", "05"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                // TODO
//                messages = Message.Retrieve(p, u.getCoords(), u);
//                boolean found = false;
//                for (Message msg : messages) {
//                    String text = msg.getMessage().toLowerCase();
//                    if (text.contains("") && text.contains("")) found = true;
//                }
//                if (found) retval = fail(tokens[1] + ": Skeleton nicht überschrieben.");
            }
        } // next unit

        return retval;
    }

}
