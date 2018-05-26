package de.x8bit.Fantasya.Host.ManualTests.Befehle;


import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.EVA.util.Einzelbefehl;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;
import java.util.Collection;

/**
 * Testet, ob die Bewegung und die Rotation (Kürzung) der Befehlsteile
 * bei NACH wie gewünscht funktionieren.
 * 
 * @author hapebe
 */
public class Nach extends TestBase {

	@Override
	public void mySetupTest() {
		TestWorld tw = this.getTestWorld();
		Partei p = tw.createPartei(Mensch.class);
        Region r = null;
        Region r1 = null;
        Region r2 = null;

        for (Region maybe : tw.nurBetretbar(getRegions())) {
            r1 = Region.Load(maybe.getCoords().shift(Richtung.Osten));
            if (!r1.istBetretbar(null)) continue;
            r2 = Region.Load(r1.getCoords().shift(Richtung.Osten));
            if (!r2.istBetretbar(null)) continue;

            // gotcha!
            r = maybe;
			break;
        }
        if (r == null) throw new IllegalStateException("Keine passende Region für " + this.getName() + " gefunden - einfach nochmal probieren...");
        getRegions().remove(r);
        getRegions().remove(r1);
        getRegions().remove(r2);

        Unit u = this.createUnit(p, r);
        u.setName(this.getName() + " 01 " + r1.getCoords().getX() + " " + r1.getCoords().getY() + " Wanderer 1");
        String befehl = "NACH o o";
        u.setItem(Silber.class, 500);
        u.Befehle.add(befehl);
		u.setBeschreibung("Befehl war: " + befehl);

        u = this.createUnit(p, r1);
        u.setName(this.getName() + " 02 " + r2.getCoords().getX() + " " + r2.getCoords().getY() + " Wanderer 2");
        befehl = "NACH o";
        u.setItem(Silber.class, 500);
        u.Befehle.add(befehl);
		u.setBeschreibung("Befehl war: " + befehl);

        u = this.createUnit(p, r1);
        u.setName(this.getName() + " 03 " + r.getCoords().getX() + " " + r.getCoords().getY() + " Reiter 1");
        befehl = "NACH w PAUSE o PAUSE";
        u.setItem(Silber.class, 500);
        u.setItem(Pferd.class, 1);
        u.setSkill(Reiten.class, 30);
        u.Befehle.add(befehl);
		u.setBeschreibung("Befehl war: " + befehl);

        u = this.createUnit(p, r);
        u.setName(this.getName() + " 04 " + r2.getCoords().getX() + " " + r2.getCoords().getY() + " Reiter 2");
        befehl = "NACH o o w w";
        u.setItem(Silber.class, 500);
        u.setItem(Pferd.class, 1);
        u.setSkill(Reiten.class, 30);
        u.Befehle.add(befehl);
		u.setBeschreibung("Befehl war: " + befehl);

        new Info(this.getName() + " Setup in " + r + ".", p);
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

        String missing = this.verifyExpectedUnits(units, new String[] {"01", "02", "03", "04"} );
        if (missing != null) retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);


        for (Unit u:units) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName() + " ")) continue;

            String[] tokens = u.getName().split("\\ ");

            boolean verifyCoords = false;
            if (tokens[1].equals("01")) verifyCoords = true;
            if (tokens[1].equals("02")) verifyCoords = true;
            if (tokens[1].equals("03")) verifyCoords = true;
            if (tokens[1].equals("04")) verifyCoords = true;

            if (verifyCoords) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                    retval = this.fail(tokens[1] + ": Ist nicht in der erwarteten Region.");
                }
            }

            // unit 01, 03, 04
            boolean verifyCommand = false;
            String expected = null;
            if (tokens[1].equals("01")) {verifyCommand = true; expected = "nach o";}
            if (tokens[1].equals("03")) {verifyCommand = true; expected = "nach o pause";}
            if (tokens[1].equals("04")) {verifyCommand = true; expected = "nach w w";}

            if (verifyCommand) {
                boolean found = false;
                for (Einzelbefehl eb : u.BefehleExperimental) {
                    if (eb.getBefehlCanonical().equalsIgnoreCase(expected)) found = true;
                }
                if (!found) retval = fail(tokens[1] + ": Der verbleibende / neue Befehl wurde nicht gefunden.");
            }

            if (tokens[1].equals("02")) {
                boolean found = false;
                for (Einzelbefehl eb : u.BefehleExperimental) {
                    if (eb.getBefehlCanonical().toLowerCase().contains("nach")) found = true;
                }
                if (found) retval = fail(tokens[1] + ": Der vollendete NACH-Befehl wurde nicht entfernt.");
            }

        } // next unit

        return retval;
    }

}
