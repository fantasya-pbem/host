package de.x8bit.Fantasya.Host.ManualTests.Befehle;

import java.util.Collection;

import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Pferd;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;
import de.x8bit.Fantasya.Host.ManualTests.TestWorld;

/**
 * Testet, ob die Bewegung und die Rotation der Befehlsteile
 * bei ROUTE wie gewünscht funktionieren.
 * 
 * @author hapebe
 */
public class Route extends TestBase {

	@Override
	public void mySetupTest() {
		TestWorld tw = this.getTestWorld();
		Partei p = tw.getSpieler1();
		{
			int regIdx = 0;
			Region r = tw.nurBetretbar(getRegions()).get(regIdx);
			boolean okay = false;
			while (!okay) {
				int landNachbarn = 0;
				for (Region n : r.getNachbarn()) {
					if (n.istBetretbar(null)) landNachbarn ++;
				}
				if (landNachbarn > 2) {
					okay = true;
				} else {
					regIdx ++;
					r = tw.nurBetretbar(getRegions()).get(regIdx);
				}
			}
			Region r1 = null;
			Region r2 = null;

			// so, Pfad finden:
			for (Region temp:r.getNachbarn()) {
				if (temp.istBetretbar(null)) {
					r1 = temp;
					break;
				}
			}
			if (r1 == null)	throw new RuntimeException("Keine Nachbarregion zum Bewegen gefunden, einfach nochmal versuchen!");

			for (Region temp:r1.getNachbarn()) {
				if (temp.istBetretbar(null)) {
					if ( temp.getCoords().equals(r.getCoords()) ) continue; // nicht die Ursprungsregion akzeptieren
					r2 = temp;
					break;
				}
			}
			if (r2 == null)	throw new RuntimeException("Keine Nachbarregion zum Bewegen gefunden, einfach nochmal versuchen!");
			getRegions().remove(r);
			getRegions().remove(r1);
			getRegions().remove(r2);

			// Richtungen?
			Richtung rHin1 = r.getCoords().getRichtungNach(r1.getCoords());
			Richtung rHin2 = r1.getCoords().getRichtungNach(r2.getCoords());
			Richtung rWeg1 = rHin2.invert();
			Richtung rWeg2 = rHin1.invert();
			String hin1 = rHin1.getShortcut();
			String hin2 = rHin2.getShortcut();
			String weg1 = rWeg1.getShortcut();
			String weg2 = rWeg2.getShortcut();

			{
				Unit u1 = this.createUnit(p, r);
				u1.setName("Tunichtgut");
				u1.setBeschreibung("Die Einheit faulenzt einfach nur und markiert die Startregion " + r + ".");
				u1.setItem(Silber.class, 500);
			}

			{
				Unit u1 = this.createUnit(p, r);
				u1.setName(this.getName() + " 01 " + r1.getCoords().getX() + " " + r1.getCoords().getY() + " Wanderer 1");
				String befehl = "ROUTE " + hin1 + " " + hin2 + " PAUSE " + weg1 + " " + weg2 + " PAUSE";
				u1.setBeschreibung("Erster Befehl war: " + befehl + ", erwartet: Die erste Richtungsangabe wandert ans Ende, die Einheit steht in " + r1 + ".");
				u1.setItem(Silber.class, 500);
				u1.Befehle.add(befehl);
			}

			{
				Unit u1 = this.createUnit(p, r1);
				u1.setName(this.getName() + " 02 " + r2.getCoords().getX() + " " + r2.getCoords().getY() + " Wanderer 2");
				String befehl = "ROUTE " + hin2 + " PAUSE " + weg1 + " " + weg2 + " PAUSE "  + hin1;
				u1.setBeschreibung("Erster Befehl war: " + befehl + ", erwartet: Die erste Richtungsangabe (und die PAUSE!) wandert ans Ende, die Einheit steht in " + r2 + ".");
				u1.setItem(Silber.class, 500);
				u1.Befehle.add(befehl);
			}

			{
				Unit u1 = this.createUnit(p, r);
				u1.setName(this.getName() + " 03 " + r1.getCoords().getX() + " " + r1.getCoords().getY() + " Reiter 1");
				String befehl = "ROUTE " + hin1 + " PAUSE " + weg2 + " PAUSE";
				u1.setBeschreibung("Erster Befehl war: " + befehl + ", erwartet: Die erste Richtungsangabe (und die PAUSE!) wandert ans Ende, die Einheit steht in " + r1 + ".");
				u1.setItem(Silber.class, 500);
				u1.setItem(Pferd.class, 1);
				u1.setSkill(Reiten.class, 30);
				u1.Befehle.add(befehl);
			}

			{
				Unit u1 = this.createUnit(p, r);
				u1.setName(this.getName() + " 04 " + r2.getCoords().getX() + " " + r2.getCoords().getY() + " Reiter 2");
				String befehl = "ROUTE " + hin1 + " " + hin2 + " PAUSE " + weg1 + " " + weg2 + " PAUSE";
				u1.setBeschreibung("Erster Befehl war: " + befehl + ", erwartet: Die ersten BEIDEN Richtungsangabe (und die PAUSE!) wandern ans Ende, die Einheit steht in " + r2 + ".");
				u1.setItem(Silber.class, 500);
				u1.setItem(Pferd.class, 1);
				u1.setSkill(Reiten.class, 30);
				u1.Befehle.add(befehl);
			}

			new Info(this.getClass().getSimpleName() + " Setup in " + r + ".", p);
		}
	}

    @Override
    protected boolean verifyTest() {
        boolean retval = true;
		Collection<Unit> units = Unit.CACHE;
//        Partei p = this.getTestWorld().getSpieler1();
//        List<Message> messages = null;

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
            if (!u.getName().startsWith(this.getName())) continue;

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

            // unit 01, 02, 03, 04
//            boolean verifyCommand = false;
//            String expected = null;
//            if (tokens[1].equals("01")) {verifyCommand = true; expected = "";}
//            if (tokens[1].equals("02")) {verifyCommand = true; expected = "";}
//            if (tokens[1].equals("03")) {verifyCommand = true; expected = "";}
//            if (tokens[1].equals("04")) {verifyCommand = true; expected = "";}
//
//            if (verifyCommand) {
//                boolean found = false;
//                for (Einzelbefehl eb : u.BefehleExperimental) {
//                    if (eb.getBefehlCanonical().equalsIgnoreCase(expected)) found = true;
//                }
//                if (!found) retval = fail(tokens[1] + ": Der verbleibende / neue Befehl wurde nicht gefunden.");
//            }



        } // next unit

        return retval;
    }

}
