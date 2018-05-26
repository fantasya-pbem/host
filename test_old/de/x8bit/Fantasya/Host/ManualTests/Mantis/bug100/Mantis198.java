package de.x8bit.Fantasya.Host.ManualTests.Mantis.bug100;

import java.util.List;

import de.x8bit.Fantasya.Atlantis.Message;
import de.x8bit.Fantasya.Atlantis.Partei;
import de.x8bit.Fantasya.Atlantis.Region;
import de.x8bit.Fantasya.Atlantis.Richtung;
import de.x8bit.Fantasya.Atlantis.Unit;
import de.x8bit.Fantasya.Atlantis.Items.Silber;
import de.x8bit.Fantasya.Atlantis.Messages.Info;
import de.x8bit.Fantasya.Atlantis.Messages.TestMsg;
import de.x8bit.Fantasya.Atlantis.Skills.Wahrnehmung;
import de.x8bit.Fantasya.Host.GameRules;
import de.x8bit.Fantasya.Host.Main;
import de.x8bit.Fantasya.Host.ManualTests.TestBase;

/**
 *
 * @author hapebe
 */
public class Mantis198 extends TestBase {

    public Mantis198 () {
        super();
    }

    @Override
	protected void mySetupTest() {
		Partei p = this.getTestWorld().getSpieler1();
		Region r = null;
        Region west = null;
        
        while (true) {
            r = this.getTestWorld().nurBetretbar(getRegions()).get(0); 
            getRegions().remove(r);
            
            west = Region.Load(r.getCoords().shift(Richtung.Westen));
            if (west.istBetretbar(null)) break;
        }

        int targetX = west.getCoords().getX();
        int targetY = west.getCoords().getY();
        
		{
			Unit u = this.createUnit(p, r);
            u.setName(this.getClass().getSimpleName()+" 01 " + targetX + " " + targetY);
			u.Befehle.add("LERNE Wahrnehmung");
			u.Befehle.add("DEFAULT NACH w");
			u.setBeschreibung("Erwartet: Lernt Wahrnehmung und bietet NACH w als neue Befehlsvorlage an, " +
					" auch longorder wird auf NACH w gesetzt. Mit NMR testen! (einfach -zat, ohne -debug)");
            u.setItem(Silber.class, 500);
		}

		{
			Unit u = this.createUnit(p, r);
            u.setName(this.getClass().getSimpleName()+" 02 " + targetX + " " + targetY);
			u.Befehle.add("LERNE Wahrnehmung");
			u.Befehle.add("DEFAULT \"NACH w\"");
			u.setBeschreibung("Erwartet: Lernt Wahrnehmung und bietet NACH w als neue Befehlsvorlage an, " +
					" auch longorder wird auf NACH w gesetzt. Mit NMR testen! (einfach -zat, ohne -debug)");
            u.setItem(Silber.class, 500);
		}

		{
			Unit u = this.createUnit(p, r);
			u.Befehle.add("LERNE Wahrnehmung");
			u.Befehle.add("DEFAULT");
			u.setBeschreibung("Erwartet: Lernt Wahrnehmung und es gibt eine Fehlermeldung, weil DEFAULT nicht korrekt ist.");
		}

		{
			Unit u = this.createUnit(p, r);
            u.setName(this.getClass().getSimpleName()+" 04 " + r.getCoords().getX() + " " + r.getCoords().getY());
			u.setItem(Silber.class, 500);
			u.Befehle.add("LERNE Wahrnehmung");
			u.Befehle.add("NACH w");
			u.Befehle.add("DEFAULT LERNE Wahrnehmung");
			u.Befehle.add("DEFAULT NACH w");
            u.Befehle.add("DEFAULT GIB BAUERN 1 Pferd");
            u.setBeschreibung("Erwartet: Lernt Wahrnehmung und bietet NACH w als neue " +
					"Befehlsvorlage an. Fehlermeldung, dass schon ein langer Befehl gegeben wurde.");
		}

		{
			Unit u = this.createUnit(p, r);
            u.setName(this.getClass().getSimpleName()+" 05");
			u.setBeschreibung("Erwartet: Faulenzt und will das auch weiterhin.");
		}

		new Info("Mantis #198 Setup in " + r + " " + r.getCoords() + ".", p);
	}

    @Override
    protected boolean verifyTest() {
        String testName = this.getClass().getSimpleName();
        boolean retval = true;

        Partei p = this.getTestWorld().getSpieler1();
        List<Message> messages = null;

        new TestMsg("Verifiziere " + testName + "...");

        for (Unit u:Unit.CACHE) {
            // "fremde" Einheiten überspringen.
            if (!u.getName().startsWith(testName)) continue;

            String[] tokens = u.getName().split("\\ ");
            
            if (GameRules.getRunde() == 2) {
				new TestMsg("Mantis198 bitte auch mit NMR testen - enthält Funktionen für NMRs (DEFAULT)! (einfach -zat, ohne -debug)");
				if (Main.getBFlag("EVA")) {
					new TestMsg("Warnung: DEFAULT kann derzeit mit EVA nicht verlässlich getestet werden - anpassen, sobald die Befehls-Behandlung (Einlesen und ggf. Übernehmen von alten Befehlen) fertig ist.");
					return true;
				}


                // unit 01 & 02 - müssen "NACH w" als aktiven Befehl haben.
                if (tokens[1].equals("01") || tokens[1].equals("02")) {
                    boolean foundNach = u.getLongOrder().startsWith("NACH w");
                    boolean foundLerne = false;
                    for (String befehl:u.Befehle) {
                        if (befehl.startsWith("LERNE")) foundLerne = true;
                    }

                    if ( (!foundNach) || foundLerne) {
                        this.fail(testName + "-Test " + tokens[1] + " ist fehlgeschlagen. (" + u + ", " + u.getCoords()+")");
                        retval = false;
                    }
                }

                // unit 04 -
                if (tokens[1].equals("04")) {
                    if (
                      (!u.getLongOrder().startsWith("LERNE Wahrnehmung"))
                      || (u.getSkill(Wahrnehmung.class).getLerntage() != 30)
                    ) {

                        this.fail(testName + "-Test " + tokens[1] + " ist fehlgeschlagen. (" + u + ", " + u.getCoords()+")");
                        retval = false;
                    }

                    messages = Message.Retrieve(p, u.getCoords(), u);
                    boolean found = false;
                    for (Message msg : messages) {
                        String text = msg.getText().toLowerCase();
                        if (text.contains("hat mehrere lange") && text.contains("letzte (nach w)")) found = true;
                    }
                    if (!found) retval = fail(tokens[1] + ": Meldung über mehrere lange DEFAULT-Befehle fehlt.");
                }

                // unit 05 -
                if (tokens[1].equals("05")) {
                    if ( !u.getLongOrder().toLowerCase().startsWith("faulenze") ) {
                        this.fail(testName + "-Test " + tokens[1] + " ist fehlgeschlagen. (" + u + ", " + u.getCoords()+")");
                        retval = false;
                    }
                }


                continue;
            }


            if (GameRules.getRunde() == 3) {

                // unit 01 & 02 - diese müssen in ihrer Zielregion sein.
                // unit 04 - diese muss noch in der Ausgangsregion sein.
                if (tokens[1].equals("01") 
                        || tokens[1].equals("02")
                        || tokens[1].equals("04")) {
                    int x = Integer.parseInt(tokens[2]);
                    int y = Integer.parseInt(tokens[3]);

                    if ( (x != u.getCoords().getX()) || (y != u.getCoords().getY()) ) {
                        this.fail(testName + "-Test " + tokens[1] + " ist fehlgeschlagen. (" + u + ", " + u.getCoords()+")");
                        retval = false;
                    }
                }

                continue;
            }

            throw new IllegalStateException("GameRules.getRunde() muss 2 oder 3 sein - ist aber " + GameRules.getRunde() + ".");
        } // next unit

        return retval;
    }

}
