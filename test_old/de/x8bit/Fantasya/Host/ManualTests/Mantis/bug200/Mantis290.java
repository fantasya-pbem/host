package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug200;

import de.x8bit.Fantasya.Atlantis.Items.Eisen;
import de.x8bit.Fantasya.Atlantis.Items.Elefant;
import de.x8bit.Fantasya.Atlantis.Items.Schwert;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Items.Stein;
import de.x8bit.Fantasya.Host.ManualTests.*;
import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Regions.Sumpf;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Skills.Hiebwaffen;
import de.x8bit.Fantasya.Atlantis.Skills.Reiten;
import de.x8bit.Fantasya.Atlantis.Units.Mensch;
import de.x8bit.Fantasya.Host.GameRules;

/**
 * 0000290: Straße ausgefallen?
 * Meine Einheit [2hq], Reiten T6, 15 Personen, 45 Elefanten, 201 Steine,
 * 280 Eisen (sollte also aus meiner Sicht alles koscher sein) wolle in Runde
 * 138 von Xudyaw über Odivodesoh nach Hemnumkirlum wandern. Über die gesamte
 * Strecke führt eine Straße, die habe ich mühsam in den letzten Runden gebaut.
 * Insofern hätten zwei Regionen pro Runde drin sein sollen. Allerdings bleibt
 * die Einheit auf halbem Weg in Odivodesoh hängen und "schafft keine weitere
 * Reise in die Nachbarregion."
 * @author hb
 */
public class Mantis290 extends TestBase {

    @Override
    protected void mySetupTest() {
        TestWorld tw = this.getTestWorld();
		Partei p = tw.createPartei(Mensch.class);
		Partei fremde = tw.createPartei(Mensch.class);

        Region r = null;
        Region r1 = null;
        Region r2 = null;

        for (Region maybe : tw.nurBetretbar(getRegions())) {
            r1 = Region.Load(maybe.getCoords().shift(Richtung.Osten));
            if (!r1.istBetretbar(null)) continue;
			if (!(r1 instanceof Sumpf)) continue;
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

        {
			r.setStrassensteine(Richtung.Osten, r.getSteineFuerStrasse());
			r1.setStrassensteine(Richtung.Westen, r1.getSteineFuerStrasse());
			r1.setStrassensteine(Richtung.Osten, r1.getSteineFuerStrasse() - 1);
			r2.setStrassensteine(Richtung.Westen, r2.getSteineFuerStrasse());

			Unit u = this.createUnit(p, r);
			u.setName(this.getName() + " 01 " + r2.getCoords().getX() + " " + r2.getCoords().getY() + " Straßengeher");
			u.setPersonen(15);
			u.setSkill(Reiten.class, 630 * u.getPersonen());
			u.setItem(Elefant.class, 45);
			u.setItem(Stein.class, 201);
			u.setItem(Eisen.class, 280);
			u.setItem(Silber.class, 0);
			u.Befehle.add("NACH o o");

			u = this.createUnit(p, r); // nur fürs menschliche Auge - beim Blick in NR und CR
			u.setName("Beobachter");

			u = this.createUnit(p, r1); // nur fürs menschliche Auge - beim Blick in NR und CR
			u.setName("Beobachter");

			u = this.createUnit(p, r2); // nur fürs menschliche Auge - beim Blick in NR und CR
			u.setName("Beobachter");

			u = this.createUnit(fremde, r1);
			u.setName("Quälgeister");
			u.setPersonen(10);
			u.setSkill(Hiebwaffen.class, 300 * u.getPersonen());
			u.setItem(Schwert.class, u.getPersonen());
			u.Befehle.add("BEWACHE");

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

        String missing = this.verifyExpectedUnits(Unit.CACHE, new String[] {"01"});
        if (missing != null) {
            retval = this.fail("Es wurden erwartete Einheiten gar nicht mehr gefunden: " + missing);
        }


        for (Unit u:Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(this.getName())) continue;

            String[] tokens = u.getName().split("\\ ");

            // unit 01
            if (tokens[1].equals("01")) {
                if (!this.verifyUnitCoords(tokens, u.getCoords())) {
                    retval = this.fail(tokens[1] + ": Ist nicht in der erwarteten Region.");
                }


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
